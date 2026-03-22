package io.kiw.luxis.web.test.handler;

import io.kiw.luxis.web.handler.WebSocketRoute;
import io.kiw.luxis.web.internal.WebSocketPipeline;
import io.kiw.luxis.web.pipeline.WebSocketStream;
import io.kiw.luxis.web.test.MyApplicationState;

public class NoResponseWebSocketHandler extends WebSocketRoute<WebSocketEchoRequest, Void, MyApplicationState> {

    public boolean messageReceived = false;

    @Override
    public WebSocketPipeline<Void> onMessage(final WebSocketStream<WebSocketEchoRequest, MyApplicationState> stream) {
        return stream
            .map(ctx -> {
                messageReceived = true;
                return ctx.in().message;
            })
            .completeWithNoResponse();
    }
}
