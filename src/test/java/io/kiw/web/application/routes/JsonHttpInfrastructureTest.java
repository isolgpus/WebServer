package io.kiw.web.application.routes;

import io.kiw.web.test.StubHttpResponse;
import io.kiw.web.test.TestApplicationClient;
import io.kiw.web.test.StubRequest;
import io.kiw.web.test.TestHelper;
import io.vertx.core.http.impl.CookieImpl;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class JsonHttpInfrastructureTest {

    private TestApplicationClient testApplicationClient;
    private static final String DEFAULT_POST_RESPONSE = TestHelper.json(
            TestHelper.entry("intExample", 0),
            TestHelper.entry("stringExample", null),
            TestHelper.entry("queryExample", null),
            TestHelper.entry("requestHeaderExample", null),
            TestHelper.entry("requestCookieExample", null)
    );

    @Before
    public void setUp() throws Exception {

        testApplicationClient = new TestApplicationClient();
    }

    @After
    public void tearDown() throws Exception {
        testApplicationClient.assertNoMoreExceptions();
    }

    @Test
    public void shouldHandlePopulatingJsonValues() {
        final String requestBody = TestHelper.json(
                TestHelper.entry("intExample", 17),
                TestHelper.entry("stringExample", "hiya")
        );


        StubHttpResponse response = testApplicationClient.post(StubRequest.request("/echo").body(requestBody));

        final String expectedResponse = TestHelper.json(
                TestHelper.entry("intExample", 17),
                TestHelper.entry("stringExample", "hiya"),
                TestHelper.entry("queryExample", null),
                TestHelper.entry("requestHeaderExample", null),
                TestHelper.entry("requestCookieExample", null)

        );

        Assert.assertEquals(
                StubHttpResponse.response(expectedResponse),
                response);
    }

    @Test
    public void shouldReadQueryParamsInPost() {
        StubHttpResponse response = testApplicationClient.put(
                StubRequest.request("/echo")
                        .body("{}")
                        .queryParam("queryExample", "hi"));

        final String expectedResponse = TestHelper.json(
                TestHelper.entry("intExample", 0),
                TestHelper.entry("stringExample", null),
                TestHelper.entry("queryExample", "hi"),
                TestHelper.entry("requestHeaderExample", null),
                TestHelper.entry("requestCookieExample", null)

        );

        Assert.assertEquals(
                StubHttpResponse.response(expectedResponse),
                response);
    }

    @Test
    public void shouldReadRequestHeaderParamsOnPost() {
        StubHttpResponse response = testApplicationClient.post(
                StubRequest.request("/echo")
                        .body("{}")
                        .queryParam("queryExample", "hi")
                        .headerParam("requestHeaderExample", "test"));

        final String expectedResponse = TestHelper.json(
                TestHelper.entry("intExample", 0),
                TestHelper.entry("stringExample", null),
                TestHelper.entry("queryExample", "hi"),
                TestHelper.entry("requestHeaderExample", "test"),
                TestHelper.entry("requestCookieExample", null)

        );

        Assert.assertEquals(
                StubHttpResponse.response(expectedResponse),
                response);
    }

    @Test
    public void shouldReadRequestHeaderParamsOnPut() {
        StubHttpResponse response = testApplicationClient.put(
            StubRequest.request("/echo")
                .body("{}")
                .queryParam("queryExample", "hi")
                .headerParam("requestHeaderExample", "test"));

        final String expectedResponse = TestHelper.json(
            TestHelper.entry("intExample", 0),
            TestHelper.entry("stringExample", null),
            TestHelper.entry("queryExample", "hi"),
            TestHelper.entry("requestHeaderExample", "test"),
            TestHelper.entry("requestCookieExample", null)

        );

        Assert.assertEquals(
            StubHttpResponse.response(expectedResponse),
            response);
    }

    @Test
    public void shouldReadRequestHeaderParamsOnDelete() {
        StubHttpResponse response = testApplicationClient.delete(
            StubRequest.request("/echo")
                .body("{}")
                .queryParam("queryExample", "hi")
                .headerParam("requestHeaderExample", "test"));

        final String expectedResponse = TestHelper.json(
            TestHelper.entry("intExample", 0),
            TestHelper.entry("stringExample", null),
            TestHelper.entry("queryExample", "hi"),
            TestHelper.entry("requestHeaderExample", "test"),
            TestHelper.entry("requestCookieExample", null)

        );

        Assert.assertEquals(
            StubHttpResponse.response(expectedResponse),
            response);
    }

    @Test
    public void shouldReadRequestHeaderParamsOnGet() {
        StubHttpResponse response = testApplicationClient.get(
            StubRequest.request("/echo")
                .queryParam("queryExample", null)
                .headerParam("requestHeaderExample", "test"));

        final String expectedResponse = TestHelper.json(
            TestHelper.entry("intExample", 188),
                TestHelper.entry("stringExample", "You invoked a GET"),
                TestHelper.entry("queryExample", null),
                TestHelper.entry("requestHeaderExample", "test"),
                TestHelper.entry("requestCookieExample", null)

        );

        Assert.assertEquals(
                StubHttpResponse.response(expectedResponse),
                response);
    }

    @Test
    public void shouldReadQueryParamsInGet() {
        StubHttpResponse response = testApplicationClient.get(
                StubRequest.request("/echo")
                        .queryParam("queryExample", "hi"));

        final String expectedResponse = TestHelper.json(
                TestHelper.entry("intExample", 188),
                TestHelper.entry("stringExample", "You invoked a GET"),
                TestHelper.entry("queryExample", "hi"),
                TestHelper.entry("requestHeaderExample", null),
                TestHelper.entry("requestCookieExample", null)

        );

        Assert.assertEquals(
                StubHttpResponse.response(expectedResponse),
                response);
    }


    @Test
    public void shouldIgnoreWhenClientSendsUnknownValues() {

        final String requestBody = TestHelper.json(
                TestHelper.entry("intExample", 17),
                TestHelper.entry("stringExample", "hiya"),
                TestHelper.entry("something", "else")
        );


        StubHttpResponse response = testApplicationClient.post(
                StubRequest.request("/echo")
                .body(requestBody));

        final String expectedResponse = TestHelper.json(
                TestHelper.entry("intExample", 17),
                TestHelper.entry("stringExample", "hiya"),
                TestHelper.entry("queryExample", null),
                TestHelper.entry("requestHeaderExample", null),
                TestHelper.entry("requestCookieExample", null)

        );

        Assert.assertEquals(StubHttpResponse.response(expectedResponse), response);

    }

    @Test
    public void shouldRespondWithErrorNicelyWhenRequestBodyIsNotPresentOnPost() {
        StubHttpResponse response = testApplicationClient.post(StubRequest.request("/echo"));

        final String expectedResponse = TestHelper.json(
            TestHelper.entry("message", "Invalid json request"),
            TestHelper.entry("errors", TestHelper.object())
        );

        Assert.assertEquals(StubHttpResponse.response(expectedResponse).withStatusCode(400), response);
    }

    @Test
    public void shouldCallGetRoute() {
        StubHttpResponse response = testApplicationClient.get(StubRequest.request("/echo"));


        final String expectedResponseBody = TestHelper.json(
                TestHelper.entry("intExample", 188),
                TestHelper.entry("stringExample", "You invoked a GET"),
                TestHelper.entry("queryExample", null),
                TestHelper.entry("requestHeaderExample", null),
                TestHelper.entry("requestCookieExample", null)

        );

        Assert.assertEquals(StubHttpResponse.response(expectedResponseBody), response);
    }

    @Test
    public void shouldPopulateResponseHeaders() {
        final String request = TestHelper.json(
                TestHelper.entry("responseHeaderExample", "responseTest")
        );

        StubHttpResponse response = testApplicationClient.post(StubRequest.request("/echo")
                .body(request));

        Assert.assertEquals(StubHttpResponse.response(DEFAULT_POST_RESPONSE)
                .withHeader("responseHeaderExample", "responseTest"), response);
    }


    @Test
    public void shouldReadRequestCookies() {
        StubHttpResponse response = testApplicationClient.post(
                StubRequest.request("/echo")
                        .body("{}")
                        .cookie("requestCookieExample", "cookietest"));

        String expectedResponse = TestHelper.json(
                TestHelper.entry("intExample", 0),
                TestHelper.entry("stringExample", null),
                TestHelper.entry("queryExample", null),
                TestHelper.entry("requestHeaderExample", null),
                TestHelper.entry("requestCookieExample", "cookietest")
        );
        Assert.assertEquals(StubHttpResponse.response(expectedResponse), response);
    }

    @Test
    public void shouldPopulateResponseCookie() {
        StubHttpResponse response = testApplicationClient.post(
                StubRequest.request("/echo")
                        .body(TestHelper.json(TestHelper.entry("responseCookieExample", "responseCookieTest"))));

        Assert.assertEquals(StubHttpResponse.response(DEFAULT_POST_RESPONSE)
                .withCookie(new CookieImpl("responseCookieExample", "responseCookieTest")), response);
    }

    @Test
    public void shouldMapThroughABlockingCall() {
        StubHttpResponse response = testApplicationClient.post(
            StubRequest.request("/blocking")
                .body(TestHelper.json(TestHelper.entry("numberToMultiply", 22))));

        Assert.assertEquals(StubHttpResponse.response(TestHelper.json(TestHelper.entry("multipliedNumber", 44))), response);
    }

    @Test
    public void shouldMapThroughABlockingCompleteCall() {
        StubHttpResponse response = testApplicationClient.post(
                StubRequest.request("/blockingComplete")
                        .body(TestHelper.json(TestHelper.entry("numberToMultiply", 22))));

        Assert.assertEquals(StubHttpResponse.response(TestHelper.json(TestHelper.entry("multipliedNumber", 44))), response);
    }

    @Test
    public void shouldReturnWithErrorOnBadRequest() {
        StubHttpResponse response = testApplicationClient.post(
            StubRequest.request("/failing")
                .body(TestHelper.json(TestHelper.entry("numberToMultiply", 22))));

        Assert.assertEquals(StubHttpResponse.response(TestHelper.json(
            TestHelper.entry("message", "intentionally failed"),
            TestHelper.entry("errors", TestHelper.object())
        )).withStatusCode(400), response);
    }


    @Test
    public void shouldApplyFilterBeforeHandle() {
        StubHttpResponse response = testApplicationClient.post(
            StubRequest.request("/root/filter/test")
                .body(TestHelper.json()));


        Assert.assertEquals(
            StubHttpResponse.response(TestHelper.json(TestHelper.entry("filterMessage", "hit handler")))
            .withCookie(new CookieImpl("rootFilter", "hitfilter"))
            .withCookie(new CookieImpl("pathFilter", "hitfilter")),
            response
        );

    }

    @Test
    public void shouldHandleMalformedJsonRequest() {
        StubHttpResponse response = testApplicationClient.post(
            StubRequest.request("/throw")
                .body("<not json at all>"));

        Assert.assertEquals(
            StubHttpResponse.response(TestHelper.json(
                TestHelper.entry("message", "Invalid json request"),
                TestHelper.entry("errors", TestHelper.object())
            )).withStatusCode(400),
            response
        );
    }

    @Test
    public void shouldHandleItWhenThrowingAnExceptionWithinTheHandler() {
        StubHttpResponse response = testApplicationClient.post(
            StubRequest.request("/throw")
                .body(TestHelper.json(TestHelper.entry("where", "complete"))));


        Assert.assertEquals(
            StubHttpResponse.response(TestHelper.json(TestHelper.entry("message", "Something went wrong"))).withStatusCode(500),
            response
        );

        testApplicationClient.assertException("app error in complete");
    }

    @Test
    public void shouldHandleItWhenThrowingAnExceptionInMapHandler() {
        StubHttpResponse response = testApplicationClient.post(
            StubRequest.request("/throw")
                .body(TestHelper.json(TestHelper.entry("where", "map"))));

        Assert.assertEquals(
            StubHttpResponse.response(TestHelper.json(TestHelper.entry("message", "Something went wrong"))).withStatusCode(500),
            response
        );

        testApplicationClient.assertException("app error in map");
    }

    @Test
    public void shouldHandleItWhenThrowingAnExceptionInBlockingHandler() {
        StubHttpResponse response = testApplicationClient.post(
            StubRequest.request("/throw")
                .body(TestHelper.json(TestHelper.entry("where", "blocking"))));

        Assert.assertEquals(
            StubHttpResponse.response(TestHelper.json(TestHelper.entry("message", "Something went wrong"))).withStatusCode(500),
            response
        );

        testApplicationClient.assertException("app error in blocking");
    }

    @Test
    public void shouldUploadAFile() {

        StubHttpResponse response = testApplicationClient.post(
            StubRequest.request("/upload")
                .fileUpload("file1", "some bytes")
                .fileUpload("file2", "even more bytes"));

        Assert.assertEquals(
            StubHttpResponse.response(TestHelper.json(TestHelper.entry("results", TestHelper.object(TestHelper.entry("file1", 10), TestHelper.entry("file2", 15))))),
            response
        );
    }

    @Test
    public void shouldDownloadFile() {
        StubHttpResponse response = testApplicationClient.get(StubRequest.request("/download"));

        Assert.assertEquals(
            StubHttpResponse.response(TestHelper.file("file contents"), "text/html; charset=utf-8")
                .withHeader("Transfer-Encoding", "chunked")
                .withHeader("Content-Disposition", "data.txt"),
            response
        );
    }
}
