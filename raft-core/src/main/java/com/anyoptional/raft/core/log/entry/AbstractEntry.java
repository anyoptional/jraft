package com.anyoptional.raft.core.log.entry;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public abstract class AbstractEntry implements Entry {

    private final int kind;
    protected final int index;
    protected final int term;

    @Override
    public EntryMeta getMeta() {
        return new EntryMeta(kind, index, term);
    }

}
