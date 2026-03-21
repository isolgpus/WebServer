package io.kiw.luxis.web.test.handler;

import io.kiw.luxis.web.pipeline.*;
import io.kiw.luxis.web.handler.*;
import io.kiw.luxis.web.http.*;
import io.kiw.luxis.web.validation.*;
import io.kiw.luxis.web.websocket.*;
import io.kiw.luxis.web.internal.*;
import io.kiw.luxis.web.test.MyApplicationState;
import io.kiw.luxis.web.http.HttpCookie;

public class GetEchoHandler extends VertxJsonRoute<EmptyRequest, EchoResponse, MyApplicationState> {

    @Override
    public RequestPipeline<EchoResponse> handle(HttpStream<EmptyRequest, MyApplicationState> httpStream) {
        return
            httpStream
                .complete(ctx -> {
                    HttpCookie requestCookieExample = ctx.http().getRequestCookie("requestCookieExample");

                    return HttpResult.success(new EchoResponse(188,
                        "You invoked a GET",
                        ctx.http().getPathParam("pathExample"),
                        ctx.http().getQueryParam("queryExample"),
                        ctx.http().getRequestHeader("requestHeaderExample"),
                        requestCookieExample != null ? requestCookieExample.value() : null));
                });
    }
}
