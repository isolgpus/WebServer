package io.kiw.luxis.web.handler;

import io.kiw.luxis.web.http.*;
import io.kiw.luxis.web.pipeline.*;
import io.kiw.luxis.web.websocket.*;
import io.kiw.luxis.web.internal.*;
import io.kiw.luxis.web.internal.ender.*;

import com.fasterxml.jackson.core.type.TypeReference;

public abstract class VertxJsonRoute<IN, OUT, APP> extends TypeReference<IN> {


    public abstract RequestPipeline<OUT> handle(HttpStream<IN, APP> e);

}
