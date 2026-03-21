package io.kiw.web.test.handler;

import io.kiw.web.internal.WebSocketPipeline;
import io.kiw.web.handler.WebSocketRoute;
import io.kiw.web.pipeline.WebSocketStream;
import io.kiw.web.test.MyApplicationState;

public class BlockingMapWebSocketHandler extends WebSocketRoute<WebSocketNumberRequest, WebSocketNumberResponse, MyApplicationState> {

    @Override
    public WebSocketPipeline<WebSocketNumberResponse> onMessage(WebSocketStream<WebSocketNumberRequest, MyApplicationState> stream) {
        return stream
            .map(ctx -> ctx.in().value)
            .blockingMap(ctx -> ctx.in() * 2)
            .map(ctx -> new WebSocketNumberResponse(ctx.in()))
            .complete();
    }
}
