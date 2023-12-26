package io.kiw.web.infrastructure.ender;

import io.kiw.web.infrastructure.VertxContext;

public sealed interface Ender permits FileEnder, JsonEnder {

    <T> void end(VertxContext vertxContext, T value);
}
