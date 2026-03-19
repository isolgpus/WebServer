package io.kiw.web.test.handler;

import io.kiw.web.infrastructure.WebSocketPipeline;
import io.kiw.web.infrastructure.WebSocketRoute;
import io.kiw.web.infrastructure.WebSocketStream;
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
