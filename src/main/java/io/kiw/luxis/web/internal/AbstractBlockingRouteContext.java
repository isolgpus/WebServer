package io.kiw.luxis.web.internal;

public abstract class AbstractBlockingRouteContext<IN> {
    private final IN in;

    protected AbstractBlockingRouteContext(final IN in) {
        this.in = in;
    }

    public IN in() {
        return in;
    }

}
