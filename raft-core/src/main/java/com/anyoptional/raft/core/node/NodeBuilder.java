package com.anyoptional.raft.core.node;

import com.anyoptional.raft.core.node.config.NodeConfig;
import com.anyoptional.raft.core.log.FileLog;
import com.anyoptional.raft.core.log.Log;
import com.anyoptional.raft.core.log.MemoryLog;
import com.anyoptional.raft.core.node.store.FileNodeStore;
import com.anyoptional.raft.core.node.store.MemoryNodeStore;
import com.anyoptional.raft.core.node.store.NodeStore;
import com.anyoptional.raft.core.rpc.Connector;
import com.anyoptional.raft.core.schedule.DefaultScheduler;
import com.anyoptional.raft.core.schedule.Scheduler;
import com.anyoptional.raft.core.support.SingleThreadTaskExecutor;
import com.anyoptional.raft.core.support.TaskExecutor;
import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import io.netty.channel.nio.NioEventLoopGroup;
import org.springframework.lang.Nullable;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.Collection;
import java.util.Collections;

/**
 * Node builder.
 */
public class NodeBuilder {

    /**
     * Group.
     */
    private final NodeGroup group;

    /**
     * Self id.
     */
    private final NodeId selfId;

    /**
     * Event bus, INTERNAL.
     */
    private final EventBus eventBus;

    /**
     * Node configuration.
     */
    private NodeConfig config = new NodeConfig();

    /**
     * Starts as standby or not.
     */
    private boolean standby = false;

    /**
     * Log.
     * If data directory specified, {@link FileLog} will be created.
     * Default to {@link MemoryLog}.
     */
    @Nullable
    private Log log = null;

    /**
     * Store for current term and last node id voted for.
     * If data directory specified, {@link FileNodeStore} will be created.
     * Default to {@link MemoryNodeStore}.
     */
    @Nullable
    private NodeStore store = null;

    /**
     * Scheduler, INTERNAL.
     */
    @Nullable
    private Scheduler scheduler = null;

    /**
     * Connector, component to communicate between nodes, INTERNAL.
     */
    @Nullable
    private Connector connector = null;

    /**
     * Task executor for node, INTERNAL.
     */
    @Nullable
    private TaskExecutor taskExecutor = null;

    /**
     * Task executor for group config change task, INTERNAL.
     */
    @Nullable
    private TaskExecutor groupConfigChangeTaskExecutor = null;

    /**
     * Event loop group for worker.
     * If specified, reuse. otherwise create one.
     */
    @Nullable
    private NioEventLoopGroup workerNioEventLoopGroup = null;

    public NodeBuilder(NodeEndpoint endpoint) {
        this(Collections.singletonList(endpoint), endpoint.getId());
    }

    public NodeBuilder(Collection<NodeEndpoint> endpoints, NodeId selfId) {
        Preconditions.checkNotNull(endpoints);
        Preconditions.checkNotNull(selfId);
        this.group = new NodeGroup(selfId, endpoints);
        this.selfId = selfId;
        this.eventBus = new EventBus(selfId.getValue());
    }

    /**
     * Create.
     *
     * @param selfId self id
     * @param group  group
     */
    @Deprecated
    public NodeBuilder(NodeId selfId, NodeGroup group) {
        Preconditions.checkNotNull(selfId);
        Preconditions.checkNotNull(group);
        this.selfId = selfId;
        this.group = group;
        this.eventBus = new EventBus(selfId.getValue());
    }

    /**
     * Set standby.
     *
     * @param standby standby
     * @return this
     */
    public NodeBuilder setStandby(boolean standby) {
        this.standby = standby;
        return this;
    }

    /**
     * Set configuration.
     *
     * @param config config
     * @return this
     */
    public NodeBuilder setConfig(NodeConfig config) {
        Preconditions.checkNotNull(config);
        this.config = config;
        return this;
    }

    /**
     * Set connector.
     *
     * @param connector connector
     * @return this
     */
    NodeBuilder setConnector(Connector connector) {
        Preconditions.checkNotNull(connector);
        this.connector = connector;
        return this;
    }

    /**
     * Set event loop for worker.
     * If specified, it's caller's responsibility to close worker event loop.
     *
     * @param workerNioEventLoopGroup worker event loop
     * @return this
     */
    public NodeBuilder setWorkerNioEventLoopGroup(NioEventLoopGroup workerNioEventLoopGroup) {
        Preconditions.checkNotNull(workerNioEventLoopGroup);
        this.workerNioEventLoopGroup = workerNioEventLoopGroup;
        return this;
    }

    /**
     * Set scheduler.
     *
     * @param scheduler scheduler
     * @return this
     */
    NodeBuilder setScheduler(Scheduler scheduler) {
        Preconditions.checkNotNull(scheduler);
        this.scheduler = scheduler;
        return this;
    }

    /**
     * Set task executor.
     *
     * @param taskExecutor task executor
     * @return this
     */
    NodeBuilder setTaskExecutor(TaskExecutor taskExecutor) {
        Preconditions.checkNotNull(taskExecutor);
        this.taskExecutor = taskExecutor;
        return this;
    }

    /**
     * Set group config change task executor.
     *
     * @param groupConfigChangeTaskExecutor group config change task executor
     * @return this
     */
    NodeBuilder setGroupConfigChangeTaskExecutor(TaskExecutor groupConfigChangeTaskExecutor) {
        Preconditions.checkNotNull(groupConfigChangeTaskExecutor);
        this.groupConfigChangeTaskExecutor = groupConfigChangeTaskExecutor;
        return this;
    }

    /**
     * Set store.
     *
     * @param store store
     * @return this
     */
    NodeBuilder setStore(NodeStore store) {
        Preconditions.checkNotNull(store);
        this.store = store;
        return this;
    }

    /**
     * Set data directory.
     *
     * @param dataDirPath data directory
     * @return this
     */
    public NodeBuilder setDataDir(@Nullable String dataDirPath) {
        if (dataDirPath == null || dataDirPath.isEmpty()) {
            return this;
        }
        File dataDir = new File(dataDirPath);
        if (!dataDir.isDirectory() || !dataDir.exists()) {
            throw new IllegalArgumentException("[" + dataDirPath + "] not a directory, or not exists");
        }
        log = new FileLog(dataDir);
        store = new FileNodeStore(new File(dataDir, FileNodeStore.FILE_NAME));
        return this;
    }

    /**
     * Build node.
     *
     * @return node
     */
    @Nonnull
    public Node build() {
        return new NodeImpl(buildContext());
    }

    /**
     * Build context for node.
     *
     * @return node context
     */
    @Nonnull
    private NodeContext buildContext() {
        NodeContext context = new NodeContext();
        context.setGroup(group);
//        context.setMode(evaluateMode());
        context.setLog(log != null ? log : new MemoryLog());
        context.setStore(store != null ? store : new MemoryNodeStore());
        context.setSelfId(selfId);
        context.setConfig(config);
        context.setEventBus(eventBus);
        context.setScheduler(scheduler != null ? scheduler : new DefaultScheduler(config));
        context.setConnector(connector);
        context.setTaskExecutor(taskExecutor != null ? taskExecutor : new SingleThreadTaskExecutor("taskExecutor"));
        context.setGroupConfigChangeTaskExecutor(groupConfigChangeTaskExecutor != null ? groupConfigChangeTaskExecutor :
                new SingleThreadTaskExecutor("groupConfigChangeTaskExecutor"));
        return context;
    }

//    /**
//     * Create nio connector.
//     *
//     * @return nio connector
//     */
//    @Nonnull
//    private NioConnector createNioConnector() {
//        int port = group.findSelf().getEndpoint().getPort();
//        if (workerNioEventLoopGroup != null) {
//            return new NioConnector(workerNioEventLoopGroup, selfId, eventBus, port, config.getLogReplicationInterval());
//        }
//        return new NioConnector(new NioEventLoopGroup(config.getNioWorkerThreads()), false,
//                selfId, eventBus, port, config.getLogReplicationInterval());
//    }

//    /**
//     * Evaluate mode.
//     *
//     * @return mode
//     * @see NodeGroup#isStandalone()
//     */
//    @Nonnull
//    private NodeMode evaluateMode() {
//        if (standby) {
//            return NodeMode.STANDBY;
//        }
//        if (group.isStandalone()) {
//            return NodeMode.STANDALONE;
//        }
//        return NodeMode.GROUP_MEMBER;
//    }

}
