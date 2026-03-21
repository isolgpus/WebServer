package io.kiw.luxis.web.test;

import io.kiw.luxis.web.internal.WebSocketRouterWrapper;

public class WebSocketStubRouterWrapper implements WebSocketRouterWrapper {
    @Override
    public void handleBlocking(final Runnable o) {
        o.run();
    }

    @Override
    public void handleOnEventLoop(final Runnable o) {
        o.run();
    }
}
