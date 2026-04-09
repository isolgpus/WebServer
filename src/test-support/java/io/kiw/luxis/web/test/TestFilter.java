package io.kiw.luxis.web.test;

import io.kiw.luxis.web.handler.JsonFilter;
import io.kiw.luxis.web.http.HttpCookie;
import io.kiw.luxis.web.http.HttpResult;
import io.kiw.luxis.web.internal.RequestPipeline;
import io.kiw.luxis.web.pipeline.HttpStream;

public class TestFilter implements JsonFilter<MyApplicationState> {
    private String cookieKey;

    public TestFilter(final String testFilter) {
        cookieKey = testFilter;
    }

    @Override
    public RequestPipeline<Void> handle(final HttpStream<Void, MyApplicationState> e) {
        return e.complete(ctx -> {
            ctx.http().addResponseCookie(new HttpCookie(cookieKey, "hitfilter"));
            return HttpResult.success();
        });
    }
}
