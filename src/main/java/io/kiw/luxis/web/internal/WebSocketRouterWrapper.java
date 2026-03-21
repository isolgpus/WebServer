package io.kiw.luxis.web.internal;

public interface WebSocketRouterWrapper {
    void handleBlocking(Runnable o);

    void handleOnEventLoop(Runnable o);
}
