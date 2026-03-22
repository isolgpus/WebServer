package io.kiw.luxis.web.test.handler;

import io.kiw.luxis.web.handler.WebSocketRoute;
import io.kiw.luxis.web.internal.WebSocketPipeline;
import io.kiw.luxis.web.pipeline.WebSocketStream;
import io.kiw.luxis.web.test.MyApplicationState;

public class EchoWebSocketHandler extends WebSocketRoute<WebSocketEchoRequest, WebSocketEchoResponse, MyApplicationState> {

    @Override
    public WebSocketPipeline<WebSocketEchoResponse> onMessage(final WebSocketStream<WebSocketEchoRequest, MyApplicationState> stream) {
        return stream
            .map(ctx -> "echo: " + ctx.in().message)
            .map(ctx -> new WebSocketEchoResponse(ctx.in()))
            .complete();
    }
}
