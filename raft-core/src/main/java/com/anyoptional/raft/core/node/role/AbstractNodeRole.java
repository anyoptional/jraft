package com.anyoptional.raft.core.node.role;

import com.anyoptional.raft.core.node.NodeId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.lang.Nullable;

/**
 * Raft 中三种角色的共性
 */
@Getter
@AllArgsConstructor
public abstract class AbstractNodeRole {

    /**
     * 当前节点的角色
     */
    private final RoleName name;

    /**
     * leader 节点任期号
     */
    protected final int term;

    public RoleNameAndLeaderId getNameAndLeaderId(NodeId selfId) {
        return new RoleNameAndLeaderId(name, getLeaderId(selfId));
    }

    @Nullable
    public abstract NodeId getLeaderId(NodeId selfId);

    /**
     * 取消超时 或 取消定时任务
     * 比如对 follower 或 candidate 来说存在选举超时，
     * 对 leader 来说存在日志复制定时任务
     */
    public abstract void cancelTimeoutOrTask();

    /**
     * 获取泛化的节点描述信息
     */
    public abstract RoleState getState();

    public boolean stateEquals(AbstractNodeRole that) {
        if (this.name != that.name || this.term != that.term) {
            return false;
        }
        return doStateEquals(that);
    }

    protected abstract boolean doStateEquals(AbstractNodeRole role);

}
