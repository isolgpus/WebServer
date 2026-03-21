package io.kiw.luxis.web.pipeline;

import io.kiw.luxis.web.internal.RouteContext;

public interface HttpControlStreamMapper<REQ, RES, APP> {
    RES handle(RouteContext<REQ, APP> ctx);
}
