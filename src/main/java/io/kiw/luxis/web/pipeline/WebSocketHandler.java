package io.kiw.luxis.web.pipeline;

import io.kiw.luxis.web.internal.WebSocketPipeline;

public interface WebSocketHandler<IN, OUT, APP, RESP> {
    WebSocketPipeline<OUT> handle(WebSocketStream<IN, APP, RESP> stream);
}
