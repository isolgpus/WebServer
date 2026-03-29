package io.kiw.luxis.web.test.handler;

import io.kiw.luxis.result.Result;
import io.kiw.luxis.web.Luxis;
import io.kiw.luxis.web.handler.WebSocketRoutes;
import io.kiw.luxis.web.pipeline.WebSocketRoutesRegister;
import io.kiw.luxis.web.test.MyApplicationState;
import io.kiw.luxis.web.websocket.WebSocketResult;

public class ThrowWebSocketRoutes extends WebSocketRoutes<MyApplicationState> {

    private Luxis<?> luxis;

    @Override
    public void registerRoutes(final WebSocketRoutesRegister<MyApplicationState> routesRegister) {
        routesRegister
            .route("throw", WebSocketThrowRequest.class, s ->
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
                .<String>asyncMap(ctx -> {
                    if ("asyncMap".equals(ctx.in())) {
                        throw new RuntimeException("app error in asyncMap");
                    }
                    luxis.handleAsyncResponse(ctx.correlationId(), Result.success(ctx.in()));
                })
                .<String>asyncBlockingMap(ctx -> {
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
                .complete());
            
    }

    public void evillyReferenceLuxis(Luxis<?> luxis) {
        this.luxis = luxis;
    }
}
