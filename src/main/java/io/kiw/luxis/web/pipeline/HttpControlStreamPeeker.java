package io.kiw.luxis.web.pipeline;

import io.kiw.luxis.web.internal.RouteContext;

public interface HttpControlStreamPeeker<REQ, APP> {
    void handle(RouteContext<REQ, APP> ctx);
}
