package com.anyoptional.raft.core.election.role;

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
