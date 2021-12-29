package io.kiw.template.web.test.handler;

import io.kiw.template.web.infrastructure.HttpContext;
import io.kiw.template.web.infrastructure.VertxJsonRoute;
import io.vertx.core.http.Cookie;
import io.vertx.core.http.impl.CookieImpl;

public class PostEchoHandler extends VertxJsonRoute<EchoRequest> {

    @Override
    public EchoResponse handle(EchoRequest echoRequest, HttpContext httpContext)
    {
        if(echoRequest.responseHeaderExample != null)
        {
            httpContext.addResponseHeader("responseHeaderExample", echoRequest.responseHeaderExample);
        }

        if(echoRequest.responseCookieExample != null)
        {
            httpContext.addResponseCookie(new CookieImpl("responseCookieExample", echoRequest.responseCookieExample));
        }
        Cookie requestCookieExample = httpContext.getRequestCookie("requestCookieExample");
        return new EchoResponse(
                echoRequest.intExample,
                echoRequest.stringExample,
                httpContext.getQueryParam("queryExample"),
                httpContext.getRequestHeader("requestHeaderExample"),
                requestCookieExample != null ? requestCookieExample.getValue() :  null);
    }
}
