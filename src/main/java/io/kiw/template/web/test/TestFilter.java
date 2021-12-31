package io.kiw.template.web.test;

import io.kiw.template.web.infrastructure.Flow;
import io.kiw.template.web.infrastructure.HttpControlStream;
import io.kiw.template.web.infrastructure.HttpResult;
import io.kiw.template.web.infrastructure.VertxJsonFilter;
import io.vertx.core.http.impl.CookieImpl;

public class TestFilter implements VertxJsonFilter
{


    @Override
    public Flow handle(HttpControlStream<Void> e) {
        return e.complete((request, httpContext) -> {
            httpContext.addResponseCookie(new CookieImpl("testFilter", "hitfilter"));
            return HttpResult.success(null);
        });
    }
}
