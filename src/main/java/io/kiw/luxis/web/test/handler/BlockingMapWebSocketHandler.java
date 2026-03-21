package io.kiw.luxis.web.test.handler;

import io.kiw.luxis.web.internal.WebSocketPipeline;
import io.kiw.luxis.web.handler.WebSocketRoute;
import io.kiw.luxis.web.pipeline.WebSocketStream;
import io.kiw.luxis.web.test.MyApplicationState;

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
