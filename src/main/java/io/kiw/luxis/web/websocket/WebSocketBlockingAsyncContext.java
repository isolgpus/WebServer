package io.kiw.luxis.web.websocket;

import io.kiw.luxis.web.http.client.CorrelatedAsync;
import io.kiw.luxis.web.internal.CorrelatedUtil;
import io.kiw.luxis.web.internal.PendingAsyncResponses;

public final class WebSocketBlockingAsyncContext<IN> extends WebSocketBlockingContext<IN> {
    private final PendingAsyncResponses pendingAsyncResponses;

    public WebSocketBlockingAsyncContext(final IN in, final PendingAsyncResponses pendingAsyncResponses) {
        super(in);
        this.pendingAsyncResponses = pendingAsyncResponses;
    }


    public <T> CorrelatedAsync<T> correlated() {
        return CorrelatedUtil.correlated(pendingAsyncResponses);
    }
}
