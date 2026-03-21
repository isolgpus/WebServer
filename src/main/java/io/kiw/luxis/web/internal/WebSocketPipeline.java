package io.kiw.luxis.web.internal;

import java.util.List;

public class WebSocketPipeline<T> {
    private final List<WebSocketMapInstruction> instructions;
    private final Object applicationState;

    public WebSocketPipeline(final List<WebSocketMapInstruction> instructions, final Object applicationState) {
        this.instructions = instructions;
        this.applicationState = applicationState;
    }

    public List<WebSocketMapInstruction> getApplicationInstructions() {
        return instructions;
    }

    public Object getApplicationState() {
        return applicationState;
    }
}
