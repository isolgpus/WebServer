package io.kiw.luxis.web.pipeline;

import io.kiw.luxis.web.http.BlockingRouteContext;

public interface HttpControlStreamBlockingMapper<REQ, RES> {
    RES handle(BlockingRouteContext<REQ> ctx);
}
