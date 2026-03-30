package io.kiw.luxis.web.http.client;

import java.util.Map;

public record HttpClientResponse(int statusCode, String body, Map<String, String> headers) {
}
