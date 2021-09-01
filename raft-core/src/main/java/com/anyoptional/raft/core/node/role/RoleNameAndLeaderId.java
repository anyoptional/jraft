package com.anyoptional.raft.core.node.role;

import com.anyoptional.raft.core.node.NodeId;
import com.google.common.base.Preconditions;
import org.springframework.lang.Nullable;

import javax.annotation.concurrent.Immutable;

/**
 * Role name and leader id.
 */
@Immutable
public class RoleNameAndLeaderId {

    private final RoleName roleName;

    @Nullable
    private final NodeId leaderId;

    /**
     * Create.
     *
     * @param roleName role name
     * @param leaderId leader id
     */
    public RoleNameAndLeaderId(RoleName roleName, @Nullable NodeId leaderId) {
        Preconditions.checkNotNull(roleName);
        this.roleName = roleName;
        this.leaderId = leaderId;
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
     * Get leader id.
     *
     * @return leader id
     */
    @Nullable
    public NodeId getLeaderId() {
        return leaderId;
    }

}
