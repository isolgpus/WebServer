package io.kiw.luxis.web.pipeline;

import io.kiw.luxis.web.internal.IndividualMessageWebSocketPipeline;
import io.kiw.luxis.web.internal.PendingAsyncResponses;
import io.kiw.luxis.web.internal.WebSocketRoute;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class WebSocketRoutesRegister<APP> {
    private final Map<String, WebSocketRoute<?>> routes;
    private final APP applicationState;
    private final PendingAsyncResponses pendingAsyncResponses;

    public WebSocketRoutesRegister(final APP applicationState, final PendingAsyncResponses pendingAsyncResponses, final LinkedHashMap<String, WebSocketRoute<?>> routes) {
        this.applicationState = applicationState;
        this.pendingAsyncResponses = pendingAsyncResponses;
        this.routes = routes;
    }

    public <IN, OUT> void route(final String typeKey, final Class<IN> messageType, final WebSocketHandler<IN, OUT, APP> webSocketHandler) {
        if (routes.containsKey(typeKey)) {
            throw new IllegalArgumentException("Duplicate type key: " + typeKey);
        }
        final WebSocketStream<IN, APP> stream = new WebSocketStream<>(new ArrayList<>(), applicationState, pendingAsyncResponses);
        final IndividualMessageWebSocketPipeline<?> pipeline = webSocketHandler.handle(stream);
        routes.put(typeKey, new WebSocketRoute<>(messageType, pipeline));
    }
}
