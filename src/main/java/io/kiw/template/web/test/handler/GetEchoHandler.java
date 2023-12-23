package io.kiw.template.web.test.handler;

import io.kiw.template.web.infrastructure.*;
import io.kiw.template.web.test.MyApplicationState;
import io.vertx.core.http.Cookie;

public class GetEchoHandler extends VertxJsonRoute<EmptyRequest, EchoResponse, MyApplicationState> {

    @Override
    public Flow<EchoResponse> handle(HttpResponseStream<EmptyRequest, MyApplicationState> httpResponseStream) {
        return
            httpResponseStream
                .complete((number, httpContext, myApplicationState) -> {
                    Cookie requestCookieExample = httpContext.getRequestCookie("requestCookieExample");

                    return HttpResult.success(new EchoResponse(188,
                        "You invoked a GET",
                        httpContext.getQueryParam("queryExample"),
                        httpContext.getRequestHeader("requestHeaderExample"),
                        requestCookieExample != null ? requestCookieExample.getValue() : null));
                });
    }
}
