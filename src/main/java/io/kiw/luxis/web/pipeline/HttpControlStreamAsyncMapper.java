package io.kiw.luxis.web.pipeline;

import io.kiw.luxis.web.http.client.LuxisAsync;
import io.kiw.luxis.web.internal.RouteContext;

public interface HttpControlStreamAsyncMapper<REQ, RES, APP> {
    LuxisAsync<RES> handle(RouteContext<REQ, APP> ctx);
}
