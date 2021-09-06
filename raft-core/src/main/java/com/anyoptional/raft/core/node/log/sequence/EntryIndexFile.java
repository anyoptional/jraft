package com.anyoptional.raft.core.node.log.sequence;

import com.anyoptional.raft.core.support.RandomAccessFileAdapter;
import com.anyoptional.raft.core.support.SeekableFile;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * 日志条目索引文件
 *
 *       4bytes          4bytes
 * +---------------+---------------+
 * | minEntryIndex | maxEntryIndex |
 * +---------------+---------------+
 *   8bytes  4bytes  4bytes
 * +--------+------+------+
 * | offset | kind | term |
 * +--------+------+------+
 * | offset | kind | term |
 * +--------+------+------+
 * | offset | kind | term |
 * +--------+------+------+
 */
public class EntryIndexFile implements Iterable<EntryIndexItem> {

    // 最大条目索引的偏移
    private static final long OFFSET_MAX_ENTRY_INDEX = Integer.BYTES;
    // 单条日志条目元信息的长度 8 + 4 + 4
    private static final int LENGTH_ENTRY_INDEX_ITEM = 16;

    private final SeekableFile seekableFile;

    /**
     * 日志条目数
     */
    private int entryIndexCount;

    /**
     * 最小日志条目索引
     */
    private int minEntryIndex;

    /**
     * 最大日志条目索引
     */
    private int maxEntryIndex;

    private final Map<Integer, EntryIndexItem> entryIndexMap = new HashMap<>();

    public EntryIndexFile(File file) throws IOException {
        this(new RandomAccessFileAdapter(file));
    }

    public EntryIndexFile(SeekableFile seekableFile) throws IOException {
        this.seekableFile = seekableFile;
        load();
    }

    private void load() throws IOException {
        if (seekableFile.size() == 0) {
            entryIndexCount = 0;
            return;
        }
        minEntryIndex = seekableFile.readInt();
        maxEntryIndex = seekableFile.readInt();
        updateEntryIndexCount();

        for (int i = minEntryIndex; i <= maxEntryIndex; i++) {
            long offset = seekableFile.readLong();
            int kind = seekableFile.readInt();
            int term = seekableFile.readInt();
            entryIndexMap.put(i, new EntryIndexItem(i, offset, kind, term));
        }
    }

    private void updateEntryIndexCount() {
        entryIndexCount = maxEntryIndex - minEntryIndex + 1;
    }

    public boolean isEmpty() {
        return entryIndexCount == 0;
    }

    public int getMinEntryIndex() {
        checkEmpty();
        return minEntryIndex;
    }

    private void checkEmpty() {
        if (isEmpty()) {
            throw new IllegalStateException("no entry index");
        }
    }

    public int getMaxEntryIndex() {
        checkEmpty();
        return maxEntryIndex;
    }

    public int getEntryIndexCount() {
        return entryIndexCount;
    }

    /**
     * 追加日志条目索引信息
     */
    public void appendEntryIndex(int index, long offset, int kind, int term) throws IOException {
        // 没有内容，初始化 minEntryIndex
        if (seekableFile.size() == 0L) {
            seekableFile.writeInt(index);
            minEntryIndex = index;
        }
        // 已有内容，检查以下是不是顺序写入的
        else {
            if (index != maxEntryIndex + 1) {
                throw new IllegalArgumentException("index must be " + (maxEntryIndex + 1) + ", but was " + index);
            }
            // 跳到 maxEntryIndex
            seekableFile.seek(OFFSET_MAX_ENTRY_INDEX); // skip min entry index
        }

        // 更新 maxEntryIndex
        seekableFile.writeInt(index);
        maxEntryIndex = index;
        updateEntryIndexCount();

        // 跳到最后一条记录结尾处
        // 并记录下新增的元信息
        seekableFile.seek(getOffsetOfEntryIndexItem(index));
        seekableFile.writeLong(offset);
        seekableFile.writeInt(kind);
        seekableFile.writeInt(term);

        entryIndexMap.put(index, new EntryIndexItem(index, offset, kind, term));
    }

    private long getOffsetOfEntryIndexItem(int index) {
        return (long) (index - minEntryIndex) * LENGTH_ENTRY_INDEX_ITEM + Integer.BYTES * 2;
    }

    public void clear() throws IOException {
        seekableFile.truncate(0L);
        entryIndexCount = 0;
        entryIndexMap.clear();
    }

    public void removeAfter(int newMaxEntryIndex) throws IOException {
        if (isEmpty() || newMaxEntryIndex >= maxEntryIndex) {
            return;
        }
        if (newMaxEntryIndex < minEntryIndex) {
            clear();
            return;
        }
        seekableFile.seek(OFFSET_MAX_ENTRY_INDEX);
        seekableFile.writeInt(newMaxEntryIndex);
        seekableFile.truncate(getOffsetOfEntryIndexItem(newMaxEntryIndex + 1));
        for (int i = newMaxEntryIndex + 1; i <= maxEntryIndex; i++) {
            entryIndexMap.remove(i);
        }
        maxEntryIndex = newMaxEntryIndex;
        entryIndexCount = newMaxEntryIndex - minEntryIndex + 1;
    }

    public long getOffset(int entryIndex) {
        return get(entryIndex).getOffset();
    }

    public EntryIndexItem get(int entryIndex) {
        checkEmpty();
        if (entryIndex < minEntryIndex || entryIndex > maxEntryIndex) {
            throw new IllegalArgumentException("index < min or index > max");
        }
        return entryIndexMap.get(entryIndex);
    }

    @Override
    public Iterator<EntryIndexItem> iterator() {
        if (isEmpty()) {
            return Collections.emptyIterator();
        }
        return new EntryIndexIterator(entryIndexCount, minEntryIndex);
    }

    public void close() throws IOException {
        seekableFile.close();
    }

    private class EntryIndexIterator implements Iterator<EntryIndexItem> {

        private final int entryIndexCount;
        private int currentEntryIndex;

        EntryIndexIterator(int entryIndexCount, int minEntryIndex) {
            this.entryIndexCount = entryIndexCount;
            this.currentEntryIndex = minEntryIndex;
        }

        @Override
        public boolean hasNext() {
            checkModification();
            return currentEntryIndex <= maxEntryIndex;
        }

        private void checkModification() {
            if (this.entryIndexCount != EntryIndexFile.this.entryIndexCount) {
                throw new ConcurrentModificationException("entry index count changed");
            }
        }

        @Override
        public EntryIndexItem next() {
            checkModification();
            return entryIndexMap.get(currentEntryIndex++);
        }
    }

}
