package io.kiw.luxis.web.pipeline;

import io.kiw.luxis.web.http.BlockingRouteContext;

public interface HttpControlStreamBlockingPeeker<REQ> {
    void handle(BlockingRouteContext<REQ> ctx);
}
