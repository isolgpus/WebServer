package io.kiw.luxis.web.test.handler;

import io.kiw.luxis.web.pipeline.*;
import io.kiw.luxis.web.handler.*;
import io.kiw.luxis.web.http.*;
import io.kiw.luxis.web.validation.*;
import io.kiw.luxis.web.websocket.*;
import io.kiw.luxis.web.internal.*;
import io.kiw.luxis.web.jwt.*;
import io.kiw.luxis.web.cors.*;

public class EchoResponse {
    public final int intExample;
    public final String stringExample;
    public final String pathExample;
    public final String queryExample;
    public final String requestHeaderExample;
    public final String requestCookieExample;

    public EchoResponse(int intExample, String stringExample, String pathExample, String queryExample, String requestHeaderExample, String requestCookieExample) {

        this.intExample = intExample;
        this.stringExample = stringExample;
        this.pathExample = pathExample;
        this.queryExample = queryExample;
        this.requestHeaderExample = requestHeaderExample;
        this.requestCookieExample = requestCookieExample;
    }
}
