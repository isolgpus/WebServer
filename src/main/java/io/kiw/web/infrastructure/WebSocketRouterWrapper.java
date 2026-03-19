package io.kiw.web.infrastructure;

public interface WebSocketRouterWrapper {
    void handleBlocking(Runnable o);

    void handleOnEventLoop(Runnable o);
}
