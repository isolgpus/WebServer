package io.kiw.luxis.web;

import io.vertx.core.Future;

public interface TransactionManager<TX> {

    Future<TX> begin();

    Future<Void> commit(TX tx);

    Future<Void> rollback(TX tx);

    default Future<Void> onCommitted(final TX tx, final Runnable callback) {
        callback.run();
        return Future.succeededFuture();
    }
}
