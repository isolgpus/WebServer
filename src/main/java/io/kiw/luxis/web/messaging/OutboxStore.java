package io.kiw.luxis.web.messaging;

import io.vertx.core.Future;

import java.util.List;

public interface OutboxStore<TX> {

    Future<Void> append(TX tx, List<OutboxEvent> events);

    Future<List<PendingOutboxEvent>> readPending(int limit);

    Future<Void> markSent(long id);

    default int batchSize() {
        return 100;
    }

    default long pollIntervalMillis() {
        return 1000L;
    }

    /**
     * Return false to disable the Luxis-owned drainer. {@link #append} still runs pre-commit,
     * but Luxis will not call {@link #readPending} or {@link #markSent} — you are responsible
     * for reading and dispatching events yourself (e.g. Debezium, a separate worker process).
     */
    default boolean drainerEnabled() {
        return true;
    }
}
