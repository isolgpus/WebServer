package io.kiw.luxis.web.http.client;

import io.kiw.luxis.result.Result;
import io.kiw.luxis.web.http.ErrorMessageResponse;
import io.kiw.luxis.web.http.HttpErrorResponse;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpMethod;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public final class VertxLuxisHttpClient implements LuxisHttpClient {

    private final HttpClient httpClient;
    private final LuxisHttpClientConfig config;
    private final ObjectMapper mapper = new ObjectMapper();

    public VertxLuxisHttpClient(final Vertx vertx) {
        this(vertx, LuxisHttpClientConfig.defaults());
    }

    public VertxLuxisHttpClient(final Vertx vertx, final LuxisHttpClientConfig config) {
        this.config = config;
        if (config.isSsl()) {
            this.httpClient = vertx.createHttpClient(new HttpClientOptions().setSsl(true));
        } else {
            this.httpClient = vertx.createHttpClient();
        }
    }

    @Override
    public <T> LuxisAsync<HttpClientResponse<T>> get(final HttpClientRequest request, final Class<T> responseType) {
        return send(request, HttpMethod.GET, responseType);
    }

    @Override
    public <T> LuxisAsync<HttpClientResponse<T>> post(final HttpClientRequest request, final Class<T> responseType) {
        return send(request, HttpMethod.POST, responseType);
    }

    @Override
    public <T> LuxisAsync<HttpClientResponse<T>> put(final HttpClientRequest request, final Class<T> responseType) {
        return send(request, HttpMethod.PUT, responseType);
    }

    @Override
    public <T> LuxisAsync<HttpClientResponse<T>> delete(final HttpClientRequest request, final Class<T> responseType) {
        return send(request, HttpMethod.DELETE, responseType);
    }

    @Override
    public <T> LuxisAsync<HttpClientResponse<T>> patch(final HttpClientRequest request, final Class<T> responseType) {
        return send(request, HttpMethod.PATCH, responseType);
    }

    private <T> LuxisAsync<HttpClientResponse<T>> send(final HttpClientRequest request, final HttpMethod method, final Class<T> responseType) {
        final CompletableFuture<Result<HttpErrorResponse, HttpClientResponse<T>>> future = new CompletableFuture<>();
        final String resolvedUrl = resolveUrl(request.getUrl());
        final URI uri = URI.create(resolvedUrl);

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
                                .onSuccess(resp -> handleResponse(resp, future, responseType))
                                .onFailure(future::completeExceptionally);
                    } else {
                        req.send()
                                .onSuccess(resp -> handleResponse(resp, future, responseType))
                                .onFailure(future::completeExceptionally);
                    }
                })
                .onFailure(future::completeExceptionally);

        return new LuxisAsync<>(future);
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

    @SuppressWarnings("unchecked")
    private <T> void handleResponse(final io.vertx.core.http.HttpClientResponse resp,
                                    final CompletableFuture<Result<HttpErrorResponse, HttpClientResponse<T>>> future,
                                    final Class<T> responseType) {
        resp.body()
                .onSuccess(body -> {
                    final String rawBody = body.toString();
                    final Map<String, String> headers = new LinkedHashMap<>();
                    for (final String name : resp.headers().names()) {
                        headers.put(name, resp.getHeader(name));
                    }

                    if (config.isErrorAwareResponses() && resp.statusCode() >= 400) {
                        future.complete(Result.error(toHttpErrorResponse(rawBody, resp.statusCode())));
                    } else {
                        final T typedBody;
                        if (responseType == String.class) {
                            typedBody = (T) rawBody;
                        } else {
                            typedBody = mapper.readValue(rawBody, responseType);
                        }
                        future.complete(Result.success(new HttpClientResponse<>(resp.statusCode(), typedBody, headers)));
                    }
                })
                .onFailure(future::completeExceptionally);
    }

    private HttpErrorResponse toHttpErrorResponse(final String rawBody, final int statusCode) {
        try {
            final ErrorMessageResponse errorMessage = mapper.readValue(rawBody, ErrorMessageResponse.class);
            return new HttpErrorResponse(errorMessage, statusCode);
        } catch (final Exception ignored) {
        }
        return new HttpErrorResponse(new ErrorMessageResponse(rawBody), statusCode);
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
                sb.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8))
                        .append("=")
                        .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
                first = false;
            }
        }

        return sb.toString();
    }
}
