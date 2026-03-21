package io.kiw.luxis.web.internal;

import io.kiw.luxis.web.pipeline.*;
import io.kiw.luxis.web.handler.*;
import io.kiw.luxis.web.http.*;
import io.kiw.luxis.web.validation.*;
import io.kiw.luxis.web.websocket.*;

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
