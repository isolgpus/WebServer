package io.kiw.web.internal.ender;

import io.kiw.web.http.VertxContext;

public sealed interface Ender permits FileEnder, JsonEnder {

    <T> void end(VertxContext vertxContext, T value);
}
