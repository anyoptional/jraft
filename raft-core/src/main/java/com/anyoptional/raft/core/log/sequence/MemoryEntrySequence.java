package com.anyoptional.raft.core.log.sequence;

import com.anyoptional.raft.core.log.entry.Entry;

import java.util.ArrayList;
import java.util.List;

public class MemoryEntrySequence extends AbstractEntrySequence {

    private int commitIndex = 0;

    private final List<Entry> storage = new ArrayList<>();

    public MemoryEntrySequence() {
        // 偏移为1
        this(1);
    }

    public MemoryEntrySequence(int logIndexOffset) {
        super(logIndexOffset);
    }

    @Override
    protected Entry doGetEntry(int index) {
        return storage.get(index - logIndexOffset);
    }

    @Override
    protected List<Entry> doSubList(int fromIndex, int toIndex) {
        return storage.subList(fromIndex - logIndexOffset, toIndex - logIndexOffset);
    }

    @Override
    protected void doAppend(Entry entry) {
        storage.add(entry);
    }

    @Override
    protected void doRemoveAfter(int index) {
        // 注意更新 nextLogIndex
        if (index < doGetFirstLogIndex()) {
            storage.clear();
            nextLogIndex = logIndexOffset;
            return;
        }
        storage.subList(index - logIndexOffset + 1, storage.size());
        nextLogIndex = index + 1;
        if (index < commitIndex) {
            commitIndex = index;
        }
    }

    @Override
    public void commit(int index) {
        commitIndex = index;
    }

    @Override
    public int getCommitIndex() {
        return commitIndex;
    }

    @Override
    public void close() {

    }

    @Override
    public String toString() {
        return "MemoryEntrySequence{" +
                "logIndexOffset=" + logIndexOffset +
                ", nextLogIndex=" + nextLogIndex +
                ", commitIndex=" + commitIndex +
                ", entries.size=" + storage.size() +
                '}';
    }

}
