package com.anyoptional.kvserver.server;

import com.anyoptional.kvserver.MessageConstants;
import com.anyoptional.kvserver.message.*;
import com.fasterxml.jackson.databind.DeserializationFeature;
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

        if (msg instanceof Success) {
            out.writeInt(MessageConstants.MSG_TYPE_SUCCESS);
        } else if (msg instanceof Failure) {
            out.writeInt(MessageConstants.MSG_TYPE_FAILURE);
        } else if (msg instanceof Redirect) {
            out.writeInt(MessageConstants.MSG_TYPE_REDIRECT);
        } else if (msg instanceof GetCommand) {
            out.writeInt(MessageConstants.MSG_TYPE_GET_COMMAND);
        } else if (msg instanceof GetCommandResponse) {
            out.writeInt(MessageConstants.MSG_TYPE_GET_COMMAND_RESPONSE);
        } else if (msg instanceof SetCommand) {
            out.writeInt(MessageConstants.MSG_TYPE_SET_COMMAND);
        } else {
            return;
        }
        byte[] payload = objectMapper.writeValueAsBytes(msg);
        out.writeInt(payload.length);
        out.writeBytes(payload);
    }

}
