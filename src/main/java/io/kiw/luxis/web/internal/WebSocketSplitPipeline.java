package io.kiw.luxis.web.internal;

import java.util.Map;

public class WebSocketSplitPipeline {
    private final Map<String, SplitBranch<?>> branches;

    public WebSocketSplitPipeline(final Map<String, SplitBranch<?>> branches) {
        this.branches = Map.copyOf(branches);
    }

    public SplitBranch<?> getBranch(final String typeKey) {
        return branches.get(typeKey);
    }
}
