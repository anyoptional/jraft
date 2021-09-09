package com.anyoptional.raft.core.rpc.nio;

import com.anyoptional.raft.core.node.NodeId;
import com.anyoptional.raft.core.log.entry.Entry;
import com.anyoptional.raft.core.rpc.message.AppendEntriesRpc;
import com.anyoptional.raft.core.rpc.message.MessageConstants;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static com.anyoptional.raft.core.log.entry.Entry.KIND_GENERAL;
import static com.anyoptional.raft.core.log.entry.Entry.KIND_NO_OP;
import static org.junit.Assert.*;

public class DecoderTest {

    @Test
    public void testNodeId() throws Exception {
        Decoder decoder = new Decoder();
        ByteBuf buffer = Unpooled.buffer();
        buffer.writeInt(MessageConstants.MSG_TYPE_NODE_ID);
        buffer.writeInt(13);
        buffer.writeCharSequence("{\"value\":\"A\"}", StandardCharsets.UTF_8);
        List<Object> out = new ArrayList<>();
        decoder.decode(null, buffer, out);
        assertEquals(1, out.size());
        assertEquals(NodeId.of("A"), out.get(0));
    }

    @Test
    public void testAppendEntriesRpc() throws Exception {
        Decoder decoder = new Decoder();
        ByteBuf buffer = Unpooled.buffer();
        buffer.writeInt(MessageConstants.MSG_TYPE_APPEND_ENTRIES_RPC);

        byte[] json = ("{\"messageId\":99,\"term\":1,\"leaderId\":{\"value\":\"A\"},"
         + "\"prevLogIndex\":12,\"prevLogTerm\":1,"
         + "\"entries\":[{\"kind\":0,\"index\":2,\"term\":1,\"commandBytes\":\"\",\"meta\":{\"kind\":0,\"index\":2,\"term\":1}},"
         + "{\"kind\":1,\"index\":3,\"term\":1,\"commandBytes\":\"aGVsbG8gd29ybGQ=\",\"meta\":{\"kind\":1,\"index\":3,\"term\":1}}],"
         + "\"leaderCommit\":1}").getBytes(StandardCharsets.UTF_8);

        buffer.writeInt(json.length);
        buffer.writeBytes(json);
        List<Object> out = new ArrayList<>();
        decoder.decode(null, buffer, out);
        assertEquals(1, out.size());
        Object o = out.get(0);
        assertTrue(o instanceof AppendEntriesRpc);

        AppendEntriesRpc value = (AppendEntriesRpc) o;

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