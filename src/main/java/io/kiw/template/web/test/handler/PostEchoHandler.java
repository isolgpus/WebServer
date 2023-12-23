package io.kiw.template.web.test.handler;

import io.kiw.template.web.infrastructure.Flow;
import io.kiw.template.web.infrastructure.HttpResponseStream;
import io.kiw.template.web.infrastructure.HttpResult;
import io.kiw.template.web.infrastructure.VertxJsonRoute;
import io.kiw.template.web.test.MyApplicationState;
import io.vertx.core.http.Cookie;
import io.vertx.core.http.impl.CookieImpl;

public class PostEchoHandler extends VertxJsonRoute<EchoRequest, EchoResponse, MyApplicationState> {

    @Override
    public Flow<EchoResponse> handle(HttpResponseStream<EchoRequest, MyApplicationState> e) {
        return e.complete((echoRequest, httpContext, myApplicationState) -> {
            if(echoRequest.responseHeaderExample != null)
            {
                httpContext.addResponseHeader("responseHeaderExample", echoRequest.responseHeaderExample);
            }

            if(echoRequest.responseCookieExample != null)
            {
                httpContext.addResponseCookie(new CookieImpl("responseCookieExample", echoRequest.responseCookieExample));
            }
            Cookie requestCookieExample = httpContext.getRequestCookie("requestCookieExample");
            return HttpResult.success(new EchoResponse(
                echoRequest.intExample,
                echoRequest.stringExample,
                httpContext.getQueryParam("queryExample"),
                httpContext.getRequestHeader("requestHeaderExample"),
                requestCookieExample != null ? requestCookieExample.getValue() :  null));
        });
    }

}
