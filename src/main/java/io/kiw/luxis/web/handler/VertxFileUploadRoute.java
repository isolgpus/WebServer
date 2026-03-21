package io.kiw.luxis.web.handler;

import io.kiw.luxis.web.http.*;
import io.kiw.luxis.web.pipeline.*;
import io.kiw.luxis.web.websocket.*;
import io.kiw.luxis.web.internal.*;
import io.kiw.luxis.web.internal.ender.*;

import java.util.Map;

public abstract class VertxFileUploadRoute<OUT, APP> {

    public abstract RequestPipeline<OUT> handle(HttpStream<Map<String, HttpBuffer>, APP> e);

}
