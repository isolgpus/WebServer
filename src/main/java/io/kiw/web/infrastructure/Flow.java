package io.kiw.web.infrastructure;

import io.kiw.web.infrastructure.ender.Ender;

import java.util.List;

public class Flow<T> {
    private final List<MapInstruction> instructions;
    private final Object applicationState;
    private final Ender ender;

    Flow(List<MapInstruction> instructions, Object applicationState, Ender ender) {

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
