package io.kiw.luxis.web.test;

import io.kiw.luxis.web.http.Method;

import java.util.Objects;

public class RouteKey {
    private final String path;
    private final Method method;

    public RouteKey(final String path, final Method method) {
        this.path = path;
        this.method = method;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final RouteKey routeKey = (RouteKey) o;
        return Objects.equals(path, routeKey.path) &&
                method == routeKey.method;
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, method);
    }
}
