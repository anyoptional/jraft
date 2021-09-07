package com.anyoptional.raft.core.node;

import com.anyoptional.raft.core.node.role.*;
import com.anyoptional.raft.core.rpc.Address;
import com.anyoptional.raft.core.rpc.MockConnector;
import com.anyoptional.raft.core.rpc.message.*;
import com.anyoptional.raft.core.schedule.NullScheduler;
import com.anyoptional.raft.core.support.DirectTaskExecutor;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class NodeImplTest {

    private NodeBuilder newNodeBuilder(NodeId selfId, NodeEndpoint... endpoints) {
        return new NodeBuilder(Arrays.asList(endpoints), selfId)
                .setScheduler(new NullScheduler())
                .setConnector(new MockConnector())
                .setTaskExecutor(new DirectTaskExecutor());
    }

    @Test
    public void testStartAsFollower() {
        NodeId selfId = NodeId.of("A");
        NodeImpl node = (NodeImpl) newNodeBuilder(selfId, new NodeEndpoint(selfId, new Address("localhost", 3306))).build();

        node.start();

        AbstractNodeRole role = node.getRole();
        assertEquals(RoleName.FOLLOWER, role.getName());

        FollowerNodeRole follower = (FollowerNodeRole) role;
        assertEquals(0, follower.getTerm());
        assertNull(follower.getVotedFor());
    }

    @Test
    public void testElectionTimeoutOnFollower() {
        NodeImpl node = (NodeImpl) newNodeBuilder(NodeId.of("A"),
                new NodeEndpoint("A", "localhost", 3306),
                new NodeEndpoint("B", "localhost", 3307),
                new NodeEndpoint("C", "localhost", 3308)
                ).build();

        node.start();

        // 手动触发选举超时
        node.electionTimeout();

        AbstractNodeRole role = node.getRole();
        assertEquals(RoleName.CANDIDATE, role.getName());

        CandidateNodeRole candidate = (CandidateNodeRole) role;
        assertEquals(1, candidate.getTerm());
        assertEquals(1, candidate.getVotesCount());

        MockConnector connector = (MockConnector) node.getContext().getConnector();
        RequestVoteRpc rpc = (RequestVoteRpc) connector.getRpc();
        assertEquals(1, rpc.getTerm());
        assertEquals(NodeId.of("A"), rpc.getCandidateId());
    }

    @Test
    public void testReceiveRequestVoteRpcOnFollower() {
        NodeImpl node = (NodeImpl) newNodeBuilder(NodeId.of("A"),
                new NodeEndpoint("A", "localhost", 3306),
                new NodeEndpoint("B", "localhost", 3307),
                new NodeEndpoint("C", "localhost", 3308)
        ).build();

        node.start();

        RequestVoteRpc rpc = new RequestVoteRpc();
        rpc.setTerm(1);
        rpc.setCandidateId(NodeId.of("C"));
        rpc.setLastLogTerm(0);
        rpc.setLastLogIndex(0);

        node.onReceiveRequestVoteRpc(new RequestVoteRpcMessage(rpc, NodeId.of("C"), null));
        MockConnector connector = (MockConnector) node.getContext().getConnector();

        RequestVoteResult result = (RequestVoteResult) connector.getResult();

        assertEquals(1, result.getTerm());
        assertTrue(result.isVoteGranted());


        AbstractNodeRole role = node.getRole();
        assertEquals(RoleName.FOLLOWER, role.getName());

        FollowerNodeRole follower = (FollowerNodeRole) role;
        assertEquals(NodeId.of("C"), follower.getVotedFor());
    }


    @Test
    public void testReceiveRequestVoteResult() {
        NodeImpl node = (NodeImpl) newNodeBuilder(NodeId.of("A"),
                new NodeEndpoint("A", "localhost", 3306),
                new NodeEndpoint("B", "localhost", 3307),
                new NodeEndpoint("C", "localhost", 3308)
        ).build();

        node.start();
        // 转成 candidate
        node.electionTimeout();

        node.onReceiveRequestVoteResult(new RequestVoteResult(1, true));

        AbstractNodeRole role = node.getRole();
        assertEquals(RoleName.LEADER, role.getName());

        LeaderNodeRole leader = (LeaderNodeRole) role;
        assertEquals(1, leader.getTerm());
    }

    @Test
    public void testReplicateLog() {
        NodeImpl node = (NodeImpl) newNodeBuilder(NodeId.of("A"),
                new NodeEndpoint("A", "localhost", 3306),
                new NodeEndpoint("B", "localhost", 3307),
                new NodeEndpoint("C", "localhost", 3308)
        ).build();

        node.start();
        // 转成 candidate
        node.electionTimeout();
        node.onReceiveRequestVoteResult(new RequestVoteResult(1, true));

        node.replicateLog();
        MockConnector connector = (MockConnector) node.getContext().getConnector();

        // 一共产生了 3 条消息
        assertEquals(3, connector.getMessageCount());
        List<MockConnector.Message> messages = connector.getMessages();

        Set<NodeId> nodeIds = messages.subList(1, 3)
                .stream()
                .map(MockConnector.Message::getDestinationNodeId)
                .collect(Collectors.toSet());

        assertEquals(2, nodeIds.size());
        assertTrue(nodeIds.contains(NodeId.of("B")));
        assertTrue(nodeIds.contains(NodeId.of("C")));

        AppendEntriesRpc rpc = (AppendEntriesRpc) messages.get(2).getRpc();
        assertEquals(1, rpc.getTerm());
        assertEquals(NodeId.of("A"), rpc.getLeaderId());
    }

    @Test
    public void testReceiveAppendEntriesOnFollower() {
        NodeImpl node = (NodeImpl) newNodeBuilder(NodeId.of("A"),
                new NodeEndpoint("A", "localhost", 3306),
                new NodeEndpoint("B", "localhost", 3307),
                new NodeEndpoint("C", "localhost", 3308)
        ).build();

        node.getContext().getLog().appendEntry(1);

        node.start();

        AppendEntriesRpc rpc = new AppendEntriesRpc();
        rpc.setLeaderId(NodeId.of("B"));
        rpc.setTerm(1);
        rpc.setPrevLogIndex(1);
        rpc.setPrevLogTerm(1);

        node.onReceiveAppendEntriesRpc(new AppendEntriesRpcMessage(rpc, NodeId.of("B"), null));
        MockConnector connector = (MockConnector) node.getContext().getConnector();

        AppendEntriesResult result = (AppendEntriesResult) connector.getResult();
        assertEquals(1, result.getTerm());
        assertTrue(result.isSuccess());

        AbstractNodeRole role = node.getRole();
        assertEquals(RoleName.FOLLOWER, role.getName());

        FollowerNodeRole follower = (FollowerNodeRole) role;
        assertEquals(NodeId.of("B"), follower.getLeaderId());
        assertEquals(1, follower.getTerm());
    }

    @Test
    public void testReceiveAppendEntriesResultOnLeader() {
        NodeImpl node = (NodeImpl) newNodeBuilder(NodeId.of("A"),
                new NodeEndpoint("A", "localhost", 3306),
                new NodeEndpoint("B", "localhost", 3307),
                new NodeEndpoint("C", "localhost", 3308)
        ).build();

        node.start();
        // 转成 candidate
        node.electionTimeout();
        // A -> Leader
        node.onReceiveRequestVoteResult(new RequestVoteResult(1, true));

        node.replicateLog();

        AppendEntriesRpc rpc = new AppendEntriesRpc();
        // leader A 收到 B 回复的消息
        node.onReceiveAppendEntriesResult(new AppendEntriesResultMessage(
                new AppendEntriesResult(1, true, rpc.getMessageId()),
                NodeId.of("B"),
                rpc
        ));

        AbstractNodeRole role = node.getRole();
        assertEquals(RoleName.LEADER, role.getName());
    }

}