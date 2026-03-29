package io.kiw.luxis.web.test.handler;

import io.kiw.luxis.web.handler.WebSocketRoutes;
import io.kiw.luxis.web.internal.WebSocketPipeline;
import io.kiw.luxis.web.pipeline.WebSocketRoutesRegister;
import io.kiw.luxis.web.test.MyApplicationState;

public class EchoWebSocketHandler extends WebSocketRoutes<MyApplicationState> {

    @Override
    public WebSocketPipeline onMessage(final WebSocketRoutesRegister<MyApplicationState> stream) {
        return stream
            .route("echo", WebSocketEchoRequest.class, s ->
                s.map(ctx -> "echo: " + ctx.in().message)
                 .map(ctx -> new WebSocketEchoResponse(ctx.in()))
                 .complete())
            .build();
    }
}
