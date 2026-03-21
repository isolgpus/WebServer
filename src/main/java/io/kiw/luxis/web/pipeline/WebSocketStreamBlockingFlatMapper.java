package io.kiw.luxis.web.pipeline;

import io.kiw.luxis.result.Result;
import io.kiw.luxis.web.http.ErrorMessageResponse;
import io.kiw.luxis.web.websocket.WebSocketBlockingContext;

public interface WebSocketStreamBlockingFlatMapper<REQ, RES> {
    Result<ErrorMessageResponse, RES> handle(WebSocketBlockingContext<REQ> ctx);
}
