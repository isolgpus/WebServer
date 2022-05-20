package io.kiw.template.web;

public class WebServerConfig {
    public final int port;
    public final int defaultTimeoutMillis;

    public WebServerConfig(int port, int defaultTimeoutMillis) {

        this.port = port;
        this.defaultTimeoutMillis = defaultTimeoutMillis;
    }

}
