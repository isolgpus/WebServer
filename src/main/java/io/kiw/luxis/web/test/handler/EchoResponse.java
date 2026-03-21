package io.kiw.luxis.web.test.handler;

public class EchoResponse {
    public final int intExample;
    public final String stringExample;
    public final String pathExample;
    public final String queryExample;
    public final String requestHeaderExample;
    public final String requestCookieExample;

    public EchoResponse(final int intExample, final String stringExample, final String pathExample, final String queryExample, final String requestHeaderExample, final String requestCookieExample) {

        this.intExample = intExample;
        this.stringExample = stringExample;
        this.pathExample = pathExample;
        this.queryExample = queryExample;
        this.requestHeaderExample = requestHeaderExample;
        this.requestCookieExample = requestCookieExample;
    }
}
