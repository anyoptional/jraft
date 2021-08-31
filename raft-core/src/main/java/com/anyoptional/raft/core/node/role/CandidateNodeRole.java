package com.anyoptional.raft.core.node.role;

import com.anyoptional.raft.core.schedule.ElectionTimeout;
import com.google.common.base.Preconditions;

public class CandidateNodeRole extends AbstractNodeRole {

    /**
     * 本轮选举中获得的票数
     */
    private final int votesCount;

    /**
     * 选举超时
     */
    private final ElectionTimeout electionTimeout;

    /**
     * 票数 1，用于 follower 节点超时未收到心跳转换成 candidate 时
     * @param term 当前 leader 的任期
     * @param electionTimeout 选取超时
     */
    public CandidateNodeRole(int term, ElectionTimeout electionTimeout) {
        this(term, 1, electionTimeout);
    }

    /**
     * 收到来自其它节点的投票时使用
     */
    public CandidateNodeRole(int term, int votesCount, ElectionTimeout electionTimeout) {
        super(RoleName.CANDIDATE, term);
        Preconditions.checkArgument(votesCount >= 1);
        this.votesCount = votesCount;
        this.electionTimeout = electionTimeout;
    }

    public int getVotesCount() {
        return votesCount;
    }

    @Override
    public void cancelTimeoutOrTask() {
        electionTimeout.cancel();
    }

    @Override
    public String toString() {
        return "CandidateNodeRole{" +
                "term=" + term +
                ", votesCount=" + votesCount +
                ", electionTimeout=" + electionTimeout +
                '}';
    }

}
