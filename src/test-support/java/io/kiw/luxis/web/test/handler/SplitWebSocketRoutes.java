package io.kiw.luxis.web.test.handler;

import io.kiw.luxis.web.handler.WebSocketRoutes;
import io.kiw.luxis.web.internal.IndividualMessageWebSocketPipeline;
import io.kiw.luxis.web.pipeline.WebSocketRoutesRegister;
import io.kiw.luxis.web.pipeline.WebSocketStream;
import io.kiw.luxis.web.test.MyApplicationState;

public class SplitWebSocketRoutes extends WebSocketRoutes<MyApplicationState> {

    private static IndividualMessageWebSocketPipeline<WebSocketEchoResponse> handleEcho(WebSocketStream<WebSocketEchoRequest, MyApplicationState> stream) {
        return stream
            .map(ctx -> new WebSocketEchoResponse("echo: " + ctx.in().message))
            .complete();
    }

    private static IndividualMessageWebSocketPipeline<WebSocketNumberResponse> handleNumber(WebSocketStream<WebSocketNumberRequest, MyApplicationState> stream) {
        return stream.map(ctx -> new WebSocketNumberResponse(ctx.in().value * 2))
            .complete();
    }

    @Override
    public void registerRoutes(final WebSocketRoutesRegister<MyApplicationState> routesRegister) {
        routesRegister.route("echo", WebSocketEchoRequest.class, SplitWebSocketRoutes::handleEcho);
        routesRegister.route("number", WebSocketNumberRequest.class, SplitWebSocketRoutes::handleNumber);

    }
}
