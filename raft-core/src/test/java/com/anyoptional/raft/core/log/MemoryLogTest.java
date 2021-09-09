package com.anyoptional.raft.core.log;

import com.anyoptional.raft.core.node.NodeId;
import com.anyoptional.raft.core.log.entry.Entry;
import com.anyoptional.raft.core.log.entry.EntryMeta;
import com.anyoptional.raft.core.log.entry.NoOpEntry;
import com.anyoptional.raft.core.log.sequence.MemoryEntrySequence;
import com.anyoptional.raft.core.rpc.message.AppendEntriesRpc;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MemoryLogTest {

    @Test
    public void testGetLastEntryMetaNoLogAndSnapshot() {
        MemoryLog log = new MemoryLog();
        EntryMeta lastEntryMeta = log.getLastEntryMeta();
        Assert.assertEquals(Entry.KIND_NO_OP, lastEntryMeta.getKind());
        Assert.assertEquals(0, lastEntryMeta.getIndex());
        Assert.assertEquals(0, lastEntryMeta.getTerm());
    }


    @Test
    public void testGetLastEntryMetaNoSnapshot() {
        MemoryLog log = new MemoryLog();
        log.appendEntry(1); // 1
        log.appendEntry(1); // 2
        EntryMeta lastEntryMeta = log.getLastEntryMeta();
        Assert.assertEquals(2, lastEntryMeta.getIndex());
        Assert.assertEquals(1, lastEntryMeta.getTerm());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateAppendEntriesIllegalNextIndex() {
        MemoryLog log = new MemoryLog();
        log.createAppendEntriesRpc(1, new NodeId("A"), 2, Log.ALL_ENTRIES);
    }

    @Test
    public void testCreateAppendEntriesRpcNoLog() {
        MemoryLog log = new MemoryLog();
        NodeId nodeId = new NodeId("A");
        AppendEntriesRpc rpc = log.createAppendEntriesRpc(
                1, nodeId, 1, Log.ALL_ENTRIES
        );
        Assert.assertEquals(1, rpc.getTerm());
        Assert.assertEquals(nodeId, rpc.getLeaderId());
        Assert.assertEquals(0, rpc.getPrevLogIndex());
        Assert.assertEquals(0, rpc.getEntries().size());
        Assert.assertEquals(0, rpc.getLeaderCommit());
    }

    @Test
    public void testCreateAppendEntriesRpcStartFromOne() {
        MemoryLog log = new MemoryLog();
        log.appendEntry(1); // 1
        log.appendEntry(1); // 2
        AppendEntriesRpc rpc = log.createAppendEntriesRpc(
                1, new NodeId("A"), 1, Log.ALL_ENTRIES
        );
        Assert.assertEquals(1, rpc.getTerm());
        Assert.assertEquals(0, rpc.getPrevLogIndex());
        Assert.assertEquals(0, rpc.getPrevLogTerm());
        Assert.assertEquals(2, rpc.getEntries().size());
        Assert.assertEquals(1, rpc.getEntries().get(0).getIndex());
    }

    @Test
    public void testCreateAppendEntriesRpcOneLogEntry() {
        MemoryLog log = new MemoryLog();
        log.appendEntry(1); // 1
        AppendEntriesRpc rpc = log.createAppendEntriesRpc(
                1, new NodeId("A"), 2, Log.ALL_ENTRIES
        );
        Assert.assertEquals(1, rpc.getTerm());
        Assert.assertEquals(1, rpc.getPrevLogIndex());
        Assert.assertEquals(0, rpc.getEntries().size());
        Assert.assertEquals(0, rpc.getLeaderCommit());
    }

    @Test
    public void testCreateAppendEntriesRpcTwoLogEntriesFrom2() {
        MemoryLog log = new MemoryLog();
        log.appendEntry(1); // 1
        log.appendEntry(1); // 2
        AppendEntriesRpc rpc = log.createAppendEntriesRpc(
                1, new NodeId("A"), 2, Log.ALL_ENTRIES
        );
        Assert.assertEquals(1, rpc.getTerm());
        Assert.assertEquals(1, rpc.getPrevLogIndex());
        Assert.assertEquals(1, rpc.getEntries().size());
        Assert.assertEquals(2, rpc.getEntries().get(0).getIndex());
    }

    @Test
    public void testCreateAppendEntriesRpcTwoLogEntriesFrom3() {
        MemoryLog log = new MemoryLog();
        log.appendEntry(1); // 1
        log.appendEntry(1); // 2
        AppendEntriesRpc rpc = log.createAppendEntriesRpc(
                1, new NodeId("A"), 3, Log.ALL_ENTRIES
        );
        Assert.assertEquals(1, rpc.getTerm());
        Assert.assertEquals(2, rpc.getPrevLogIndex());
        Assert.assertEquals(0, rpc.getEntries().size());
    }

    @Test
    public void testCreateAppendEntriesRpcLimit1() {
        MemoryLog log = new MemoryLog();
        log.appendEntry(1); // 1
        log.appendEntry(1); // 2
        log.appendEntry(1); // 3
        log.appendEntry(1); // 4
        AppendEntriesRpc rpc = log.createAppendEntriesRpc(
                1, new NodeId("A"), 3, 1
        );
        Assert.assertEquals(1, rpc.getTerm());
        Assert.assertEquals(2, rpc.getPrevLogIndex());
        Assert.assertEquals(1, rpc.getEntries().size());
        Assert.assertEquals(3, rpc.getEntries().get(0).getIndex());
    }

    @Test
    public void testCreateAppendEntriesRpcLimit2() {
        MemoryLog log = new MemoryLog();
        log.appendEntry(1); // 1
        log.appendEntry(1); // 2
        log.appendEntry(1); // 3
        log.appendEntry(1); // 4
        AppendEntriesRpc rpc = log.createAppendEntriesRpc(
                1, new NodeId("A"), 3, 2
        );
        Assert.assertEquals(1, rpc.getTerm());
        Assert.assertEquals(2, rpc.getPrevLogIndex());
        Assert.assertEquals(2, rpc.getEntries().size());
        Assert.assertEquals(3, rpc.getEntries().get(0).getIndex());
        Assert.assertEquals(4, rpc.getEntries().get(1).getIndex());
    }

    @Test
    public void testCreateAppendEntriesRpcLimit3() {
        MemoryLog log = new MemoryLog();
        log.appendEntry(1); // 1
        log.appendEntry(1); // 2
        log.appendEntry(1); // 3
        log.appendEntry(1); // 4
        AppendEntriesRpc rpc = log.createAppendEntriesRpc(
                1, new NodeId("A"), 3, 3
        );
        Assert.assertEquals(1, rpc.getTerm());
        Assert.assertEquals(2, rpc.getPrevLogIndex());
        Assert.assertEquals(2, rpc.getEntries().size());
        Assert.assertEquals(3, rpc.getEntries().get(0).getIndex());
        Assert.assertEquals(4, rpc.getEntries().get(1).getIndex());
    }

    @Test
    public void testGetNextLogEmpty() {
        MemoryLog log = new MemoryLog();
        Assert.assertEquals(1, log.getNextIndex());
    }

    @Test
    public void testGetNextLog() {
        MemoryLog log = new MemoryLog(
                new MemoryEntrySequence(4)
        );
        Assert.assertEquals(4, log.getNextIndex());
    }

    @Test
    public void testIsNewerThanNoLog() {
        MemoryLog log = new MemoryLog();
        Assert.assertFalse(log.isNewerThan(0, 0));
    }

    @Test
    public void testIsNewerThanSame() {
        MemoryLog log = new MemoryLog();
        log.appendEntry(1); // index = 1, term = 1
        Assert.assertFalse(log.isNewerThan(1, 1));
    }

    @Test
    public void testIsNewerThanHighTerm() {
        MemoryLog log = new MemoryLog();
        log.appendEntry(2); // index = 1, term = 2
        Assert.assertTrue(log.isNewerThan(1, 1));
    }

    @Test
    public void testIsNewerThanMoreLog() {
        MemoryLog log = new MemoryLog();
        log.appendEntry(1);
        log.appendEntry(1); // index = 2, term = 1
        Assert.assertTrue(log.isNewerThan(1, 1));
    }


    // prevLogIndex == snapshot.lastIncludedIndex
    // prevLogTerm != snapshot.lastIncludedTerm
    @Test
    public void testAppendEntriesFromLeaderSnapshot2() {
        MemoryLog log = new MemoryLog(
                new MemoryEntrySequence(4)
        );
        Assert.assertFalse(log.appendEntriesFromLeader(3, 5, Collections.emptyList()));
    }

    // prevLogIndex < snapshot.lastIncludedIndex
    @Test
    public void testAppendEntriesFromLeaderSnapshot3() {
        MemoryLog log = new MemoryLog(
                new MemoryEntrySequence(4)
        );
        Assert.assertFalse(log.appendEntriesFromLeader(1, 4, Collections.emptyList()));
    }

    @Test
    public void testAppendEntriesFromLeaderPrevLogNotFound() {
        MemoryLog log = new MemoryLog();
        Assert.assertEquals(1, log.getNextIndex());
        Assert.assertFalse(log.appendEntriesFromLeader(1, 1, Collections.emptyList()));
    }

    @Test
    public void testAppendEntriesFromLeaderPrevLogTermNotMatch() {
        MemoryLog log = new MemoryLog();
        log.appendEntry(1);
        Assert.assertFalse(log.appendEntriesFromLeader(1, 2, Collections.emptyList()));
    }

    // (index, term)
    // follower: (1, 1), (2, 1)
    // leader  :         (2, 1), (3, 2)
    @Test
    public void testAppendEntriesFromLeaderSkip() {
        MemoryLog log = new MemoryLog();
        log.appendEntry(1); // 1
        log.appendEntry(1); // 2
        List<Entry> leaderEntries = Arrays.asList(
                new NoOpEntry(2, 1),
                new NoOpEntry(3, 2)
        );
        Assert.assertTrue(log.appendEntriesFromLeader(1, 1, leaderEntries));
    }

    @Test
    public void testAppendEntriesFromLeaderNoConflict() {
        MemoryLog log = new MemoryLog();
        log.appendEntry(1); // 1
        List<Entry> leaderEntries = Arrays.asList(
                new NoOpEntry(2, 1),
                new NoOpEntry(3, 1)
        );
        Assert.assertTrue(log.appendEntriesFromLeader(1, 1, leaderEntries));
    }

    // follower: (1, 1), (2, 1)
    // leader  :         (2, 2), (3, 2)
    @Test
    public void testAppendEntriesFromLeaderConflict1() {
        MemoryLog log = new MemoryLog();
        log.appendEntry(1); // 1
        log.appendEntry(1); // 2
        List<Entry> leaderEntries = Arrays.asList(
                new NoOpEntry(2, 2),
                new NoOpEntry(3, 2)
        );
        Assert.assertTrue(log.appendEntriesFromLeader(1, 1, leaderEntries));
    }

    // follower: (1, 1), (2, 1), (3, 1)
    // leader  :         (2, 1), (3, 2)
    @Test
    public void testAppendEntriesFromLeaderConflict2() {
        MemoryLog log = new MemoryLog();
        log.appendEntry(1); // 1
        log.appendEntry(1); // 2
        log.appendEntry(1); // 3
        List<Entry> leaderEntries = Arrays.asList(
                new NoOpEntry(2, 1),
                new NoOpEntry(3, 2)
        );
        Assert.assertTrue(log.appendEntriesFromLeader(1, 1, leaderEntries));
    }

    // follower: (1, 1), (2, 1), (3, 1, no-op, committed)
    // leader  :         (2, 1), (3, 2)
    @Test
    public void testAppendEntriesFromLeaderConflict3() {
        MemoryLog log = new MemoryLog();
        log.appendEntry(1); // 1
        log.appendEntry(1); // 2
        log.appendEntry(1); // 3
        log.advanceCommitIndex(3, 1);
        List<Entry> leaderEntries = Arrays.asList(
                new NoOpEntry(2, 1),
                new NoOpEntry(3, 2)
        );
        Assert.assertTrue(log.appendEntriesFromLeader(1, 1, leaderEntries));
    }

    // follower: (1, 1), (2, 1), (3, 1, general, committed)
    // leader  :         (2, 1), (3, 2)
    @Test
    public void testAppendEntriesFromLeaderConflict4() {
        MemoryLog log = new MemoryLog();
        log.appendEntry(1); // 1
        log.appendEntry(1); // 2
        log.appendEntry(1, "test".getBytes()); // 3
        log.advanceCommitIndex(3, 1);
        List<Entry> leaderEntries = Arrays.asList(
                new NoOpEntry(2, 1),
                new NoOpEntry(3, 2)
        );
        Assert.assertTrue(log.appendEntriesFromLeader(1, 1, leaderEntries));
        Assert.assertEquals(2, log.getCommitIndex());
    }


    @Test
    public void testAdvanceCommitIndexLessThanCurrentCommitIndex() {
        MemoryLog log = new MemoryLog();
        log.advanceCommitIndex(0, 1);
    }

    @Test
    public void testAdvanceCommitIndexEntryNotFound() {
        MemoryLog log = new MemoryLog();
        log.advanceCommitIndex(1, 1);
    }

    @Test
    public void testAdvanceCommitIndexNotCurrentTerm() {
        MemoryLog log = new MemoryLog();
        log.appendEntry(1);
        log.advanceCommitIndex(1, 2);
    }

    @Test
    public void testAdvanceCommitIndex() {
        MemoryLog log = new MemoryLog();
        log.appendEntry(1);
        Assert.assertEquals(0, log.getCommitIndex());
        log.advanceCommitIndex(1, 1);
        Assert.assertEquals(1, log.getCommitIndex());
    }

}