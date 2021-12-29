package io.kiw.template.web.test.handler;

import io.kiw.template.web.infrastructure.EmptyRequest;
import io.kiw.template.web.infrastructure.HttpContext;
import io.kiw.template.web.infrastructure.VertxJsonRoute;
import io.vertx.core.http.Cookie;

public class GetEchoHandler extends VertxJsonRoute<EmptyRequest> {

    @Override
    public EchoResponse handle(EmptyRequest helloRequest, HttpContext httpContext)
    {
        Cookie requestCookieExample = httpContext.getRequestCookie("requestCookieExample");
        return new EchoResponse(188,
                "You invoked a GET",
                httpContext.getQueryParam("queryExample"),
                httpContext.getRequestHeader("requestHeaderExample"),
                requestCookieExample != null ? requestCookieExample.getValue() : null);
    }
}
