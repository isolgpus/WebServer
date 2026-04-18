package io.kiw.luxis.web.handler;

import io.kiw.luxis.web.internal.LuxisPipeline;
import io.kiw.luxis.web.pipeline.HttpStream;
import tools.jackson.core.type.TypeReference;

public abstract class JsonHandler<IN, OUT, APP> extends TypeReference<IN> {


    public abstract LuxisPipeline<OUT> handle(HttpStream<IN, APP> e);

}
