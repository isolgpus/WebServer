package io.kiw.luxis.web.pipeline;

import io.kiw.luxis.result.Result;
import io.kiw.luxis.web.http.ErrorMessageResponse;
import io.kiw.luxis.web.websocket.WebSocketContext;

public interface WebSocketStreamFlatMapper<REQ, RES, APP> {
    Result<ErrorMessageResponse, RES> handle(WebSocketContext<REQ, APP> ctx);
}
