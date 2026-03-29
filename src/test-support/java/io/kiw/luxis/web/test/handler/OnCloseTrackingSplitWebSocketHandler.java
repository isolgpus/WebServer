package io.kiw.luxis.web.test.handler;

import io.kiw.luxis.web.handler.WebSocketRoute;
import io.kiw.luxis.web.internal.WebSocketPipeline;
import io.kiw.luxis.web.pipeline.WebSocketSplitStream;
import io.kiw.luxis.web.test.MyApplicationState;
import io.kiw.luxis.web.websocket.WebSocketSession;

public class OnCloseTrackingSplitWebSocketHandler extends WebSocketRoute<SplitWebSocketMessage, MyApplicationState> {

    @Override
    public void onOpen(final WebSocketSession session, final MyApplicationState appState) {

    }

    @Override
    public WebSocketPipeline onMessage(final WebSocketSplitStream<SplitWebSocketMessage, MyApplicationState> splitStream) {
        return splitStream
            .on("echo", WebSocketEchoRequest.class, stream ->
                stream.map(ctx -> new WebSocketEchoResponse("echo: " + ctx.in().message))
                      .complete())
            .build();
    }

    @Override
    public void onClose(final WebSocketSession session, final MyApplicationState appState) {

    }
}
