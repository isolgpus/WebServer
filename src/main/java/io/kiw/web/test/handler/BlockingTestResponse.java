package io.kiw.web.test.handler;

import io.kiw.web.infrastructure.JsonResponse;

public class BlockingTestResponse {
    public final int multipliedNumber;

    public BlockingTestResponse(int multipliedNumber) {
        this.multipliedNumber = multipliedNumber;
    }
}
