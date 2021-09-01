package com.anyoptional.raft.core.rpc.message;

import com.anyoptional.raft.core.node.NodeId;
import com.anyoptional.raft.core.rpc.Channel;

public class AppendEntriesRpcMessage extends AbstractRpcMessage<AppendEntriesRpc> {

    public AppendEntriesRpcMessage(AppendEntriesRpc rpc, NodeId sourceNodeId, Channel channel) {
        super(rpc, sourceNodeId, channel);
    }

}
