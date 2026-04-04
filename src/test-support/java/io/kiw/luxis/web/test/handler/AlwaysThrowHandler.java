package io.kiw.luxis.web.test.handler;

import io.kiw.luxis.web.handler.JsonHandler;
import io.kiw.luxis.web.internal.RequestPipeline;
import io.kiw.luxis.web.pipeline.HttpStream;
import io.kiw.luxis.web.test.MyApplicationState;

public class AlwaysThrowHandler extends JsonHandler<Void, Object, MyApplicationState> {

    @Override
    public RequestPipeline<Object> handle(final HttpStream<Void, MyApplicationState> httpStream) {
        return httpStream
                .complete(ctx -> {
                    throw new RuntimeException("test exception");
                });
    }
}
