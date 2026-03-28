package io.kiw.luxis.web.test.handler;

import io.kiw.luxis.result.Result;
import io.kiw.luxis.web.Luxis;
import io.kiw.luxis.web.handler.WebSocketRoute;
import io.kiw.luxis.web.internal.WebSocketPipeline;
import io.kiw.luxis.web.pipeline.WebSocketStream;
import io.kiw.luxis.web.test.MyApplicationState;

public class AsyncBlockingMapWebSocketHandler extends WebSocketRoute<WebSocketNumberRequest, WebSocketNumberResponse, MyApplicationState> {

    private Luxis<?> luxis;

    @Override
    public WebSocketPipeline<WebSocketNumberResponse> onMessage(final WebSocketStream<WebSocketNumberRequest, MyApplicationState> stream) {
        return stream
            .correlatedAsyncBlockingMap(Integer.class, ctx -> {
                luxis.handleAsyncResponse(ctx.correlationId(), Result.success(ctx.in().value * 20));
            })
            .map(ctx -> new WebSocketNumberResponse(ctx.in()))
            .complete();
    }

    public void evillyReferenceLuxis(Luxis<?> luxis) {
        this.luxis = luxis;
    }
}
