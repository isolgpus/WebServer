package io.kiw.web;

import io.kiw.web.infrastructure.cors.CorsConfig;

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
