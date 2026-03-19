package io.kiw.web.test.handler;

import io.kiw.web.infrastructure.WebSocketPipeline;
import io.kiw.web.infrastructure.WebSocketRoute;
import io.kiw.web.infrastructure.WebSocketStream;
import io.kiw.web.test.MyApplicationState;

public class EchoWebSocketHandler extends WebSocketRoute<WebSocketEchoRequest, WebSocketEchoResponse, MyApplicationState> {

    @Override
    public WebSocketPipeline<WebSocketEchoResponse> onMessage(WebSocketStream<WebSocketEchoRequest, MyApplicationState> stream) {
        return stream
            .map(ctx -> new WebSocketEchoResponse("echo: " + ctx.in().message))
            .complete();
    }
}
