package io.kiw.web;

import java.util.OptionalLong;
import java.util.function.Consumer;

public class WebServiceConfigBuilder {
    private int port = 8080;
    private int defaultTimeoutMillis = 30_000;
    private Consumer<Exception> exceptionHandler = (e) -> {};
    private OptionalLong maxBodySize = OptionalLong.empty();

    public WebServiceConfigBuilder setPort(int port) {
        this.port = port;
        return this;
    }

    public WebServiceConfigBuilder setExceptionHandler(Consumer<Exception> exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
        return this;
    }

    public WebServerConfig build() {
        return new WebServerConfig(port, defaultTimeoutMillis, exceptionHandler, maxBodySize);
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
