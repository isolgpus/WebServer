package io.kiw.luxis.web.internal;

import io.kiw.luxis.web.http.RequestContext;

public interface ContextHandler {
    void handle(RequestContext context);
}
