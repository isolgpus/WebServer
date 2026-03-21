package io.kiw.luxis.web.test.handler;

import io.kiw.luxis.web.pipeline.*;
import io.kiw.luxis.web.handler.*;
import io.kiw.luxis.web.http.*;
import io.kiw.luxis.web.validation.*;
import io.kiw.luxis.web.websocket.*;
import io.kiw.luxis.web.internal.*;
import io.kiw.luxis.web.jwt.*;
import io.kiw.luxis.web.cors.*;

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
