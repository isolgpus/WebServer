package io.kiw.template.web.test.handler;

import io.kiw.template.web.infrastructure.JsonRequest;

public class EchoRequest implements JsonRequest {
    public int intExample;
    public String stringExample;
    public String responseHeaderExample;
    public String responseCookieExample;
}
