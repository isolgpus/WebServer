package io.kiw.luxis.web;

import io.kiw.luxis.web.pipeline.CorruptWebSocketInputStrategy;
import io.kiw.luxis.web.pipeline.DisconnectSession;

public class WebSocketRouteConfigBuilder {

    private CorruptWebSocketInputStrategy corruptInputStrategy = DisconnectSession.INSTANCE;

    public WebSocketRouteConfigBuilder corruptInputStrategy(final CorruptWebSocketInputStrategy corruptInputStrategy) {
        this.corruptInputStrategy = corruptInputStrategy;
        return this;
    }

    public WebSocketRouteConfig build() {
        return new WebSocketRouteConfig(corruptInputStrategy);
    }
}
