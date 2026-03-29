package io.kiw.luxis.web.internal;

public class WebSocketRoute<IN> {
    private final Class<IN> messageType;
    private final IndividualMessageWebSocketPipeline<?> pipeline;

    public WebSocketRoute(final Class<IN> messageType, final IndividualMessageWebSocketPipeline<?> pipeline) {
        this.messageType = messageType;
        this.pipeline = pipeline;
    }

    public Class<IN> messageType() {
        return messageType;
    }

    public IndividualMessageWebSocketPipeline<?> pipeline() {
        return pipeline;
    }
}
