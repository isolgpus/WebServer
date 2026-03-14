package io.kiw.web.test.handler;

import io.kiw.web.infrastructure.WebSocketRoute;
import io.kiw.web.infrastructure.WebSocketSession;
import io.kiw.web.test.MyApplicationState;

public class EchoWebSocketHandler extends WebSocketRoute<WebSocketEchoRequest, WebSocketEchoResponse, MyApplicationState> {

    @Override
    public void onMessage(WebSocketEchoRequest message, WebSocketSession<WebSocketEchoResponse> session, MyApplicationState appState) {
        session.send(new WebSocketEchoResponse("echo: " + message.message));
    }
}
