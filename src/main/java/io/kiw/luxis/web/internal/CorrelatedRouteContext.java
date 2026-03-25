package io.kiw.luxis.web.internal;

import io.kiw.luxis.web.http.HttpContext;

public record CorrelatedRouteContext<IN, APP>(long correlationId, IN in, HttpContext http, APP app) {
}
