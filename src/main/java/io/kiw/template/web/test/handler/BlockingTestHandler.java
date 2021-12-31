package io.kiw.template.web.test.handler;

import io.kiw.template.web.infrastructure.*;

import static io.kiw.template.web.infrastructure.HttpResult.success;

public class BlockingTestHandler extends VertxJsonRoute<BlockingRequest> {

    @Override
    public Flow handle(FlowControl<BlockingRequest> flowControl) {
        return
            flowControl
                .map((blockingRequest, httpRequest) -> success(blockingRequest.numberToMultiply))
                .blockingMap((numberToMultiply, httpRequest) -> success(numberToMultiply * 2))
                .complete((multipliedNumber, httpContext) -> success(new BlockingTestResponse(multipliedNumber)));
    }
}
