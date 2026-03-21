package io.kiw.web.test;

import io.kiw.web.pipeline.*;
import io.kiw.web.handler.*;
import io.kiw.web.http.*;
import io.kiw.web.validation.*;
import io.kiw.web.websocket.*;
import io.kiw.web.internal.*;
import io.kiw.web.jwt.*;
import io.kiw.web.cors.*;
import io.kiw.web.openapi.*;

import io.kiw.web.http.Method;

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
