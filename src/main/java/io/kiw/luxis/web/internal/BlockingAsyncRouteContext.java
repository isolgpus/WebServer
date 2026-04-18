package io.kiw.luxis.web.internal;

import io.kiw.luxis.web.http.client.CorrelatedAsync;

public final class BlockingAsyncRouteContext<IN, SESSION> extends BlockingRouteContext<IN, SESSION> {

    private final PendingAsyncResponses pendingAsyncResponses;

    public BlockingAsyncRouteContext(final IN in, final SESSION session, final PendingAsyncResponses pendingAsyncResponses) {
        super(in, session);
        this.pendingAsyncResponses = pendingAsyncResponses;
    }

    public <T> CorrelatedAsync<T> correlated() {
        return CorrelatedUtil.correlated(pendingAsyncResponses);
    }
}
