package io.kiw.web.test.handler;

import io.kiw.web.pipeline.*;
import io.kiw.web.handler.*;
import io.kiw.web.http.*;
import io.kiw.web.validation.*;
import io.kiw.web.websocket.*;
import io.kiw.web.internal.*;
import io.kiw.web.test.MyApplicationState;
import io.vertx.core.http.Cookie;

public class GetEchoHandler extends VertxJsonRoute<EmptyRequest, EchoResponse, MyApplicationState> {

    @Override
    public RequestPipeline<EchoResponse> handle(HttpStream<EmptyRequest, MyApplicationState> httpStream) {
        return
            httpStream
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
