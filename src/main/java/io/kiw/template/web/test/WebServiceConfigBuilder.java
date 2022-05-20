package io.kiw.template.web.test;

import io.kiw.template.web.WebServerConfig;

import java.util.function.Consumer;

public class WebServiceConfigBuilder {
    private int port = 8080;
    private int defaultTimeoutMillis = 30_000;
    private Consumer<Exception> exceptionHandler = (e) -> {};

    public WebServiceConfigBuilder setPort(int port) {
        this.port = port;
        return this;
    }

    public void setExceptionHandler(Consumer<Exception> exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }

    public WebServerConfig build() {
        return new WebServerConfig(port, defaultTimeoutMillis, exceptionHandler);
    }

    public WebServiceConfigBuilder setDefaultBlockingTimeoutMillis(int timeoutMillis) {
        this.defaultTimeoutMillis = timeoutMillis;
        return this;
    }
}
