package io.kiw.luxis.web.pipeline;

import io.kiw.luxis.web.internal.CorrelatedRouteContext;

public interface HttpControlStreamCorrelatedAsyncHandler<REQ, APP> {
    void handle(CorrelatedRouteContext<REQ, APP> ctx);
}
