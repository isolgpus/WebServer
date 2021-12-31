package io.kiw.template.web.test;

import io.kiw.template.web.infrastructure.JsonResponse;

public class TestFilterResponse implements JsonResponse {
    public final String filterMessage;

    public TestFilterResponse(String filterMessage) {

        this.filterMessage = filterMessage;
    }
}
