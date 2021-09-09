package com.anyoptional.raft.core.log.entry;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@RequiredArgsConstructor
public class EntryMeta {

    private final int kind;
    private final int index;
    private final int term;

}
