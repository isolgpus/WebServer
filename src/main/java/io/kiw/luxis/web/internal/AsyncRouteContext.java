package io.kiw.luxis.web.internal;

import io.kiw.luxis.web.http.HttpSession;
import io.kiw.luxis.web.http.client.CorrelatedAsync;

public final class AsyncRouteContext<IN, APP> extends RouteContext<IN, APP> {

    private final PendingAsyncResponses pendingAsyncResponses;

    public AsyncRouteContext(final IN in, final HttpSession http, final APP app, final PendingAsyncResponses pendingAsyncResponses) {
        super(in, http, app);
        this.pendingAsyncResponses = pendingAsyncResponses;
    }

    public <T> CorrelatedAsync<T> correlated() {
        return CorrelatedUtil.correlated(pendingAsyncResponses);
    }

}
