package io.kiw.luxis.web.test;

import io.kiw.luxis.web.TransactionManager;
import io.vertx.core.Future;
import io.vertx.core.Promise;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class InMemoryTransactionManager implements TransactionManager<Integer> {

    private static final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor(runnable -> {
        final Thread thread = new Thread(runnable, "luxis-tm-test-delay");
        thread.setDaemon(true);
        return thread;
    });

    private static final long DELAY_MS = 50L;

    private final AtomicInteger nextTxId = new AtomicInteger(1);
    private final List<String> events = Collections.synchronizedList(new ArrayList<>());

    private boolean commitShouldFail;
    private boolean rollbackShouldFail;

    public InMemoryTransactionManager failCommits() {
        this.commitShouldFail = true;
        return this;
    }

    public InMemoryTransactionManager failRollbacks() {
        this.rollbackShouldFail = true;
        return this;
    }

    public List<String> events() {
        synchronized (events) {
            return new ArrayList<>(events);
        }
    }

    @Override
    public Future<Integer> begin() {
        final int id = nextTxId.getAndIncrement();
        events.add("begin:" + id);
        return scheduleSuccess(id);
    }

    @Override
    public Future<Void> commit(final Integer tx) {
        events.add("commit:" + tx);
        if (commitShouldFail) {
            return scheduleFailure(new RuntimeException("commit failed"));
        }
        return scheduleSuccess(null);
    }

    @Override
    public Future<Void> rollback(final Integer tx) {
        events.add("rollback:" + tx);
        if (rollbackShouldFail) {
            return scheduleFailure(new RuntimeException("rollback failed"));
        }
        return scheduleSuccess(null);
    }

    @Override
    public Future<Void> onCommitted(final Integer tx, final Runnable callback) {
        events.add("onCommitted:" + tx);
        final Promise<Void> promise = Promise.promise();
        SCHEDULER.schedule(() -> {
            try {
                callback.run();
                promise.complete();
            } catch (final Throwable t) {
                promise.fail(t);
            }
        }, DELAY_MS, TimeUnit.MILLISECONDS);
        return promise.future();
    }

    private static <T> Future<T> scheduleSuccess(final T value) {
        final Promise<T> promise = Promise.promise();
        SCHEDULER.schedule(() -> promise.complete(value), DELAY_MS, TimeUnit.MILLISECONDS);
        return promise.future();
    }

    private static <T> Future<T> scheduleFailure(final Throwable cause) {
        final Promise<T> promise = Promise.promise();
        SCHEDULER.schedule(() -> promise.fail(cause), DELAY_MS, TimeUnit.MILLISECONDS);
        return promise.future();
    }
}
