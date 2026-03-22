package io.kiw.luxis.web;

import io.kiw.luxis.web.pipeline.CorruptWebSocketInputStrategy;

public record WebSocketRouteConfig(CorruptWebSocketInputStrategy corruptInputStrategy) {
}
