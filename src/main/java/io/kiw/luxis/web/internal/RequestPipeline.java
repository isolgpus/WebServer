package io.kiw.luxis.web.internal;

import io.kiw.luxis.web.pipeline.*;
import io.kiw.luxis.web.handler.*;
import io.kiw.luxis.web.http.*;
import io.kiw.luxis.web.validation.*;
import io.kiw.luxis.web.websocket.*;

import io.kiw.luxis.web.internal.ender.Ender;

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
