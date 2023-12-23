package io.kiw.web.test.handler;

import io.kiw.web.infrastructure.JsonResponse;

public class EchoResponse implements JsonResponse {
    public final int intExample;
    public final String stringExample;
    public final String queryExample;
    public final String requestHeaderExample;
    public final String requestCookieExample;

    public EchoResponse(int intExample, String stringExample, String queryExample, String requestHeaderExample, String requestCookieExample) {

        this.intExample = intExample;
        this.stringExample = stringExample;
        this.queryExample = queryExample;
        this.requestHeaderExample = requestHeaderExample;
        this.requestCookieExample = requestCookieExample;
    }
}
