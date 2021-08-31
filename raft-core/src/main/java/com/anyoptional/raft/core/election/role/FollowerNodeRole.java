package com.anyoptional.raft.core.election.role;

import com.anyoptional.raft.core.NodeId;
import com.anyoptional.raft.core.election.scheduler.ElectionTimeout;
import org.springframework.lang.Nullable;

/**
 * FollowerNodeRole 是 Immutable 的，当选举超时 或 收到来自 leader
 * 的心跳信息时，需要新建一个角色实例。
 */
public class FollowerNodeRole extends AbstractNodeRole {

    /**
     * 本轮选举中投票的节点，未发生选举时为空
     */
    @Nullable
    private final NodeId votedFor;

    /**
     * 当前 leader 节点，可能为空，比如集群
     * 刚启动时
     */
    @Nullable
    private final NodeId leaderId;

    /**
     * 选举超时
     */
    private final ElectionTimeout electionTimeout;

    public FollowerNodeRole(int term, @Nullable NodeId votedFor,
                            @Nullable NodeId leaderId, ElectionTimeout electionTimeout) {
        super(RoleName.FOLLOWER, term);
        this.votedFor = votedFor;
        this.leaderId = leaderId;
        this.electionTimeout = electionTimeout;
    }

    @Nullable
    public NodeId getVotedFor() {
        return votedFor;
    }

    @Nullable
    public NodeId getLeaderId() {
        return leaderId;
    }

    @Override
    public void cancelTimeoutOrTask() {
        electionTimeout.cancel();
    }

    @Override
    public String toString() {
        return "FollowerNodeRole{" +
                "term=" + term +
                ", votedFor=" + votedFor +
                ", leaderId=" + leaderId +
                ", electionTimeout=" + electionTimeout +
                '}';
    }

}
