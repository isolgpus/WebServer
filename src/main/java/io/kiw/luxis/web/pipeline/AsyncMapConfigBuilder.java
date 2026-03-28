package io.kiw.luxis.web.pipeline;

public class AsyncMapConfigBuilder {

    private long timeoutMillis = 30_000;

    public AsyncMapConfigBuilder setTimeoutMillis(final long timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
        return this;
    }

    public AsyncMapConfig build() {
        return new AsyncMapConfig(timeoutMillis);
    }
}
