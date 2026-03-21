package io.kiw.luxis.web.test;

import io.kiw.luxis.web.pipeline.*;
import io.kiw.luxis.web.handler.*;
import io.kiw.luxis.web.http.*;
import io.kiw.luxis.web.validation.*;
import io.kiw.luxis.web.websocket.*;
import io.kiw.luxis.web.internal.*;
import io.kiw.luxis.web.jwt.*;
import io.kiw.luxis.web.cors.*;
import io.kiw.luxis.web.openapi.*;

import io.kiw.luxis.web.internal.WebSocketRouterWrapper;

public class WebSocketStubRouterWrapper implements WebSocketRouterWrapper {
    @Override
    public void handleBlocking(Runnable o) {
        o.run();
    }

    @Override
    public void handleOnEventLoop(Runnable o) {
        o.run();
    }
}
