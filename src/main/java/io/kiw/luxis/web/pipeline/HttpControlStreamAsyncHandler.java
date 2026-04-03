package io.kiw.luxis.web.pipeline;

import io.kiw.luxis.web.internal.AsyncRouteContext;

public interface HttpControlStreamAsyncHandler<REQ, APP> {
    void handle(AsyncRouteContext<REQ, APP> ctx);
}
