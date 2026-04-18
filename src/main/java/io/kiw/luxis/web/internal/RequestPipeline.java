package io.kiw.luxis.web.internal;

import io.kiw.luxis.web.internal.ender.Ender;

import java.util.List;

public class RequestPipeline<T> {
    private final List<HttpMapInstruction> instructions;
    private final Object applicationState;
    private final Ender ender;

    public RequestPipeline(final List<HttpMapInstruction> instructions, final Object applicationState, final Ender ender) {

        this.instructions = instructions;
        this.applicationState = applicationState;
        this.ender = ender;
    }

    public List<HttpMapInstruction> getApplicationInstructions() {
        return instructions;
    }


    public Object getApplicationState() {
        return applicationState;
    }

    public Ender getEnder() {
        return ender;
    }
}
