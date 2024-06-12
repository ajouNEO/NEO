package com.neo.back.utility;
import java.util.concurrent.*;
import java.time.Instant;

public class TrackableScheduledFuture<V> implements ScheduledFuture<V> {
    private final ScheduledFuture<V> future;
    private final Runnable task;
    private final String taskId;
    private final Instant startTime;
    private final Instant endTime;

    public TrackableScheduledFuture(ScheduledFuture<V> future, Runnable task, String taskId, Instant startTime, Instant endTime) {
        this.future = future;
        this.task = task;
        this.taskId = taskId;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getTaskId() {
        return taskId;
    }

    public Runnable getTask() {
        return task;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public Instant getEndTime() {
        return endTime;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return future.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
        return future.isCancelled();
    }

    @Override
    public boolean isDone() {
        return future.isDone();
    }

    @Override
    public V get() throws InterruptedException, ExecutionException {
        return future.get();
    }

    @Override
    public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return future.get(timeout, unit);
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return future.getDelay(unit);
    }

    @Override
    public int compareTo(Delayed o) {
        return future.compareTo(o);
    }
}
