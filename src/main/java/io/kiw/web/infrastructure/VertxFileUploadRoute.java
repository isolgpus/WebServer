package io.kiw.web.infrastructure;

import io.vertx.core.buffer.Buffer;

import java.util.Map;

public abstract class VertxFileUploadRoute<OUT, APP> {


    public abstract RequestPipeline<OUT> handle(HttpStream<Map<String, Buffer>, APP> e);

}
