package io.kiw.web.test.handler;

import io.kiw.web.internal.WebSocketPipeline;
import io.kiw.web.handler.WebSocketRoute;
import io.kiw.web.websocket.WebSocketSession;
import io.kiw.web.pipeline.WebSocketStream;
import io.kiw.web.test.MyApplicationState;

public class StatefulWebSocketHandler extends WebSocketRoute<WebSocketEchoRequest, WebSocketEchoResponse, MyApplicationState> {

    @Override
    public void onOpen(WebSocketSession<WebSocketEchoResponse> session, MyApplicationState appState) {
        session.send(new WebSocketEchoResponse("connected"));
    }

    @Override
    public WebSocketPipeline<WebSocketEchoResponse> onMessage(WebSocketStream<WebSocketEchoRequest, MyApplicationState> stream) {
        return stream
            .map(ctx -> {
                return new WebSocketEchoResponse(ctx.in().message);
            })
            .complete();
    }

    @Override
    public void onClose(WebSocketSession<WebSocketEchoResponse> session, MyApplicationState appState) {
    }
}
