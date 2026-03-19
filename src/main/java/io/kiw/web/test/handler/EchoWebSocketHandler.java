package io.kiw.web.test.handler;

import io.kiw.web.infrastructure.*;
import io.kiw.web.test.MyApplicationState;

public class EchoWebSocketHandler extends WebSocketRoute<WebSocketEchoRequest, WebSocketEchoResponse, MyApplicationState> {

    @Override
    public WebSocketPipeline<WebSocketEchoResponse> onMessage(WebSocketStream<WebSocketEchoRequest, MyApplicationState> stream) {
        return stream
            .map(ctx -> "echo: " + ctx.in().message)
            .map(ctx -> new WebSocketEchoResponse(ctx.in()))
            .complete();
    }
}
