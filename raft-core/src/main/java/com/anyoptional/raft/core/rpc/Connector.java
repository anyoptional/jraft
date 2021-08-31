package com.anyoptional.raft.core.rpc;

import com.anyoptional.raft.core.node.NodeEndpoint;
import com.anyoptional.raft.core.rpc.message.AppendEntriesResult;
import com.anyoptional.raft.core.rpc.message.AppendEntriesRpc;
import com.anyoptional.raft.core.rpc.message.RequestVoteResult;
import com.anyoptional.raft.core.rpc.message.RequestVoteRpc;

import java.util.Collection;

public interface Connector extends AutoCloseable {

    /**
     * Initialize connector.
     * <p>
     * SHOULD NOT call more than one.
     * </p>
     */
    void initialize();

    /**
     * candidate 会请求集群中其它节点为它投票
     */
    void sendRequestVote(RequestVoteRpc rpc, Collection<NodeEndpoint> destinationEndpoints);

    /**
     * 回复投票结果
     */
    void replyRequestVote(RequestVoteResult result, NodeEndpoint destinationEndpoint);

    /**
     * leader 节点向单个 follower 同步日志，因为每个 follower 的复制进度可能是不同的，因此需要单发
     */
    void sendAppendEntries(AppendEntriesRpc rpc, NodeEndpoint destinationEndpoint);

    /**
     * 回复日志追加结果
     */
    void replyAppendEntries(AppendEntriesResult result, NodeEndpoint destinationEndpoint);

    /**
     * Close connector.
     */
    @Override
    void close();
}
