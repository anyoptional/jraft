package com.anyoptional.raft.core.rpc.message;

import com.anyoptional.raft.core.node.NodeId;
import com.anyoptional.raft.core.rpc.Channel;

public class RequestVoteRpcMessage extends AbstractRpcMessage<RequestVoteRpc> {

    public RequestVoteRpcMessage(RequestVoteRpc rpc, NodeId sourceNodeId, Channel channel) {
        super(rpc, sourceNodeId, channel);
    }

}
