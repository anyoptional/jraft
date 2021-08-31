package com.anyoptional.raft.core.election.scheduler;

import lombok.RequiredArgsConstructor;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * 日志复制定时任务，按固定时间间隔执行（fixed delay）
 */
@RequiredArgsConstructor
public class LogReplicationTask {


    private final ScheduledFuture<?> scheduledFuture;

    public void cancel() {
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
