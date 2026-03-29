package io.kiw.luxis.web.test.handler;

import io.kiw.luxis.result.Result;
import io.kiw.luxis.web.Luxis;
import io.kiw.luxis.web.handler.WebSocketRoute;
import io.kiw.luxis.web.internal.WebSocketPipeline;
import io.kiw.luxis.web.pipeline.WebSocketSplitStream;
import io.kiw.luxis.web.test.MyApplicationState;

public class AsyncMapWebSocketHandler extends WebSocketRoute<WebSocketNumberRequest, MyApplicationState> {

    private Luxis<?> luxis;

    @Override
    public WebSocketPipeline onMessage(final WebSocketSplitStream<WebSocketNumberRequest, MyApplicationState> stream) {
        return stream
            .on("number", WebSocketNumberRequest.class, s ->
                s.<Integer>correlatedAsyncMap(ctx -> {
                    luxis.handleAsyncResponse(ctx.correlationId(), Result.success(ctx.in().value * 10));
                })
                .map(ctx -> new WebSocketNumberResponse(ctx.in()))
                .complete())
            .build();
    }

    public void evillyReferenceLuxis(Luxis<?> luxis) {
        this.luxis = luxis;
    }
}
