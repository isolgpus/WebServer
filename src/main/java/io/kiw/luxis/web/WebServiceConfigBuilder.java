package io.kiw.luxis.web;

import io.kiw.luxis.web.internal.*;
import io.kiw.luxis.web.handler.*;
import io.kiw.luxis.web.http.*;
import io.kiw.luxis.web.cors.*;
import io.kiw.luxis.web.jwt.*;
import io.kiw.luxis.web.openapi.*;

import io.kiw.luxis.web.cors.CorsConfig;

import java.util.Optional;
import java.util.OptionalLong;
import java.util.function.Consumer;

public class WebServiceConfigBuilder {
    private int port = 8080;
    private int defaultTimeoutMillis = 30_000;
    private Consumer<Exception> exceptionHandler = (e) -> {};
    private OptionalLong maxBodySize = OptionalLong.empty();
    private Optional<CorsConfig> corsConfig = Optional.empty();

    public WebServiceConfigBuilder setPort(int port) {
        this.port = port;
        return this;
    }

    public WebServiceConfigBuilder setExceptionHandler(Consumer<Exception> exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
        return this;
    }

    public WebServiceConfigBuilder setCorsConfig(CorsConfig corsConfig) {
        this.corsConfig = Optional.of(corsConfig);
        return this;
    }

    public WebServerConfig build() {
        return new WebServerConfig(port, defaultTimeoutMillis, exceptionHandler, maxBodySize, corsConfig);
    }

    public WebServiceConfigBuilder setDefaultBlockingTimeoutMillis(int timeoutMillis) {
        this.defaultTimeoutMillis = timeoutMillis;
        return this;
    }

    public WebServiceConfigBuilder setMaxBodySize(long maxBodySize) {
        this.maxBodySize = OptionalLong.of(maxBodySize);
        return this;
    }
}
