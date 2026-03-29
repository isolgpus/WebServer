package io.kiw.luxis.web.test.handler;

import io.kiw.luxis.web.Luxis;
import io.kiw.luxis.web.handler.WebSocketRoutes;
import io.kiw.luxis.web.http.ErrorMessageResponse;
import io.kiw.luxis.web.http.ErrorStatusCode;
import io.kiw.luxis.web.http.HttpResult;
import io.kiw.luxis.web.internal.WebSocketPipeline;
import io.kiw.luxis.web.pipeline.WebSocketRoutesRegister;
import io.kiw.luxis.web.test.MyApplicationState;

public class AsyncFlatMapFailWebSocketRoutes extends WebSocketRoutes<MyApplicationState> {

    private Luxis<?> luxis;

    @Override
    public void registerRoutes(final WebSocketRoutesRegister<MyApplicationState> routesRegister) {
        routesRegister
            .route("echo", WebSocketEchoRequest.class, s ->
                s.<WebSocketEchoResponse>asyncMap(ctx -> {
                    luxis.handleAsyncResponse(ctx.correlationId(), HttpResult.error(ErrorStatusCode.BAD_REQUEST, new ErrorMessageResponse("async flatMap failed")));
                })
                .complete());
    }

    public void evillyReferenceLuxis(Luxis<?> luxis) {
        this.luxis = luxis;
    }
}
