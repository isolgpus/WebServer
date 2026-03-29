package io.kiw.luxis.web.pipeline;

import io.kiw.luxis.web.http.AsyncBlockingContext;

public interface HttpControlStreamAsyncBlockingHandler<REQ> {
    void handle(AsyncBlockingContext<REQ> ctx);
}
