package io.kiw.luxis.web.test.handler;

import io.kiw.luxis.web.handler.WebSocketSplitRoute;
import io.kiw.luxis.web.internal.WebSocketSplitPipeline;
import io.kiw.luxis.web.pipeline.WebSocketSplitStream;
import io.kiw.luxis.web.test.MyApplicationState;

public class SplitWebSocketHandler extends WebSocketSplitRoute<MyApplicationState> {

    @Override
    public WebSocketSplitPipeline onMessage(final WebSocketSplitStream<MyApplicationState> splitStream) {
        return splitStream
            .on("echo", WebSocketEchoRequest.class, stream ->
                stream.map(ctx -> new WebSocketEchoResponse("echo: " + ctx.in().message))
                      .complete())
            .on("number", WebSocketNumberRequest.class, stream ->
                stream.map(ctx -> new WebSocketNumberResponse(ctx.in().value * 2))
                      .complete())
            .build();
    }
}
