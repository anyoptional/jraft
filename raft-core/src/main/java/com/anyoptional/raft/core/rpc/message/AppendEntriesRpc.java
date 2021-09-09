package com.anyoptional.raft.core.rpc.message;

import com.anyoptional.raft.core.node.NodeId;
import com.anyoptional.raft.core.log.entry.Entry;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Collections;
import java.util.List;

/**
 * 日志复制和心跳消息（未新增日志时表示心跳消息），由 leader 发起
 */
@Getter
@Setter
@ToString
public class AppendEntriesRpc {

    private int messageId;

    /**
     * 选举ID
     */
    private int term;

    /**
     * leader 节点ID
     */
    private NodeId leaderId;

    /**
     * 前一条日志的索引
     */
    private int prevLogIndex;

    /**
     * 前一条日志的term
     */
    private int prevLogTerm;

    /**
     * 要复制的日志条目
     */
    private List<Entry> entries = Collections.emptyList();

    /**
     * leader 的 commitIndex
     * 索引小于 commitIndex 的日志是已持久化的，
     * 索引大于 commitIndex 的日志是已追加但未持久化的（还未得到超半数节点的确认）
     */
    private int leaderCommit;

    @JsonIgnore
    public int getLastEntryIndex() {
        return this.entries.isEmpty() ? this.prevLogIndex : this.entries.get(this.entries.size() - 1).getIndex();
    }

}