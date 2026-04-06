package io.kiw.luxis.web.http.client;

import io.kiw.luxis.web.websocket.ClientWebSocketRoutes;
import io.kiw.luxis.web.websocket.WebSocketSession;

public interface LuxisHttpClient {

    <APP, RESP> WebSocketSession<RESP> connectToWebSocket(String path, ClientWebSocketRoutes<APP, RESP> routes);

    <T> LuxisAsync<HttpClientResponse<T>> get(HttpClientRequest request, Class<T> responseType);

    <T> LuxisAsync<HttpClientResponse<T>> post(HttpClientRequest request, Class<T> responseType);

    <T> LuxisAsync<HttpClientResponse<T>> put(HttpClientRequest request, Class<T> responseType);

    <T> LuxisAsync<HttpClientResponse<T>> delete(HttpClientRequest request, Class<T> responseType);

    <T> LuxisAsync<HttpClientResponse<T>> patch(HttpClientRequest request, Class<T> responseType);

    default LuxisAsync<HttpClientResponse<String>> get(final String url) {
        return get(HttpClientRequest.request(url), String.class);
    }

    default LuxisAsync<HttpClientResponse<String>> post(final String url, final String body) {
        return post(HttpClientRequest.request(url, body), String.class);
    }

    default LuxisAsync<HttpClientResponse<String>> put(final String url, final String body) {
        return put(HttpClientRequest.request(url, body), String.class);
    }

    default LuxisAsync<HttpClientResponse<String>> delete(final String url) {
        return delete(HttpClientRequest.request(url), String.class);
    }

    default LuxisAsync<HttpClientResponse<String>> patch(final String url, final String body) {
        return patch(HttpClientRequest.request(url, body), String.class);
    }

    default <T> LuxisAsync<HttpClientResponse<T>> get(final String url, final Class<T> responseType) {
        return get(HttpClientRequest.request(url), responseType);
    }

    default <T> LuxisAsync<HttpClientResponse<T>> post(final String url, final String body, final Class<T> responseType) {
        return post(HttpClientRequest.request(url, body), responseType);
    }

    default <T> LuxisAsync<HttpClientResponse<T>> put(final String url, final String body, final Class<T> responseType) {
        return put(HttpClientRequest.request(url, body), responseType);
    }

    default <T> LuxisAsync<HttpClientResponse<T>> delete(final String url, final Class<T> responseType) {
        return delete(HttpClientRequest.request(url), responseType);
    }

    default <T> LuxisAsync<HttpClientResponse<T>> patch(final String url, final String body, final Class<T> responseType) {
        return patch(HttpClientRequest.request(url, body), responseType);
    }
}
