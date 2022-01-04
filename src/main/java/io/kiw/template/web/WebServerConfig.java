package io.kiw.template.web;

import javax.xml.parsers.DocumentBuilder;
import java.util.function.Consumer;

public class WebServerConfig {
    private int port = 8080;
    private Consumer<Throwable> errorHandler = (t) -> {};

    public int getPort() {
        return port;
    }

    public WebServerConfig setPort(int port) {
        this.port = port;
        return this;
    }

    public WebServerConfig setErrorHandler(Consumer<Throwable> t) {
        this.errorHandler = t;
        return this;
    }

    public Consumer<Throwable> getErrorHandler() {
        return this.errorHandler;
    }
}
