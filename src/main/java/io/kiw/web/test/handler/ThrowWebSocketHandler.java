package io.kiw.web.test.handler;

import io.kiw.web.internal.WebSocketPipeline;
import io.kiw.web.websocket.WebSocketResult;
import io.kiw.web.handler.WebSocketRoute;
import io.kiw.web.pipeline.WebSocketStream;
import io.kiw.web.test.MyApplicationState;

import java.util.concurrent.CompletableFuture;

public class ThrowWebSocketHandler extends WebSocketRoute<WebSocketThrowRequest, WebSocketEchoResponse, MyApplicationState> {

    @Override
    public WebSocketPipeline<WebSocketEchoResponse> onMessage(WebSocketStream<WebSocketThrowRequest, MyApplicationState> stream) {
        return stream
            .map(ctx -> ctx.in().where)
            .map(ctx -> {
                if ("map".equals(ctx.in())) {
                    throw new RuntimeException("app error in map");
                }
                return ctx.in();
            })
            .blockingMap(ctx -> {
                if ("blocking".equals(ctx.in())) {
                    throw new RuntimeException("app error in blocking");
                }
                return ctx.in();
            })
            .asyncMap(ctx -> {
                if ("asyncMap".equals(ctx.in())) {
                    throw new RuntimeException("app error in asyncMap");
                }
                return CompletableFuture.completedFuture(ctx.in());
            })
            .asyncBlockingMap(ctx -> {
                if ("asyncBlockingMap".equals(ctx.in())) {
                    throw new RuntimeException("app error in asyncBlockingMap");
                }
                return CompletableFuture.completedFuture(ctx.in());
            })
            .flatMap(ctx -> {
                if ("complete".equals(ctx.in())) {
                    throw new RuntimeException("app error in complete");
                }
                return WebSocketResult.success(new WebSocketEchoResponse("ok"));
            })
            .complete();
    }
}
