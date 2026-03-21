package io.kiw.luxis.web.test.handler;

import io.kiw.luxis.web.http.ErrorMessageResponse;
import io.kiw.luxis.web.internal.WebSocketPipeline;
import io.kiw.luxis.web.websocket.WebSocketResult;
import io.kiw.luxis.web.handler.WebSocketRoute;
import io.kiw.luxis.web.pipeline.WebSocketStream;
import io.kiw.luxis.web.test.MyApplicationState;

public class FlatMapFailWebSocketHandler extends WebSocketRoute<WebSocketEchoRequest, WebSocketEchoResponse, MyApplicationState> {

    @Override
    public WebSocketPipeline<WebSocketEchoResponse> onMessage(WebSocketStream<WebSocketEchoRequest, MyApplicationState> stream) {
        return stream
            .<WebSocketEchoResponse>flatMap(ctx -> WebSocketResult.error(new ErrorMessageResponse("flatMap failed")))
            .complete();
    }
}
