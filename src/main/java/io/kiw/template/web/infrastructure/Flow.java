package io.kiw.template.web.infrastructure;

import java.util.List;

public class Flow<T extends JsonResponse> {
    private final List<FlowInstruction> instructions;
    public Flow(List<FlowInstruction> instructions) {

        this.instructions = instructions;
    }

    public List<FlowInstruction> getApplicationInstructions() {
        return instructions;
    }


}
