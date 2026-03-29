package io.kiw.luxis.web.pipeline;

import io.kiw.luxis.web.internal.PendingAsyncResponses;
import io.kiw.luxis.web.internal.SplitBranch;
import io.kiw.luxis.web.internal.IndividualMessageWebSocketPipeline;
import io.kiw.luxis.web.internal.WebSocketPipeline;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

public class WebSocketSplitStream<SPLIT, APP> {
    private final Map<String, SplitBranch<?>> branches = new LinkedHashMap<>();
    private final APP applicationState;
    private final PendingAsyncResponses pendingAsyncResponses;

    public WebSocketSplitStream(final APP applicationState, final PendingAsyncResponses pendingAsyncResponses) {
        this.applicationState = applicationState;
        this.pendingAsyncResponses = pendingAsyncResponses;
    }

    public <IN extends SPLIT> WebSocketSplitStream<SPLIT, APP> on(final String typeKey, final Class<IN> messageType, final Function<WebSocketStream<IN, APP>, IndividualMessageWebSocketPipeline<?>> pipelineBuilder) {
        if (branches.containsKey(typeKey)) {
            throw new IllegalArgumentException("Duplicate type key: " + typeKey);
        }
        final WebSocketStream<IN, APP> stream = new WebSocketStream<>(new ArrayList<>(), applicationState, pendingAsyncResponses);
        final IndividualMessageWebSocketPipeline<?> pipeline = pipelineBuilder.apply(stream);
        branches.put(typeKey, new SplitBranch<>(messageType, pipeline));
        return this;
    }

    public WebSocketPipeline build() {
        if (branches.isEmpty()) {
            throw new IllegalStateException("At least one branch must be registered");
        }
        return new WebSocketPipeline(branches);
    }
}
