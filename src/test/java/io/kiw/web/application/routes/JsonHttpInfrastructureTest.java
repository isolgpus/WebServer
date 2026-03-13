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

import static io.kiw.web.test.TestHelper.json;
import static org.junit.Assert.assertEquals;

public class JsonHttpInfrastructureTest {

    private TestApplicationClient testApplicationClient;
    private static final String DEFAULT_POST_RESPONSE = json()
            .put("intExample", 0)
            .putNull("stringExample")
            .putNull("pathExample")
            .putNull("queryExample")
            .putNull("requestHeaderExample")
            .putNull("requestCookieExample")
            .toString();

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
        final String requestBody = json()
                .put("intExample", 17)
                .put("stringExample", "hiya")
                .toString();


        StubHttpResponse response = testApplicationClient.post(StubRequest.request("/echo").body(requestBody));

        final String expectedResponse = json()
                .put("intExample", 17)
                .put("stringExample", "hiya")
                .putNull("pathExample")
                .putNull("queryExample")
                .putNull("requestHeaderExample")
                .putNull("requestCookieExample")
                .toString();

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

        final String expectedResponse = json()
                .put("intExample", 0)
                .putNull("stringExample")
                .putNull("pathExample")
                .put("queryExample", "hi")
                .putNull("requestHeaderExample")
                .putNull("requestCookieExample")
                .toString();

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

        final String expectedResponse = json()
                .put("intExample", 0)
                .putNull("stringExample")
                .putNull("pathExample")
                .put("queryExample", "hi")
                .put("requestHeaderExample", "test")
                .putNull("requestCookieExample")
                .toString();

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

        final String expectedResponse = json()
                .put("intExample", 0)
                .putNull("stringExample")
                .putNull("pathExample")
                .put("queryExample", "hi")
                .put("requestHeaderExample", "test")
                .putNull("requestCookieExample")
                .toString();

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

        final String expectedResponse = json()
                .put("intExample", 0)
                .putNull("stringExample")
                .putNull("pathExample")
                .put("queryExample", "hi")
                .put("requestHeaderExample", "test")
                .putNull("requestCookieExample")
                .toString();

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

        final String expectedResponse = json()
                .put("intExample", 188)
                .put("stringExample", "You invoked a GET")
                .putNull("pathExample")
                .putNull("queryExample")
                .put("requestHeaderExample", "test")
                .putNull("requestCookieExample")
                .toString();

        Assert.assertEquals(
                StubHttpResponse.response(expectedResponse),
                response);
    }

    @Test
    public void shouldReadQueryParamsInGet() {
        StubHttpResponse response = testApplicationClient.get(
                StubRequest.request("/echo")
                        .queryParam("queryExample", "hi"));

        final String expectedResponse = json()
                .put("intExample", 188)
                .put("stringExample", "You invoked a GET")
                .putNull("pathExample")
                .put("queryExample", "hi")
                .putNull("requestHeaderExample")
                .putNull("requestCookieExample")
                .toString();

        Assert.assertEquals(
                StubHttpResponse.response(expectedResponse),
                response);
    }


    @Test
    public void shouldIgnoreWhenClientSendsUnknownValues() {

        final String requestBody = json()
                .put("intExample", 17)
                .put("stringExample", "hiya")
                .putNull("pathExample")
                .put("something", "else")
                .toString();


        StubHttpResponse response = testApplicationClient.post(
                StubRequest.request("/echo")
                .body(requestBody));

        final String expectedResponse = json()
                .put("intExample", 17)
                .put("stringExample", "hiya")
                .putNull("pathExample")
                .putNull("queryExample")
                .putNull("requestHeaderExample")
                .putNull("requestCookieExample")
                .toString();

        Assert.assertEquals(StubHttpResponse.response(expectedResponse), response);

    }

    @Test
    public void shouldRespondWithErrorNicelyWhenRequestBodyIsNotPresentOnPost() {
        StubHttpResponse response = testApplicationClient.post(StubRequest.request("/echo"));

        final String expectedResponse = json()
                .put("message", "Invalid json request")
                .set("errors", json())
                .toString();

        Assert.assertEquals(StubHttpResponse.response(expectedResponse).withStatusCode(400), response);
    }

    @Test
    public void shouldCallGetRoute() {
        StubHttpResponse response = testApplicationClient.get(StubRequest.request("/echo"));


        final String expectedResponseBody = json()
                .put("intExample", 188)
                .put("stringExample", "You invoked a GET")
                .putNull("pathExample")
                .putNull("queryExample")
                .putNull("requestHeaderExample")
                .putNull("requestCookieExample")
                .toString();

        Assert.assertEquals(StubHttpResponse.response(expectedResponseBody), response);
    }

    @Test
    public void shouldPopulateResponseHeaders() {
        final String request = json()
                .put("responseHeaderExample", "responseTest")
                .toString();

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

        String expectedResponse = json()
                .put("intExample", 0)
                .putNull("stringExample")
                .putNull("pathExample")
                .putNull("queryExample")
                .putNull("requestHeaderExample")
                .put("requestCookieExample", "cookietest")
                .toString();
        Assert.assertEquals(StubHttpResponse.response(expectedResponse), response);
    }

    @Test
    public void shouldPopulateResponseCookie() {
        StubHttpResponse response = testApplicationClient.post(
                StubRequest.request("/echo")
                        .body(json().put("responseCookieExample", "responseCookieTest").toString()));

        Assert.assertEquals(StubHttpResponse.response(DEFAULT_POST_RESPONSE)
                .withCookie(new CookieImpl("responseCookieExample", "responseCookieTest")), response);
    }

    @Test
    public void shouldMapThroughABlockingCall() {
        StubHttpResponse response = testApplicationClient.post(
            StubRequest.request("/blocking")
                .body(json().put("numberToMultiply", 22).toString()));

        Assert.assertEquals(StubHttpResponse.response(json().put("multipliedNumber", 44).toString()), response);
    }

    @Test
    public void shouldMapThroughABlockingCompleteCall() {
        StubHttpResponse response = testApplicationClient.post(
                StubRequest.request("/blockingComplete")
                        .body(json().put("numberToMultiply", 22).toString()));

        Assert.assertEquals(StubHttpResponse.response(json().put("multipliedNumber", 44).toString()), response);
    }

    @Test
    public void shouldReturnWithErrorOnBadRequest() {
        StubHttpResponse response = testApplicationClient.post(
            StubRequest.request("/failing")
                .body(json().put("numberToMultiply", 22).toString()));

        Assert.assertEquals(StubHttpResponse.response(json()
                .put("message", "intentionally failed")
                .set("errors", json())
                .toString()).withStatusCode(400), response);
    }


    @Test
    public void shouldApplyFilterBeforeHandle() {
        StubHttpResponse response = testApplicationClient.post(
            StubRequest.request("/root/filter/test")
                .body(json().toString()));


        Assert.assertEquals(
            StubHttpResponse.response(json().put("filterMessage", "hit handler").toString())
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
            StubHttpResponse.response(json()
                .put("message", "Invalid json request")
                .set("errors", json())
                .toString()).withStatusCode(400),
            response
        );
    }

    @Test
    public void shouldHandleItWhenThrowingAnExceptionWithinTheHandler() {
        StubHttpResponse response = testApplicationClient.post(
            StubRequest.request("/throw")
                .body(json().put("where", "complete").toString()));


        Assert.assertEquals(
            StubHttpResponse.response(json().put("message", "Something went wrong").toString()).withStatusCode(500),
            response
        );

        testApplicationClient.assertException("app error in complete");
    }

    @Test
    public void shouldHandleItWhenThrowingAnExceptionInMapHandler() {
        StubHttpResponse response = testApplicationClient.post(
            StubRequest.request("/throw")
                .body(json().put("where", "map").toString()));

        Assert.assertEquals(
            StubHttpResponse.response(json().put("message", "Something went wrong").toString()).withStatusCode(500),
            response
        );

        testApplicationClient.assertException("app error in map");
    }

    @Test
    public void shouldHandleItWhenThrowingAnExceptionInBlockingHandler() {
        StubHttpResponse response = testApplicationClient.post(
            StubRequest.request("/throw")
                .body(json().put("where", "blocking").toString()));

        Assert.assertEquals(
            StubHttpResponse.response(json().put("message", "Something went wrong").toString()).withStatusCode(500),
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
            StubHttpResponse.response(json()
                .set("results", json().put("file1", 10).put("file2", 15))
                .toString()),
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


    @Test
    public void shouldSupportPathParam() {
        StubHttpResponse response = testApplicationClient.get(
                StubRequest.request("/echo/myvariable"));

        final String expectedResponse = json()
                .put("intExample", 188)
                .put("stringExample", "You invoked a GET")
                .put("pathExample", "myvariable")
                .putNull("queryExample")
                .putNull("requestHeaderExample")
                .putNull("requestCookieExample")
                .toString();

        Assert.assertEquals(
                StubHttpResponse.response(expectedResponse),
                response
        );
    }
}
