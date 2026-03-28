package io.kiw.luxis.web.test.handler;

import io.kiw.luxis.web.handler.WebSocketSplitRoute;
import io.kiw.luxis.web.internal.WebSocketPipeline;
import io.kiw.luxis.web.internal.WebSocketSplitPipeline;
import io.kiw.luxis.web.pipeline.WebSocketSplitStream;
import io.kiw.luxis.web.pipeline.WebSocketStream;
import io.kiw.luxis.web.test.MyApplicationState;

public class SplitWebSocketHandler extends WebSocketSplitRoute<SplitWebSocketMessage, MyApplicationState> {

    private static WebSocketPipeline<?> handleEcho(WebSocketStream<WebSocketEchoRequest, MyApplicationState> stream) {
        return stream.map(ctx -> new WebSocketEchoResponse("echo: " + ctx.in().message))
                .complete();
    }

    private static WebSocketPipeline<?> handleNumber(WebSocketStream<WebSocketNumberRequest, MyApplicationState> stream) {
        return stream.map(ctx -> new WebSocketNumberResponse(ctx.in().value * 2))
                .complete();
    }

    @Override
    public WebSocketSplitPipeline onMessage(final WebSocketSplitStream<SplitWebSocketMessage, MyApplicationState> splitStream) {
        return splitStream
            .on("echo", WebSocketEchoRequest.class, SplitWebSocketHandler::handleEcho)
            .on("number", WebSocketNumberRequest.class, SplitWebSocketHandler::handleNumber)
            .build();
    }
}
