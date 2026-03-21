package io.kiw.luxis.web.test.handler;

import io.kiw.luxis.web.handler.WebSocketRoute;
import io.kiw.luxis.web.http.ErrorMessageResponse;
import io.kiw.luxis.web.internal.WebSocketPipeline;
import io.kiw.luxis.web.pipeline.WebSocketStream;
import io.kiw.luxis.web.test.MyApplicationState;
import io.kiw.luxis.web.websocket.WebSocketResult;

import java.util.concurrent.CompletableFuture;

public class AsyncFlatMapFailWebSocketHandler extends WebSocketRoute<WebSocketEchoRequest, WebSocketEchoResponse, MyApplicationState> {

    @Override
    public WebSocketPipeline<WebSocketEchoResponse> onMessage(final WebSocketStream<WebSocketEchoRequest, MyApplicationState> stream) {
        return stream
            .<WebSocketEchoResponse>asyncFlatMap(ctx ->
                CompletableFuture.completedFuture(WebSocketResult.error(new ErrorMessageResponse("async flatMap failed"))))
            .complete();
    }
}
