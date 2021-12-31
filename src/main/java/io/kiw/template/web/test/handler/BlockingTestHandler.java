package io.kiw.template.web.test.handler;

import io.kiw.template.web.infrastructure.Flow;
import io.kiw.template.web.infrastructure.FlowControl;
import io.kiw.template.web.infrastructure.VertxJsonRoute;

import static io.kiw.template.web.infrastructure.HttpResult.success;

public class BlockingTestHandler extends VertxJsonRoute<BlockingRequest, BlockingTestResponse> {

    @Override
    public Flow<BlockingTestResponse> handle(FlowControl<BlockingRequest> flowControl) {
        return
            flowControl
                .map((blockingRequest, httpContext) -> success(blockingRequest.numberToMultiply))
                .blockingMap((numberToMultiply, httpContext) -> success(numberToMultiply * 2))
                .complete((multipliedNumber, httpContext) -> success(new BlockingTestResponse(multipliedNumber)));
    }
}
