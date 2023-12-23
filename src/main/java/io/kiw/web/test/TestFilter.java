package io.kiw.web.test;

import io.kiw.web.infrastructure.Flow;
import io.kiw.web.infrastructure.HttpResponseStream;
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
    public Flow handle(HttpResponseStream<Void, MyApplicationState> e) {
        return e.complete((request, httpContext, applicationState) -> {
            httpContext.addResponseCookie(new CookieImpl(cookieKey, "hitfilter"));
            return HttpResult.success();
        });
    }
}
