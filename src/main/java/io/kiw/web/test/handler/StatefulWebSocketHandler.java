package io.kiw.web.test.handler;

import io.kiw.web.infrastructure.WebSocketPipeline;
import io.kiw.web.infrastructure.WebSocketRoute;
import io.kiw.web.infrastructure.WebSocketSession;
import io.kiw.web.infrastructure.WebSocketStream;
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
