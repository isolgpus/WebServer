package io.kiw.web.test.handler;

import java.util.OptionalInt;

public class RouteConfigBuilder {
    private OptionalInt timeoutInMillis = OptionalInt.empty();

    public RouteConfigBuilder() {
    }

    public RouteConfigBuilder timeout(int timeoutInMillis) {
        this.timeoutInMillis = OptionalInt.of(timeoutInMillis);
        return this;
    }

    public RouteConfig build() {
        return new RouteConfig(timeoutInMillis);
    }
}
