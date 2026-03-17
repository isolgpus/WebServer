package io.kiw.web.test;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.WebSocketConnectOptions;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class VertxHttpApplicationClient implements ApplicationClient, AutoCloseable {

    private final Vertx vertx;
    private final HttpClient httpClient;
    private final String host;
    private final int port;
    private final long timeoutSeconds;

    public VertxHttpApplicationClient(String host, int port) {
        this(host, port, 10);
    }

    public VertxHttpApplicationClient(String host, int port, long timeoutSeconds) {
        this.host = host;
        this.port = port;
        this.timeoutSeconds = timeoutSeconds;
        this.vertx = Vertx.vertx();
        this.httpClient = vertx.createHttpClient();
    }

    @Override
    public TestHttpResponse post(StubRequest stubRequest) {
        return request(stubRequest, HttpMethod.POST);
    }

    @Override
    public TestHttpResponse put(StubRequest stubRequest) {
        return request(stubRequest, HttpMethod.PUT);
    }

    @Override
    public TestHttpResponse delete(StubRequest stubRequest) {
        return request(stubRequest, HttpMethod.DELETE);
    }

    @Override
    public TestHttpResponse patch(StubRequest stubRequest) {
        return request(stubRequest, HttpMethod.PATCH);
    }

    @Override
    public TestHttpResponse get(StubRequest stubRequest) {
        return request(stubRequest, HttpMethod.GET);
    }

    @Override
    public TestHttpResponse options(StubRequest stubRequest) {
        return request(stubRequest, HttpMethod.OPTIONS);
    }

    @Override
    public WebSocketClient webSocket(StubRequest stubRequest) {
        return webSocketConnection(stubRequest);
    }

    @Override
    public void assertException(String expected) {

    }

    @Override
    public void assertNoMoreExceptions() {

    }

    public WebSocketClient webSocketConnection(StubRequest stubRequest) {
        CompletableFuture<VertxWebSocketClient> future = new CompletableFuture<>();

        String uri = buildUri(stubRequest);
        WebSocketConnectOptions options = new WebSocketConnectOptions()
            .setHost(host)
            .setPort(port)
            .setURI(uri);

        for (Map.Entry<String, String> header : stubRequest.headers.entrySet()) {
            options.addHeader(header.getKey(), header.getValue());
        }

        httpClient.webSocket(options)
            .onSuccess(ws -> future.complete(new VertxWebSocketClient(ws)))
            .onFailure(future::completeExceptionally);

        try {
            return future.get(timeoutSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException("WebSocket connection failed", e);
        }
    }

    private TestHttpResponse request(StubRequest stubRequest, HttpMethod method) {
        CompletableFuture<TestHttpResponse> future = new CompletableFuture<>();

        String uri = buildUri(stubRequest);

        httpClient.request(method, port, host, uri)
            .onSuccess(req -> {
                for (Map.Entry<String, String> header : stubRequest.headers.entrySet()) {
                    req.putHeader(header.getKey(), header.getValue());
                }

                for (Map.Entry<String, io.vertx.core.http.Cookie> cookie : stubRequest.cookies.entrySet()) {
                    req.putHeader("Cookie", cookie.getValue().getName() + "=" + cookie.getValue().getValue());
                }

                if (!stubRequest.fileUploads.isEmpty()) {
                    String boundary = "----VertxHttpClientBoundary" + System.nanoTime();
                    req.putHeader("Content-Type", "multipart/form-data; boundary=" + boundary);

                    Buffer multipartBody = Buffer.buffer();
                    for (Map.Entry<String, Buffer> upload : stubRequest.fileUploads.entrySet()) {
                        multipartBody.appendString("--" + boundary + "\r\n");
                        multipartBody.appendString("Content-Disposition: form-data; name=\"" + upload.getKey() + "\"; filename=\"" + upload.getKey() + "\"\r\n");
                        multipartBody.appendString("Content-Type: application/octet-stream\r\n\r\n");
                        multipartBody.appendBuffer(upload.getValue());
                        multipartBody.appendString("\r\n");
                    }
                    multipartBody.appendString("--" + boundary + "--\r\n");

                    req.send(multipartBody)
                        .onSuccess(resp -> handleResponse(resp, future))
                        .onFailure(future::completeExceptionally);
                } else if (stubRequest.body != null) {
                    req.putHeader("Content-Type", "application/json");
                    req.send(Buffer.buffer(stubRequest.body))
                        .onSuccess(resp -> handleResponse(resp, future))
                        .onFailure(future::completeExceptionally);
                } else {
                    req.send()
                        .onSuccess(resp -> handleResponse(resp, future))
                        .onFailure(future::completeExceptionally);
                }
            })
            .onFailure(future::completeExceptionally);

        try {
            return future.get(timeoutSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException("HTTP request failed: " + method + " " + uri, e);
        }
    }

    private void handleResponse(HttpClientResponse resp, CompletableFuture<TestHttpResponse> future) {
        resp.body()
            .onSuccess(body -> {
                TestHttpResponse testResponse = new TestHttpResponse(body.toString())
                    .withStatusCode(resp.statusCode());

                for (String headerName : resp.headers().names()) {
                    testResponse.withHeader(headerName, resp.getHeader(headerName));
                }

                if (resp.cookies() != null) {
                    for (String setCookie : resp.cookies()) {
                        String[] parts = setCookie.split(";")[0].split("=", 2);
                        if (parts.length == 2) {
                            testResponse.withCookie(parts[0].trim(), parts[1].trim());
                        }
                    }
                }

                future.complete(testResponse);
            })
            .onFailure(future::completeExceptionally);
    }

    private String buildUri(StubRequest stubRequest) {
        StringBuilder uri = new StringBuilder(stubRequest.path);

        if (!stubRequest.queryParams.isEmpty()) {
            uri.append("?");
            boolean first = true;
            for (Map.Entry<String, String> entry : stubRequest.queryParams.entrySet()) {
                if (!first) {
                    uri.append("&");
                }
                if(entry.getValue() != null)
                {
                    uri.append(entry.getKey()).append("=").append(entry.getValue());
                }
                first = false;
            }
        }

        return uri.toString();
    }

    @Override
    public void close() {
        try {
            CompletableFuture<Void> clientClose = new CompletableFuture<>();
            httpClient.close()
                .onSuccess(v -> clientClose.complete(null))
                .onFailure(clientClose::completeExceptionally);
            clientClose.get(timeoutSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            // best effort
        }

        try {
            CompletableFuture<Void> vertxClose = new CompletableFuture<>();
            vertx.close()
                .onSuccess(v -> vertxClose.complete(null))
                .onFailure(vertxClose::completeExceptionally);
            vertxClose.get(timeoutSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            // best effort
        }
    }
}
