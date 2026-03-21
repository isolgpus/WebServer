package io.kiw.web.internal;

import io.kiw.web.pipeline.*;
import io.kiw.web.handler.*;
import io.kiw.web.http.*;
import io.kiw.web.validation.*;
import io.kiw.web.websocket.*;

import java.util.List;

public class WebSocketPipeline<T> {
    private final List<WebSocketMapInstruction> instructions;
    private final Object applicationState;

    public WebSocketPipeline(List<WebSocketMapInstruction> instructions, Object applicationState) {
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
