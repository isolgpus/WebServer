package io.kiw.luxis.web.test.handler;

import io.kiw.luxis.web.handler.WebSocketRoute;
import io.kiw.luxis.web.internal.WebSocketPipeline;
import io.kiw.luxis.web.pipeline.WebSocketSplitStream;
import io.kiw.luxis.web.test.MyApplicationState;

public class NoResponseWebSocketHandler extends WebSocketRoute<WebSocketEchoRequest, MyApplicationState> {

    public boolean messageReceived = false;

    @Override
    public WebSocketPipeline onMessage(final WebSocketSplitStream<WebSocketEchoRequest, MyApplicationState> stream) {
        return stream
            .on("echo", WebSocketEchoRequest.class, s ->
                s.map(ctx -> {
                    messageReceived = true;
                    return ctx.in().message;
                })
                .completeWithNoResponse())
            .build();
    }
}
