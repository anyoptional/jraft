package com.anyoptional.raft.core.node.log.entry;

import static com.anyoptional.raft.core.node.log.entry.Entry.KIND_GENERAL;
import static com.anyoptional.raft.core.node.log.entry.Entry.KIND_NO_OP;

public class EntryFactory {

    public Entry create(int kind, int index, int term, byte[] commandBytes) {
        switch (kind) {
            case KIND_NO_OP: return new NoOpEntry(index, term);
            case KIND_GENERAL: return new GeneralEntry(index, term, commandBytes);
            default: throw new IllegalStateException("unexpected kind");
        }
    }

}
