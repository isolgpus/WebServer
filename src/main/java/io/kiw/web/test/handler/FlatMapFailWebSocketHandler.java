package io.kiw.web.test.handler;

import io.kiw.web.infrastructure.ErrorMessageResponse;
import io.kiw.web.infrastructure.WebSocketPipeline;
import io.kiw.web.infrastructure.WebSocketResult;
import io.kiw.web.infrastructure.WebSocketRoute;
import io.kiw.web.infrastructure.WebSocketStream;
import io.kiw.web.test.MyApplicationState;

public class FlatMapFailWebSocketHandler extends WebSocketRoute<WebSocketEchoRequest, WebSocketEchoResponse, MyApplicationState> {

    @Override
    public WebSocketPipeline<WebSocketEchoResponse> onMessage(WebSocketStream<WebSocketEchoRequest, MyApplicationState> stream) {
        return stream
            .<WebSocketEchoResponse>flatMap(ctx -> WebSocketResult.error(new ErrorMessageResponse("flatMap failed")))
            .complete();
    }
}
