package io.kiw.luxis.web.messaging;

import io.vertx.core.Future;

import java.nio.ByteBuffer;

public interface Publisher {

    Future<Void> publish(String key, String message);

    Future<Void> publish(String key, byte[] message);

    Future<Void> publish(String key, ByteBuffer message);
}
