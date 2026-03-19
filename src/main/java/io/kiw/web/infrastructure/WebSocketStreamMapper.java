package io.kiw.web.infrastructure;

public interface WebSocketStreamMapper<REQ, RES, APP> {
    RES handle(WebSocketContext<REQ, APP> ctx);
}
