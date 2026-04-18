package io.kiw.luxis.web.internal;

import io.kiw.luxis.web.http.HttpErrorResponse;
import io.kiw.luxis.web.http.client.CorrelatedAsync;

import java.util.function.Function;

public final class AsyncRouteContext<IN, APP, SESSION, ERR> extends RouteContext<IN, APP, SESSION> {

    private final PendingAsyncResponses pendingAsyncResponses;
    private final Function<HttpErrorResponse, ERR> errorMapper;

    public AsyncRouteContext(final IN in, final SESSION session, final APP app, final PendingAsyncResponses pendingAsyncResponses, final Function<HttpErrorResponse, ERR> errorMapper) {
        super(in, session, app);
        this.pendingAsyncResponses = pendingAsyncResponses;
        this.errorMapper = errorMapper;
    }

    public <T> CorrelatedAsync<T, ERR> correlated() {
        return CorrelatedUtil.correlated(pendingAsyncResponses, errorMapper);
    }
}
