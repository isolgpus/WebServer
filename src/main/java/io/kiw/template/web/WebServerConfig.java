package io.kiw.template.web;

import java.util.OptionalLong;
import java.util.function.Consumer;

public class WebServerConfig {
    final int port;
    final int defaultTimeoutMillis;
    final Consumer<Exception> exceptionHandler;
    final OptionalLong maxBodySize;

    WebServerConfig(int port, int defaultTimeoutMillis, Consumer<Exception> exceptionHandler, OptionalLong maxBodySize) {

        this.port = port;
        this.defaultTimeoutMillis = defaultTimeoutMillis;
        this.exceptionHandler = exceptionHandler;
        this.maxBodySize = maxBodySize;
    }

}
