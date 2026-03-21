package io.kiw.web.test;

import io.kiw.web.pipeline.*;
import io.kiw.web.handler.*;
import io.kiw.web.http.*;
import io.kiw.web.validation.*;
import io.kiw.web.websocket.*;
import io.kiw.web.internal.*;
import io.kiw.web.jwt.*;
import io.kiw.web.cors.*;
import io.kiw.web.openapi.*;

import io.kiw.web.internal.WebSocketRouterWrapper;

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
