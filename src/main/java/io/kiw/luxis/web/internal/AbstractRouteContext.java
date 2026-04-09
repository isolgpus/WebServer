package io.kiw.luxis.web.internal;

public abstract class AbstractRouteContext<IN, APP> {
    private final IN in;
    private final APP app;

    protected AbstractRouteContext(final IN in, final APP app) {
        this.in = in;
        this.app = app;
    }

    public IN in() {
        return in;
    }

    public APP app() {
        return app;
    }

}
