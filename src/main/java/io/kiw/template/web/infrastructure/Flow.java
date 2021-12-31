package io.kiw.template.web.infrastructure;

import java.util.List;

public class Flow<T extends JsonResponse> {
    private final List<MapInstruction> instructions;
    Flow(List<MapInstruction> instructions) {

        this.instructions = instructions;
    }

    public List<MapInstruction> getApplicationInstructions() {
        return instructions;
    }


}
