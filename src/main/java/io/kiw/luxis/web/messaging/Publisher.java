package io.kiw.luxis.web.messaging;

import io.vertx.core.Future;

import java.util.List;

public interface Publisher {

    /**
     * Publish a batch of events. Treated as all-or-nothing: if the returned future fails,
     * Luxis assumes none of the events were delivered and the whole batch is retried on the
     * next drain pass. Implementations should fail the future for any partial failure.
     */
    Future<Void> publish(List<OutboxEvent> events);
}
