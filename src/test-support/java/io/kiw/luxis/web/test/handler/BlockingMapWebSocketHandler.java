package io.kiw.luxis.web.test.handler;

import io.kiw.luxis.web.handler.WebSocketRoute;
import io.kiw.luxis.web.internal.WebSocketPipeline;
import io.kiw.luxis.web.pipeline.WebSocketSplitStream;
import io.kiw.luxis.web.test.MyApplicationState;

public class BlockingMapWebSocketHandler extends WebSocketRoute<WebSocketNumberRequest, MyApplicationState> {

    @Override
    public WebSocketPipeline onMessage(final WebSocketSplitStream<WebSocketNumberRequest, MyApplicationState> stream) {
        return stream
            .on("number", WebSocketNumberRequest.class, s ->
                s.map(ctx -> ctx.in().value)
                 .blockingMap(ctx -> ctx.in() * 2)
                 .map(ctx -> new WebSocketNumberResponse(ctx.in()))
                 .complete())
            .build();
    }
}
