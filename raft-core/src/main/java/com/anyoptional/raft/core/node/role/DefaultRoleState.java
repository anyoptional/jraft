package com.anyoptional.raft.core.node.role;

import com.anyoptional.raft.core.node.NodeId;
import org.springframework.lang.Nullable;

/**
 * Default role state.
 */
public class DefaultRoleState implements RoleState {

    private final RoleName roleName;
    private final int term;
    private int votesCount = VOTES_COUNT_NOT_SET;

    @Nullable
    private NodeId votedFor;

    @Nullable
    private NodeId leaderId;

    public DefaultRoleState(RoleName roleName, int term) {
        this.roleName = roleName;
        this.term = term;
    }

    @Override
    public RoleName getRoleName() {
        return roleName;
    }

    @Override
    public int getTerm() {
        return term;
    }

    @Override
    public int getVotesCount() {
        return votesCount;
    }

    public void setVotesCount(int votesCount) {
        this.votesCount = votesCount;
    }

    @Nullable
    @Override
    public NodeId getVotedFor() {
        return votedFor;
    }

    public void setVotedFor(@Nullable NodeId votedFor) {
        this.votedFor = votedFor;
    }

    @Nullable
    @Override
    public NodeId getLeaderId() {
        return leaderId;
    }

    public void setLeaderId(@Nullable NodeId leaderId) {
        this.leaderId = leaderId;
    }

    public String toString() {
        switch (this.roleName) {
            case FOLLOWER:
                return "Follower{term=" + this.term + ", votedFor=" + this.votedFor + ", leaderId=" + this.leaderId + "}";
            case CANDIDATE:
                return "Candidate{term=" + this.term + ", votesCount=" + this.votesCount + "}";
            case LEADER:
                return "Leader{term=" + this.term + "}";
            default:
                throw new IllegalStateException("unexpected node role name [" + this.roleName + "]");
        }
    }

}
