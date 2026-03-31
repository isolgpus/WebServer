package io.kiw.luxis.web.http.client;

public record CorrelatedAsync<T>(long correlationId, LuxisAsync<T> async) {
}
