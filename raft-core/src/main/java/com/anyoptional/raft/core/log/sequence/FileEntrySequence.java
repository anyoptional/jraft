package com.anyoptional.raft.core.log.sequence;

import com.anyoptional.raft.core.log.LogDir;
import com.anyoptional.raft.core.log.LogException;
import com.anyoptional.raft.core.log.entry.Entry;
import com.anyoptional.raft.core.log.entry.EntryFactory;
import com.anyoptional.raft.core.log.entry.EntryMeta;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class FileEntrySequence extends AbstractEntrySequence {

    private final EntryFactory entryFactory = new EntryFactory();

    private final EntriesFile entriesFile;

    private final EntryIndexFile entryIndexFile;

    /**
     * 待写入文件的日志缓冲
     */
    private final LinkedList<Entry> pendingEntries = new LinkedList<>();

    /**
     * raft 中初始化时 commitIndex 为 0，和日志是否持久化无关
     */
    private int commitIndex = 0;

    public FileEntrySequence(LogDir logDir, int logIndexOffset) {
        super(logIndexOffset);
        try {
            entriesFile = new EntriesFile(logDir.getEntriesFile());
            entryIndexFile = new EntryIndexFile(logDir.getEntryOffsetIndexFile());
            initialize();
        } catch (IOException ex) {
            throw new LogException("failed to open log file", ex);
        }
    }

    public FileEntrySequence(EntriesFile entriesFile, EntryIndexFile entryIndexFile, int logIndexOffset) {
        super(logIndexOffset);
        this.entryIndexFile = entryIndexFile;
        this.entriesFile = entriesFile;
        initialize();
    }

    private void initialize() {
        // 日志为空时，logIndexOffset 由外部参数决定
        if (entryIndexFile.isEmpty()) {
            return;
        }

        // 使用日志索引文件的 minEntryIndex 作为 logIndexOffset
        logIndexOffset = entryIndexFile.getMinEntryIndex();
        // 对应的，nextLogIndex 就是 maxEntryIndex + 1
        nextLogIndex = entryIndexFile.getMaxEntryIndex() + 1;
    }

    @Override
    public int getCommitIndex() {
        return commitIndex;
    }

    @Override
    protected List<Entry> doSubList(int fromIndex, int toIndex) {
        // 结果分为来自文件和来自缓冲区共两部分
        List<Entry> result = new ArrayList<>();
        // 1. 从文件中获取日志条目
        if (!entryIndexFile.isEmpty() && fromIndex <= entryIndexFile.getMaxEntryIndex()) {
            int maxIndex = Math.min(entryIndexFile.getMaxEntryIndex() + 1, toIndex);
            for (int i = fromIndex; i < maxIndex; i++) {
                result.add(getEntryInFile(i));
            }
        }
        // 2. 从缓冲区中获取条目
        if (!pendingEntries.isEmpty() && toIndex > pendingEntries.getFirst().getIndex()) {
            for (Entry entry : pendingEntries) {
                int index = entry.getIndex();
                if (index >= toIndex) {
                    break;
                }
                if (index >= fromIndex) {
                    result.add(entry);
                }
            }
        }
        return result;
    }

    @Override
    protected Entry doGetEntry(int index) {
        // 还未持久化到文件
        if (!pendingEntries.isEmpty()) {
            int i = pendingEntries.getFirst().getIndex();
            if (index >= i) {
                // raft 日志条目是顺序写入的
                return pendingEntries.get(index - i);
            }
        }
        // 从日志文件中获取
        Assert.isTrue(!entryIndexFile.isEmpty(), "entryIndexFile should not be empty");
        return getEntryInFile(index);
    }

    @Nullable
    @Override
    public EntryMeta getEntryMeta(int index) {
        if (!isEntryPresent(index)) {
            return null;
        }
        if (!pendingEntries.isEmpty()) {
            int i = pendingEntries.getFirst().getIndex();
            if (index >= i) {
                // raft 日志条目时顺序写入的
                return pendingEntries.get(index - i).getMeta();
            }
        }
        return entryIndexFile.get(index).toEntryMeta();
    }

    @Nullable
    @Override
    public Entry getLastEntry() {
        if (isEmpty()) {
            return null;
        }
        if (!pendingEntries.isEmpty()) {
            return pendingEntries.getLast();
        }
        Assert.isTrue(!entryIndexFile.isEmpty(), "entryIndexFile should not be empty");
        return getEntryInFile(entryIndexFile.getMaxEntryIndex());
    }

    @Override
    protected void doAppend(Entry entry) {
        // 将日志添加到缓冲区
        pendingEntries.add(entry);
    }

    @Override
    protected void doRemoveAfter(int index) {
        // 若还在缓冲区，只需要删掉缓存就可以了
        if (!pendingEntries.isEmpty() && index >= pendingEntries.getFirst().getIndex() - 1) {
            // 删掉多余的
            for (int i = index + 1; i <= doGetLastLogIndex(); i++) {
                pendingEntries.removeLast();
            }
            nextLogIndex = index + 1;
            return;
        }
        // 索引比日志缓冲区中的第一条小
        // 说明在文件中
        try {
            // 清除部分
            if (index >= doGetFirstLogIndex()) {
                pendingEntries.clear();
                // remove entries whose index >= (index + 1)
                entriesFile.truncate(entryIndexFile.getOffset(index + 1));
                entryIndexFile.removeAfter(index);
                nextLogIndex = index + 1;
                commitIndex = index;
            } else {
                // 清除所有
                pendingEntries.clear();
                entriesFile.clear();
                entryIndexFile.clear();
                nextLogIndex = logIndexOffset;
                commitIndex = logIndexOffset - 1;
            }
        } catch (IOException e) {
            throw new LogException(e);
        }
    }

    /**
     * 提交日志，将日志条目从缓冲区刷新到磁盘
     */
    @Override
    public void commit(int index) {
        // append only
        if (index < commitIndex) {
            throw new IllegalArgumentException("commit index < " + commitIndex);
        }
        // 已提交
        if (index == commitIndex) {
            return;
        }

        // 如果 commitIndex 在文件内
        // raft 算法在启动时将节点的 commitIndex 设置为0
        // 如果 follower 宕机并重启，且在这段时间内没有新的
        // 日志添加进来，此时重启后的 follower 收到来自leader
        // 的心跳消息并更新自己的 commitIndex，显然这会 commitIndex
        // 还在文件中
        if (!entryIndexFile.isEmpty() && index <= entryIndexFile.getMaxEntryIndex()) {
            // 直接更新即可
            commitIndex = index;
            return;
        }

        // 检查 commitIndex 是否在日志缓冲的区间内
        if (pendingEntries.isEmpty() ||
                pendingEntries.getFirst().getIndex() > index ||
                pendingEntries.getLast().getIndex() < index) {
            throw new IllegalArgumentException("no entry to commit or commit index exceed");
        }

        // 更新 commitIndex 并刷新日志到磁盘
        long offset;
        Entry entry = null;
        try {
            for (int i = pendingEntries.getFirst().getIndex(); i <= index; i++) {
                entry = pendingEntries.removeFirst();
                offset = entriesFile.appendEntry(entry);
                entryIndexFile.appendEntryIndex(i, offset, entry.getKind(), entry.getTerm());
                // 记录已提交的位置
                commitIndex = i;
            }
        } catch (IOException e) {
            throw new LogException("failed to commit entry " + entry, e);
        }
    }

    @Override
    public void close() {
        try {
            commitIndex = 0;
            entriesFile.close();
            entryIndexFile.close();
        } catch (IOException e) {
            throw new LogException(e);
        }
    }

    /**
     * 按照索引获取文件中的日志条目
     */
    private Entry getEntryInFile(int index) {
        // 1. 通过索引获取偏移量
        long offset = entryIndexFile.getOffset(index);
        try {
            // 2. 根据偏移量直接加载
            return entriesFile.loadEntry(offset, entryFactory);
        } catch (IOException e) {
            throw new LogException(String.format("failed to load entry at %d", index), e);
        }
    }
}
