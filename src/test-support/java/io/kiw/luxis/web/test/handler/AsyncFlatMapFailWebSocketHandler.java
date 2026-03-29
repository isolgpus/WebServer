package io.kiw.luxis.web.test.handler;

import io.kiw.luxis.web.Luxis;
import io.kiw.luxis.web.handler.WebSocketRoute;
import io.kiw.luxis.web.http.ErrorMessageResponse;
import io.kiw.luxis.web.http.ErrorStatusCode;
import io.kiw.luxis.web.http.HttpResult;
import io.kiw.luxis.web.internal.WebSocketPipeline;
import io.kiw.luxis.web.pipeline.WebSocketSplitStream;
import io.kiw.luxis.web.test.MyApplicationState;

public class AsyncFlatMapFailWebSocketHandler extends WebSocketRoute<MyApplicationState> {

    private Luxis<?> luxis;

    @Override
    public WebSocketPipeline onMessage(final WebSocketSplitStream<MyApplicationState> stream) {
        return stream
            .on("echo", WebSocketEchoRequest.class, s ->
                s.<WebSocketEchoResponse>asyncMap(ctx -> {
                    luxis.handleAsyncResponse(ctx.correlationId(), HttpResult.error(ErrorStatusCode.BAD_REQUEST, new ErrorMessageResponse("async flatMap failed")));
                })
                .complete())
            .build();
    }

    public void evillyReferenceLuxis(Luxis<?> luxis) {
        this.luxis = luxis;
    }
}
