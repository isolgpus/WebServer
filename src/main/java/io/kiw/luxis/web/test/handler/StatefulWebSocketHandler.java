package io.kiw.luxis.web.test.handler;

import io.kiw.luxis.web.handler.WebSocketRoute;
import io.kiw.luxis.web.internal.WebSocketPipeline;
import io.kiw.luxis.web.pipeline.WebSocketStream;
import io.kiw.luxis.web.test.MyApplicationState;
import io.kiw.luxis.web.websocket.WebSocketSession;

public class StatefulWebSocketHandler extends WebSocketRoute<WebSocketEchoRequest, WebSocketEchoResponse, MyApplicationState> {

    @Override
    public void onOpen(final WebSocketSession<WebSocketEchoResponse> session, final MyApplicationState appState) {
        session.send(new WebSocketEchoResponse("connected"));
    }

    @Override
    public WebSocketPipeline<WebSocketEchoResponse> onMessage(final WebSocketStream<WebSocketEchoRequest, MyApplicationState> stream) {
        return stream
            .map(ctx -> {
                return new WebSocketEchoResponse(ctx.in().message);
            })
            .complete();
    }

    @Override
    public void onClose(final WebSocketSession<WebSocketEchoResponse> session, final MyApplicationState appState) {
    }
}
