package io.kiw.luxis.web.http.client;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpMethod;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public final class VertxLuxisHttpClient implements LuxisHttpClient {

    private final HttpClient httpClient;

    public VertxLuxisHttpClient(final Vertx vertx) {
        this.httpClient = vertx.createHttpClient();
    }

    @Override
    public LuxisAsync<HttpClientResponse> get(final HttpClientRequest request) {
        return send(request, HttpMethod.GET);
    }

    @Override
    public LuxisAsync<HttpClientResponse> post(final HttpClientRequest request) {
        return send(request, HttpMethod.POST);
    }

    @Override
    public LuxisAsync<HttpClientResponse> put(final HttpClientRequest request) {
        return send(request, HttpMethod.PUT);
    }

    @Override
    public LuxisAsync<HttpClientResponse> delete(final HttpClientRequest request) {
        return send(request, HttpMethod.DELETE);
    }

    @Override
    public LuxisAsync<HttpClientResponse> patch(final HttpClientRequest request) {
        return send(request, HttpMethod.PATCH);
    }

    private LuxisAsync<HttpClientResponse> send(final HttpClientRequest request, final HttpMethod method) {
        final CompletableFuture<HttpClientResponse> future = new CompletableFuture<>();
        final URI uri = URI.create(request.getUrl());

        final String host = uri.getHost();
        final int port = uri.getPort() != -1 ? uri.getPort() : (
            "https".equals(uri.getScheme()) ? 443 : 80
        );
        final String requestUri = buildRequestUri(uri, request.getQueryParams());

        httpClient.request(method, port, host, requestUri)
            .onSuccess(req -> {
                for (final Map.Entry<String, String> header : request.getHeaders().entrySet()) {
                    req.putHeader(header.getKey(), header.getValue());
                }

                if (request.getBody() != null) {
                    req.putHeader("Content-Type", "application/json");
                    req.send(Buffer.buffer(request.getBody()))
                        .onSuccess(resp -> handleResponse(resp, future))
                        .onFailure(future::completeExceptionally);
                } else {
                    req.send()
                        .onSuccess(resp -> handleResponse(resp, future))
                        .onFailure(future::completeExceptionally);
                }
            })
            .onFailure(future::completeExceptionally);

        return new LuxisAsync<>(future);
    }

    private static void handleResponse(final io.vertx.core.http.HttpClientResponse resp,
                                        final CompletableFuture<HttpClientResponse> future) {
        resp.body()
            .onSuccess(body -> {
                final Map<String, String> headers = new LinkedHashMap<>();
                for (final String name : resp.headers().names()) {
                    headers.put(name, resp.getHeader(name));
                }
                future.complete(new HttpClientResponse(resp.statusCode(), body.toString(), headers));
            })
            .onFailure(future::completeExceptionally);
    }

    private static String buildRequestUri(final URI uri, final Map<String, String> extraQueryParams) {
        final StringBuilder sb = new StringBuilder();
        sb.append(uri.getRawPath() != null ? uri.getRawPath() : "/");

        final String existingQuery = uri.getRawQuery();
        final boolean hasExisting = existingQuery != null && !existingQuery.isEmpty();
        final boolean hasExtra = !extraQueryParams.isEmpty();

        if (hasExisting || hasExtra) {
            sb.append("?");
            if (hasExisting) {
                sb.append(existingQuery);
            }
            boolean first = !hasExisting;
            for (final Map.Entry<String, String> entry : extraQueryParams.entrySet()) {
                if (!first) {
                    sb.append("&");
                }
                sb.append(entry.getKey()).append("=").append(entry.getValue());
                first = false;
            }
        }

        return sb.toString();
    }
}
