package io.kiw.luxis.web.http.client;

import io.kiw.luxis.result.Result;
import io.kiw.luxis.web.TestLuxis;
import io.kiw.luxis.web.http.ErrorMessageResponse;
import io.kiw.luxis.web.http.HttpErrorResponse;
import io.kiw.luxis.web.internal.ClientWebSocketHandler;
import io.kiw.luxis.web.internal.PendingAsyncResponses;
import io.kiw.luxis.web.test.StubExecutionDispatcher;
import io.kiw.luxis.web.test.StubRequest;
import io.kiw.luxis.web.test.StubTestClient;
import io.kiw.luxis.web.test.StubTestWebSocketClient;
import io.kiw.luxis.web.test.StubTimeoutScheduler;
import io.kiw.luxis.web.test.TestHttpResponse;
import io.kiw.luxis.web.test.TimeInjector;
import io.kiw.luxis.web.websocket.ClientWebSocketRoutes;
import io.kiw.luxis.web.websocket.WebSocketConnection;
import io.kiw.luxis.web.websocket.WebSocketSession;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public final class StubLuxisHttpClient implements LuxisHttpClient {

    private final StubTestClient stubTestClient;
    private final LuxisHttpClientConfig config;
    private final ObjectMapper mapper = new ObjectMapper();

    private StubLuxisHttpClient(final StubTestClient stubTestClient, final LuxisHttpClientConfig config) {
        this.stubTestClient = stubTestClient;
        this.config = config;
    }

    public static StubLuxisHttpClient create(final TestLuxis<?> targetServer) {

        return create(targetServer, LuxisHttpClientConfig.defaults());
    }

    public static StubLuxisHttpClient create(final TestLuxis<?> targetServer, final LuxisHttpClientConfig config) {
        return new StubLuxisHttpClient(new StubTestClient(null, 80, targetServer), config);
    }

    @Override
    public <T> LuxisAsync<HttpClientResponse<T>> get(final HttpClientRequest request, final Class<T> responseType) {
        return send(request, r -> stubTestClient.get(r), responseType);
    }

    @Override
    public <T> LuxisAsync<HttpClientResponse<T>> post(final HttpClientRequest request, final Class<T> responseType) {
        return send(request, r -> stubTestClient.post(r), responseType);
    }

    @Override
    public <T> LuxisAsync<HttpClientResponse<T>> put(final HttpClientRequest request, final Class<T> responseType) {
        return send(request, r -> stubTestClient.put(r), responseType);
    }

    @Override
    public <T> LuxisAsync<HttpClientResponse<T>> delete(final HttpClientRequest request, final Class<T> responseType) {
        return send(request, r -> stubTestClient.delete(r), responseType);
    }

    @Override
    public <T> LuxisAsync<HttpClientResponse<T>> patch(final HttpClientRequest request, final Class<T> responseType) {
        return send(request, r -> stubTestClient.patch(r), responseType);
    }

    @Override
    public <APP, RESP> WebSocketSession<RESP> connectToWebSocket(final String path, final ClientWebSocketRoutes<APP, RESP> routes) {
        final String resolvedUrl = resolveUrl(path);
        final URI uri = URI.create(resolvedUrl);
        final String resolvedPath = uri.getPath();

        final StubTestWebSocketClient serverWsClient = stubTestClient.webSocket(StubRequest.request(resolvedPath));
        final PendingAsyncResponses pendingAsyncResponses = new PendingAsyncResponses(new StubTimeoutScheduler(new TimeInjector()), e -> {
            throw new RuntimeException(e);
        });
        final ClientWebSocketHandler<APP, RESP> handler = new ClientWebSocketHandler<>(routes, mapper, new StubExecutionDispatcher(), pendingAsyncResponses, e -> {
            throw new RuntimeException(e);
        });

        final StubClientWebSocketBridge bridge = new StubClientWebSocketBridge(serverWsClient);
        final WebSocketSession<RESP> session = handler.createSession(bridge);
        bridge.init(handler, session);
        return session;
    }

    private static class StubClientWebSocketBridge implements WebSocketConnection {

        private final StubTestWebSocketClient serverWsClient;
        private ClientWebSocketHandler<?, ?> clientHandler;
        private WebSocketSession<?> clientSession;

        StubClientWebSocketBridge(final StubTestWebSocketClient serverWsClient) {
            this.serverWsClient = serverWsClient;
        }

        void init(final ClientWebSocketHandler<?, ?> clientHandler, final WebSocketSession<?> clientSession) {
            this.clientHandler = clientHandler;
            this.clientSession = clientSession;
        }

        @Override
        public CompletableFuture<Void> sendText(final String text) {
            serverWsClient.send(text);
            serverWsClient.onResponses(messages -> {
                for (final String msg : messages) {
                    clientHandler.onMessage(msg, clientSession);
                }
            });
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public void close() {
            serverWsClient.close();
        }
    }

    @SuppressWarnings("unchecked")
    private <T> LuxisAsync<HttpClientResponse<T>> send(final HttpClientRequest request, final java.util.function.Function<StubRequest, TestHttpResponse> method, final Class<T> responseType) {
        final String resolvedUrl = resolveUrl(request.getUrl());
        final URI uri = URI.create(resolvedUrl);
        final String path = uri.getPath();

        final StubRequest stubRequest = StubRequest.request(path);

        if (request.getBody() != null) {

            stubRequest.body(request.getBody() instanceof String ? (String) request.getBody() : mapper.writeValueAsString(request.getBody()));
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

        final TestHttpResponse testResponse = method.apply(stubRequest);
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
