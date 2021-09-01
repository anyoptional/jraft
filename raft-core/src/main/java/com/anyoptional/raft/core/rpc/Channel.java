package com.anyoptional.raft.core.rpc;


import com.anyoptional.raft.core.rpc.message.AppendEntriesResult;
import com.anyoptional.raft.core.rpc.message.AppendEntriesRpc;
import com.anyoptional.raft.core.rpc.message.RequestVoteResult;
import com.anyoptional.raft.core.rpc.message.RequestVoteRpc;

/**
 * Channel between nodes.
 */
public interface Channel extends AutoCloseable {

    /**
     * Write request vote rpc.
     *
     * @param rpc rpc
     */
    void writeRequestVoteRpc(RequestVoteRpc rpc);

    /**
     * Write request vote result.
     *
     * @param result result
     */
    void writeRequestVoteResult(RequestVoteResult result);

    /**
     * Write append entries rpc.
     *
     * @param rpc rpc
     */
    void writeAppendEntriesRpc(AppendEntriesRpc rpc);

    /**
     * Write append entries result.
     *
     * @param result result
     */
    void writeAppendEntriesResult(AppendEntriesResult result);

    /**
     * Close channel.
     */
    @Override
    void close();

}
