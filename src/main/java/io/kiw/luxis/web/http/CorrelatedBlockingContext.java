package io.kiw.luxis.web.http;

public record CorrelatedBlockingContext<IN>(long correlationId, IN in, HttpContext http) {
}
