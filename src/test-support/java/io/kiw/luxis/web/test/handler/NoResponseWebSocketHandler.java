package io.kiw.luxis.web.test.handler;

import io.kiw.luxis.web.handler.WebSocketRoutes;
import io.kiw.luxis.web.internal.WebSocketPipeline;
import io.kiw.luxis.web.pipeline.WebSocketRoutesRegister;
import io.kiw.luxis.web.test.MyApplicationState;

public class NoResponseWebSocketHandler extends WebSocketRoutes<MyApplicationState> {

    public boolean messageReceived = false;

    @Override
    public WebSocketPipeline onMessage(final WebSocketRoutesRegister<MyApplicationState> stream) {
        return stream
            .route("echo", WebSocketEchoRequest.class, s ->
                s.map(ctx -> {
                    messageReceived = true;
                    return ctx.in().message;
                })
                .completeWithNoResponse())
            .build();
    }
}
