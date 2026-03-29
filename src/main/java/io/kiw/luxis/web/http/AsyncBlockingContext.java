package io.kiw.luxis.web.http;

public record AsyncBlockingContext<IN>(long correlationId, IN in, HttpContext http) {
}
