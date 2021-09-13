package com.anyoptional.raft.core.node;

import com.anyoptional.raft.core.node.role.RoleName;
import com.google.common.base.Preconditions;
import org.springframework.lang.Nullable;

/**
 * Thrown when current node is not leader.
 */
public class NotLeaderException extends RuntimeException {

    private final RoleName roleName;
    private final NodeEndpoint leaderEndpoint;

    /**
     * Create.
     *
     * @param roleName       role name
     * @param leaderEndpoint leader endpoint
     */
    public NotLeaderException(RoleName roleName, @Nullable NodeEndpoint leaderEndpoint) {
        super("not leader");
        Preconditions.checkNotNull(roleName);
        this.roleName = roleName;
        this.leaderEndpoint = leaderEndpoint;
    }

    /**
     * Get role name.
     *
     * @return role name
     */

    public RoleName getRoleName() {
        return roleName;
    }

    /**
     * Get leader endpoint.
     *
     * @return leader endpoint
     */
    @Nullable
    public NodeEndpoint getLeaderEndpoint() {
        return leaderEndpoint;
    }

}
