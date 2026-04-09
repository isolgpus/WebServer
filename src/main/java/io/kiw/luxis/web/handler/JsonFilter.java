package io.kiw.luxis.web.handler;

import io.kiw.luxis.web.internal.RequestPipeline;
import io.kiw.luxis.web.pipeline.HttpStream;

public interface JsonFilter<APP> {

    RequestPipeline<Void> handle(HttpStream<Void, APP> e);

}
