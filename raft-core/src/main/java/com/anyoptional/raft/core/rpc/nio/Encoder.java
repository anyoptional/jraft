package com.anyoptional.raft.core.rpc.nio;

import com.anyoptional.raft.core.node.NodeId;
import com.anyoptional.raft.core.rpc.message.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class Encoder extends MessageToByteEncoder<Object> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public Encoder() {
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        if (msg instanceof NodeId) {
            out.writeInt(MessageConstants.MSG_TYPE_NODE_ID);
        } else if (msg instanceof RequestVoteRpc) {
            out.writeInt(MessageConstants.MSG_TYPE_REQUEST_VOTE_RPC);
        } else if (msg instanceof RequestVoteResult) {
            out.writeInt(MessageConstants.MSG_TYPE_REQUEST_VOTE_RESULT);
        } else if (msg instanceof AppendEntriesRpc) {
            out.writeInt(MessageConstants.MSG_TYPE_APPEND_ENTRIES_RPC);
        } else if (msg instanceof AppendEntriesResult) {
            out.writeInt(MessageConstants.MSG_TYPE_APPEND_ENTRIES_RESULT);
        } else {
            return;
        }
        byte[] payload = objectMapper.writeValueAsBytes(msg);
        out.writeInt(payload.length);
        out.writeBytes(payload);
    }
}
