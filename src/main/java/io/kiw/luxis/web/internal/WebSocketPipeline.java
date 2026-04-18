package io.kiw.luxis.web.internal;

import java.util.List;

public class WebSocketPipeline<T> {
    private final List<MapInstruction> instructions;
    private final Object applicationState;
    private final boolean sendResponse;

    public WebSocketPipeline(final List<MapInstruction> instructions, final Object applicationState) {
        this(instructions, applicationState, true);
    }

    public WebSocketPipeline(final List<MapInstruction> instructions, final Object applicationState, final boolean sendResponse) {
        this.instructions = instructions;
        this.applicationState = applicationState;
        this.sendResponse = sendResponse;
    }

    public List<MapInstruction> getApplicationInstructions() {
        return instructions;
    }

    public Object getApplicationState() {
        return applicationState;
    }

    public boolean shouldSendResponse() {
        return sendResponse;
    }
}
