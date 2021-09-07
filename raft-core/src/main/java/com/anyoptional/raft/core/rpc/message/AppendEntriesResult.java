package com.anyoptional.raft.core.rpc.message;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class AppendEntriesResult {

    /**
     * 选举term
     */
    private final int term;

    /**
     * 是否追加成功
     */
    private final boolean success;

    private final int rpcMessageId;

    @JsonCreator
    public AppendEntriesResult(@JsonProperty("term") int term,
                               @JsonProperty("success") boolean success,
                               @JsonProperty("rpcMessageId") int rpcMessageId)  {
        this.term = term;
        this.success = success;
        this.rpcMessageId = rpcMessageId;
    }

}