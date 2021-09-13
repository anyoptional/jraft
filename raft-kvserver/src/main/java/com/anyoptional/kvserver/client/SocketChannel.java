package com.anyoptional.kvserver.client;

import com.anyoptional.kvserver.MessageConstants;
import com.anyoptional.kvserver.message.*;
import com.anyoptional.raft.core.node.NodeId;
import com.anyoptional.raft.core.service.Channel;
import com.anyoptional.raft.core.service.ChannelException;
import com.anyoptional.raft.core.service.RedirectException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;

public class SocketChannel implements Channel {

    private final String host;
    private final int port;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SocketChannel(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public Object send(Object payload) {
        try (Socket socket = new Socket()) {
            socket.setTcpNoDelay(true);
            socket.connect(new InetSocketAddress(this.host, this.port));
            this.write(socket.getOutputStream(), payload);
            return this.read(socket.getInputStream());
        } catch (IOException e) {
            throw new ChannelException("failed to send and receive", e);
        }
    }

    private Object read(InputStream input) throws IOException {
        DataInputStream dataInput = new DataInputStream(input);
        int messageType = dataInput.readInt();
        int payloadLength = dataInput.readInt();
        byte[] payload = new byte[payloadLength];
        dataInput.readFully(payload);
        switch (messageType) {
            case MessageConstants.MSG_TYPE_SUCCESS:
                return null;
            case MessageConstants.MSG_TYPE_FAILURE:
                Failure failure = objectMapper.readValue(payload, Failure.class);
                throw new ChannelException("error code " + failure.getCode() + ", message " + failure.getMessage());
            case MessageConstants.MSG_TYPE_REDIRECT:
                Redirect redirect = objectMapper.readValue(payload, Redirect.class);
                throw new RedirectException(new NodeId(redirect.getLeaderId()));
            case MessageConstants.MSG_TYPE_GET_COMMAND_RESPONSE:
                GetCommandResponse response = objectMapper.readValue(payload, GetCommandResponse.class);
                if (!response.isFound()) return null;
                return response.getValue();
            default:
                throw new ChannelException("unexpected message type " + messageType);
        }
    }

    private void write(OutputStream output, Object payload) throws IOException {
        if (payload instanceof GetCommand) {
            this.write(output, MessageConstants.MSG_TYPE_GET_COMMAND, payload);
        } else if (payload instanceof SetCommand) {
            this.write(output, MessageConstants.MSG_TYPE_SET_COMMAND, payload);
        }
    }

    private void write(OutputStream output, int messageType, Object message) throws IOException {
        DataOutputStream dataOutput = new DataOutputStream(output);
        byte[] messageBytes = objectMapper.writeValueAsBytes(message);
        dataOutput.writeInt(messageType);
        dataOutput.writeInt(messageBytes.length);
        dataOutput.write(messageBytes);
        dataOutput.flush();
    }


}
