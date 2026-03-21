package io.kiw.luxis.web.pipeline;

import io.kiw.luxis.web.http.*;
import io.kiw.luxis.web.validation.*;
import io.kiw.luxis.web.websocket.*;
import io.kiw.luxis.web.handler.*;
import io.kiw.luxis.web.internal.*;
import io.kiw.luxis.web.internal.ender.*;

import io.kiw.luxis.result.Result;

public interface WebSocketStreamBlockingFlatMapper<REQ, RES> {
    Result<ErrorMessageResponse, RES> handle(WebSocketBlockingContext<REQ> ctx);
}
