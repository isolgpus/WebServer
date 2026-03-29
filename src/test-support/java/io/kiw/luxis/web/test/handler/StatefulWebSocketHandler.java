package io.kiw.luxis.web.test.handler;

import io.kiw.luxis.web.handler.WebSocketRoutes;
import io.kiw.luxis.web.internal.WebSocketPipeline;
import io.kiw.luxis.web.pipeline.WebSocketRoutesRegister;
import io.kiw.luxis.web.test.MyApplicationState;
import io.kiw.luxis.web.websocket.WebSocketSession;

public class StatefulWebSocketHandler extends WebSocketRoutes<MyApplicationState> {

    @Override
    public void onOpen(final WebSocketSession session, final MyApplicationState appState) {
        session.send(new WebSocketEchoResponse("connected"));
    }

    @Override
    public WebSocketPipeline onMessage(final WebSocketRoutesRegister<MyApplicationState> stream) {
        return stream
            .route("echo", WebSocketEchoRequest.class, s ->
                s.map(ctx -> new WebSocketEchoResponse(ctx.in().message))
                 .complete())
            .build();
    }

    @Override
    public void onClose(final WebSocketSession session, final MyApplicationState appState) {
    }
}
