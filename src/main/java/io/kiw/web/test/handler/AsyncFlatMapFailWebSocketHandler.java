package io.kiw.web.test.handler;

import io.kiw.web.infrastructure.ErrorMessageResponse;
import io.kiw.web.infrastructure.WebSocketPipeline;
import io.kiw.web.infrastructure.WebSocketResult;
import io.kiw.web.infrastructure.WebSocketRoute;
import io.kiw.web.infrastructure.WebSocketStream;
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
