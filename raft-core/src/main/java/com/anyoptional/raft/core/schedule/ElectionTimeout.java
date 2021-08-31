package com.anyoptional.raft.core.schedule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * 选举超时，主要操作有三种
 *  1. 新建
 *  2. 取消，比如 candidate 收到足够的票数成为 leader 时
 *  3. 重置，比如 follower 收到来自 leader 的心跳消息
 */
@Slf4j
@RequiredArgsConstructor
public class ElectionTimeout {

    public static final ElectionTimeout NONE = new ElectionTimeout(new NullScheduledFuture());

    /**
     * 为了减少 split vote 的影响，应该在选举超时区间（比如
     * 3~4s）内随机选择一个超时时间，然后创建一个一次性的 ScheduledFuture
     * 交给 ScheduledExecutorService 进行调度
     */
    private final ScheduledFuture<?> scheduledFuture;

    public void cancel() {
        log.debug("cancel election timeout");
        scheduledFuture.cancel(false);
    }

    @Override
    public String toString() {
        if (scheduledFuture.isCancelled()) {
            return "ElectionTimeout(state=cancelled)";
        }
        if (scheduledFuture.isDone()) {
            return "ElectionTimeout(state=done)";
        }
        return "ElectionTimeout(delay=" + scheduledFuture.getDelay(TimeUnit.MILLISECONDS) + "ms)";
    }

}
