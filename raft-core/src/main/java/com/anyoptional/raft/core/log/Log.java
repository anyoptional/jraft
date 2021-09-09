package com.anyoptional.raft.core.log;

import com.anyoptional.raft.core.node.NodeId;
import com.anyoptional.raft.core.log.entry.Entry;
import com.anyoptional.raft.core.log.entry.EntryMeta;
import com.anyoptional.raft.core.log.entry.GeneralEntry;
import com.anyoptional.raft.core.log.entry.NoOpEntry;
import com.anyoptional.raft.core.rpc.message.AppendEntriesRpc;

import java.util.List;

public interface Log extends AutoCloseable {

    int ALL_ENTRIES = -1;

    /**
     * 获取最后一条日志条目的元信息，
     * 可用于候选者发起投票请求
     */
    EntryMeta getLastEntryMeta();

    /**
     * 创建日志复制消息，leader 向 follower
     * 发送日志复制消息时使用
     *
     * @param maxEntries 最大读取的日志条目
     */
    AppendEntriesRpc createAppendEntriesRpc(int term, NodeId selfId, int nextIndex, int maxEntries);

    /**
     * 增加日志条目，上层服务操作或当前节点成为 leader
     * 后的第一条 no-op 日志
     */
    NoOpEntry appendEntry(int term);

    /**
     * 增加一般日志条目
     */
    GeneralEntry appendEntry(int term, byte[] command);

    /**
     * 追加从 leader 发送过来的日志条目（收到来自
     * leader 的日志复制请求时）
     */
    boolean appendEntriesFromLeader(int prevLogIndex, int prevLogTerm, List<Entry> entries);

    /**
     * 推进 commitIndex（收到来自
     * leader 的日志复制请求时）
     */
    void advanceCommitIndex(int newCommitIndex, int currentTerm);

    /**
     * 获取下一条日志的索引，在当前节点成为 leader 时使用，因为 leader
     * 需要重置 follower 的日志复制进度，此时所有 follower 的初始 nextLogIndex
     * 都是 leader 的下一条日志的索引
     */
    int getNextIndex();

    /**
     * 获取当前的 commitIndex
     */
    int getCommitIndex();

    /**
     * 比较日志的新旧，用于收到投票请求时判断是否投票使用，也可以使用
     * {@link #getLastEntryMeta()} 来进行比较
     */
    boolean isNewerThan(int lastLogIndex, int lastLogTerm);

    @Override
    void close();
}
