package com.anyoptional.raft.core.node.log;

import com.anyoptional.raft.core.node.log.sequence.EntrySequence;
import com.anyoptional.raft.core.node.log.sequence.MemoryEntrySequence;

public class MemoryLog extends AbstractLog {

    public MemoryLog() {
        this(new MemoryEntrySequence());
    }

    public MemoryLog(EntrySequence entrySequence) {
        this.entrySequence = entrySequence;
    }

}
