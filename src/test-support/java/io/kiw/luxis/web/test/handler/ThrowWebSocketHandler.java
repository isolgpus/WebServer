package io.kiw.luxis.web.test.handler;

import io.kiw.luxis.result.Result;
import io.kiw.luxis.web.Luxis;
import io.kiw.luxis.web.handler.WebSocketRoute;
import io.kiw.luxis.web.internal.WebSocketPipeline;
import io.kiw.luxis.web.pipeline.WebSocketSplitStream;
import io.kiw.luxis.web.test.MyApplicationState;
import io.kiw.luxis.web.websocket.WebSocketResult;

public class ThrowWebSocketHandler extends WebSocketRoute<WebSocketThrowRequest, MyApplicationState> {

    private Luxis<?> luxis;

    @Override
    public WebSocketPipeline onMessage(final WebSocketSplitStream<WebSocketThrowRequest, MyApplicationState> stream) {
        return stream
            .on("throw", WebSocketThrowRequest.class, s ->
                s.map(ctx -> ctx.in().where)
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
                .<String>correlatedAsyncMap(ctx -> {
                    if ("asyncMap".equals(ctx.in())) {
                        throw new RuntimeException("app error in asyncMap");
                    }
                    luxis.handleAsyncResponse(ctx.correlationId(), Result.success(ctx.in()));
                })
                .<String>correlatedAsyncBlockingMap(ctx -> {
                    if ("asyncBlockingMap".equals(ctx.in())) {
                        throw new RuntimeException("app error in asyncBlockingMap");
                    }
                    luxis.handleAsyncResponse(ctx.correlationId(), Result.success(ctx.in()));
                })
                .flatMap(ctx -> {
                    if ("complete".equals(ctx.in())) {
                        throw new RuntimeException("app error in complete");
                    }
                    return WebSocketResult.success(new WebSocketEchoResponse("ok"));
                })
                .complete())
            .build();
    }

    public void evillyReferenceLuxis(Luxis<?> luxis) {
        this.luxis = luxis;
    }
}
