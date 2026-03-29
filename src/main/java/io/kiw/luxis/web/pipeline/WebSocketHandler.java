package io.kiw.luxis.web.pipeline;

import io.kiw.luxis.web.internal.IndividualMessageWebSocketPipeline;

public interface WebSocketHandler<IN, OUT, APP, RESP> {
    IndividualMessageWebSocketPipeline<OUT> handle(WebSocketStream<IN, APP, RESP> stream);
}
