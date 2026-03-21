package io.kiw.web.internal.ender;

import io.kiw.web.http.RequestContext;

public sealed interface Ender permits FileEnder, JsonEnder {

    <T> void end(RequestContext requestContext, T value);
}
