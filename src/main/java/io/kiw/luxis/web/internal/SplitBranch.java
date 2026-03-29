package io.kiw.luxis.web.internal;

public class SplitBranch<IN> {
    private final Class<IN> messageType;
    private final IndividualMessageWebSocketPipeline<?> pipeline;

    public SplitBranch(final Class<IN> messageType, final IndividualMessageWebSocketPipeline<?> pipeline) {
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
