package com.anyoptional.raft.core.log;

import com.anyoptional.raft.core.node.NodeId;
import com.anyoptional.raft.core.log.entry.Entry;
import com.anyoptional.raft.core.log.entry.EntryMeta;
import com.anyoptional.raft.core.log.entry.GeneralEntry;
import com.anyoptional.raft.core.log.entry.NoOpEntry;
import com.anyoptional.raft.core.log.sequence.EntrySequence;
import com.anyoptional.raft.core.rpc.message.AppendEntriesRpc;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

import java.util.*;

public abstract class AbstractLog implements Log {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected EntrySequence entrySequence;

    @Override
    public int getNextIndex() {
        return entrySequence.getNextLogIndex();
    }

    @Override
    public int getCommitIndex() {
        return entrySequence.getCommitIndex();
    }

    /**
     * 获取最后一条日志的元信息
     */
    @Override
    public EntryMeta getLastEntryMeta() {
        if (entrySequence.isEmpty()) {
            return new EntryMeta(Entry.KIND_NO_OP, 0, 0);
        }
        return Objects.requireNonNull(entrySequence.getLastEntry()).getMeta();
    }

    /**
     * 创建 AppendEntries 消息
     */
    @Override
    public AppendEntriesRpc createAppendEntriesRpc(int term, NodeId selfId, int nextIndex, int maxEntries) {
        int nextLogIndex = entrySequence.getNextLogIndex();
        if (nextIndex > nextLogIndex) {
            throw new IllegalArgumentException("illegal next index " + nextIndex);
        }
        AppendEntriesRpc rpc = new AppendEntriesRpc();
        rpc.setMessageId(UUID.randomUUID().variant());
        rpc.setTerm(term);
        rpc.setLeaderId(selfId);
        rpc.setLeaderCommit(getCommitIndex());
        Entry entry = entrySequence.getEntry(nextIndex - 1);
        if (entry != null) {
            rpc.setPrevLogTerm(entry.getTerm());
            rpc.setPrevLogIndex(entry.getIndex());
        }
        if (!entrySequence.isEmpty()) {
            int maxIndex = (maxEntries == ALL_ENTRIES ? nextLogIndex : Math.min(nextLogIndex, nextIndex + maxEntries));
            rpc.setEntries(entrySequence.subList(nextIndex, maxIndex));
        }
        return rpc;
    }

    @Override
    public boolean isNewerThan(int lastLogIndex, int lastLogTerm) {
        EntryMeta lastEntryMeta = getLastEntryMeta();
        logger.debug("last entry ({}, {}), candidate ({}, {})", lastEntryMeta.getIndex(), lastEntryMeta.getTerm(), lastLogIndex, lastLogTerm);
        return lastEntryMeta.getTerm() > lastLogTerm || lastEntryMeta.getIndex() > lastLogIndex;
    }

    @Override
    public NoOpEntry appendEntry(int term) {
        NoOpEntry entry = new NoOpEntry(entrySequence.getNextLogIndex(), term);
        entrySequence.append(entry);
        return entry;
    }

    @Override
    public GeneralEntry appendEntry(int term, byte[] command) {
        GeneralEntry entry = new GeneralEntry(entrySequence.getNextLogIndex(), term, command);
        entrySequence.append(entry);
        return entry;
    }

    @Override
    public boolean appendEntriesFromLeader(int prevLogIndex, int prevLogTerm, List<Entry> leaderEntries) {
        // 检查从 leader 过来的 prevLogIndex/prevLogTerm 和本地是否匹配
        // prevLogIndex 不一定对应最后一条日志， leader 节点会从后往前找到
        // 第一个匹配的日志，比如发生网络分区时
        if (!checkIfPreviousLogMatches(prevLogIndex, prevLogTerm)) {
            return false;
        }

        // 空日志条目
        // 心跳消息，或者日志同步完毕
        if (leaderEntries.isEmpty()) {
            return true;
        }

        // 移除冲突的日志条目并返回接下来要追加的日志条目，如果存在的话
        EntrySequenceView newEntries = removeUnmatchedLog(new EntrySequenceView(leaderEntries));

        // 追加日志
        appendEntriesFromLeader(newEntries);

        return true;
    }

    @Override
    public void advanceCommitIndex(int newCommitIndex, int currentTerm) {
        if (!validateNewCommitIndex(newCommitIndex, currentTerm)) {
            return;
        }
        // 推进 commitIndex
        entrySequence.commit(newCommitIndex);
        // TODO: 状态机
    }

    @Override
    public void close() {
        entrySequence.close();
    }

    private boolean validateNewCommitIndex(int newCommitIndex, int currentTerm) {
        // 小于当前 commitIndex
        if (newCommitIndex <= entrySequence.getCommitIndex()) {
            return false;
        }
        // commitIndex 肯定是已经存在的
        EntryMeta meta = entrySequence.getEntryMeta(newCommitIndex);
        if (meta == null) {
            logger.debug("log of newCommitIndex {} not found", newCommitIndex);
            return false;
        }
        // 按照raft算法，推荐commitIndex需要检查日志的term与当前的term是否一致
        if (meta.getTerm() != currentTerm) {
            logger.debug("log term of newCommitIndex {} miss match currentTerm {}", meta.getTerm(), currentTerm);
            return false;
        }
        return true;
    }

    private boolean checkIfPreviousLogMatches(int prevLogIndex, int prevLogTerm) {
        EntryMeta meta = entrySequence.getEntryMeta(prevLogIndex);
        if (meta == null) {
            logger.debug("previous log {} not found", prevLogIndex);
            return false;
        }
        int term = meta.getTerm();
        // 发生过脑裂，这种情况需要删除 follower 日志中
        // 与 leader 不匹配的部分
        if (prevLogTerm != term) {
            logger.debug("different term of previous log, local: {}, remote: {}", term, prevLogTerm);
            return false;
        }
        return true;
    }

    private EntrySequenceView removeUnmatchedLog(EntrySequenceView leaderEntries) {
        // 从 leader 过来的日志条目不应该为空（为空的情况应提前处理）
        Preconditions.checkState(!leaderEntries.isEmpty());
        // 查找第一条不同步的日志
        int firstUnmatched = findFirstUnmatchedLog(leaderEntries);
        // 均已同步
        if (firstUnmatched < 0) {
            return new EntrySequenceView(Collections.emptyList());
        }
        // 移除不匹配的日志条目
        removeEntriesAfter(firstUnmatched - 1);

        // 返回追加的日志条目
        return leaderEntries.subView(firstUnmatched);
    }

    private int findFirstUnmatchedLog(EntrySequenceView leaderEntries) {
        // 遍历从 leader 过来的日志条目
        for (Entry entry : leaderEntries) {
            int logIndex = entry.getIndex();
            // 按照索引查找本地同步日志
            EntryMeta followerEntryMeta = entrySequence.getEntryMeta(logIndex);
            // 如果
            // 1. 本地没有对应日志，说明从这以后的日志均未同步
            // 2. 本地的term和leader的term不一致，说明发生过网络分区，这种情况要删除本地的多余数据
            if (followerEntryMeta == null || followerEntryMeta.getTerm() != entry.getTerm()) {
                return logIndex;
            }
        }

        return -1;
    }

    private void appendEntriesFromLeader(EntrySequenceView entries) {
        if (entries.isEmpty()) {
            return;
        }
        logger.debug("append entries from leader from {} to {}", entries.getFirstLogIndex(), entries.getLastLogIndex());
        for (Entry entry : entries) {
            entrySequence.append(entry);
        }
    }

    private void removeEntriesAfter(int index) {
        if (entrySequence.isEmpty() ||
                index > entrySequence.getLastLogIndex()) {
            return;
        }
        // TODO: 如果日志已经应用了，需要重新构建状态机
        logger.debug("remove entries after {}", index);
        entrySequence.removeAfter(index);
    }

    private static class EntrySequenceView implements Iterable<Entry> {

        private final List<Entry> entries;
        private int firstLogIndex = -1;
        private int lastLogIndex = -1;

        EntrySequenceView(List<Entry> entries) {
            this.entries = entries;
            if (!entries.isEmpty()) {
                this.firstLogIndex = entries.get(0).getIndex();
                this.lastLogIndex = entries.get(entries.size() - 1).getIndex();
            }
        }

        /**
         * 获取指定位置的日志条目，注意此 index 非
         * entries 列表下标，而是日志条目在日志文件
         * 中的位置
         */
        @Nullable
        Entry get(int index) {
            if (entries.isEmpty() ||
                    index < firstLogIndex ||
                    index > lastLogIndex) {
                return null;
            }
            return entries.get(index - firstLogIndex);
        }

        boolean isEmpty() {
            return entries.isEmpty();
        }

        public int getFirstLogIndex() {
            return firstLogIndex;
        }

        public int getLastLogIndex() {
            return lastLogIndex;
        }

        /**
         * 返回子视图
         *
         * @param fromIndex 日志文件中的位置（偏移量）
         */
        EntrySequenceView subView(int fromIndex) {
            if (entries.isEmpty() || fromIndex > lastLogIndex) {
                return new EntrySequenceView(Collections.emptyList());
            }
            return new EntrySequenceView(entries.subList(fromIndex - firstLogIndex, entries.size()));
        }

        @Override
        public Iterator<Entry> iterator() {
            return entries.iterator();
        }

    }

}
