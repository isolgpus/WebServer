package io.kiw.template.web.test.handler;

import io.kiw.template.web.infrastructure.*;
import io.kiw.template.web.test.MyApplicationState;
import io.vertx.core.http.Cookie;

public class GetEchoHandler extends VertxJsonRoute<EmptyRequest, EchoResponse, MyApplicationState> {

    @Override
    public Flow<EchoResponse> handle(HttpControlStream<EmptyRequest, MyApplicationState> httpControlStream) {
        return httpControlStream
                .flatMap(EchoHelper::mapQueryParams)
                .flatMap(EchoHelper::mapHeaders)
                .complete((request, httpContext, myApplicationState) ->
                {
                    Cookie requestCookieExample = httpContext.getRequestCookie("requestCookieExample");

                    return HttpResult.success(new EchoResponse(188,
                            "You invoked a GET",
                            request.queryExample,
                            request.requestHeaderExample,
                            requestCookieExample != null ? requestCookieExample.getValue() : null));
                });
    }
}
