package io.kiw.luxis.web.internal;

import io.kiw.luxis.web.db.DatabaseClient;
import io.kiw.luxis.web.db.DbAccessor;
import io.kiw.luxis.web.http.HttpErrorResponse;
import io.kiw.luxis.web.http.client.CorrelatedAsync;

import java.util.function.Function;

public final class AsyncRouteContext<IN, APP, SESSION, ERR> extends RouteContext<IN, APP, SESSION> {

    private final PendingAsyncResponses pendingAsyncResponses;
    private final Function<HttpErrorResponse, ERR> errorMapper;
    private final DatabaseClient<?, ?, ?> databaseClient;

    public AsyncRouteContext(final IN in, final SESSION session, final APP app, final PendingAsyncResponses pendingAsyncResponses, final Function<HttpErrorResponse, ERR> errorMapper) {
        this(in, session, app, pendingAsyncResponses, errorMapper, null);
    }

    public AsyncRouteContext(final IN in, final SESSION session, final APP app, final PendingAsyncResponses pendingAsyncResponses, final Function<HttpErrorResponse, ERR> errorMapper, final DatabaseClient<?, ?, ?> databaseClient) {
        super(in, session, app);
        this.pendingAsyncResponses = pendingAsyncResponses;
        this.errorMapper = errorMapper;
        this.databaseClient = databaseClient;
    }

    public <T> CorrelatedAsync<T, ERR> correlated() {
        return CorrelatedUtil.correlated(pendingAsyncResponses, errorMapper);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public <ROW, KEY> DbAccessor<ROW, KEY> db() {
        if (databaseClient == null) {
            return null;
        }
        return new DbAccessor<>((DatabaseClient) databaseClient, null);
    }
}
