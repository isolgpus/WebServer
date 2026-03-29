package io.kiw.luxis.web.test.handler;

import io.kiw.luxis.web.handler.JsonHandler;
import io.kiw.luxis.web.http.EmptyRequest;
import io.kiw.luxis.web.http.HttpCookie;
import io.kiw.luxis.web.http.HttpResult;
import io.kiw.luxis.web.internal.RequestPipeline;
import io.kiw.luxis.web.pipeline.HttpStream;
import io.kiw.luxis.web.test.MyApplicationState;

public class GetEchoHandler extends JsonHandler<EmptyRequest, EchoResponse, MyApplicationState> {

    @Override
    public RequestPipeline<EchoResponse> handle(final HttpStream<EmptyRequest, MyApplicationState> httpStream) {
        return
            httpStream
                .complete(ctx -> {
                    final HttpCookie requestCookieExample = ctx.http().getRequestCookie("requestCookieExample");

                    return HttpResult.success(new EchoResponse(188,
                        "You invoked a GET",
                        ctx.http().getPathParam("pathExample"),
                        ctx.http().getQueryParam("queryExample"),
                        ctx.http().getRequestHeader("requestHeaderExample"),
                        requestCookieExample != null ? requestCookieExample.value() : null));
                });
    }
}
