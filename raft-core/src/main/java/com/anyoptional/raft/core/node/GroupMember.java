package com.anyoptional.raft.core.node;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * 代表集群中的一个节点
 */
public class GroupMember {

    /**
     * 节点的 id 和 ip
     */
    private final NodeEndpoint endpoint;

    /**
     * 复制进度，用于日志复制
     */
    @Nullable
    private ReplicatingState replicatingState;

    /**
     * 无日志复制状态
     */
    public GroupMember(NodeEndpoint endpoint) {
        this(endpoint, null);
    }

    /**
     * 带日志复制状态
     */
    public GroupMember(NodeEndpoint endpoint, @Nullable ReplicatingState replicatingState) {
        this.endpoint = endpoint;
        this.replicatingState = replicatingState;
    }

    public NodeEndpoint getEndpoint() {
        return endpoint;
    }

    public void setReplicatingState(@Nullable ReplicatingState replicatingState) {
        this.replicatingState = replicatingState;
    }

    public int getNextIndex() {
        return unwrapReplicatingState().getNextIndex();
    }

    public int getMatchIndex() {
        return unwrapReplicatingState().getMatchIndex();
    }

    private ReplicatingState unwrapReplicatingState() {
        Assert.state(replicatingState != null, "replicatingState not set");
        return replicatingState;
    }

}
