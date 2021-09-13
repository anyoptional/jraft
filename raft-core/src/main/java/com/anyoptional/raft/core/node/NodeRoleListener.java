package com.anyoptional.raft.core.node;

import com.anyoptional.raft.core.node.role.RoleState;

/**
 * Node role listener.
 */
public interface NodeRoleListener {

    /**
     * Called when node role changes. e.g FOLLOWER to CANDIDATE.
     *
     * @param roleState role state
     */
    void nodeRoleChanged(RoleState roleState);

}
