package com.anyoptional.raft.core.schedule;

import com.anyoptional.raft.core.schedule.ElectionTimeout;
import com.anyoptional.raft.core.schedule.LogReplicationTask;

public interface Scheduler {

    /**
     * 创建日志复制定时任务
     */
    LogReplicationTask scheduleLogReplicationTask(Runnable task);

    /**
     * 创建选举超时定时任务
     */
    ElectionTimeout scheduleElectionTimeout(Runnable task);

    /**
     * 停止当前 Scheduler 的调度
     */
    void stop() throws InterruptedException;

}
