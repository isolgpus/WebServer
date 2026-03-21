package io.kiw.web.http;

public enum Method {
    POST(true),
    PUT(true),
    GET(false),
    DELETE(true),
    PATCH(true),
    OPTIONS(false);

    private final boolean requiresBody;

    Method(boolean requiresBody) {
        this.requiresBody = requiresBody;
    }

    public boolean canHaveABody() {
        return requiresBody;
    }
}
