package io.kiw.luxis.web.test;

import io.kiw.luxis.web.http.Method;

public record RouteKey(String path, Method method) {
}
