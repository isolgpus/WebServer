package io.kiw.luxis.web.test;

import io.kiw.luxis.web.messaging.Publisher;
import io.vertx.core.Future;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InMemoryPublisher implements Publisher {

    private final List<String> events = Collections.synchronizedList(new ArrayList<>());
    private boolean shouldFail;

    public InMemoryPublisher failPublishes() {
        this.shouldFail = true;
        return this;
    }

    public List<String> events() {
        synchronized (events) {
            return new ArrayList<>(events);
        }
    }

    @Override
    public Future<Void> publish(final String key, final String message) {
        if (shouldFail) {
            return Future.failedFuture(new RuntimeException("publish failed"));
        }
        events.add("publish:str:" + key + ":" + message);
        return Future.succeededFuture();
    }

    @Override
    public Future<Void> publish(final String key, final byte[] message) {
        if (shouldFail) {
            return Future.failedFuture(new RuntimeException("publish failed"));
        }
        events.add("publish:bytes:" + key + ":" + new String(message, StandardCharsets.UTF_8));
        return Future.succeededFuture();
    }

    @Override
    public Future<Void> publish(final String key, final ByteBuffer message) {
        if (shouldFail) {
            return Future.failedFuture(new RuntimeException("publish failed"));
        }
        final byte[] bytes = new byte[message.remaining()];
        message.duplicate().get(bytes);
        events.add("publish:buf:" + key + ":" + new String(bytes, StandardCharsets.UTF_8));
        return Future.succeededFuture();
    }
}
