package io.kiw.luxis.web.internal;

import io.kiw.luxis.web.http.ErrorMessageResponse;

import java.util.List;

public class WebSocketPipeline<T> {
    private final List<LuxisMapInstruction<ErrorMessageResponse>> instructions;
    private final Object applicationState;
    private final boolean sendResponse;

    public WebSocketPipeline(final List<LuxisMapInstruction<ErrorMessageResponse>> instructions, final Object applicationState) {
        this(instructions, applicationState, true);
    }

    public WebSocketPipeline(final List<LuxisMapInstruction<ErrorMessageResponse>> instructions, final Object applicationState, final boolean sendResponse) {
        this.instructions = instructions;
        this.applicationState = applicationState;
        this.sendResponse = sendResponse;
    }

    public List<LuxisMapInstruction<ErrorMessageResponse>> getApplicationInstructions() {
        return instructions;
    }

    public Object getApplicationState() {
        return applicationState;
    }

    public boolean shouldSendResponse() {
        return sendResponse;
    }
}
