package io.kiw.luxis.web.test;

import io.kiw.luxis.web.internal.RequestPipeline;
import io.kiw.luxis.web.pipeline.HttpStream;
import io.kiw.luxis.web.http.HttpResult;
import io.kiw.luxis.web.handler.VertxJsonFilter;
import io.kiw.luxis.web.http.HttpCookie;

public class TestFilter implements VertxJsonFilter<MyApplicationState>
{
    private String cookieKey;

    public TestFilter(String testFilter) {
        cookieKey = testFilter;
    }

    @Override
    public RequestPipeline handle(HttpStream<Void, MyApplicationState> e) {
        return e.complete(ctx -> {
            ctx.http().addResponseCookie(new HttpCookie(cookieKey, "hitfilter"));
            return HttpResult.success();
        });
    }
}
