package io.kiw.web.test.handler;

import io.kiw.web.infrastructure.WebSocketRoute;
import io.kiw.web.infrastructure.WebSocketSession;
import io.kiw.web.test.MyApplicationState;

public class StatefulWebSocketHandler extends WebSocketRoute<WebSocketEchoRequest, WebSocketEchoResponse, MyApplicationState> {

    @Override
    public void onOpen(WebSocketSession<WebSocketEchoResponse> session, MyApplicationState appState) {
        session.send(new WebSocketEchoResponse("connected"));
    }

    @Override
    public void onMessage(WebSocketEchoRequest message, WebSocketSession<WebSocketEchoResponse> session, MyApplicationState appState) {
        String pathRoom = session.pathParam("room");
        String queryUser = session.queryParam("user");
        String prefix = (pathRoom != null ? pathRoom : "") + (queryUser != null ? "/" + queryUser : "");
        session.send(new WebSocketEchoResponse(prefix + ": " + message.message));
    }

    @Override
    public void onClose(WebSocketSession<WebSocketEchoResponse> session, MyApplicationState appState) {
    }
}
