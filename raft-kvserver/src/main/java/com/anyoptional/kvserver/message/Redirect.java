package com.anyoptional.kvserver.message;

import com.anyoptional.raft.core.node.NodeId;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.lang.Nullable;

/**
 * 重定向，follower 收到请求时重定向给 leader 去处理
 */
public class Redirect {

    /**
     * 刚启动的时候，集群中还未有 leader
     */
    @Nullable
    private final String leaderId;

    public Redirect(@Nullable NodeId leaderId) {
        this(leaderId != null ? leaderId.getValue() : null);
    }

    @JsonCreator
    public Redirect(@Nullable @JsonProperty("leaderId") String leaderId) {
        this.leaderId = leaderId;
    }

    @Nullable
    public String getLeaderId() {
        return leaderId;
    }

    @Override
    public String toString() {
        return "Redirect{" + "leaderId=" + leaderId + '}';
    }

}
