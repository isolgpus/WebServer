package io.kiw.luxis.web.test;

import io.kiw.luxis.web.internal.WebSocketRouterWrapper;

public class WebSocketStubRouterWrapper implements WebSocketRouterWrapper {
    @Override
    public void handleBlocking(Runnable o) {
        o.run();
    }

    @Override
    public void handleOnEventLoop(Runnable o) {
        o.run();
    }
}
