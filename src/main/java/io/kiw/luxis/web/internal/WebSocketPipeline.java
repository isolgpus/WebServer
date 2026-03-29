package io.kiw.luxis.web.internal;

import java.util.Map;

public class WebSocketPipeline {
    private final Map<String, WebSocketRoute<?>> branches;

    public WebSocketPipeline(final Map<String, WebSocketRoute<?>> branches) {
        this.branches = Map.copyOf(branches);
    }

}
