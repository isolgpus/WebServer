package io.kiw.luxis.web.pipeline;

import io.kiw.luxis.web.http.BlockingAsyncRouteContext;
import io.kiw.luxis.web.http.client.LuxisAsync;

public interface HttpControlStreamAsyncBlockingMapper<REQ, RES> {
    LuxisAsync<RES> handle(BlockingAsyncRouteContext<REQ> ctx);
}
