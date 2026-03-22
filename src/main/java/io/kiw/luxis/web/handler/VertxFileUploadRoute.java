package io.kiw.luxis.web.handler;

import io.kiw.luxis.web.http.HttpBuffer;
import io.kiw.luxis.web.internal.RequestPipeline;
import io.kiw.luxis.web.pipeline.HttpMapStream;

import java.util.Map;

public abstract class VertxFileUploadRoute<OUT, APP> {

    public abstract RequestPipeline<OUT> handle(HttpMapStream<Map<String, HttpBuffer>, APP> e);

}
