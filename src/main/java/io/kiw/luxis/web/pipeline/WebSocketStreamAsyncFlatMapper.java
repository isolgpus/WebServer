package io.kiw.luxis.web.pipeline;

import io.kiw.luxis.result.Result;
import io.kiw.luxis.web.http.ErrorMessageResponse;
import io.kiw.luxis.web.websocket.WebSocketContext;

import java.util.concurrent.CompletableFuture;

public interface WebSocketStreamAsyncFlatMapper<REQ, RES, APP> {
    CompletableFuture<Result<ErrorMessageResponse, RES>> handle(WebSocketContext<REQ, APP> ctx);
}
