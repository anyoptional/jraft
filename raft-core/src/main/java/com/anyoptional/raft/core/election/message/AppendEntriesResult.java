package com.anyoptional.raft.core.election.message;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@RequiredArgsConstructor
public class AppendEntriesResult {

    /**
     * 选举term
     */
    private final int term;

    /**
     * 是否追加成功
     */
    private final boolean success;

}
