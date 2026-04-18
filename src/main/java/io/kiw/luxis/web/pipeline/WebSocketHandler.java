package io.kiw.luxis.web.pipeline;

import io.kiw.luxis.web.internal.WebSocketPipeline;

public interface WebSocketHandler<IN, OUT, APP, RESP, ERR, SESSION> {
    WebSocketPipeline<OUT> handle(WebSocketStream<IN, APP, RESP, ERR, SESSION> stream);
}
