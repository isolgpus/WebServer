package io.kiw.luxis.web.pipeline;

import io.kiw.luxis.result.Result;
import io.kiw.luxis.web.http.ErrorMessageResponse;
import io.kiw.luxis.web.websocket.WebSocketAsyncContext;

import java.util.concurrent.CompletableFuture;

public interface WebSocketStreamAsyncFlatMapper<REQ, RES, APP, RESP> {
    CompletableFuture<Result<ErrorMessageResponse, RES>> handle(WebSocketAsyncContext<REQ, APP, RESP> ctx);
}
