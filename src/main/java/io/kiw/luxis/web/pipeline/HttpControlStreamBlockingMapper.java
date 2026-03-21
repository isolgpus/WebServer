package io.kiw.luxis.web.pipeline;

import io.kiw.luxis.web.http.BlockingContext;

public interface HttpControlStreamBlockingMapper<REQ, RES> {
    RES handle(BlockingContext<REQ> ctx);
}
