package io.kiw.template.web;

import java.util.function.Consumer;

public class WebServerConfig {
    public final int port;
    public final int defaultTimeoutMillis;
    public final Consumer<Exception> exceptionHandler;

    public WebServerConfig(int port, int defaultTimeoutMillis, Consumer<Exception> exceptionHandler) {

        this.port = port;
        this.defaultTimeoutMillis = defaultTimeoutMillis;
        this.exceptionHandler = exceptionHandler;
    }

}
