package io.kiw.web.test.handler;

public class EchoResponse {
    public final int intExample;
    public final String stringExample;
    public final String pathExample;
    public final String queryExample;
    public final String requestHeaderExample;
    public final String requestCookieExample;

    public EchoResponse(int intExample, String stringExample, String pathExample, String queryExample, String requestHeaderExample, String requestCookieExample) {

        this.intExample = intExample;
        this.stringExample = stringExample;
        this.pathExample = pathExample;
        this.queryExample = queryExample;
        this.requestHeaderExample = requestHeaderExample;
        this.requestCookieExample = requestCookieExample;
    }
}
