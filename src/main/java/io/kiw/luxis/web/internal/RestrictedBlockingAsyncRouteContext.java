package io.kiw.luxis.web.internal;

import io.kiw.luxis.web.http.client.CorrelatedAsync;

public final class RestrictedBlockingAsyncRouteContext<IN> extends RestrictedBlockingRouteContext<IN> {

    private final PendingAsyncResponses pendingAsyncResponses;

    public RestrictedBlockingAsyncRouteContext(final IN in, final PendingAsyncResponses pendingAsyncResponses) {
        super(in);
        this.pendingAsyncResponses = pendingAsyncResponses;
    }

    public <T> CorrelatedAsync<T> correlated() {
        return CorrelatedUtil.correlated(pendingAsyncResponses);
    }
}
