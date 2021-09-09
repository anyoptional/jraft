package com.anyoptional.raft.core.log.sequence;

import com.anyoptional.raft.core.log.entry.Entry;
import com.anyoptional.raft.core.log.entry.EntryMeta;
import com.anyoptional.raft.core.log.entry.NoOpEntry;
import com.anyoptional.raft.core.support.ByteArraySeekableFile;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

public class FileEntrySequenceTest {

    private EntriesFile entriesFile;
    private EntryIndexFile entryIndexFile;
    
    @Before
    public void setUp() throws IOException {
        entriesFile = new EntriesFile(new ByteArraySeekableFile());
        entryIndexFile = new EntryIndexFile(new ByteArraySeekableFile());
    }

    @Test
    public void testInitializeEmpty() {
        FileEntrySequence sequence = new FileEntrySequence(entriesFile, entryIndexFile, 5);
        assertEquals(5, sequence.getNextLogIndex());
        assertTrue(sequence.isEmpty());
    }

    @Test
    public void testInitialize() throws IOException {
        entryIndexFile.appendEntryIndex(1, 0L, 1, 1);
        entryIndexFile.appendEntryIndex(2, 20L, 1, 1);
        FileEntrySequence sequence = new FileEntrySequence(entriesFile, entryIndexFile, 1);
        assertEquals(3, sequence.getNextLogIndex());
        assertEquals(1, sequence.getFirstLogIndex());
        assertEquals(2, sequence.getLastLogIndex());
        assertEquals(0, sequence.getCommitIndex());
    }
    

    private void appendEntryToFile(Entry entry) throws IOException {
        long offset = entriesFile.appendEntry(entry);
        entryIndexFile.appendEntryIndex(entry.getIndex(), offset, entry.getKind(), entry.getTerm());
    }
    
    @Test
    public void testSubList1() throws IOException {
        appendEntryToFile(new NoOpEntry(1, 1)); // 1
        appendEntryToFile(new NoOpEntry(2, 2)); // 2
        FileEntrySequence sequence = new FileEntrySequence(entriesFile, entryIndexFile, 1);
        sequence.append(new NoOpEntry(sequence.getNextLogIndex(), 3)); // 3
        sequence.append(new NoOpEntry(sequence.getNextLogIndex(), 4)); // 4

        List<Entry> subList = sequence.subList(1);
        assertEquals(4, subList.size());
        assertEquals(1, subList.get(0).getIndex());
        assertEquals(4, subList.get(3).getIndex());
    }

    @Test
    public void testSubList2() throws IOException {
        appendEntryToFile(new NoOpEntry(1, 1)); // 1
        appendEntryToFile(new NoOpEntry(2, 2)); // 2
        FileEntrySequence sequence = new FileEntrySequence(entriesFile, entryIndexFile, 1);
        sequence.append(new NoOpEntry(sequence.getNextLogIndex(), 3)); // 3
        sequence.append(new NoOpEntry(sequence.getNextLogIndex(), 4)); // 4

        List<Entry> subList = sequence.subList(2);
        assertEquals(3, subList.size());
        assertEquals(2, subList.get(0).getIndex());
        assertEquals(4, subList.get(2).getIndex());
    }

    @Test
    public void testSubList3() throws IOException {
        appendEntryToFile(new NoOpEntry(1, 1)); // 1
        appendEntryToFile(new NoOpEntry(2, 2)); // 2
        FileEntrySequence sequence = new FileEntrySequence(entriesFile, entryIndexFile, 1);
        sequence.append(new NoOpEntry(sequence.getNextLogIndex(), 3)); // 3
        sequence.append(new NoOpEntry(sequence.getNextLogIndex(), 4)); // 4

        List<Entry> subList = sequence.subList(3);
        assertEquals(2, subList.size());
        assertEquals(3, subList.get(0).getIndex());
        assertEquals(4, subList.get(1).getIndex());
    }

    @Test
    public void testSubList4() throws IOException {
        appendEntryToFile(new NoOpEntry(1, 1)); // 1
        appendEntryToFile(new NoOpEntry(2, 2)); // 2
        FileEntrySequence sequence = new FileEntrySequence(entriesFile, entryIndexFile, 1);
        sequence.append(new NoOpEntry(sequence.getNextLogIndex(), 3)); // 3
        sequence.append(new NoOpEntry(sequence.getNextLogIndex(), 4)); // 4

        List<Entry> subList = sequence.subList(4);
        assertEquals(1, subList.size());
        assertEquals(4, subList.get(0).getIndex());
    }

    @Test
    public void testSubList5() throws IOException {
        appendEntryToFile(new NoOpEntry(1, 1)); // 1
        appendEntryToFile(new NoOpEntry(2, 2)); // 2
        FileEntrySequence sequence = new FileEntrySequence(entriesFile, entryIndexFile, 1);
        sequence.append(new NoOpEntry(sequence.getNextLogIndex(), 3)); // 3
        sequence.append(new NoOpEntry(sequence.getNextLogIndex(), 4)); // 4

        List<Entry> subList = sequence.subList(5);
        assertEquals(0, subList.size());
    }

    @Test
    public void testSubList6() throws IOException {
        appendEntryToFile(new NoOpEntry(1, 1)); // 1
        appendEntryToFile(new NoOpEntry(2, 2)); // 2
        FileEntrySequence sequence = new FileEntrySequence(entriesFile, entryIndexFile, 1);
        sequence.append(new NoOpEntry(sequence.getNextLogIndex(), 3)); // 3
        sequence.append(new NoOpEntry(sequence.getNextLogIndex(), 4)); // 4

        List<Entry> subList = sequence.subList(3, 4);
        assertEquals(1, subList.size());
        assertEquals(3, subList.get(0).getIndex());
    }

    @Test
    public void testSubList7() throws IOException {
        appendEntryToFile(new NoOpEntry(1, 1)); // 1
        appendEntryToFile(new NoOpEntry(2, 2)); // 2
        FileEntrySequence sequence = new FileEntrySequence(entriesFile, entryIndexFile, 1);

        List<Entry> subList = sequence.subList(1, 3);
        assertEquals(2, subList.size());
        assertEquals(1, subList.get(0).getIndex());
        assertEquals(2, subList.get(1).getIndex());
    }

    @Test
    public void testGetEntry() throws IOException {
        appendEntryToFile(new NoOpEntry(1, 1));
        FileEntrySequence sequence = new FileEntrySequence(entriesFile, entryIndexFile, 1);
        sequence.append(new NoOpEntry(2, 1));
        assertNull(sequence.getEntry(0));
        assertEquals(1, sequence.getEntry(1).getIndex());
        assertEquals(2, sequence.getEntry(2).getIndex());
        assertNull(sequence.getEntry(3));
    }

    @Test
    public void testGetEntryMetaNotFound() throws IOException {
        appendEntryToFile(new NoOpEntry(1, 1));
        FileEntrySequence sequence = new FileEntrySequence(entriesFile, entryIndexFile, 1);
        assertNull(sequence.getEntryMeta(2));
    }

    @Test
    public void testGetEntryMetaInPendingEntries() {
        FileEntrySequence sequence = new FileEntrySequence(entriesFile, entryIndexFile, 1);
        sequence.append(new NoOpEntry(1, 1));
        EntryMeta meta = sequence.getEntryMeta(1);
        assertNotNull(meta);
        assertEquals(Entry.KIND_NO_OP, meta.getKind());
        assertEquals(1, meta.getIndex());
        assertEquals(1, meta.getTerm());
    }

    @Test
    public void testGetEntryMetaInIndexFile() throws IOException {
        appendEntryToFile(new NoOpEntry(1, 1));
        FileEntrySequence sequence = new FileEntrySequence(entriesFile, entryIndexFile, 1);
        EntryMeta meta = sequence.getEntryMeta(1);
        assertNotNull(meta);
        assertEquals(Entry.KIND_NO_OP, meta.getKind());
        assertEquals(1, meta.getIndex());
        assertEquals(1, meta.getTerm());
    }

    @Test
    public void testGetLastEntryEmpty() {
        FileEntrySequence sequence = new FileEntrySequence(entriesFile, entryIndexFile, 1);
        assertNull(sequence.getLastEntry());
    }

    @Test
    public void testGetLastEntryFromFile() throws IOException {
        appendEntryToFile(new NoOpEntry(1, 1));
        FileEntrySequence sequence = new FileEntrySequence(entriesFile, entryIndexFile, 1);
        assertEquals(1, sequence.getLastEntry().getIndex());
    }

    @Test
    public void testGetLastEntryFromPendingEntries() {
        FileEntrySequence sequence = new FileEntrySequence(entriesFile, entryIndexFile, 1);
        sequence.append(new NoOpEntry(1, 1));
        assertEquals(1, sequence.getLastEntry().getIndex());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAppendIllegalIndex() {
        FileEntrySequence sequence = new FileEntrySequence(entriesFile, entryIndexFile, 1);
        sequence.append(new NoOpEntry(2, 1));
    }

    @Test
    public void testAppendEntry() {
        FileEntrySequence sequence = new FileEntrySequence(entriesFile, entryIndexFile, 1);
        assertEquals(1, sequence.getNextLogIndex());
        sequence.append(new NoOpEntry(1, 1));
        assertEquals(2, sequence.getNextLogIndex());
        assertEquals(1, sequence.getLastEntry().getIndex());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCommitBeforeCommitIndex() {
        FileEntrySequence sequence = new FileEntrySequence(entriesFile, entryIndexFile, 1);
        sequence.commit(-1);
    }

    @Test
    public void testCommitDoNothing() {
        FileEntrySequence sequence = new FileEntrySequence(entriesFile, entryIndexFile, 1);
        sequence.commit(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCommitNoPendingEntry() {
        FileEntrySequence sequence = new FileEntrySequence(entriesFile, entryIndexFile, 1);
        sequence.commit(2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCommitAfterLastLogIndex() {
        FileEntrySequence sequence = new FileEntrySequence(entriesFile, entryIndexFile, 1);
        sequence.append(new NoOpEntry(1, 1));
        sequence.commit(2);
    }

    @Test
    public void testCommitOne() {
        FileEntrySequence sequence = new FileEntrySequence(entriesFile, entryIndexFile, 1);
        sequence.append(new NoOpEntry(1, 1));
        sequence.append(new NoOpEntry(2, 1));
        assertEquals(0, sequence.getCommitIndex());
        assertEquals(0, entryIndexFile.getEntryIndexCount());
        sequence.commit(1);
        assertEquals(1, sequence.getCommitIndex());
        assertEquals(1, entryIndexFile.getEntryIndexCount());
        assertEquals(1, entryIndexFile.getMaxEntryIndex());
    }

    @Test
    public void testCommitMultiple() {
        FileEntrySequence sequence = new FileEntrySequence(entriesFile, entryIndexFile, 1);
        sequence.append(new NoOpEntry(1, 1));
        sequence.append(new NoOpEntry(2, 1));
        assertEquals(0, sequence.getCommitIndex());
        assertEquals(0, entryIndexFile.getEntryIndexCount());
        sequence.commit(2);
        assertEquals(2, sequence.getCommitIndex());
        assertEquals(2, entryIndexFile.getEntryIndexCount());
        assertEquals(2, entryIndexFile.getMaxEntryIndex());
    }

    @Test
    public void testRemoveAfterEmpty() {
        FileEntrySequence sequence = new FileEntrySequence(entriesFile, entryIndexFile, 1);
        sequence.removeAfter(1);
    }

    @Test
    public void testRemoveAfterLargerThanLastLogIndex() {
        FileEntrySequence sequence = new FileEntrySequence(entriesFile, entryIndexFile, 1);
        sequence.append(new NoOpEntry(1, 1));
        sequence.removeAfter(1);
        assertEquals(1, sequence.getFirstLogIndex());
        assertEquals(1, sequence.getLastLogIndex());
    }

    // (no entry in file) : (1 pending entry)
    @Test
    public void testRemoveAfterSmallerThanFirstLogIndex1() {
        FileEntrySequence sequence = new FileEntrySequence(entriesFile, entryIndexFile, 1);
        sequence.append(new NoOpEntry(sequence.getNextLogIndex(), 1));
        assertEquals(1, sequence.getFirstLogIndex());
        assertEquals(1, sequence.getLastLogIndex());
        sequence.removeAfter(0);
        assertTrue(sequence.isEmpty());
    }

    // (1 entry in file) : (no pending entry)
    @Test
    public void testRemoveAfterSmallerThanFirstLogIndex2() throws IOException {
        appendEntryToFile(new NoOpEntry(1, 1));
        FileEntrySequence sequence = new FileEntrySequence(entriesFile, entryIndexFile, 1);
        assertEquals(1, sequence.getFirstLogIndex());
        assertEquals(1, sequence.getLastLogIndex());
        sequence.removeAfter(0);
        assertTrue(sequence.isEmpty());
        assertEquals(0L, entriesFile.size());
        assertTrue(entryIndexFile.isEmpty());
    }

    // (1 entry in file) : (1 pending entry)
    @Test
    public void testRemoveAfterSmallerThanFirstLogIndex3() throws IOException {
        appendEntryToFile(new NoOpEntry(1, 1));
        FileEntrySequence sequence = new FileEntrySequence(entriesFile, entryIndexFile, 1);
        sequence.append(new NoOpEntry(2, 1));
        assertEquals(1, sequence.getFirstLogIndex());
        assertEquals(2, sequence.getLastLogIndex());
        sequence.removeAfter(0);
        assertTrue(sequence.isEmpty());
        assertEquals(0L, entriesFile.size());
        assertTrue(entryIndexFile.isEmpty());
    }

    @Test
    public void testRemoveAfterPendingEntries2() {
        FileEntrySequence sequence = new FileEntrySequence(entriesFile, entryIndexFile, 1);
        sequence.append(new NoOpEntry(sequence.getNextLogIndex(), 1));
        sequence.append(new NoOpEntry(sequence.getNextLogIndex(), 2));
        assertEquals(1, sequence.getFirstLogIndex());
        assertEquals(2, sequence.getLastLogIndex());
        sequence.removeAfter(1);
        assertEquals(1, sequence.getFirstLogIndex());
        assertEquals(1, sequence.getLastLogIndex());
    }

    @Test
    public void testRemoveAfterEntriesInFile2() throws IOException {
        appendEntryToFile(new NoOpEntry(1, 1)); // 1
        appendEntryToFile(new NoOpEntry(2, 1)); // 2
        FileEntrySequence sequence = new FileEntrySequence(entriesFile, entryIndexFile, 1);
        sequence.append(new NoOpEntry(3, 2)); // 3
        assertEquals(1, sequence.getFirstLogIndex());
        assertEquals(3, sequence.getLastLogIndex());
        sequence.removeAfter(1);
        assertEquals(1, sequence.getFirstLogIndex());
        assertEquals(1, sequence.getLastLogIndex());
    }


}