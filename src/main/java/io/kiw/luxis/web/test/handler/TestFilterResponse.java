package io.kiw.luxis.web.test.handler;

import io.kiw.luxis.web.pipeline.*;
import io.kiw.luxis.web.handler.*;
import io.kiw.luxis.web.http.*;
import io.kiw.luxis.web.validation.*;
import io.kiw.luxis.web.websocket.*;
import io.kiw.luxis.web.internal.*;
import io.kiw.luxis.web.jwt.*;
import io.kiw.luxis.web.cors.*;

import io.kiw.luxis.web.http.JsonResponse;

public class TestFilterResponse {
    public final String filterMessage;

    public TestFilterResponse(String filterMessage) {

        this.filterMessage = filterMessage;
    }
}
