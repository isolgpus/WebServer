package io.kiw.web.infrastructure;

import io.kiw.result.Result;

public interface WebSocketStreamBlockingFlatMapper<REQ, RES> {
    Result<ErrorMessageResponse, RES> handle(WebSocketBlockingContext<REQ> ctx);
}
