package com.anyoptional.raft.core.node.store;

import com.anyoptional.raft.core.node.NodeId;
import org.springframework.lang.Nullable;

/**
 * 除了日志条目，当前选举term和投票过给谁也需要进行持久化，
 * 这是因为，假设节点B在给节点A投票后重启，收到来自候选节点
 * C的消息后再次投票，就违反了一票制。因此，节点B重启后需要
 * 恢复之前的已投票节点，同时对应的选举term也必须恢复，否则
 * 不能判断先后顺序。
 */
public interface NodeStore extends AutoCloseable {

    int getTerm();

    void setTerm(int term);

    @Nullable
    NodeId getVotedFor();

    void setVotedFor(@Nullable NodeId votedFor);

    @Override
    void close();

}
