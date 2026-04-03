package io.kiw.luxis.web.websocket;

import io.kiw.luxis.web.http.client.CorrelatedAsync;
import io.kiw.luxis.web.internal.CorrelatedUtil;
import io.kiw.luxis.web.internal.PendingAsyncResponses;

public final class WebSocketAsyncContext<IN, APP, RESP> extends WebSocketContext<IN, APP, RESP> {


    private final PendingAsyncResponses pendingAsyncResponses;

    public WebSocketAsyncContext(final IN in, final WebSocketSession<RESP> connection, final APP app, final PendingAsyncResponses pendingAsyncResponses) {
        super(in, connection, app);
        this.pendingAsyncResponses = pendingAsyncResponses;
    }

    public <T> CorrelatedAsync<T> correlated() {
        return CorrelatedUtil.correlated(pendingAsyncResponses);

    }
}
