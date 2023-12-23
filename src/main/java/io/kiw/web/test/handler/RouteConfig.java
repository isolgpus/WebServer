package io.kiw.web.test.handler;

import java.util.OptionalInt;

public class RouteConfig {
    public final OptionalInt timeoutInMillis;

    public RouteConfig(final OptionalInt timeoutInMillis) {

        this.timeoutInMillis = timeoutInMillis;
    }
}
