package com.anyoptional.raft.core.node.role;

public enum RoleName {

    /**
     * 跟随者
     */
    FOLLOWER,

    /**
     * 候选者
     */
    CANDIDATE,

    /**
     * 首领
     */
    LEADER

}
