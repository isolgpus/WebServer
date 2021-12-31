package io.kiw.template.web.test.handler;

import io.kiw.template.web.infrastructure.*;
import io.vertx.core.http.Cookie;

public class GetEchoHandler extends VertxJsonRoute<EmptyRequest, EchoResponse> {

    @Override
    public Flow<EchoResponse> handle(HttpControlStream<EmptyRequest> httpControlStream) {
        return
            httpControlStream
                .complete((number, httpContext) -> {
                    Cookie requestCookieExample = httpContext.getRequestCookie("requestCookieExample");

                    return HttpResult.success(new EchoResponse(188,
                        "You invoked a GET",
                        httpContext.getQueryParam("queryExample"),
                        httpContext.getRequestHeader("requestHeaderExample"),
                        requestCookieExample != null ? requestCookieExample.getValue() : null));
                });
    }
}
