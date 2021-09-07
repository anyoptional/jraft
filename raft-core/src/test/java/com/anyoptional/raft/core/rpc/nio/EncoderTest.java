package com.anyoptional.raft.core.rpc.nio;

import com.anyoptional.raft.core.node.NodeId;
import com.anyoptional.raft.core.node.log.entry.Entry;
import com.anyoptional.raft.core.node.log.entry.GeneralEntry;
import com.anyoptional.raft.core.node.log.entry.NoOpEntry;
import com.anyoptional.raft.core.rpc.message.AppendEntriesRpc;
import com.anyoptional.raft.core.rpc.message.MessageConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static com.anyoptional.raft.core.node.log.entry.Entry.KIND_GENERAL;
import static com.anyoptional.raft.core.node.log.entry.Entry.KIND_NO_OP;
import static org.junit.Assert.*;

public class EncoderTest {

    @Test
    public void testNodeId() throws Exception {
        Encoder encoder = new Encoder();
        ByteBuf buffer = Unpooled.buffer();
        encoder.encode(null, NodeId.of("A"), buffer);
        assertEquals(MessageConstants.MSG_TYPE_NODE_ID, buffer.readInt());
        assertEquals(13, buffer.readInt());
        assertEquals("{\"value\":\"A\"}", buffer.readCharSequence(13, StandardCharsets.UTF_8));
    }

    @Test
    public void testAppendEntriesRpc() throws Exception {
        Encoder encoder = new Encoder();
        ByteBuf buffer = Unpooled.buffer();

        AppendEntriesRpc rpc = new AppendEntriesRpc();
        rpc.setTerm(1);
        rpc.setPrevLogTerm(1);
        rpc.setPrevLogIndex(12);
        rpc.setMessageId(99);
        rpc.setLeaderCommit(1);
        rpc.setLeaderId(NodeId.of("A"));
        rpc.setEntries(Arrays.asList(new NoOpEntry(2, 1), new GeneralEntry(3, 1, "hello world".getBytes(StandardCharsets.UTF_8))));

        encoder.encode(null, rpc, buffer);

        assertEquals(MessageConstants.MSG_TYPE_APPEND_ENTRIES_RPC, buffer.readInt());
        buffer.readInt();
        byte[] bytes = new byte[buffer.readableBytes()];
        buffer.readBytes(bytes);
        AppendEntriesRpc value = new ObjectMapper().readValue(bytes, AppendEntriesRpc.class);

        /**
         * {"messageId":99,"term":1,"leaderId":{"value":"A"},
         * "prevLogIndex":12,"prevLogTerm":1,
         * "entries":[{"kind":0,"index":2,"term":1,"commandBytes":"","meta":{"kind":0,"index":2,"term":1}},
         * {"kind":1,"index":3,"term":1,"commandBytes":"aGVsbG8gd29ybGQ=","meta":{"kind":1,"index":3,"term":1}}],
         * "leaderCommit":1}
         */
        assertEquals(1, value.getTerm());
        assertEquals(1, value.getPrevLogTerm());
        assertEquals(12, value.getPrevLogIndex());
        assertEquals(99, value.getMessageId());
        assertEquals(1, value.getLeaderCommit());
        assertEquals(NodeId.of("A"), value.getLeaderId());
        assertEquals(2, value.getEntries().size());
        Entry entry = value.getEntries().get(0);
        assertEquals(2, entry.getIndex());
        assertEquals(1, entry.getTerm());
        assertEquals(KIND_NO_OP, entry.getKind());
        entry = value.getEntries().get(1);
        assertEquals(KIND_GENERAL, entry.getKind());
        assertEquals(3, entry.getIndex());
        assertEquals(1, entry.getTerm());
        assertEquals("hello world", new String(entry.getCommandBytes(), StandardCharsets.UTF_8));
    }

}