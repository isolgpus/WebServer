package io.kiw.web.test.handler;

import io.kiw.web.pipeline.*;
import io.kiw.web.handler.*;
import io.kiw.web.http.*;
import io.kiw.web.validation.*;
import io.kiw.web.websocket.*;
import io.kiw.web.internal.*;
import io.kiw.web.jwt.*;
import io.kiw.web.cors.*;

import io.kiw.web.openapi.OpenApiMetadataBuilder;

import java.util.Optional;
import java.util.OptionalInt;

public class RouteConfigBuilder {
    private OptionalInt timeoutInMillis = OptionalInt.empty();
    private OpenApiMetadataBuilder openApiBuilder = null;

    public RouteConfigBuilder() {
    }

    public RouteConfigBuilder timeout(int timeoutInMillis) {
        this.timeoutInMillis = OptionalInt.of(timeoutInMillis);
        return this;
    }

    public OpenApiMetadataBuilder openApi() {
        if (openApiBuilder == null) {
            openApiBuilder = new OpenApiMetadataBuilder(this);
        }
        return openApiBuilder;
    }

    public RouteConfig build() {
        return new RouteConfig(
            timeoutInMillis,
            openApiBuilder != null ? Optional.of(openApiBuilder.buildMetadata()) : Optional.empty()
        );
    }
}
