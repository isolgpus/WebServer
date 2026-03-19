package io.kiw.web.infrastructure;

import io.kiw.result.Result;

public interface WebSocketStreamFlatMapper<REQ, RES, APP> {
    Result<ErrorMessageResponse, RES> handle(WebSocketContext<REQ, APP> ctx);
}
