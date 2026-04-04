package io.kiw.luxis.web.test.handler;

import io.kiw.luxis.web.handler.JsonHandler;
import io.kiw.luxis.web.http.client.HttpClientResponse;
import io.kiw.luxis.web.http.client.LuxisHttpClient;
import io.kiw.luxis.web.internal.RequestPipeline;
import io.kiw.luxis.web.pipeline.HttpStream;
import io.kiw.luxis.web.test.MyApplicationState;

import static io.kiw.luxis.web.http.HttpResult.success;

public class HttpClientDeleteCallHandler extends JsonHandler<HttpClientGetRequest, HttpClientGetResponse, MyApplicationState> {

    private final LuxisHttpClient httpClient;

    public HttpClientDeleteCallHandler(final LuxisHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public RequestPipeline<HttpClientGetResponse> handle(final HttpStream<HttpClientGetRequest, MyApplicationState> httpStream) {
        return httpStream
            .<HttpClientResponse>asyncMap(ctx -> httpClient.delete("http://serverB" + ctx.in().targetPath))
            .map(ctx -> new HttpClientGetResponse(ctx.in().statusCode(), ctx.in().body()))
            .complete(ctx -> success(ctx.in()));
    }
}
