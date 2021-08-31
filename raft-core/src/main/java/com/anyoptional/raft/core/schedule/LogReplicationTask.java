package com.anyoptional.raft.core.schedule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * 日志复制定时任务，按固定时间间隔执行（fixed delay）
 */
@Slf4j
@RequiredArgsConstructor
public class LogReplicationTask {

    public static final LogReplicationTask NONE = new LogReplicationTask(new NullScheduledFuture());

    private final ScheduledFuture<?> scheduledFuture;

    public void cancel() {
        log.debug("cancel log replication task");
        scheduledFuture.cancel(false);
    }

    @Override
    public String toString() {
        if (scheduledFuture.isCancelled()) {
            return "LogReplicationTask(state=cancelled)";
        }
        if (scheduledFuture.isDone()) {
            return "LogReplicationTask(state=done)";
        }
        return "LogReplicationTask(delay=" + scheduledFuture.getDelay(TimeUnit.MILLISECONDS) + "ms)";
    }

}
