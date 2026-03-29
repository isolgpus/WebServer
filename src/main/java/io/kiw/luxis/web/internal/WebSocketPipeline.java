package io.kiw.luxis.web.internal;

import java.util.Map;

public class WebSocketPipeline {
    private final Map<String, SplitBranch<?>> branches;

    public WebSocketPipeline(final Map<String, SplitBranch<?>> branches) {
        this.branches = Map.copyOf(branches);
    }

    public SplitBranch<?> getBranch(final String typeKey) {
        return branches.get(typeKey);
    }
}
