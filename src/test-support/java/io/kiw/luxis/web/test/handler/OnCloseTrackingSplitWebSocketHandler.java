package io.kiw.luxis.web.test.handler;

import io.kiw.luxis.web.handler.WebSocketSplitRoute;
import io.kiw.luxis.web.internal.WebSocketSplitPipeline;
import io.kiw.luxis.web.pipeline.WebSocketSplitStream;
import io.kiw.luxis.web.test.MyApplicationState;
import io.kiw.luxis.web.websocket.WebSocketSession;

public class OnCloseTrackingSplitWebSocketHandler extends WebSocketSplitRoute<MyApplicationState> {

    @Override
    public void onOpen(final WebSocketSession<?> session, final MyApplicationState appState) {

    }

    @Override
    public WebSocketSplitPipeline onMessage(final WebSocketSplitStream<MyApplicationState> splitStream) {
        return splitStream
            .on("echo", WebSocketEchoRequest.class, stream ->
                stream.map(ctx -> new WebSocketEchoResponse("echo: " + ctx.in().message))
                      .complete())
            .build();
    }

    @Override
    public void onClose(final WebSocketSession<?> session, final MyApplicationState appState) {

    }
}
