package io.kiw.web.pipeline;

import io.kiw.web.http.*;
import io.kiw.web.validation.*;
import io.kiw.web.websocket.*;
import io.kiw.web.handler.*;
import io.kiw.web.internal.*;
import io.kiw.web.internal.ender.*;

public interface HttpControlStreamBlockingMapper<REQ, RES> {
    RES handle(BlockingContext<REQ> ctx);
}
