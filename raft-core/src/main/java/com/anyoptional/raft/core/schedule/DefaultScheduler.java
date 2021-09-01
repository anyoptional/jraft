package com.anyoptional.raft.core.schedule;

import com.anyoptional.raft.core.node.config.NodeConfig;
import com.google.common.base.Preconditions;
import io.netty.util.concurrent.DefaultEventExecutor;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.EventExecutor;
import lombok.extern.slf4j.Slf4j;

import java.util.Random;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
public class DefaultScheduler implements Scheduler {

    /**
     * 最小选举超时时间
     */
    private final int minElectionTimeout;

    /**
     * 最大选举超时时间
     */
    private final int maxElectionTimeout;

    /**
     * 随机数生成器，用于产生选举超时时间
     */
    private final Random electionTimeoutRandom = new Random();

    /**
     * 初次进行日志复制的延迟时间
     */
    private final int logReplicationDelay;

    /**
     * 日志复制时间间隔
     */
    private final int logReplicationInterval;

    /**
     * 用于调度定时任务的执行器
     */
    private final EventExecutor executor = new DefaultEventExecutor(new DefaultThreadFactory("scheduler"));

    public DefaultScheduler(NodeConfig config) {
        this(config.getMinElectionTimeout(), config.getMaxElectionTimeout(), config.getLogReplicationDelay(),
                config.getLogReplicationInterval());
    }

    public DefaultScheduler(int minElectionTimeout, int maxElectionTimeout,
                            int logReplicationDelay, int logReplicationInterval) {
        Preconditions.checkArgument(minElectionTimeout >= 0 && maxElectionTimeout >= 0);
        Preconditions.checkArgument(minElectionTimeout <= maxElectionTimeout);
        Preconditions.checkArgument(logReplicationDelay >= 0 && logReplicationInterval >= 0);
        this.minElectionTimeout = minElectionTimeout;
        this.maxElectionTimeout = maxElectionTimeout;
        this.logReplicationDelay = logReplicationDelay;
        this.logReplicationInterval = logReplicationInterval;
    }

    @Override
    public LogReplicationTask scheduleLogReplicationTask(Runnable task) {
        log.debug("schedule log replication task");
        ScheduledFuture<?> scheduledFuture = executor.scheduleWithFixedDelay(task, logReplicationDelay, logReplicationInterval, TimeUnit.MILLISECONDS);
        return new LogReplicationTask(scheduledFuture);
    }

    @Override
    public ElectionTimeout scheduleElectionTimeout(Runnable task) {
        log.debug("schedule election timeout");
        int delay = electionTimeoutRandom.nextInt(maxElectionTimeout - minElectionTimeout) + minElectionTimeout;
        ScheduledFuture<?> scheduledFuture = executor.schedule(task, delay, TimeUnit.MILLISECONDS);
        return new ElectionTimeout(scheduledFuture);
    }

    @Override
    public void stop() throws InterruptedException {
        log.debug("stop scheduler");
        executor.shutdownGracefully();
    }

}
