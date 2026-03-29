package io.kiw.luxis.web.handler;

import tools.jackson.core.type.TypeReference;
import io.kiw.luxis.web.internal.RequestPipeline;
import io.kiw.luxis.web.pipeline.HttpStream;

public abstract class JsonHandler<IN, OUT, APP> extends TypeReference<IN> {


    public abstract RequestPipeline<OUT> handle(HttpStream<IN, APP> e);

}
