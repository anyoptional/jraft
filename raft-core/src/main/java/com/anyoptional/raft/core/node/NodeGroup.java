package com.anyoptional.raft.core.node;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 集群中的节点信息，每个节点都会保存一份
 */
public class NodeGroup {

    private static final Logger logger = LoggerFactory.getLogger(NodeGroup.class);

    /**
     * 当前节点id
     */
    private final NodeId selfId;

    /**
     * 节点映射表
     */
    private final Map<NodeId, GroupMember> members;

    /**
     * 单机模式
     */
    public NodeGroup(NodeEndpoint endpoint) {
        this(endpoint.getId(), Collections.singletonList(endpoint));
    }

    /**
     * 集群模式
     * @param selfId 当前节点id
     * @param endpoints 集群内所有节点的连接信息
     */
    public NodeGroup(NodeId selfId, Collection<NodeEndpoint> endpoints) {
        Assert.notEmpty(endpoints, "endpoints may not be empty");
        this.selfId = selfId;
        this.members = initMembers(endpoints);
    }

    @Nullable
    public GroupMember getGroupMember(NodeId id) {
        return members.get(id);
    }

    public GroupMember getRequiredGroupMember(NodeId id) {
        GroupMember result = getGroupMember(id);
        Assert.notNull(result, "group member not exists");
        return result;
    }

    /**
     * 列出正在进行日志复制的节点（仅 leader 使用）
     */
    public Collection<GroupMember> getReplicatingGroupMembers() {
        return members.values().stream()
                // 除 selfId 之外的节点
                .filter($0 -> !$0.getEndpoint().getId().equals(selfId))
                .collect(Collectors.toList());
    }

    public Set<NodeEndpoint> getEndpointsExceptSelf() {
        Set<NodeEndpoint> result = new HashSet<>();
        for (GroupMember member : members.values()) {
            if (!member.getEndpoint().getId().equals(selfId)) {
                result.add(member.getEndpoint());
            }
        }
        return result;
    }

    public int getCount() {
        return members.size();
    }

    private Map<NodeId, GroupMember> initMembers(Collection<NodeEndpoint> endpoints) {
        Map<NodeId, GroupMember> result = new HashMap<>();
        for (NodeEndpoint endpoint : endpoints) {
            Assert.notNull(endpoint, "endpoint may not be null");
            result.putIfAbsent(endpoint.getId(), new GroupMember(endpoint));
        }
        return result;
    }

    /**
     * Reset replicating state.
     *
     * @param nextLogIndex next log index
     */
    void resetReplicatingStates(int nextLogIndex) {
        for (GroupMember member : members.values()) {
            if (!member.idEquals(selfId)) {
                member.setReplicatingState(new ReplicatingState(nextLogIndex));
            }
        }
    }

    /**
     * Get match index of major members.
     * <p>
     * To get major match index in group, sort match indices and get the middle one.
     * </p>
     *
     * @return match index
     */
    int getMatchIndexOfMajor() {
        List<NodeMatchIndex> matchIndices = new ArrayList<>();
        for (GroupMember member : members.values()) {
            if (!member.idEquals(selfId)) {
                matchIndices.add(new NodeMatchIndex(member.getEndpoint().getId(), member.getMatchIndex()));
            }
        }
        int count = matchIndices.size();
        if (count == 0) {
            throw new IllegalStateException("standalone or no major node");
        }
        // 按 matchIndex 排序
        Collections.sort(matchIndices);
        logger.debug("match indices {}", matchIndices);
        return matchIndices.get(count / 2).getMatchIndex();
    }

    /**
     * Node match index.
     *
     * @see NodeGroup#getMatchIndexOfMajor()
     */
    private static class NodeMatchIndex implements Comparable<NodeMatchIndex> {

        private final NodeId nodeId;
        private final int matchIndex;

        NodeMatchIndex(NodeId nodeId, int matchIndex) {
            this.nodeId = nodeId;
            this.matchIndex = matchIndex;
        }

        int getMatchIndex() {
            return matchIndex;
        }

        @Override
        public int compareTo(NodeMatchIndex o) {
            return Integer.compare(matchIndex, o.matchIndex);
        }

        @Override
        public String toString() {
            return "<" + nodeId + ", " + matchIndex + ">";
        }

    }
}
