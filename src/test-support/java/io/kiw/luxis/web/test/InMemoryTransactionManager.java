package io.kiw.luxis.web.test;

import io.kiw.luxis.web.TransactionManager;
import io.vertx.core.Future;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class InMemoryTransactionManager implements TransactionManager<Integer> {

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
        return Future.succeededFuture(id);
    }

    @Override
    public Future<Void> commit(final Integer tx) {
        events.add("commit:" + tx);
        if (commitShouldFail) {
            return Future.failedFuture(new RuntimeException("commit failed"));
        }
        return Future.succeededFuture();
    }

    @Override
    public Future<Void> rollback(final Integer tx) {
        events.add("rollback:" + tx);
        if (rollbackShouldFail) {
            return Future.failedFuture(new RuntimeException("rollback failed"));
        }
        return Future.succeededFuture();
    }

    @Override
    public Future<Void> onCommitted(final Integer tx, final Runnable callback) {
        events.add("onCommitted:" + tx);
        callback.run();
        return Future.succeededFuture();
    }
}
