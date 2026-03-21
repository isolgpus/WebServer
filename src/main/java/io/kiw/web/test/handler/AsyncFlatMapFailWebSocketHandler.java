package io.kiw.web.test.handler;

import io.kiw.web.http.ErrorMessageResponse;
import io.kiw.web.internal.WebSocketPipeline;
import io.kiw.web.websocket.WebSocketResult;
import io.kiw.web.handler.WebSocketRoute;
import io.kiw.web.pipeline.WebSocketStream;
import io.kiw.web.test.MyApplicationState;

import java.util.concurrent.CompletableFuture;

public class AsyncFlatMapFailWebSocketHandler extends WebSocketRoute<WebSocketEchoRequest, WebSocketEchoResponse, MyApplicationState> {

    @Override
    public WebSocketPipeline<WebSocketEchoResponse> onMessage(WebSocketStream<WebSocketEchoRequest, MyApplicationState> stream) {
        return stream
            .<WebSocketEchoResponse>asyncFlatMap(ctx ->
                CompletableFuture.completedFuture(WebSocketResult.error(new ErrorMessageResponse("async flatMap failed"))))
            .complete();
    }
}
