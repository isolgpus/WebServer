package io.kiw.template.web.application.routes;

import io.kiw.template.web.test.StubHttpResponse;
import io.kiw.template.web.test.TestApplicationClient;
import io.vertx.core.http.impl.CookieImpl;
import org.junit.Before;
import org.junit.Test;

import static io.kiw.template.web.test.StubHttpResponse.response;
import static io.kiw.template.web.test.StubRequest.request;
import static io.kiw.template.web.test.TestHelper.entry;
import static io.kiw.template.web.test.TestHelper.json;
import static org.junit.Assert.assertEquals;

public class JsonHttpInfrastructureTest {

    TestApplicationClient testApplicationClient;
    public static final String DEFAULT_POST_RESPONSE = json(
            entry("intExample", 0),
            entry("stringExample", null),
            entry("queryExample", null),
            entry("requestHeaderExample", null),
            entry("requestCookieExample", null)
    );

    @Before
    public void setUp() throws Exception {

        testApplicationClient = new TestApplicationClient();
    }

    @Test
    public void shouldHandlePopulatingJsonValues() {
        final String requestBody = json(
                entry("intExample", 17),
                entry("stringExample", "hiya")
        );


        StubHttpResponse response = testApplicationClient.post(request("/echo").body(requestBody));

        final String expectedResponse = json(
                entry("intExample", 17),
                entry("stringExample", "hiya"),
                entry("queryExample", null),
                entry("requestHeaderExample", null),
                entry("requestCookieExample", null)

        );

        assertEquals(
                response(expectedResponse),
                response);
    }

    @Test
    public void shouldReadQueryParamsInPost() {
        StubHttpResponse response = testApplicationClient.post(
                request("/echo")
                        .body("{}")
                        .queryParam("queryExample", "hi"));

        final String expectedResponse = json(
                entry("intExample", 0),
                entry("stringExample", null),
                entry("queryExample", "hi"),
                entry("requestHeaderExample", null),
                entry("requestCookieExample", null)

        );

        assertEquals(
                response(expectedResponse),
                response);
    }

    @Test
    public void shouldReadRequestHeaderParamsOnPost() {
        StubHttpResponse response = testApplicationClient.post(
                request("/echo")
                        .body("{}")
                        .queryParam("queryExample", "hi")
                        .headerParam("requestHeaderExample", "test"));

        final String expectedResponse = json(
                entry("intExample", 0),
                entry("stringExample", null),
                entry("queryExample", "hi"),
                entry("requestHeaderExample", "test"),
                entry("requestCookieExample", null)

        );

        assertEquals(
                response(expectedResponse),
                response);
    }

    @Test
    public void shouldReadRequestHeaderParamsOnGet() {
        StubHttpResponse response = testApplicationClient.get(
                request("/echo")
                        .queryParam("queryExample", null)
                        .headerParam("requestHeaderExample", "test"));

        final String expectedResponse = json(
                entry("intExample", 188),
                entry("stringExample", "You invoked a GET"),
                entry("queryExample", null),
                entry("requestHeaderExample", "test"),
                entry("requestCookieExample", null)

        );

        assertEquals(
                response(expectedResponse),
                response);
    }

    @Test
    public void shouldReadQueryParamsInGet() {
        StubHttpResponse response = testApplicationClient.get(
                request("/echo")
                        .queryParam("queryExample", "hi"));

        final String expectedResponse = json(
                entry("intExample", 188),
                entry("stringExample", "You invoked a GET"),
                entry("queryExample", "hi"),
                entry("requestHeaderExample", null),
                entry("requestCookieExample", null)

        );

        assertEquals(
                response(expectedResponse),
                response);
    }


    @Test
    public void shouldIgnoreWhenClientSendsUnknownValues() {

        final String requestBody = json(
                entry("intExample", 17),
                entry("stringExample", "hiya"),
                entry("something", "else")
        );


        StubHttpResponse response = testApplicationClient.post(
                request("/echo")
                .body(requestBody));

        final String expectedResponse = json(
                entry("intExample", 17),
                entry("stringExample", "hiya"),
                entry("queryExample", null),
                entry("requestHeaderExample", null),
                entry("requestCookieExample", null)

        );

        assertEquals(response(expectedResponse), response);

    }

    @Test
    public void shouldRespondWithErrorNicelyWhenRequestBodyIsNotPresentOnPost() {
        StubHttpResponse response = testApplicationClient.post(request("/echo"));

        final String expectedResponse = json(
                entry("message", "Invalid request")
        );

        assertEquals(response(expectedResponse).withStatusCode(400), response);
    }

    @Test
    public void shouldCallGetRoute() {
        StubHttpResponse response = testApplicationClient.get(request("/echo"));


        final String expectedResponseBody = json(
                entry("intExample", 188),
                entry("stringExample", "You invoked a GET"),
                entry("queryExample", null),
                entry("requestHeaderExample", null),
                entry("requestCookieExample", null)

        );

        assertEquals(response(expectedResponseBody), response);
    }

    @Test
    public void shouldPopulateResponseHeaders() {
        final String request = json(
                entry("responseHeaderExample", "responseTest")
        );

        StubHttpResponse response = testApplicationClient.post(request("/echo")
                .body(request));

        assertEquals(response(DEFAULT_POST_RESPONSE)
                .withHeader("responseHeaderExample", "responseTest"), response);
    }


    @Test
    public void shouldReadRequestCookies() {
        StubHttpResponse response = testApplicationClient.post(
                request("/echo")
                        .body("{}")
                        .cookie("requestCookieExample", "cookietest"));

        String expectedResponse = json(
                entry("intExample", 0),
                entry("stringExample", null),
                entry("queryExample", null),
                entry("requestHeaderExample", null),
                entry("requestCookieExample", "cookietest")
        );
        assertEquals(response(expectedResponse), response);
    }

    @Test
    public void shouldPopulateResponseCookie() {
        StubHttpResponse response = testApplicationClient.post(
                request("/echo")
                        .body(json(entry("responseCookieExample", "responseCookieTest"))));

        assertEquals(response(DEFAULT_POST_RESPONSE)
                .withCookie("responseCookieExample", new CookieImpl("responseCookieExample", "responseCookieTest")), response);
    }
}
