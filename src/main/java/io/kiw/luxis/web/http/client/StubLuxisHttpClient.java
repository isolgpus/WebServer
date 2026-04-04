package io.kiw.luxis.web.http.client;

import io.kiw.luxis.result.Result;
import io.kiw.luxis.web.TestLuxis;
import io.kiw.luxis.web.http.ErrorMessageResponse;
import io.kiw.luxis.web.http.HttpErrorResponse;
import io.kiw.luxis.web.http.Method;
import io.kiw.luxis.web.test.StubRequest;
import io.kiw.luxis.web.test.StubRouter;
import io.kiw.luxis.web.test.TestHttpResponse;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public final class StubLuxisHttpClient implements LuxisHttpClient {

    private final StubRouter router;
    private final LuxisHttpClientConfig config;
    private final ObjectMapper mapper = new ObjectMapper();

    private StubLuxisHttpClient(final StubRouter router, final LuxisHttpClientConfig config) {
        this.router = router;
        this.config = config;
    }

    public static StubLuxisHttpClient create(final TestLuxis<?> targetServer) {
        return new StubLuxisHttpClient(targetServer.getRouter(), LuxisHttpClientConfig.defaults());
    }

    public static StubLuxisHttpClient create(final TestLuxis<?> targetServer, final LuxisHttpClientConfig config) {
        return new StubLuxisHttpClient(targetServer.getRouter(), config);
    }

    @Override
    public <T> LuxisAsync<HttpClientResponse<T>> get(final HttpClientRequest request, final Class<T> responseType) {
        return send(request, Method.GET, responseType);
    }

    @Override
    public <T> LuxisAsync<HttpClientResponse<T>> post(final HttpClientRequest request, final Class<T> responseType) {
        return send(request, Method.POST, responseType);
    }

    @Override
    public <T> LuxisAsync<HttpClientResponse<T>> put(final HttpClientRequest request, final Class<T> responseType) {
        return send(request, Method.PUT, responseType);
    }

    @Override
    public <T> LuxisAsync<HttpClientResponse<T>> delete(final HttpClientRequest request, final Class<T> responseType) {
        return send(request, Method.DELETE, responseType);
    }

    @Override
    public <T> LuxisAsync<HttpClientResponse<T>> patch(final HttpClientRequest request, final Class<T> responseType) {
        return send(request, Method.PATCH, responseType);
    }

    @SuppressWarnings("unchecked")
    private <T> LuxisAsync<HttpClientResponse<T>> send(final HttpClientRequest request, final Method method, final Class<T> responseType) {
        final String resolvedUrl = resolveUrl(request.getUrl());
        final URI uri = URI.create(resolvedUrl);
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
        final String rawBody = testResponse.responseBody;
        final int statusCode = testResponse.statusCode;

        if (config.isErrorAwareResponses() && statusCode >= 400) {
            final HttpErrorResponse errorResponse = toHttpErrorResponse(rawBody, statusCode);
            return new LuxisAsync<>(CompletableFuture.completedFuture(Result.error(errorResponse)));
        }

        final Map<String, String> headers = new LinkedHashMap<>();
        final String contentType = testResponse.getHeader("Content-Type");
        if (contentType != null) {
            headers.put("Content-Type", contentType);
        }

        final T typedBody;
        if (responseType == String.class) {
            typedBody = (T) rawBody;
        } else {
            typedBody = mapper.readValue(rawBody, responseType);
        }

        return LuxisAsync.completed(new HttpClientResponse<>(statusCode, typedBody, headers));
    }

    private String resolveUrl(final String url) {
        if (config.getBaseUrl() == null || url.contains("://")) {
            return url;
        }
        final String base = config.getBaseUrl().endsWith("/")
                ? config.getBaseUrl().substring(0, config.getBaseUrl().length() - 1)
                : config.getBaseUrl();
        final String path = url.startsWith("/") ? url : "/" + url;
        return base + path;
    }

    private HttpErrorResponse toHttpErrorResponse(final String rawBody, final int statusCode) {
        try {
            final ErrorMessageResponse errorMessage = mapper.readValue(rawBody, ErrorMessageResponse.class);
            return new HttpErrorResponse(errorMessage, statusCode);
        } catch (final Exception ignored) {
        }
        return new HttpErrorResponse(new ErrorMessageResponse(rawBody), statusCode);
    }
}
