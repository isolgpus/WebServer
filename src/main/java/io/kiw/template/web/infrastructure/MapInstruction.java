package io.kiw.template.web.infrastructure;

public class MapInstruction<IN, OUT, APP> {
    final boolean isBlocking;
    final HttpControlStreamFlatMapper<IN, OUT, APP> consumer;
    final boolean lastStep;

    MapInstruction(boolean isBlocking, HttpControlStreamFlatMapper<IN, OUT, APP> consumer, boolean lastStep) {
        this.isBlocking = isBlocking;
        this.consumer = consumer;
        this.lastStep = lastStep;
    }



}
