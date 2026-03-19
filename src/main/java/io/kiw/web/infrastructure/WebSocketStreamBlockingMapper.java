package io.kiw.web.infrastructure;

public interface WebSocketStreamBlockingMapper<REQ, RES> {
    RES handle(WebSocketBlockingContext<REQ> ctx);
}
