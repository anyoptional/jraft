package com.anyoptional.raft.core;


import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.ToString;

import java.util.Objects;

/**
 * 集群节点唯一标识
 */
@Getter
@ToString
public class NodeId {

    private final String value;

    public NodeId(String value) {
        Preconditions.checkNotNull(value);
        this.value = value;
    }

    public static NodeId of(String value) {
        return new NodeId(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NodeId nodeId = (NodeId) o;
        return Objects.equals(value, nodeId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

}
