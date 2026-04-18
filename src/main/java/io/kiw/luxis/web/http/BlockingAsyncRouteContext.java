package io.kiw.luxis.web.http;

import io.kiw.luxis.web.http.client.CorrelatedAsync;
import io.kiw.luxis.web.internal.CorrelatedUtil;
import io.kiw.luxis.web.internal.PendingAsyncResponses;

public final class BlockingAsyncRouteContext<IN> extends BlockingRouteContext<IN> {

    private final PendingAsyncResponses pendingAsyncResponses;

    public BlockingAsyncRouteContext(final IN in, final HttpSession http, final PendingAsyncResponses pendingAsyncResponses) {
        super(in, http);
        this.pendingAsyncResponses = pendingAsyncResponses;
    }

    public <T> CorrelatedAsync<T> correlated() {
        return CorrelatedUtil.correlated(pendingAsyncResponses);
    }

}
