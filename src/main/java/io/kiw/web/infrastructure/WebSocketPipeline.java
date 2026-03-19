package io.kiw.web.infrastructure;

import java.util.List;

public class WebSocketPipeline<T> {
    private final List<WebSocketMapInstruction> instructions;
    private final Object applicationState;

    WebSocketPipeline(List<WebSocketMapInstruction> instructions, Object applicationState) {
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
