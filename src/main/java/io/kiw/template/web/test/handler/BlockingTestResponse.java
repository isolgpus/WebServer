package io.kiw.template.web.test.handler;

import io.kiw.template.web.infrastructure.JsonResponse;

public class BlockingTestResponse implements JsonResponse {
    public final int multipliedNumber;

    public BlockingTestResponse(int multipliedNumber) {
        this.multipliedNumber = multipliedNumber;
    }
}
