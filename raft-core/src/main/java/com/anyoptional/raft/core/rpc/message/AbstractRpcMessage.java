package com.anyoptional.raft.core.rpc.message;

import com.anyoptional.raft.core.node.NodeId;
import com.anyoptional.raft.core.rpc.Channel;

public abstract class AbstractRpcMessage<T> {

    /**
     * rpc 请求
     */
    private final T rpc;

    /**
     * 发起请求的节点
     */
    private final NodeId sourceNodeId;

    /**
     * 节点间通信的信道
     */
    private final Channel channel;

    AbstractRpcMessage(T rpc, NodeId sourceNodeId, Channel channel) {
        this.rpc = rpc;
        this.sourceNodeId = sourceNodeId;
        this.channel = channel;
    }

    public T get() {
        return this.rpc;
    }

    public NodeId getSourceNodeId() {
        return sourceNodeId;
    }

    public Channel getChannel() {
        return channel;
    }

}
