package io.kiw.luxis.web.test.handler;

import io.kiw.luxis.web.handler.WebSocketRoutes;
import io.kiw.luxis.web.http.ErrorMessageResponse;
import io.kiw.luxis.web.internal.WebSocketPipeline;
import io.kiw.luxis.web.pipeline.WebSocketRoutesRegister;
import io.kiw.luxis.web.test.MyApplicationState;
import io.kiw.luxis.web.websocket.WebSocketResult;

public class BlockingFlatMapFailWebSocketHandler extends WebSocketRoutes<MyApplicationState> {

    @Override
    public WebSocketPipeline onMessage(final WebSocketRoutesRegister<MyApplicationState> stream) {
        return stream
            .route("echo", WebSocketEchoRequest.class, s ->
                s.<WebSocketEchoResponse>blockingFlatMap(ctx -> WebSocketResult.error(new ErrorMessageResponse("blocking flatMap failed")))
                 .complete())
            .build();
    }
}
