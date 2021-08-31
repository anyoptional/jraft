package com.anyoptional.raft.core.node.store;


import com.anyoptional.raft.core.node.NodeId;
import org.springframework.lang.Nullable;

import javax.annotation.concurrent.NotThreadSafe;

@NotThreadSafe
public class MemoryNodeStore implements NodeStore {

    private int term;

    @Nullable
    private NodeId votedFor;

    public MemoryNodeStore() {
        this(0, null);
    }

    public MemoryNodeStore(int term, @Nullable NodeId votedFor) {
        this.term = term;
        this.votedFor = votedFor;
    }

    @Override
    public int getTerm() {
        return term;
    }

    @Override
    public void setTerm(int term) {
        this.term = term;
    }

    @Override
    @Nullable
    public NodeId getVotedFor() {
        return votedFor;
    }

    @Override
    public void setVotedFor(@Nullable NodeId votedFor) {
        this.votedFor = votedFor;
    }

    @Override
    public void close() {
    }

}
