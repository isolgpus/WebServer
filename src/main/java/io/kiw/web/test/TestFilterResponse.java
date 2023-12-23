package io.kiw.web.test;

import io.kiw.web.infrastructure.JsonResponse;

public class TestFilterResponse implements JsonResponse {
    public final String filterMessage;

    public TestFilterResponse(String filterMessage) {

        this.filterMessage = filterMessage;
    }
}
