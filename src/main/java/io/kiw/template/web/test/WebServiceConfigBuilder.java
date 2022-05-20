package io.kiw.template.web.test;

import io.kiw.template.web.WebServerConfig;

public class WebServiceConfigBuilder {
    private int port = 8080;
    private int defaultTimeoutMillis = 30_000;

    public WebServiceConfigBuilder setPort(int port) {
        this.port = port;
        return this;
    }

    public WebServerConfig build() {
        return new WebServerConfig(port, defaultTimeoutMillis);
    }

    public WebServiceConfigBuilder setDefaultBlockingTimeoutMillis(int timeoutMillis) {
        this.defaultTimeoutMillis = timeoutMillis;
        return this;
    }
}
