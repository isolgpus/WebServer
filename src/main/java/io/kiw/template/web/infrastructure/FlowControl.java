package io.kiw.template.web.infrastructure;

import java.util.List;

public class FlowControl<IN> {
    private final List<FlowInstruction> instructionChain;

    public FlowControl(List<FlowInstruction> instructionChain) {
        this.instructionChain = instructionChain;
    }

    public <OUT> FlowControl<OUT> map(FlowHandler<IN, OUT> flowHandler)
    {
        instructionChain.add(new FlowInstruction<>(false, flowHandler, false));
        return new FlowControl<>(instructionChain);
    }

    public <OUT> FlowControl<OUT> blockingMap(FlowHandler<IN, OUT> flowHandler)
    {
        instructionChain.add(new FlowInstruction<>(true, flowHandler, false));
        return new FlowControl<>(instructionChain);
    }

    public <OUT extends JsonResponse> Flow<OUT> complete(FlowHandler<IN, OUT> flowHandler)
    {
        instructionChain.add(new FlowInstruction<>(false, flowHandler, true));
        return new Flow<>(instructionChain);
    }
}
