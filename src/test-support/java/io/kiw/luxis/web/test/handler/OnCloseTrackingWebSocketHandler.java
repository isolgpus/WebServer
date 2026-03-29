package io.kiw.luxis.web.test.handler;

import io.kiw.luxis.web.handler.WebSocketRoute;
import io.kiw.luxis.web.internal.WebSocketPipeline;
import io.kiw.luxis.web.pipeline.WebSocketSplitStream;
import io.kiw.luxis.web.test.MyApplicationState;
import io.kiw.luxis.web.websocket.WebSocketSession;

public class OnCloseTrackingWebSocketHandler extends WebSocketRoute<MyApplicationState> {

    public volatile boolean onCloseCalled = false;
    public volatile boolean onOpenCalled = false;

    @Override
    public void onOpen(final WebSocketSession session, final MyApplicationState appState) {
        onOpenCalled = true;
    }

    @Override
    public WebSocketPipeline onMessage(final WebSocketSplitStream<MyApplicationState> stream) {
        return stream
            .on("echo", WebSocketEchoRequest.class, s ->
                s.map(ctx -> new WebSocketEchoResponse("echo: " + ctx.in().message))
                 .complete())
            .build();
    }

    @Override
    public void onClose(final WebSocketSession session, final MyApplicationState appState) {
        onCloseCalled = true;
    }
}
