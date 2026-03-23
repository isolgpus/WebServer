package io.kiw.luxis.web.test.handler;

import io.kiw.luxis.web.handler.WebSocketRoute;
import io.kiw.luxis.web.internal.WebSocketPipeline;
import io.kiw.luxis.web.pipeline.WebSocketStream;
import io.kiw.luxis.web.test.MyApplicationState;
import io.kiw.luxis.web.websocket.WebSocketSession;

public class OnCloseTrackingWebSocketHandler extends WebSocketRoute<WebSocketEchoRequest, WebSocketEchoResponse, MyApplicationState> {

    public volatile boolean onCloseCalled = false;
    public volatile boolean onOpenCalled = false;

    @Override
    public void onOpen(final WebSocketSession<WebSocketEchoResponse> session, final MyApplicationState appState) {
        onOpenCalled = true;
    }

    @Override
    public WebSocketPipeline<WebSocketEchoResponse> onMessage(final WebSocketStream<WebSocketEchoRequest, MyApplicationState> stream) {
        return stream
            .map(ctx -> new WebSocketEchoResponse("echo: " + ctx.in().message))
            .complete();
    }

    @Override
    public void onClose(final WebSocketSession<WebSocketEchoResponse> session, final MyApplicationState appState) {
        onCloseCalled = true;
    }
}
