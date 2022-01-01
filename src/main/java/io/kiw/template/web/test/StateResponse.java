package io.kiw.template.web.test;

import io.kiw.template.web.infrastructure.JsonResponse;

public class StateResponse implements JsonResponse {
    public final long longValue;

    public StateResponse(long longValue) {

        this.longValue = longValue;
    }
}
