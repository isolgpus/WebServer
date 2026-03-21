package io.kiw.luxis.web.test.handler;

import io.kiw.luxis.web.handler.WebSocketRoute;
import io.kiw.luxis.web.internal.WebSocketPipeline;
import io.kiw.luxis.web.pipeline.WebSocketStream;
import io.kiw.luxis.web.test.MyApplicationState;

import java.util.concurrent.CompletableFuture;

public class AsyncBlockingMapWebSocketHandler extends WebSocketRoute<WebSocketNumberRequest, WebSocketNumberResponse, MyApplicationState> {

    @Override
    public WebSocketPipeline<WebSocketNumberResponse> onMessage(WebSocketStream<WebSocketNumberRequest, MyApplicationState> stream) {
        return stream
            .asyncBlockingMap(ctx -> CompletableFuture.supplyAsync(() -> ctx.in().value * 20))
            .map(ctx -> new WebSocketNumberResponse(ctx.in()))
            .complete();
    }
}
