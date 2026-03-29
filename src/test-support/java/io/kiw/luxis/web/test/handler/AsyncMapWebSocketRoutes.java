package io.kiw.luxis.web.test.handler;

import io.kiw.luxis.result.Result;
import io.kiw.luxis.web.Luxis;
import io.kiw.luxis.web.handler.WebSocketRoutes;
import io.kiw.luxis.web.pipeline.WebSocketRoutesRegister;
import io.kiw.luxis.web.test.MyApplicationState;

public class AsyncMapWebSocketRoutes extends WebSocketRoutes<MyApplicationState, TestWebSocketResponse> {

    private Luxis<?> luxis;

    @Override
    public void registerRoutes(final WebSocketRoutesRegister<MyApplicationState, TestWebSocketResponse> routesRegister) {
        routesRegister.responseType("numberResponse", WebSocketNumberResponse.class);

        routesRegister
                .route("number", WebSocketNumberRequest.class, s ->
                        s.<Integer>asyncMap(ctx -> {
                                    luxis.handleAsyncResponse(ctx.correlationId(), Result.success(ctx.in().value * 10));
                                })
                                .map(ctx -> new WebSocketNumberResponse(ctx.in()))
                                .complete());

    }

    public void evillyReferenceLuxis(Luxis<?> luxis) {
        this.luxis = luxis;
    }
}
