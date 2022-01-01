package io.kiw.template.web.test;

import io.kiw.template.web.infrastructure.Flow;
import io.kiw.template.web.infrastructure.HttpControlStream;
import io.kiw.template.web.infrastructure.HttpResult;
import io.kiw.template.web.infrastructure.VertxJsonFilter;
import io.vertx.core.http.impl.CookieImpl;

public class TestFilter implements VertxJsonFilter
{


    private String cookieKey;

    public TestFilter(String testFilter) {
        cookieKey = testFilter;
    }

    @Override
    public Flow handle(HttpControlStream<Void> e) {
        return e.complete((request, httpContext) -> {
            httpContext.addResponseCookie(new CookieImpl(cookieKey, "hitfilter"));
            return HttpResult.success();
        });
    }
}
