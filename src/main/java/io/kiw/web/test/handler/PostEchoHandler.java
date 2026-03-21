package io.kiw.web.test.handler;

import io.kiw.web.internal.RequestPipeline;
import io.kiw.web.pipeline.HttpStream;
import io.kiw.web.http.HttpResult;
import io.kiw.web.handler.VertxJsonRoute;
import io.kiw.web.test.MyApplicationState;
import io.vertx.core.http.Cookie;
import io.vertx.core.http.impl.CookieImpl;

public class PostEchoHandler extends VertxJsonRoute<EchoRequest, EchoResponse, MyApplicationState> {

    @Override
    public RequestPipeline<EchoResponse> handle(HttpStream<EchoRequest, MyApplicationState> e) {
        return e.complete(ctx -> {
            if(ctx.in().responseHeaderExample != null)
            {
                ctx.http().addResponseHeader("responseHeaderExample", ctx.in().responseHeaderExample);
            }

            if(ctx.in().responseCookieExample != null)
            {
                ctx.http().addResponseCookie(new CookieImpl("responseCookieExample", ctx.in().responseCookieExample));
            }
            Cookie requestCookieExample = ctx.http().getRequestCookie("requestCookieExample");
            return HttpResult.success(new EchoResponse(
                ctx.in().intExample,
                ctx.in().stringExample,
                ctx.http().getPathParam("pathExample"),
                ctx.http().getQueryParam("queryExample"),
                ctx.http().getRequestHeader("requestHeaderExample"),
                requestCookieExample != null ? requestCookieExample.getValue() :  null));
        });
    }

}
