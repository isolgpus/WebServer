package io.kiw.template.web.infrastructure;

public class MapInstruction<IN, OUT> {
    final boolean isBlocking;
    final HttpControlStreamFlatMapper<IN, OUT> consumer;
    final boolean lastStep;

    MapInstruction(boolean isBlocking, HttpControlStreamFlatMapper<IN, OUT> consumer, boolean lastStep) {
        this.isBlocking = isBlocking;
        this.consumer = consumer;
        this.lastStep = lastStep;
    }



}
