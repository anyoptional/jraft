package com.anyoptional.raft.core.support;

import io.netty.util.concurrent.DefaultEventExecutor;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.EventExecutor;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public class SingleThreadTaskExecutor implements TaskExecutor {

    private final EventExecutor executor;

    public SingleThreadTaskExecutor() {
        this("task-executor");
    }

    public SingleThreadTaskExecutor(String poolName) {
        executor = new DefaultEventExecutor(new DefaultThreadFactory(poolName));
    }

    @Override
    public Future<?> submit(Runnable task) {
        return executor.submit(task);
    }

    @Override
    public <V> Future<V> submit(Callable<V> task) {
        return executor.submit(task);
    }

    @Override
    public void shutdown() {
        executor.shutdownGracefully();
    }

}
