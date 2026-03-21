package io.kiw.web.internal;

import io.kiw.web.pipeline.*;
import io.kiw.web.handler.*;
import io.kiw.web.http.*;
import io.kiw.web.validation.*;
import io.kiw.web.websocket.*;

public interface WebSocketRouterWrapper {
    void handleBlocking(Runnable o);

    void handleOnEventLoop(Runnable o);
}
