package io.kiw.web.test;

import io.kiw.web.infrastructure.WebSocketRouterWrapper;

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
