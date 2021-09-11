package com.anyoptional.raft.core.node;


import com.anyoptional.raft.core.log.entry.EntryMeta;
import com.anyoptional.raft.core.node.role.*;
import com.anyoptional.raft.core.node.store.NodeStore;
import com.anyoptional.raft.core.rpc.Connector;
import com.anyoptional.raft.core.rpc.message.*;
import com.anyoptional.raft.core.schedule.ElectionTimeout;
import com.anyoptional.raft.core.schedule.LogReplicationTask;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.Set;

/**
 * NodeImpl 处理任务只在 {@link NodeContext#getTaskExecutor()} 线程，因而线程封闭。
 */
public class NodeImpl implements Node {

    private static final Logger logger = LoggerFactory.getLogger(NodeImpl.class);

    /**
     * 上下文，用以间接访问其它组件
     */
    private final NodeContext context;

    /**
     * 是否已经启动
     */
    private boolean started;

    /**
     * 当前的角色及信息
     */
    private AbstractNodeRole role;

    public NodeImpl(NodeContext context) {
        Preconditions.checkNotNull(context);
        this.context = context;
    }

    @VisibleForTesting
    NodeContext getContext() {
        return context;
    }

    @VisibleForTesting
    AbstractNodeRole getRole() {
        return role;
    }

    @Override
    public synchronized void start() {
        if (started) return;

        // 注册自身到 EventBus
        context.getEventBus().register(this);
        // 初始化 rpc 组件
        context.getConnector().initialize();
        // 根据 raft 算法要求，节点以 follower 角色启动
        // 获取可能的投票记录和leader任期
        NodeStore store = context.getStore();
        changeToRole(new FollowerNodeRole(store.getTerm(), store.getVotedFor(), null, scheduleElectionTimeout()));

        // 标记已启动
        started = true;
    }

    @Override
    public synchronized void stop() throws InterruptedException {
        if (!started) {
            throw new IllegalStateException("not started");
        }

        // 逐个关闭组件
        context.getScheduler().stop();
        context.getConnector().close();
        context.getTaskExecutor().shutdown();

        // 标记为未启动
        started = false;
    }

    /**
     * 角色切换
     */
    private synchronized void changeToRole(AbstractNodeRole newRole) {
        logger.debug("node: {}, role state changed -> {}", context.getSelfId(), newRole);

        // votedFor 需要被记录，避免破坏一票制
        NodeStore store = context.getStore();
        store.setTerm(newRole.getTerm());
        if (newRole.getName() == RoleName.FOLLOWER) {
            store.setVotedFor(((FollowerNodeRole) newRole).getVotedFor());
        }
        role = newRole;
    }

    private ElectionTimeout scheduleElectionTimeout() {
        return context.getScheduler().scheduleElectionTimeout(this::electionTimeout);
    }

    private LogReplicationTask scheduleLogReplicationTask() {
        return context.getScheduler().scheduleLogReplicationTask(this::replicateLog);
    }

    /**
     * 选举超时入口
     */
    void electionTimeout() {
        // electionTimeout 在定时任务线程中执行的
        // 我们需要把它调度到任务线程中执行（NodeImpl的逻辑处理都在任务线程）
        context.getTaskExecutor().submit(this::doProcessElectionTimeout);
    }

    /**
     * 日志复制、心跳入口
     */
    void replicateLog() {
        context.getTaskExecutor().submit(this::doReplicateLog);
    }

    /**
     * 选举超时后有两个任务：
     *  1、切换成 candidate
     *  2、发起投票请求
     */
    private void doProcessElectionTimeout() {
        // bug ?
        if (role.getName() == RoleName.LEADER) {
            logger.warn("node {} is leader right now, ignore election timeout", context.getSelfId());
            return;
        }

        // 1、切换成 Candidate
        // 对 follower 来说是发起选举
        // 对 candidate 来说是再次发起选举
        // 根据 raft 算法，每次发起选举时 term 都要递增
        int newTerm = role.getTerm() + 1;
        // 取消当前定时任务
        role.cancelTimeoutOrTask();
        // 切换角色为 candidate
        changeToRole(new CandidateNodeRole(newTerm, scheduleElectionTimeout()));

        // 2、发起投票请求
        RequestVoteRpc rpc = new RequestVoteRpc();
        rpc.setTerm(newTerm);
        rpc.setCandidateId(context.getSelfId());
        EntryMeta lastEntryMeta = context.getLog().getLastEntryMeta();
        rpc.setLastLogIndex(lastEntryMeta.getIndex());
        rpc.setLastLogTerm(lastEntryMeta.getTerm());

        Connector connector = context.getConnector();
        // 获取集群中的其它节点（不包括自身）
        Set<NodeEndpoint> endpoints = context.getGroup().getEndpointsExceptSelf();
        connector.sendRequestVote(rpc, endpoints);
    }

    /**
     * 节点收到 请求投票 的请求后，需要选择投票还是不投票
     */
    @Subscribe
    public void onReceiveRequestVoteRpc(RequestVoteRpcMessage rpcMessage) {
        // 需要转移到任务线程执行
        context.getTaskExecutor().submit(() -> {
            // 决定是否投票
            RequestVoteResult voteResult = doProcessRequestVoteRpc(rpcMessage);
            // 回复候选者
            GroupMember member = context.getGroup().getRequiredGroupMember(rpcMessage.getSourceNodeId());
            context.getConnector().replyRequestVote(voteResult, member.getEndpoint());
        });
    }

    private RequestVoteResult doProcessRequestVoteRpc(RequestVoteRpcMessage rpcMessage) {
        RequestVoteRpc rpc = rpcMessage.get();
        // 时代变啦
        // 网络抖动产生的过期消息
        if (rpc.getTerm() < role.getTerm()) {
            logger.debug("term from rpc < current term, don't vote: {} < {}", rpc.getTerm(), role.getTerm());
            // 返回当前的 term 给发起方，不投票
            // 返回当前的 term 是为了让对方得到更新，进入新时代
            return new RequestVoteResult(role.getTerm(), false);
        }

        boolean voteGranted = !context.getLog().isNewerThan(rpc.getLastLogIndex(), rpc.getLastLogTerm());

        // candidate 的 term 比自身的大
        if (rpc.getTerm() > role.getTerm()) {
            // 这里收到投票请求的，可能是另一个 candidate
            // 投不投票看情况，但是切换成 follower 是必须的
            becomeFollower(rpc.getTerm(), voteGranted ? rpc.getCandidateId() : null, null, true);
            // 角色已经发生了变化，成为 follower 之后可以投票了，并不未违反一票制
            return new RequestVoteResult(rpc.getTerm(), voteGranted);
        }

        // candidate 的 term 和自身的一样大
        // 出现这种情况可能是
        // case 1:
        // 多个节点以不同的 term 启动（宕机前记录的），选举超时后，
        // candidate 碰巧发送投票请求到比自己 term 大的 follower 节点
        // 此时仍旧比较日志决定是否投票
        // 比如
        // node1 1 2 3
        // node2 1 2 3 （4）
        // node3 1 2 3
        // 3个节点term相同，假如此时发生了选举，node2
        // 收到了 term 为4的投票请求，投完票后宕机，此时
        // NodeStore记录的就是4，假如node3成功成为leader，
        // 但还没有处理任何日志复制就宕机，那么它的 term 仍然
        // 是3。都重启后再次发生选举，node1成为候选者，它发出
        // term为4的投票请求，就会发生 case1
        // case 2:
        // 同时出现了两个 candidate，其中部分已投票的 follower
        // 可能收到其它 candidate 的投票请求，此时就需要比对是不是
        // 之前已经投过票的节点
        switch (role.getName()) {
            case FOLLOWER: {
                FollowerNodeRole follower = (FollowerNodeRole) role;
                NodeId votedFor = follower.getVotedFor();
                // 以下两种情况可以投票
                // 1. 还未投过票，并且对方日志比自己新
                // 2. 自己已经给对方投过票，重新回复一次
                if ((votedFor == null && voteGranted) || // case 1
                        rpc.getCandidateId().equals(votedFor)) { // case 2
                    becomeFollower(rpc.getTerm(), rpc.getCandidateId(), null, true);
                    return new RequestVoteResult(rpc.getTerm(), true);
                }
                return new RequestVoteResult(role.getTerm(), false);
            }
            case CANDIDATE: // 一票制，candidate 之间不互相投票
                // fall-through
            case LEADER: // 无论如何不可能比 leader 的日志还新
                return new RequestVoteResult(role.getTerm(), false);
            default:
                throw new IllegalStateException("unexpected node role [" + role.getName() + "]");
        }
    }

    private void becomeFollower(int term, @Nullable NodeId votedFor, @Nullable NodeId leaderId, boolean scheduleElectionTimeout) {
        // 首次取消定时任务
        role.cancelTimeoutOrTask();
        if (leaderId != null && !leaderId.equals(role.getLeaderId(context.getSelfId()))) {
            logger.info("current leader is {}, term {}", leaderId, term);
        }
        ElectionTimeout electionTimeout = scheduleElectionTimeout ? scheduleElectionTimeout() : ElectionTimeout.NONE;
        changeToRole(new FollowerNodeRole(term, votedFor, leaderId, electionTimeout));
    }

    @Subscribe
    public void onReceiveRequestVoteResult(RequestVoteResult result) {
        context.getTaskExecutor().submit(() -> doProcessRequestVoteResult(result));
    }

    /**
     * 收到 投票请求 的响应
     */
    private void doProcessRequestVoteResult(RequestVoteResult result) {
        // 对方的 term 比自己的大
        if (result.getTerm() > role.getTerm()) {
            // 退化成 follower
            becomeFollower(result.getTerm(), null, null, true);
            return;
        }

        // 不是 candidate，忽略
        if (role.getName() != RoleName.CANDIDATE) {
            logger.debug("receive request vote result and current role is not candidate, ignore");
            return;
        }

        // 如果对方的 term 比自己小，或者对方未投票，忽略
        if (result.getTerm() < role.getTerm() || !result.isVoteGranted()) {
            return;
        }

        // 当前票数
        int currentVotesCount = ((CandidateNodeRole) role).getVotesCount() + 1;
        // 节点数
        int countOfMajor = context.getGroup().getCount();
        logger.debug("votes count {}, major node count {}", currentVotesCount, countOfMajor);
        // 取消定时任务
        // 因为接下来要么成为 leader，要么递增票数
        role.cancelTimeoutOrTask();
        // 多数赞同
        if (currentVotesCount > countOfMajor / 2) {
            logger.info("become leader, term {}", role.getTerm());
            resetReplicatingStates();
            // raft 算法要求，成为 leader 后必须马上发送心跳消息给其它 follower 节点从而
            // 重置其选举超时，进而使集群的主从关系稳定下来
            changeToRole(new LeaderNodeRole(role.getTerm(), scheduleLogReplicationTask()));
            // 添加一条 no-op log，用来同步 index
            context.getLog().appendEntry(role.getTerm()); // no-op log
        } else {
            changeToRole(new CandidateNodeRole(role.getTerm(), currentVotesCount, scheduleElectionTimeout()));
        }
    }

    private void doReplicateLog() {
        logger.debug("replicate log");
        // 发送 AppendEntries 消息进行日志复制
        for (GroupMember member : context.getGroup().getReplicatingGroupMembers()) {
            doReplicateLog0(member, context.getConfig().getMaxReplicationEntries());
        }
    }

    private void doReplicateLog0(GroupMember member, int maxEntries) {
        AppendEntriesRpc rpc = context.getLog().createAppendEntriesRpc(role.getTerm(), context.getSelfId(), member.getNextIndex(), maxEntries);
        context.getConnector().sendAppendEntries(rpc, member.getEndpoint());
    }

    /**
     * Reset replicating states.
     */
    private void resetReplicatingStates() {
        context.getGroup().resetReplicatingStates(context.getLog().getNextIndex());
    }

    /**
     * 收到来自 leader 的日志复制消息
     */
    @Subscribe
    public void onReceiveAppendEntriesRpc(AppendEntriesRpcMessage rpcMessage) {
       context.getTaskExecutor().submit(() -> {
           // 处理 日志复制 消息
           // 并进行回复
           GroupMember member = context.getGroup().getRequiredGroupMember(rpcMessage.getSourceNodeId());
           context.getConnector().replyAppendEntries(doProcessAppendEntriesRpc(rpcMessage), member.getEndpoint());
       });
    }

    private AppendEntriesResult doProcessAppendEntriesRpc(AppendEntriesRpcMessage rpcMessage) {
        AppendEntriesRpc rpc = rpcMessage.get();
        // 如果对方 term 比自己小，回复自己的 term
        if (rpc.getTerm() < role.getTerm()) {
            // leader 收到回信后退化成 follower
            return new AppendEntriesResult(role.getTerm(), false, rpc.getMessageId());
        }

        // 如果对方 term 比自己大，则退化成 follower
        // 可能是前 leader 或 candidate
        if (rpc.getTerm() > role.getTerm()) {
            becomeFollower(rpc.getTerm(), null, rpc.getLeaderId(), true);
            return new AppendEntriesResult(rpc.getTerm(), appendEntries(rpc), rpc.getMessageId());
        }

        Assert.isTrue(rpc.getTerm() == role.getTerm(), "should equal");

        switch (role.getName()) {
            // follower 需要重置选举定时器
            case FOLLOWER: {
                becomeFollower(rpc.getTerm(), ((FollowerNodeRole) role).getVotedFor(), rpc.getLeaderId(), true);
                // 追加日志
                return new AppendEntriesResult(rpc.getTerm(), appendEntries(rpc), rpc.getMessageId());
            }
            // 同时有多个 candidate，其中一个成为了 leader
            case CANDIDATE: {
                // 退化成 follower，重置选举定时器
                becomeFollower(rpc.getTerm(), null, rpc.getLeaderId(), true);
                // 追加日志
                return new AppendEntriesResult(rpc.getTerm(), appendEntries(rpc), rpc.getMessageId());
            }
            case LEADER: {
                // bug !
                logger.warn("receive append entries rpc from another leader {}, ignore", rpc.getLeaderId());
                return new AppendEntriesResult(rpc.getTerm(), false, rpc.getMessageId());
            }
            default:
                throw new IllegalStateException("unexpected node role [" + role.getName() + "]");
        }
    }

    private boolean appendEntries(AppendEntriesRpc rpc) {
        boolean result = context.getLog().appendEntriesFromLeader(rpc.getPrevLogIndex(), rpc.getPrevLogTerm(), rpc.getEntries());
        // 追加成功时，follower 需要根据 leader 的 commitIndex 决定是否推进本地的 commitIndex
        if (result) {
            context.getLog().advanceCommitIndex(Math.min(rpc.getLeaderCommit(), rpc.getLastEntryIndex()), rpc.getTerm());
        }
        return result;
    }

    @Subscribe
    public void onReceiveAppendEntriesResult(AppendEntriesResultMessage resultMessage) {
        context.getTaskExecutor().submit(() ->
            doProcessAppendEntriesResult(resultMessage)
        );
    }

    private void doProcessAppendEntriesResult(AppendEntriesResultMessage resultMessage) {
        AppendEntriesResult result = resultMessage.get();
        AppendEntriesRpc rpc = resultMessage.getRpc();
        // 如果对方的 term 比自己大，退化成 follower
        if (result.getTerm() > role.getTerm()) {
            // 此时是不确定leader是谁的
            // 收到心跳消息后就知道了
            becomeFollower(result.getTerm(), null, null, true);
            return;
        }

        // 只有 leader 才有资格收到 日志复制 的响应
        if (role.getName() != RoleName.LEADER) {
            logger.warn("receive append entries result from node {} but current node is not leader, ignore", resultMessage.getSourceNodeId());
        }

        NodeId sourceNodeId = resultMessage.getSourceNodeId();
        GroupMember member = context.getGroup().getGroupMember(sourceNodeId);
        // 没有指定的成员
        if (member == null) {
            logger.info("unexpected append entries result from node {}, node maybe removed", sourceNodeId);
            return;
        }
        // 回复成功
        if (result.isSuccess()) {
            // 推进 matchIndex 和 nextIndex
            if (member.advanceReplicatingState(rpc.getLastEntryIndex())) {
                // 过半节点持久化了的化就推进 leader 的 commitIndex
                context.getLog().advanceCommitIndex(context.getGroup().getMatchIndexOfMajor(), role.getTerm());
            }
        }
        // 回复失败
        else {
            // 回退 nextIndex，直到找到和 leader 日志重叠的部分
            if (!member.backOffNextIndex()) {
                logger.warn("cannot back off next index more, node {}", sourceNodeId);
                return;
            }
        }

        // 立即重新同步日志，不用等下一次定时任务触发
        doReplicateLog0(member, context.getConfig().getMaxReplicationEntries());
    }

}
