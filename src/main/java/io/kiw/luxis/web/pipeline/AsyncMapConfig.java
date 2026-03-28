package io.kiw.luxis.web.pipeline;

public class AsyncMapConfig {

    private static final AsyncMapConfig DEFAULT = new AsyncMapConfigBuilder().build();

    final long timeoutMillis;

    AsyncMapConfig(final long timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
    }

    static AsyncMapConfig defaultConfig() {
        return DEFAULT;
    }
}
