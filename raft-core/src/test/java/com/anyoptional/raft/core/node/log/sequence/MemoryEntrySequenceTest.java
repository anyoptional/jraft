package com.anyoptional.raft.core.node.log.sequence;

import com.anyoptional.raft.core.node.log.entry.Entry;
import com.anyoptional.raft.core.node.log.entry.EntryMeta;
import com.anyoptional.raft.core.node.log.entry.NoOpEntry;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class MemoryEntrySequenceTest {

    @Test
    public void testAppendEntry() {
        MemoryEntrySequence sequence = new MemoryEntrySequence();
        sequence.append(new NoOpEntry(sequence.getNextLogIndex(), 1));
        assertEquals(2, sequence.getNextLogIndex());
        assertEquals(1, sequence.getLastLogIndex());
    }

    @Test
    public void testAppendEntries() {
        MemoryEntrySequence sequence = new MemoryEntrySequence();
        sequence.append(Arrays.asList(
                new NoOpEntry(1, 1),
                new NoOpEntry(2, 1)
        ));
        assertEquals(3, sequence.getNextLogIndex());
        assertEquals(2, sequence.getLastLogIndex());
    }

    /**
     * 随机访问日志条目
     */
    @Test
    public void testGetEntry() {
        MemoryEntrySequence sequence = new MemoryEntrySequence(2);
        sequence.append(Arrays.asList(new NoOpEntry(2, 1), new NoOpEntry(3, 1)));
        assertNull(sequence.getEntry(1));
        assertEquals(2, sequence.getEntry(2).getIndex());
        assertEquals(3, sequence.getEntry(3).getIndex());
        assertNull(sequence.getEntry(4));
    }

    @Test
    public void testGetEntryMeta() {
        MemoryEntrySequence sequence = new MemoryEntrySequence(2);
        assertNull(sequence.getEntry(2));
        sequence.append(new NoOpEntry(2, 1));
        EntryMeta meta = sequence.getEntryMeta(2);
        assertNotNull(meta);
        assertEquals(2, meta.getIndex());
        assertEquals(1, meta.getTerm());
    }

    @Test
    public void testIsEntryPresent() {
        MemoryEntrySequence sequence = new MemoryEntrySequence(1);
        assertFalse(sequence.isEntryPresent(1));
        sequence.append(new NoOpEntry(1, 1));
        assertTrue(sequence.isEntryPresent(1));
        assertFalse(sequence.isEntryPresent(0));
        assertFalse(sequence.isEntryPresent(2));
    }

    @Test(expected = EmptySequenceException.class)
    public void testSubListEmpty() {
        MemoryEntrySequence sequence = new MemoryEntrySequence(2);
        assertTrue(sequence.subList(2, 2).isEmpty());
    }

    @Test
    public void testSubListResultEmpty() {
        MemoryEntrySequence sequence = new MemoryEntrySequence(2);
        sequence.append(new NoOpEntry(2, 1));
        assertTrue(sequence.subList(2, 2).isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSubListOutOfIndex() {
        MemoryEntrySequence sequence = new MemoryEntrySequence(2);
        sequence.append(new NoOpEntry(2, 1));
        sequence.subList(1, 3);
    }

    @Test
    public void testSubListOneElement() {
        MemoryEntrySequence sequence = new MemoryEntrySequence(2);
        sequence.append(Arrays.asList(
                new NoOpEntry(2, 1),
                new NoOpEntry(3, 1)
        ));
        List<Entry> subList = sequence.subList(2, 3);
        assertEquals(1, subList.size());
        assertEquals(2, subList.get(0).getIndex());
    }

    @Test
    public void testSubView() {
        MemoryEntrySequence sequence = new MemoryEntrySequence(2);
        sequence.append(Arrays.asList(
                new NoOpEntry(2, 1),
                new NoOpEntry(3, 1)
        ));
        List<Entry> subList = sequence.subList(2);
        assertEquals(2, subList.size());
        assertEquals(2, subList.get(0).getIndex());
        assertEquals(3, subList.get(1).getIndex());
    }

    @Test
    public void testSubView2() {
        MemoryEntrySequence sequence = new MemoryEntrySequence(2);
        sequence.append(Arrays.asList(
                new NoOpEntry(2, 1),
                new NoOpEntry(3, 1)
        ));
        List<Entry> subList = sequence.subList(4);
        assertEquals(0, subList.size());
    }

    @Test
    public void testSubView3() {
        MemoryEntrySequence sequence = new MemoryEntrySequence(2);
        sequence.append(Arrays.asList(
                new NoOpEntry(2, 1),
                new NoOpEntry(3, 1)
        ));
        List<Entry> subList = sequence.subList(1);
        assertEquals(2, subList.size());
    }

    @Test
    public void testRemoveAfterEmpty() {
        MemoryEntrySequence sequence = new MemoryEntrySequence();
        sequence.removeAfter(1);
    }

    @Test
    public void testRemoveAfterNoAction() {
        MemoryEntrySequence sequence = new MemoryEntrySequence(2);
        sequence.append(Arrays.asList(
                new NoOpEntry(2, 1),
                new NoOpEntry(3, 1)
        ));
        sequence.removeAfter(3);
        assertEquals(3, sequence.getLastLogIndex());
        assertEquals(4, sequence.getNextLogIndex());
    }

    @Test
    public void testRemoveAfterPartial() {
        MemoryEntrySequence sequence = new MemoryEntrySequence(2);
        sequence.append(Arrays.asList(
                new NoOpEntry(2, 1),
                new NoOpEntry(3, 1)
        ));
        sequence.removeAfter(2);
        assertEquals(2, sequence.getLastLogIndex());
        assertEquals(3, sequence.getNextLogIndex());
    }

    @Test
    public void testRemoveAfterAll() {
        MemoryEntrySequence sequence = new MemoryEntrySequence(2);
        sequence.append(Arrays.asList(
                new NoOpEntry(2, 1),
                new NoOpEntry(3, 1)
        ));
        assertNotNull(sequence.getEntry(2));
        sequence.removeAfter(1);
        assertTrue(sequence.isEmpty());
        assertEquals(2, sequence.getNextLogIndex());
    }

}