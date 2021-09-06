package com.anyoptional.raft.core.node.log.sequence;

import com.anyoptional.raft.core.node.log.entry.Entry;
import com.anyoptional.raft.core.node.log.entry.EntryMeta;
import com.google.common.base.Preconditions;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;

/**
 * 初始情况下 logIndexOffset = nextLogIndex = 1，
 * 因此当 logIndexOffset = nextLogIndex 时日志序列为空,
 * 且 lastLogIndex = nextLogIndex - 1
 *                                              nextLogIndex
 *                                                 /|\
 * logIndexOffset            lastLogIndex           |
 *    /|\                        /|\       +--------
 *     |                          |        |
 * +-----+  +-----+  +-----+  +-----+  +-----+  +-----+
 * |  E  |  |  E  |  |  E  |  |  E  |  |  E  |  |  E  |----> 此时这条日志还不存在
 * +-----+  +-----+  +-----+  +-----+  +-----+  +-----+
 */
public abstract class AbstractEntrySequence implements EntrySequence {

    /**
     * 日志索引偏移量，当存在日志快照时，日志
     * 序列的索引就不是从 1 开始了，而是要参
     * 照已存在的日志快照有个相对位移
     */
    int logIndexOffset;

    /**
     * 下一条日志的索引
     */
    int nextLogIndex;

    public AbstractEntrySequence(int logIndexOffset) {
        this.logIndexOffset = logIndexOffset;
        this.nextLogIndex = logIndexOffset;
    }

    @Override
    public boolean isEmpty() {
        return logIndexOffset == nextLogIndex;
    }

    //======================
    //      区间索引
    //======================

    @Override
    public int getFirstLogIndex() {
        if (isEmpty()) {
            throw new EmptySequenceException();
        }
        return doGetFirstLogIndex();
    }

    int doGetFirstLogIndex() {
        return logIndexOffset;
    }

    @Override
    public int getLastLogIndex() {
        if (isEmpty()) {
            throw new EmptySequenceException();
        }
        return doGetLastLogIndex();
    }

    int doGetLastLogIndex() {
        return nextLogIndex - 1;
    }

    @Override
    public int getNextLogIndex() {
        return nextLogIndex;
    }

    @Override
    public boolean isEntryPresent(int index) {
        if (isEmpty()) {
            return false;
        }
        return index >= doGetFirstLogIndex() && index <= doGetLastLogIndex();
    }

    //======================
    //      日志条目
    //======================

    @Nullable
    @Override
    public Entry getEntry(int index) {
        if (!isEntryPresent(index)) {
            return null;
        }
        return doGetEntry(index);
    }

    @Nullable
    @Override
    public EntryMeta getEntryMeta(int index) {
        Entry entry = getEntry(index);
        if (entry != null) {
            return entry.getMeta();
        }
        return null;
    }

    @Nullable
    @Override
    public Entry getLastEntry() {
        if (isEmpty()) {
            return null;
        }
        return doGetEntry(doGetLastLogIndex());
    }

    @Override
    public List<Entry> subList(int fromIndex) {
        if (isEmpty() || fromIndex > doGetLastLogIndex()) {
            return Collections.emptyList();
        }
        return subList(Math.max(fromIndex, doGetFirstLogIndex()), nextLogIndex);
    }

    @Override
    public List<Entry> subList(int fromIndex, int toIndex) {
        if (isEmpty()) {
            throw new EmptySequenceException();
        }
        if (fromIndex < doGetFirstLogIndex() || toIndex > doGetLastLogIndex() + 1 || fromIndex > toIndex) {
            throw new IllegalArgumentException("illegal from index " + fromIndex + " or to index " + toIndex);
        }
        return doSubList(fromIndex, toIndex);
    }

    @Override
    public void append(Entry entry) {
        Preconditions.checkNotNull(entry);
        if (entry.getIndex() != getNextLogIndex()) {
            throw new IllegalArgumentException("entry index must be " + getNextLogIndex());
        }
        doAppend(entry);
        // 递增序列
        nextLogIndex += 1;
    }

    @Override
    public void append(List<Entry> entries) {
        if (CollectionUtils.isEmpty(entries)) {
            return;
        }
        for (Entry entry : entries) {
            append(entry);
        }
    }

    @Override
    public void removeAfter(int index) {
        if (isEmpty() || index >= doGetLastLogIndex()) {
            return;
        }
        doRemoveAfter(index);
    }

    protected abstract Entry doGetEntry(int index);

    protected abstract List<Entry> doSubList(int fromIndex, int toIndex);

    protected abstract void doAppend(Entry entry);

    protected abstract void doRemoveAfter(int index);

}
