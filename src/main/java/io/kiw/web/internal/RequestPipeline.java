package io.kiw.web.internal;

import io.kiw.web.pipeline.*;
import io.kiw.web.handler.*;
import io.kiw.web.http.*;
import io.kiw.web.validation.*;
import io.kiw.web.websocket.*;

import io.kiw.web.internal.ender.Ender;

import java.util.List;

public class RequestPipeline<T> {
    private final List<MapInstruction> instructions;
    private final Object applicationState;
    private final Ender ender;

    public RequestPipeline(List<MapInstruction> instructions, Object applicationState, Ender ender) {

        this.instructions = instructions;
        this.applicationState = applicationState;
        this.ender = ender;
    }

    public List<MapInstruction> getApplicationInstructions() {
        return instructions;
    }


    public Object getApplicationState() {
        return applicationState;
    }

    public Ender getEnder() {
        return ender;
    }
}
