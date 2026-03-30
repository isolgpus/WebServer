package io.kiw.luxis.web.http.client;

import io.kiw.luxis.web.TestLuxis;
import io.kiw.luxis.web.http.Method;
import io.kiw.luxis.web.test.StubRequest;
import io.kiw.luxis.web.test.StubRouter;
import io.kiw.luxis.web.test.TestHttpResponse;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

public final class StubLuxisHttpClient implements LuxisHttpClient {

    private final StubRouter router;

    private StubLuxisHttpClient(final StubRouter router) {
        this.router = router;
    }

    public static StubLuxisHttpClient create(final TestLuxis<?> targetServer) {
        return new StubLuxisHttpClient(targetServer.getRouter());
    }

    @Override
    public LuxisAsync<HttpClientResponse> get(final HttpClientRequest request) {
        return send(request, Method.GET);
    }

    @Override
    public LuxisAsync<HttpClientResponse> post(final HttpClientRequest request) {
        return send(request, Method.POST);
    }

    @Override
    public LuxisAsync<HttpClientResponse> put(final HttpClientRequest request) {
        return send(request, Method.PUT);
    }

    @Override
    public LuxisAsync<HttpClientResponse> delete(final HttpClientRequest request) {
        return send(request, Method.DELETE);
    }

    @Override
    public LuxisAsync<HttpClientResponse> patch(final HttpClientRequest request) {
        return send(request, Method.PATCH);
    }

    private LuxisAsync<HttpClientResponse> send(final HttpClientRequest request, final Method method) {
        final URI uri = URI.create(request.getUrl());
        final String path = uri.getPath();

        final StubRequest stubRequest = StubRequest.request(path);

        if (request.getBody() != null) {
            stubRequest.body(request.getBody());
        }

        for (final Map.Entry<String, String> entry : request.getHeaders().entrySet()) {
            stubRequest.headerParam(entry.getKey(), entry.getValue());
        }

        for (final Map.Entry<String, String> entry : request.getQueryParams().entrySet()) {
            stubRequest.queryParam(entry.getKey(), entry.getValue());
        }

        final String query = uri.getQuery();
        if (query != null) {
            for (final String param : query.split("&")) {
                final String[] parts = param.split("=", 2);
                if (parts.length == 2) {
                    stubRequest.queryParam(parts[0], parts[1]);
                }
            }
        }

        final TestHttpResponse testResponse = router.handle(stubRequest, method);
        final HttpClientResponse response = toHttpClientResponse(testResponse);
        return LuxisAsync.completed(response);
    }

    private static HttpClientResponse toHttpClientResponse(final TestHttpResponse testResponse) {
        final Map<String, String> headers = new LinkedHashMap<>();
        final String contentType = testResponse.getHeader("Content-Type");
        if (contentType != null) {
            headers.put("Content-Type", contentType);
        }
        return new HttpClientResponse(testResponse.statusCode, testResponse.responseBody, headers);
    }
}
