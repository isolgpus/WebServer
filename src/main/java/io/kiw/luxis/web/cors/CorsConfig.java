package io.kiw.luxis.web.cors;

import java.util.Collections;
import java.util.Set;

public class CorsConfig {
    final Set<String> allowedOrigins;
    final Set<String> allowedMethods;
    final Set<String> allowedHeaders;
    final Set<String> exposedHeaders;
    final boolean allowCredentials;
    final int maxAgeSeconds;

    CorsConfig(final Set<String> allowedOrigins, final Set<String> allowedMethods, final Set<String> allowedHeaders,
               final Set<String> exposedHeaders, final boolean allowCredentials, final int maxAgeSeconds) {
        this.allowedOrigins = Collections.unmodifiableSet(allowedOrigins);
        this.allowedMethods = Collections.unmodifiableSet(allowedMethods);
        this.allowedHeaders = Collections.unmodifiableSet(allowedHeaders);
        this.exposedHeaders = Collections.unmodifiableSet(exposedHeaders);
        this.allowCredentials = allowCredentials;
        this.maxAgeSeconds = maxAgeSeconds;
    }

    public Set<String> getAllowedOrigins() {
        return allowedOrigins;
    }

    public Set<String> getAllowedMethods() {
        return allowedMethods;
    }

    public Set<String> getAllowedHeaders() {
        return allowedHeaders;
    }

    public Set<String> getExposedHeaders() {
        return exposedHeaders;
    }

    public boolean isAllowCredentials() {
        return allowCredentials;
    }

    public int getMaxAgeSeconds() {
        return maxAgeSeconds;
    }

}
