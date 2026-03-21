package io.kiw.luxis.web.test.handler;

import io.kiw.luxis.web.internal.RequestPipeline;
import io.kiw.luxis.web.pipeline.HttpStream;
import io.kiw.luxis.web.http.HttpResult;
import io.kiw.luxis.web.handler.VertxJsonRoute;
import io.kiw.luxis.web.test.MyApplicationState;
import io.kiw.luxis.web.http.HttpCookie;

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
                ctx.http().addResponseCookie(new HttpCookie("responseCookieExample", ctx.in().responseCookieExample));
            }
            HttpCookie requestCookieExample = ctx.http().getRequestCookie("requestCookieExample");
            return HttpResult.success(new EchoResponse(
                ctx.in().intExample,
                ctx.in().stringExample,
                ctx.http().getPathParam("pathExample"),
                ctx.http().getQueryParam("queryExample"),
                ctx.http().getRequestHeader("requestHeaderExample"),
                requestCookieExample != null ? requestCookieExample.value() :  null));
        });
    }

}
