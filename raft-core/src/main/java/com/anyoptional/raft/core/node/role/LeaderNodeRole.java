package com.anyoptional.raft.core.node.role;

import com.anyoptional.raft.core.node.NodeId;
import com.anyoptional.raft.core.schedule.LogReplicationTask;
import com.anyoptional.raft.core.node.NodeGroup;

/**
 * leader 节点没有选举超时（本身就是leader了还选什么），但它需要定时给
 * follower 发送心跳消息、同步日志
 *
 * 注意，日志复制进度在 {@link NodeGroup}
 */
public class LeaderNodeRole extends AbstractNodeRole {

    private final LogReplicationTask logReplicationTask;

    public LeaderNodeRole(int term, LogReplicationTask logReplicationTask) {
        super(RoleName.LEADER, term);
        this.logReplicationTask = logReplicationTask;
    }

    @Override
    public void cancelTimeoutOrTask() {
        logReplicationTask.cancel();
    }

    @Override
    public NodeId getLeaderId(NodeId selfId) {
        return selfId;
    }

    @Override
    public RoleState getState() {
        return new DefaultRoleState(RoleName.LEADER, term);
    }

    @Override
    protected boolean doStateEquals(AbstractNodeRole role) {
        return true;
    }

    @Override
    public String toString() {
        return "LeaderNodeRole{" +
                "term=" + term +
                ", logReplicationTask=" + logReplicationTask +
                '}';
    }

}
