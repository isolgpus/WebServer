package io.kiw.web;

import io.kiw.web.internal.*;
import io.kiw.web.handler.*;
import io.kiw.web.http.*;
import io.kiw.web.cors.*;
import io.kiw.web.jwt.*;
import io.kiw.web.openapi.*;

import io.kiw.web.cors.CorsConfig;

import java.util.Optional;
import java.util.OptionalLong;
import java.util.function.Consumer;

public class WebServerConfig {
    final int port;
    final int defaultTimeoutMillis;
    final Consumer<Exception> exceptionHandler;
    final OptionalLong maxBodySize;
    final Optional<CorsConfig> corsConfig;

    WebServerConfig(int port, int defaultTimeoutMillis, Consumer<Exception> exceptionHandler, OptionalLong maxBodySize, Optional<CorsConfig> corsConfig) {

        this.port = port;
        this.defaultTimeoutMillis = defaultTimeoutMillis;
        this.exceptionHandler = exceptionHandler;
        this.maxBodySize = maxBodySize;
        this.corsConfig = corsConfig;
    }

}
