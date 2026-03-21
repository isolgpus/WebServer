package io.kiw.web.pipeline;

import io.kiw.web.http.*;
import io.kiw.web.validation.*;
import io.kiw.web.websocket.*;
import io.kiw.web.handler.*;
import io.kiw.web.internal.*;
import io.kiw.web.internal.ender.*;

import java.util.concurrent.CompletableFuture;

public interface WebSocketStreamAsyncMapper<REQ, RES, APP> {
    CompletableFuture<RES> handle(WebSocketContext<REQ, APP> ctx);
}
