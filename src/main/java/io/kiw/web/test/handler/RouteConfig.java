package io.kiw.web.test.handler;

import io.kiw.web.pipeline.*;
import io.kiw.web.handler.*;
import io.kiw.web.http.*;
import io.kiw.web.validation.*;
import io.kiw.web.websocket.*;
import io.kiw.web.internal.*;
import io.kiw.web.jwt.*;
import io.kiw.web.cors.*;

import io.kiw.web.openapi.OpenApiMetadata;

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
