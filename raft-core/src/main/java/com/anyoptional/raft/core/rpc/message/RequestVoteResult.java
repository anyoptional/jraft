package com.anyoptional.raft.core.rpc.message;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class RequestVoteResult {

    /**
     * 选举ID
     */
    private final int term;

    /**
     * 是否投票
     */
    private final boolean voteGranted;

    @JsonCreator
    public RequestVoteResult(@JsonProperty("term") int term,
                             @JsonProperty("voteGranted") boolean voteGranted) {
        this.term = term;
        this.voteGranted = voteGranted;
    }

}