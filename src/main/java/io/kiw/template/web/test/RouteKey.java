package io.kiw.template.web.test;

import io.kiw.template.web.infrastructure.Method;

import java.util.Objects;

public class RouteKey {
    private final String path;
    private final Method method;

    public RouteKey(String path, Method method) {
        this.path = path;
        this.method = method;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RouteKey routeKey = (RouteKey) o;
        return Objects.equals(path, routeKey.path) &&
                method == routeKey.method;
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, method);
    }
}
