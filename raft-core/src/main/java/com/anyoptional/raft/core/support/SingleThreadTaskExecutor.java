package com.anyoptional.raft.core.support;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.FutureCallback;
import io.netty.util.concurrent.DefaultEventExecutor;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.EventExecutor;

import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public class SingleThreadTaskExecutor extends AbstractTaskExecutor {

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

    @Override
    public void submit(Runnable task, Collection<FutureCallback<Object>> callbacks) {
        Preconditions.checkNotNull(task);
        Preconditions.checkNotNull(callbacks);
        executor.submit(() -> {
            try {
                task.run();
                callbacks.forEach(c -> c.onSuccess(null));
            } catch (Exception e) {
                callbacks.forEach(c -> c.onFailure(e));
            }
        });
    }

}
