package io.kiw.web.test.handler;

import io.kiw.web.infrastructure.*;
import io.kiw.web.test.MyApplicationState;
import io.vertx.core.http.Cookie;

public class GetEchoHandler extends VertxJsonRoute<EmptyRequest, EchoResponse, MyApplicationState> {

    @Override
    public RequestPipeline<EchoResponse> handle(HttpResponseStream<EmptyRequest, MyApplicationState> httpResponseStream) {
        return
            httpResponseStream
                .complete(ctx -> {
                    Cookie requestCookieExample = ctx.http().getRequestCookie("requestCookieExample");

                    return HttpResult.success(new EchoResponse(188,
                        "You invoked a GET",
                        ctx.http().getPathParam("pathExample"),
                        ctx.http().getQueryParam("queryExample"),
                        ctx.http().getRequestHeader("requestHeaderExample"),
                        requestCookieExample != null ? requestCookieExample.getValue() : null));
                });
    }
}
