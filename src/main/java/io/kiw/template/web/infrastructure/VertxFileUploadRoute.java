package io.kiw.template.web.infrastructure;

import io.vertx.core.buffer.Buffer;

import java.util.Map;

public abstract class VertxFileUploadRoute<OUT extends JsonResponse, APP> {


    public abstract Flow<OUT> handle(HttpControlStream<Map<String, Buffer>, APP> e);

}
