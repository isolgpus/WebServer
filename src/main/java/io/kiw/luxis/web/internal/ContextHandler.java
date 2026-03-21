package io.kiw.luxis.web.internal;

import io.kiw.luxis.web.pipeline.*;
import io.kiw.luxis.web.handler.*;
import io.kiw.luxis.web.http.*;
import io.kiw.luxis.web.validation.*;
import io.kiw.luxis.web.websocket.*;

public interface ContextHandler {
    void handle(RequestContext context);
}
