package io.kiw.web.infrastructure.cors;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class CorsConfigBuilder {
    private final Set<String> allowedOrigins = new LinkedHashSet<>();
    private final Set<String> allowedMethods = new LinkedHashSet<>();
    private final Set<String> allowedHeaders = new LinkedHashSet<>();
    private final Set<String> exposedHeaders = new LinkedHashSet<>();
    private boolean allowCredentials = false;
    private int maxAgeSeconds = -1;

    public CorsConfigBuilder allowOrigin(String origin) {
        this.allowedOrigins.add(origin);
        return this;
    }

    public CorsConfigBuilder allowMethod(String method) {
        this.allowedMethods.add(method);
        return this;
    }

    public CorsConfigBuilder allowHeader(String header) {
        this.allowedHeaders.add(header);
        return this;
    }

    public CorsConfigBuilder exposeHeader(String header) {
        this.exposedHeaders.add(header);
        return this;
    }

    public CorsConfigBuilder allowCredentials(boolean allowCredentials) {
        this.allowCredentials = allowCredentials;
        return this;
    }

    public CorsConfigBuilder maxAgeSeconds(int maxAgeSeconds) {
        this.maxAgeSeconds = maxAgeSeconds;
        return this;
    }

    public CorsConfig build() {
        return new CorsConfig(
            new LinkedHashSet<>(allowedOrigins),
            new LinkedHashSet<>(allowedMethods),
            new LinkedHashSet<>(allowedHeaders),
            new LinkedHashSet<>(exposedHeaders),
            allowCredentials,
            maxAgeSeconds
        );
    }
}
