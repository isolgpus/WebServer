package io.kiw.luxis.web.pipeline;

import io.kiw.luxis.web.http.CorrelatedBlockingContext;

public interface HttpControlStreamCorrelatedAsyncBlockingHandler<REQ> {
    void handle(CorrelatedBlockingContext<REQ> ctx);
}
