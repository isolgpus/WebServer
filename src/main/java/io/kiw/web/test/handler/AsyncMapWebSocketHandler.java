package io.kiw.web.test.handler;

import io.kiw.web.infrastructure.WebSocketPipeline;
import io.kiw.web.infrastructure.WebSocketRoute;
import io.kiw.web.infrastructure.WebSocketStream;
import io.kiw.web.test.MyApplicationState;

import java.util.concurrent.CompletableFuture;

public class AsyncMapWebSocketHandler extends WebSocketRoute<WebSocketNumberRequest, WebSocketNumberResponse, MyApplicationState> {

    @Override
    public WebSocketPipeline<WebSocketNumberResponse> onMessage(WebSocketStream<WebSocketNumberRequest, MyApplicationState> stream) {
        return stream
            .asyncMap(ctx -> CompletableFuture.supplyAsync(() -> ctx.in().value * 10))
            .map(ctx -> new WebSocketNumberResponse(ctx.in()))
            .complete();
    }
}
