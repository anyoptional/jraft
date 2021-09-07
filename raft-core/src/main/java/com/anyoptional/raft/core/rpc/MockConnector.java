package com.anyoptional.raft.core.rpc;


import com.anyoptional.raft.core.node.NodeEndpoint;
import com.anyoptional.raft.core.node.NodeId;
import com.anyoptional.raft.core.rpc.message.*;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class MockConnector implements Connector {

    private LinkedList<Message> messages = new LinkedList<>();

    @Override
    public void sendRequestVote(RequestVoteRpc rpc, Collection<NodeEndpoint> destinationEndpoints) {
        Message m = new Message();
        m.rpc = rpc;
        messages.add(m);
    }

    @Override
    public void replyRequestVote(RequestVoteResult result, NodeEndpoint destinationEndpoint) {
        Message m = new Message();
        m.result = result;
        m.destinationNodeId = destinationEndpoint.getId();
        messages.add(m);
    }

    @Override
    public void sendAppendEntries(AppendEntriesRpc rpc, NodeEndpoint destinationEndpoint) {
        Message m = new Message();
        m.rpc = rpc;
        m.destinationNodeId = destinationEndpoint.getId();
        messages.add(m);
    }

    @Override
    public void replyAppendEntries(AppendEntriesResult result, NodeEndpoint destinationEndpoint) {
        Message m = new Message();
        m.result = result;
        m.destinationNodeId = destinationEndpoint.getId();
        messages.add(m);
    }

    @Nullable
    public Message getLastMessage() {
        return messages.isEmpty() ? null : messages.getLast();
    }

    @Override
    public void resetChannels() {

    }

    private Message getLastMessageOrDefault() {
        return messages.isEmpty() ? new Message() : messages.getLast();
    }

    public Object getRpc() {
        return getLastMessageOrDefault().rpc;
    }

    public Object getResult() {
        return getLastMessageOrDefault().result;
    }

    public NodeId getDestinationNodeId() {
        return getLastMessageOrDefault().destinationNodeId;
    }

    public int getMessageCount() {
        return messages.size();
    }

    public List<Message> getMessages() {
        return new ArrayList<>(messages);
    }

    public void clearMessage() {
        messages.clear();
    }

    public static class Message {

        // rpc 消息
        private Object rpc;
        // 目标节点
        private NodeId destinationNodeId;
        // 结果
        private Object result;

        public Object getRpc() {
            return rpc;
        }

        public NodeId getDestinationNodeId() {
            return destinationNodeId;
        }

        public Object getResult() {
            return result;
        }

        @Override
        public String toString() {
            return "Message{" +
                    "destinationNodeId=" + destinationNodeId +
                    ", rpc=" + rpc +
                    ", result=" + result +
                    '}';
        }

    }

}
