package io.kiw.web.test.handler;

import io.kiw.web.pipeline.*;
import io.kiw.web.handler.*;
import io.kiw.web.http.*;
import io.kiw.web.validation.*;
import io.kiw.web.websocket.*;
import io.kiw.web.internal.*;
import io.kiw.web.jwt.*;
import io.kiw.web.cors.*;

public class StatusCodeResponse {
    public String value;

    public StatusCodeResponse() {}

    public StatusCodeResponse(String value) {
        this.value = value;
    }
}
