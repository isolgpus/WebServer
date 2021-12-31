package io.kiw.template.web.test.handler;

import io.kiw.template.web.infrastructure.HttpResult;
import io.kiw.template.web.infrastructure.JsonRequest;

public class BlockingRequest implements JsonRequest {
    public int numberToMultiply;
}
