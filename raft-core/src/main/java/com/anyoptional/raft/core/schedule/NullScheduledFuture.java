package com.anyoptional.raft.core.schedule;

import org.springframework.lang.Nullable;

import java.util.concurrent.*;

public class NullScheduledFuture implements ScheduledFuture<Object> {

    @Override
    public long getDelay(TimeUnit unit) {
        return 0;
    }

    @Override
    public int compareTo(Delayed o) {
        return 0;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return false;
    }

    @Nullable
    @Override
    public Object get() throws InterruptedException, ExecutionException {
        return null;
    }

    @Nullable
    @Override
    public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return null;
    }

}
