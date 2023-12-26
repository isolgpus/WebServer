package io.kiw.web.infrastructure;

import java.util.List;

public class Flow<T> {
    private final List<MapInstruction> instructions;
    private final Object applicationState;

    Flow(List<MapInstruction> instructions, Object applicationState) {

        this.instructions = instructions;
        this.applicationState = applicationState;
    }

    public List<MapInstruction> getApplicationInstructions() {
        return instructions;
    }


    public Object getApplicationState() {
        return applicationState;
    }
}
