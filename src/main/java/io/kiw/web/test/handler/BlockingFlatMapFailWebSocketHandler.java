package io.kiw.web.test.handler;

import io.kiw.web.http.ErrorMessageResponse;
import io.kiw.web.internal.WebSocketPipeline;
import io.kiw.web.websocket.WebSocketResult;
import io.kiw.web.handler.WebSocketRoute;
import io.kiw.web.pipeline.WebSocketStream;
import io.kiw.web.test.MyApplicationState;

public class BlockingFlatMapFailWebSocketHandler extends WebSocketRoute<WebSocketEchoRequest, WebSocketEchoResponse, MyApplicationState> {

    @Override
    public WebSocketPipeline<WebSocketEchoResponse> onMessage(WebSocketStream<WebSocketEchoRequest, MyApplicationState> stream) {
        return stream
            .<WebSocketEchoResponse>blockingFlatMap(ctx -> WebSocketResult.error(new ErrorMessageResponse("blocking flatMap failed")))
            .complete();
    }
}
