package io.kiw.template.web.infrastructure;

public class FlowInstruction<IN, OUT> {
    public final boolean isBlocking;
    public final FlowHandler<IN, OUT> consumer;
    public final boolean lastStep;

    public FlowInstruction(boolean isBlocking, FlowHandler<IN, OUT> consumer, boolean lastStep) {
        this.isBlocking = isBlocking;
        this.consumer = consumer;
        this.lastStep = lastStep;
    }



}
