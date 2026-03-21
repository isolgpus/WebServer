package io.kiw.web.handler;

import io.kiw.web.http.*;
import io.kiw.web.pipeline.*;
import io.kiw.web.websocket.*;
import io.kiw.web.internal.*;
import io.kiw.web.internal.ender.*;

import com.fasterxml.jackson.core.type.TypeReference;

public abstract class VertxJsonRoute<IN, OUT, APP> extends TypeReference<IN> {


    public abstract RequestPipeline<OUT> handle(HttpStream<IN, APP> e);

}
