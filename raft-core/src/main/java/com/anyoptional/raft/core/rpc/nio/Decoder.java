package com.anyoptional.raft.core.rpc.nio;

import com.anyoptional.raft.core.node.NodeId;
import com.anyoptional.raft.core.rpc.message.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.DecoderException;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 *  4bytes  4bytes  length bytes
 * +------+--------+---------+
 * | type | length | payload |
 * +------+--------+---------+
 */
@Slf4j
public class Decoder extends ByteToMessageDecoder {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

        int messageType = in.readInt();
        int payloadLength = in.readInt();
        byte[] payload = new byte[payloadLength];
        in.readBytes(payload);

        switch (messageType) {
            case MessageConstants.MSG_TYPE_NODE_ID:
                out.add(objectMapper.readValue(payload, NodeId.class));
                break;
            case MessageConstants.MSG_TYPE_REQUEST_VOTE_RPC:
                out.add(objectMapper.readValue(payload, RequestVoteRpc.class));
                break;
            case MessageConstants.MSG_TYPE_REQUEST_VOTE_RESULT:
                out.add(objectMapper.readValue(payload, RequestVoteResult.class));
                break;
            case MessageConstants.MSG_TYPE_APPEND_ENTRIES_RPC:
                out.add(objectMapper.readValue(payload, AppendEntriesRpc.class));
                break;
            case MessageConstants.MSG_TYPE_APPEND_ENTRIES_RESULT:
                out.add(objectMapper.readValue(payload, AppendEntriesResult.class));
                break;
            default:
                throw new DecoderException("unsupported msg type");
        }

        log.info("receive {}", out);
    }

}
