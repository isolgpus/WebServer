package io.kiw.luxis.web.test.handler;

import io.kiw.luxis.web.openapi.OpenApiMetadata;

import java.util.Optional;
import java.util.OptionalInt;

public class RouteConfig {
    public final OptionalInt timeoutInMillis;
    public final Optional<OpenApiMetadata> openApiMetadata;

    public RouteConfig(final OptionalInt timeoutInMillis) {
        this(timeoutInMillis, Optional.empty());
    }

    public RouteConfig(final OptionalInt timeoutInMillis, final Optional<OpenApiMetadata> openApiMetadata) {
        this.timeoutInMillis = timeoutInMillis;
        this.openApiMetadata = openApiMetadata;
    }
}
