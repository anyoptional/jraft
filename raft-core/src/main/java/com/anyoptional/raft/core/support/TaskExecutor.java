package com.anyoptional.raft.core.support;

import com.google.common.util.concurrent.FutureCallback;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public interface TaskExecutor {

    /**
     * Submit task.
     *
     * @param task task
     * @return future
     */

    Future<?> submit(Runnable task);

    /**
     * Submit callable task.
     *
     * @param task task
     * @param <V>  result type
     * @return future
     */

    <V> Future<V> submit(Callable<V> task);

    /**
     * Submit task with callback.
     *
     * @param task     task
     * @param callback callback
     */
    void submit(Runnable task, FutureCallback<Object> callback);

    /**
     * Submit task with callbacks.
     *
     * @param task      task
     * @param callbacks callbacks, should not be empty
     */
    void submit(Runnable task, Collection<FutureCallback<Object>> callbacks);

    /**
     * Shutdown.
     *
     * @throws InterruptedException if interrupted
     */
    void shutdown() throws InterruptedException;

}
