package io.kiw.luxis.web.test.handler;

import io.kiw.luxis.web.handler.JsonHandler;
import io.kiw.luxis.web.http.EmptyRequest;
import io.kiw.luxis.web.http.client.HttpClientResponse;
import io.kiw.luxis.web.http.client.LuxisHttpClient;
import io.kiw.luxis.web.internal.RequestPipeline;
import io.kiw.luxis.web.pipeline.HttpStream;
import io.kiw.luxis.web.test.MyApplicationState;
import io.kiw.luxis.web.test.TestHelper;

import static io.kiw.luxis.web.http.HttpResult.success;

public class HttpClientChainedCallHandler extends JsonHandler<EmptyRequest, HttpClientGetResponse, MyApplicationState> {

    private final LuxisHttpClient httpClient;
    private final String firstPath;
    private final String secondPath;

    public HttpClientChainedCallHandler(final LuxisHttpClient httpClient, final String firstPath, final String secondPath) {
        this.httpClient = httpClient;
        this.firstPath = firstPath;
        this.secondPath = secondPath;
    }

    @Override
    public RequestPipeline<HttpClientGetResponse> handle(final HttpStream<EmptyRequest, MyApplicationState> httpStream) {
        return httpStream
            .<HttpClientResponse>asyncMap(ctx -> httpClient.get("http://serverB" + firstPath))
            .map(ctx -> {
                try {
                    final int value = TestHelper.MAPPER.readTree(ctx.in().body()).get("result").asInt();
                    return TestHelper.MAPPER.createObjectNode().put("value", value).toString();
                } catch (final Exception e) {
                    throw new RuntimeException(e);
                }
            })
            .<HttpClientResponse>asyncMap(ctx -> httpClient.post("http://serverB" + secondPath, ctx.in()))
            .map(ctx -> new HttpClientGetResponse(ctx.in().statusCode(), ctx.in().body()))
            .complete(ctx -> success(ctx.in()));
    }
}
