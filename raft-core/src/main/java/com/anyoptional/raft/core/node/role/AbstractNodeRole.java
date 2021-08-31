package com.anyoptional.raft.core.node.role;

import lombok.AllArgsConstructor;
import lombok.Getter;

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

    /**
     * 取消超时 或 取消定时任务
     * 比如对 follower 或 candidate 来说存在选举超时，
     * 对 leader 来说存在日志复制定时任务
     */
    public abstract void cancelTimeoutOrTask();

}
