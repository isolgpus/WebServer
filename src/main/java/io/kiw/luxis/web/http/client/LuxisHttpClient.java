package io.kiw.luxis.web.http.client;

public interface LuxisHttpClient {

    LuxisAsync<HttpClientResponse> get(HttpClientRequest request);

    LuxisAsync<HttpClientResponse> post(HttpClientRequest request);

    LuxisAsync<HttpClientResponse> put(HttpClientRequest request);

    LuxisAsync<HttpClientResponse> delete(HttpClientRequest request);

    LuxisAsync<HttpClientResponse> patch(HttpClientRequest request);

    default LuxisAsync<HttpClientResponse> get(final String url) {
        return get(HttpClientRequest.request(url));
    }

    default LuxisAsync<HttpClientResponse> post(final String url, final String body) {
        return post(HttpClientRequest.request(url).body(body));
    }

    default LuxisAsync<HttpClientResponse> put(final String url, final String body) {
        return put(HttpClientRequest.request(url).body(body));
    }

    default LuxisAsync<HttpClientResponse> delete(final String url) {
        return delete(HttpClientRequest.request(url));
    }

    default LuxisAsync<HttpClientResponse> patch(final String url, final String body) {
        return patch(HttpClientRequest.request(url).body(body));
    }
}
