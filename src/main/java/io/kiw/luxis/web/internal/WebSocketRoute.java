package io.kiw.luxis.web.internal;

public class WebSocketRoute<IN> {
    private final Class<IN> messageType;
    private final WebSocketPipeline<?> pipeline;

    public WebSocketRoute(final Class<IN> messageType, final WebSocketPipeline<?> pipeline) {
        this.messageType = messageType;
        this.pipeline = pipeline;
    }

    public Class<IN> messageType() {
        return messageType;
    }

    public WebSocketPipeline<?> pipeline() {
        return pipeline;
    }
}
