package io.kiw.luxis.web.internal;

import io.kiw.luxis.web.http.HttpContext;

public record RouteContext<IN, APP>(IN in, HttpContext http, APP app) {
}
