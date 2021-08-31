package com.anyoptional.raft.core.election.message;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class RequestVoteResult {

    /**
     * 选举ID
     */
    private final int term;

    /**
     * 是否投票
     */
    private final boolean voteGranted;

}
