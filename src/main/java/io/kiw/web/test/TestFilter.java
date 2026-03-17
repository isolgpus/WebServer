package io.kiw.web.test;

import io.kiw.web.infrastructure.RequestPipeline;
import io.kiw.web.infrastructure.HttpStream;
import io.kiw.web.infrastructure.HttpResult;
import io.kiw.web.infrastructure.VertxJsonFilter;
import io.vertx.core.http.impl.CookieImpl;

public class TestFilter implements VertxJsonFilter<MyApplicationState>
{
    private String cookieKey;

    public TestFilter(String testFilter) {
        cookieKey = testFilter;
    }

    @Override
    public RequestPipeline handle(HttpStream<Void, MyApplicationState> e) {
        return e.complete(ctx -> {
            ctx.http().addResponseCookie(new CookieImpl(cookieKey, "hitfilter"));
            return HttpResult.success();
        });
    }
}
