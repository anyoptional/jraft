package com.anyoptional.raft.core.node.role;


import com.anyoptional.raft.core.node.NodeId;
import org.springframework.lang.Nullable;

/**
 * Role state.
 */
public interface RoleState {

    int VOTES_COUNT_NOT_SET = -1;

    /**
     * Get role name.
     *
     * @return role name
     */
    RoleName getRoleName();

    /**
     * Get term.
     *
     * @return term
     */
    int getTerm();

    /**
     * Get votes count.
     *
     * @return votes count, {@value VOTES_COUNT_NOT_SET} if unknown
     */
    int getVotesCount();

    /**
     * Get voted for.
     *
     * @return voted for
     */
    @Nullable
    NodeId getVotedFor();

    /**
     * Get leader id.
     *
     * @return leader id
     */
    @Nullable
    NodeId getLeaderId();

}
