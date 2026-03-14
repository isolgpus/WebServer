package io.kiw.web.infrastructure;

public interface HttpControlStreamMapper<REQ, RES, APP> {
    RES handle(RouteContext<REQ, APP> ctx);
}
