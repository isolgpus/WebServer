package io.kiw.luxis.web.internal;

import io.kiw.luxis.web.messaging.OutboxEvent;
import io.kiw.luxis.web.messaging.OutboxStore;
import io.kiw.luxis.web.messaging.PendingOutboxEvent;
import io.kiw.luxis.web.messaging.Publisher;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public final class OutboxDrainer {

    private final Vertx vertx;
    private final Publisher publisher;
    private final OutboxStore<?> store;
    private final Consumer<Throwable> exceptionHandler;
    private final AtomicBoolean draining = new AtomicBoolean(false);
    private long timerId = -1;

    public OutboxDrainer(final Vertx vertx, final Publisher publisher, final OutboxStore<?> store, final Consumer<Throwable> exceptionHandler) {
        this.vertx = vertx;
        this.publisher = publisher;
        this.store = store;
        this.exceptionHandler = exceptionHandler != null ? exceptionHandler : err -> {};
    }

    public void start() {
        if (vertx == null || publisher == null || store == null || !store.drainerEnabled()) {
            return;
        }
        timerId = vertx.setPeriodic(store.pollIntervalMillis(), id -> kick());
    }

    public void stop() {
        if (vertx != null && timerId >= 0) {
            vertx.cancelTimer(timerId);
            timerId = -1;
        }
    }

    public void kick() {
        if (publisher == null || store == null || !store.drainerEnabled()) {
            return;
        }
        if (!draining.compareAndSet(false, true)) {
            return;
        }
        drainPass();
    }

    private void drainPass() {
        final int batch = store.batchSize();
        store.readPending(batch).onComplete(ar -> {
            if (ar.failed()) {
                exceptionHandler.accept(ar.cause());
                draining.set(false);
                return;
            }
            final List<PendingOutboxEvent> pending = ar.result();
            if (pending == null || pending.isEmpty()) {
                draining.set(false);
                return;
            }
            dispatch(pending, 0, () -> {
                draining.set(false);
                if (pending.size() >= batch) {
                    kick();
                }
            });
        });
    }

    private void dispatch(final List<PendingOutboxEvent> pending, final int idx, final Runnable done) {
        if (idx >= pending.size()) {
            done.run();
            return;
        }
        final PendingOutboxEvent pe = pending.get(idx);
        final Future<Void> sent;
        try {
            sent = publishOne(pe.event());
        } catch (final Exception e) {
            exceptionHandler.accept(e);
            dispatch(pending, idx + 1, done);
            return;
        }
        sent.onComplete(ar -> {
            if (ar.succeeded()) {
                store.markSent(pe.id()).onComplete(mark -> {
                    if (mark.failed()) {
                        exceptionHandler.accept(mark.cause());
                    }
                    dispatch(pending, idx + 1, done);
                });
            } else {
                exceptionHandler.accept(ar.cause());
                dispatch(pending, idx + 1, done);
            }
        });
    }

    private Future<Void> publishOne(final OutboxEvent event) {
        return switch (event.payload()) {
            case OutboxEvent.Payload.Str s -> publisher.publish(event.key(), s.value());
            case OutboxEvent.Payload.Bytes b -> publisher.publish(event.key(), b.value());
            case OutboxEvent.Payload.Buf b -> publisher.publish(event.key(), b.value());
        };
    }
}
