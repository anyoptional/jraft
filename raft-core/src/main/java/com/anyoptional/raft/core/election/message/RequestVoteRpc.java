package com.anyoptional.raft.core.election.message;

import com.anyoptional.raft.core.NodeId;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 请求投票，由 candidate 发起
 */
@Getter
@Setter
@ToString
public class RequestVoteRpc {

    /**
     * 递增后的 term，表示开始了新一轮
     * 的选举
     */
    private int term;

    /**
     * 候选者ID，一般就是发送者自身
     */
    private NodeId candidateId;

    /**
     * 候选者最后一条日志的索引
     */
    private int lastLogIndex;

    /**
     * 候选者最后一条日志的term
     */
    private int lastLogTerm;

}
