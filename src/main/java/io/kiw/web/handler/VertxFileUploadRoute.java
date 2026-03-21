package io.kiw.web.handler;

import io.kiw.web.http.*;
import io.kiw.web.pipeline.*;
import io.kiw.web.websocket.*;
import io.kiw.web.internal.*;
import io.kiw.web.internal.ender.*;

import io.vertx.core.buffer.Buffer;

import java.util.Map;

public abstract class VertxFileUploadRoute<OUT, APP> {


    public abstract RequestPipeline<OUT> handle(HttpStream<Map<String, Buffer>, APP> e);

}
