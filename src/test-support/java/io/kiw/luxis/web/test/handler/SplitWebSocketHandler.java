package io.kiw.luxis.web.test.handler;

import io.kiw.luxis.web.handler.WebSocketRoute;
import io.kiw.luxis.web.internal.IndividualMessageWebSocketPipeline;
import io.kiw.luxis.web.internal.WebSocketPipeline;
import io.kiw.luxis.web.pipeline.WebSocketSplitStream;
import io.kiw.luxis.web.pipeline.WebSocketStream;
import io.kiw.luxis.web.test.MyApplicationState;

public class SplitWebSocketHandler extends WebSocketRoute<MyApplicationState> {

    private static IndividualMessageWebSocketPipeline<WebSocketEchoResponse> handleEcho(WebSocketStream<WebSocketEchoRequest, MyApplicationState> stream) {
        return stream.map(ctx -> new WebSocketEchoResponse("echo: " + ctx.in().message))
                .complete();
    }

    private static IndividualMessageWebSocketPipeline<WebSocketNumberResponse> handleNumber(WebSocketStream<WebSocketNumberRequest, MyApplicationState> stream) {
        return stream.map(ctx -> new WebSocketNumberResponse(ctx.in().value * 2))
                .complete();
    }

    @Override
    public WebSocketPipeline onMessage(final WebSocketSplitStream<MyApplicationState> splitStream) {
        return splitStream
            .on("echo", WebSocketEchoRequest.class, SplitWebSocketHandler::handleEcho)
            .on("number", WebSocketNumberRequest.class, SplitWebSocketHandler::handleNumber)
            .build();
    }
}
