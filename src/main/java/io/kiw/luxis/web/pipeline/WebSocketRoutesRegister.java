package io.kiw.luxis.web.pipeline;

import io.kiw.luxis.web.http.ErrorMessageResponse;
import io.kiw.luxis.web.internal.PendingAsyncResponses;
import io.kiw.luxis.web.internal.WebSocketPipeline;
import io.kiw.luxis.web.internal.WebSocketRoute;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class WebSocketRoutesRegister<APP, RESP> {
    private final Map<String, WebSocketRoute<?>> routes;
    private final APP applicationState;
    private final PendingAsyncResponses pendingAsyncResponses;
    private final Map<Class<?>, String> responseTypeRegistry;

    public WebSocketRoutesRegister(final APP applicationState, final PendingAsyncResponses pendingAsyncResponses, final LinkedHashMap<String, WebSocketRoute<?>> routes, final Map<Class<?>, String> responseTypeRegistry) {
        this.applicationState = applicationState;
        this.pendingAsyncResponses = pendingAsyncResponses;
        this.routes = routes;
        this.responseTypeRegistry = responseTypeRegistry;
    }

    public <T extends RESP> void registerOutbound(final String typeKey, final Class<T> responseClass) {
        if (responseTypeRegistry.containsValue(typeKey)) {
            throw new IllegalArgumentException("Duplicate response type key: " + typeKey);
        }
        responseTypeRegistry.put(responseClass, typeKey);
    }

    public <IN, OUT> void registerInbound(final String typeKey, final Class<IN> messageType, final WebSocketHandler<IN, OUT, APP, RESP, ErrorMessageResponse> webSocketHandler) {
        if (routes.containsKey(typeKey)) {
            throw new IllegalArgumentException("Duplicate type key: " + typeKey);
        }
        final WebSocketStream<IN, APP, RESP, ErrorMessageResponse> stream = new WebSocketStream<>(new ArrayList<>(), applicationState, pendingAsyncResponses, e -> e);
        final WebSocketPipeline<?> pipeline = webSocketHandler.handle(stream);
        routes.put(typeKey, new WebSocketRoute<>(messageType, pipeline));
    }
}
