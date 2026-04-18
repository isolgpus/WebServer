package io.kiw.luxis.web.internal;

import io.kiw.luxis.web.http.client.CorrelatedAsync;

public final class AsyncRouteContext<IN, APP, SESSION> extends RouteContext<IN, APP, SESSION> {

    private final PendingAsyncResponses pendingAsyncResponses;

    public AsyncRouteContext(final IN in, final SESSION session, final APP app, final PendingAsyncResponses pendingAsyncResponses) {
        super(in, session, app);
        this.pendingAsyncResponses = pendingAsyncResponses;
    }

    public <T> CorrelatedAsync<T> correlated() {
        return CorrelatedUtil.correlated(pendingAsyncResponses);
    }
}
