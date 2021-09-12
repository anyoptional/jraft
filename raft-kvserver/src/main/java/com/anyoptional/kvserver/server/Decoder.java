package com.anyoptional.kvserver.server;

import com.anyoptional.kvserver.MessageConstants;
import com.anyoptional.kvserver.message.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 *  4bytes  4bytes  length bytes
 * +------+--------+---------+
 * | type | length | payload |
 * +------+--------+---------+
 */
public class Decoder extends ByteToMessageDecoder {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        int messageType = in.readInt();
        int payloadLength = in.readInt();
        byte[] payload = new byte[payloadLength];
        in.readBytes(payload);

        switch (messageType) {
            case MessageConstants.MSG_TYPE_SUCCESS:
                out.add(Success.INSTANCE);
                break;
            case MessageConstants.MSG_TYPE_FAILURE:
                out.add(objectMapper.readValue(payload, Failure.class));
                break;
            case MessageConstants.MSG_TYPE_REDIRECT:
                out.add(objectMapper.readValue(payload, Redirect.class));
                break;
            case MessageConstants.MSG_TYPE_ADD_SERVER_COMMAND:
                break;
            case MessageConstants.MSG_TYPE_REMOVE_SERVER_COMMAND:
                break;
            case MessageConstants.MSG_TYPE_GET_COMMAND:
                out.add(objectMapper.readValue(payload, GetCommand.class));

                break;
            case MessageConstants.MSG_TYPE_GET_COMMAND_RESPONSE:
                out.add(objectMapper.readValue(payload, GetCommandResponse.class));
                break;
            case MessageConstants.MSG_TYPE_SET_COMMAND:
                out.add(objectMapper.readValue(payload, SetCommand.class));
                break;
            default:
                throw new IllegalStateException("unexpected message type " + messageType);
        }
    }

}
