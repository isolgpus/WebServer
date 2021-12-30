package io.kiw.template.web.test.handler;

import io.kiw.template.web.infrastructure.*;
import io.vertx.core.http.Cookie;

public class GetEchoHandler extends VertxJsonRoute<EmptyRequest> {

    @Override
    public Flow handle(FlowControl<EmptyRequest> flowControl) {
        return
            flowControl
                .handle((emptyRequest, httpContext) -> HttpResult.success("fuff"))
                .blocking((fuff, state) -> HttpResult.success(123))
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
