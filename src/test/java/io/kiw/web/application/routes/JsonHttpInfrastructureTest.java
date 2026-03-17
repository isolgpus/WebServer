package io.kiw.web.application.routes;

import io.kiw.web.test.*;
import io.vertx.core.http.impl.CookieImpl;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static io.kiw.web.application.routes.TestApplicationClientCreator.createApplicationClient;
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

        testApplicationClient = createApplicationClient();
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


        TestHttpResponse response = testApplicationClient.post(StubRequest.request("/echo").body(requestBody));

        final String expectedResponse = json()
                .put("intExample", 17)
                .put("stringExample", "hiya")
                .putNull("pathExample")
                .putNull("queryExample")
                .putNull("requestHeaderExample")
                .putNull("requestCookieExample")
                .toString();

        Assert.assertEquals(
                TestHttpResponse.response(expectedResponse),
                response);
    }

    @Test
    public void shouldReadQueryParamsInPost() {
        TestHttpResponse response = testApplicationClient.put(
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
                TestHttpResponse.response(expectedResponse),
                response);
    }

    @Test
    public void shouldReadRequestHeaderParamsOnPost() {
        TestHttpResponse response = testApplicationClient.post(
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
                TestHttpResponse.response(expectedResponse),
                response);
    }

    @Test
    public void shouldReadRequestHeaderParamsOnPut() {
        TestHttpResponse response = testApplicationClient.put(
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
            TestHttpResponse.response(expectedResponse),
            response);
    }

    @Test
    public void shouldReadRequestHeaderParamsOnDelete() {
        TestHttpResponse response = testApplicationClient.delete(
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
            TestHttpResponse.response(expectedResponse),
            response);
    }

    @Test
    public void shouldReadRequestHeaderParamsOnPatch() {
        TestHttpResponse response = testApplicationClient.patch(
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
            TestHttpResponse.response(expectedResponse),
            response);
    }

    @Test
    public void shouldReadRequestHeaderParamsOnGet() {
        TestHttpResponse response = testApplicationClient.get(
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
                TestHttpResponse.response(expectedResponse),
                response);
    }

    @Test
    public void shouldReadQueryParamsInGet() {
        TestHttpResponse response = testApplicationClient.get(
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
                TestHttpResponse.response(expectedResponse),
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


        TestHttpResponse response = testApplicationClient.post(
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

        Assert.assertEquals(TestHttpResponse.response(expectedResponse), response);

    }

    @Test
    public void shouldRespondWithErrorNicelyWhenRequestBodyIsNotPresentOnPost() {
        TestHttpResponse response = testApplicationClient.post(StubRequest.request("/echo"));

        final String expectedResponse = json()
                .put("message", "Invalid json request")
                .set("errors", json())
                .toString();

        Assert.assertEquals(TestHttpResponse.response(expectedResponse).withStatusCode(400), response);
    }

    @Test
    public void shouldCallGetRoute() {
        TestHttpResponse response = testApplicationClient.get(StubRequest.request("/echo"));


        final String expectedResponseBody = json()
                .put("intExample", 188)
                .put("stringExample", "You invoked a GET")
                .putNull("pathExample")
                .putNull("queryExample")
                .putNull("requestHeaderExample")
                .putNull("requestCookieExample")
                .toString();

        Assert.assertEquals(TestHttpResponse.response(expectedResponseBody), response);
    }

    @Test
    public void shouldPopulateResponseHeaders() {
        final String request = json()
                .put("responseHeaderExample", "responseTest")
                .toString();

        TestHttpResponse response = testApplicationClient.post(StubRequest.request("/echo")
                .body(request));

        Assert.assertEquals(TestHttpResponse.response(DEFAULT_POST_RESPONSE)
                .withHeader("responseHeaderExample", "responseTest"), response);
    }


    @Test
    public void shouldReadRequestCookies() {
        TestHttpResponse response = testApplicationClient.post(
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
        Assert.assertEquals(TestHttpResponse.response(expectedResponse), response);
    }

    @Test
    public void shouldPopulateResponseCookie() {
        TestHttpResponse response = testApplicationClient.post(
                StubRequest.request("/echo")
                        .body(json().put("responseCookieExample", "responseCookieTest").toString()));

        Assert.assertEquals(TestHttpResponse.response(DEFAULT_POST_RESPONSE)
                .withCookie(new CookieImpl("responseCookieExample", "responseCookieTest")), response);
    }

    @Test
    public void shouldMapThroughABlockingCall() {
        TestHttpResponse response = testApplicationClient.post(
            StubRequest.request("/blocking")
                .body(json().put("numberToMultiply", 22).toString()));

        Assert.assertEquals(TestHttpResponse.response(json().put("multipliedNumber", 44).toString()), response);
    }

    @Test
    public void shouldMapThroughABlockingCompleteCall() {
        TestHttpResponse response = testApplicationClient.post(
                StubRequest.request("/blockingComplete")
                        .body(json().put("numberToMultiply", 22).toString()));

        Assert.assertEquals(TestHttpResponse.response(json().put("multipliedNumber", 44).toString()), response);
    }

    @Test
    public void shouldReturnWithErrorOnBadRequest() {
        TestHttpResponse response = testApplicationClient.post(
            StubRequest.request("/failing")
                .body(json().put("numberToMultiply", 22).toString()));

        Assert.assertEquals(TestHttpResponse.response(json()
                .put("message", "intentionally failed")
                .set("errors", json())
                .toString()).withStatusCode(400), response);
    }


    @Test
    public void shouldApplyFilterBeforeHandle() {
        TestHttpResponse response = testApplicationClient.post(
            StubRequest.request("/root/filter/test")
                .body(json().toString()));


        Assert.assertEquals(
            TestHttpResponse.response(json().put("filterMessage", "hit handler").toString())
            .withCookie(new CookieImpl("rootFilter", "hitfilter"))
            .withCookie(new CookieImpl("pathFilter", "hitfilter")),
            response
        );

    }

    @Test
    public void shouldApplyFilterBeforeHandleOnGet() {
        TestHttpResponse response = testApplicationClient.get(
            StubRequest.request("/root/filter/test"));

        Assert.assertEquals(
            TestHttpResponse.response(json().put("filterMessage", "hit handler").toString())
            .withCookie(new CookieImpl("rootFilter", "hitfilter"))
            .withCookie(new CookieImpl("pathFilter", "hitfilter")),
            response
        );
    }

    @Test
    public void shouldApplyFilterBeforeHandleOnPut() {
        TestHttpResponse response = testApplicationClient.put(
            StubRequest.request("/root/filter/test")
                .body(json().toString()));

        Assert.assertEquals(
            TestHttpResponse.response(json().put("filterMessage", "hit handler").toString())
            .withCookie(new CookieImpl("rootFilter", "hitfilter"))
            .withCookie(new CookieImpl("pathFilter", "hitfilter")),
            response
        );
    }

    @Test
    public void shouldApplyFilterBeforeHandleOnDelete() {
        TestHttpResponse response = testApplicationClient.delete(
            StubRequest.request("/root/filter/test")
                .body(json().toString()));

        Assert.assertEquals(
            TestHttpResponse.response(json().put("filterMessage", "hit handler").toString())
            .withCookie(new CookieImpl("rootFilter", "hitfilter"))
            .withCookie(new CookieImpl("pathFilter", "hitfilter")),
            response
        );
    }

    @Test
    public void shouldApplyFilterBeforeHandleOnPatch() {
        TestHttpResponse response = testApplicationClient.patch(
            StubRequest.request("/root/filter/test")
                .body(json().toString()));

        Assert.assertEquals(
            TestHttpResponse.response(json().put("filterMessage", "hit handler").toString())
            .withCookie(new CookieImpl("rootFilter", "hitfilter"))
            .withCookie(new CookieImpl("pathFilter", "hitfilter")),
            response
        );
    }

    @Test
    public void shouldHandleMalformedJsonRequest() {
        TestHttpResponse response = testApplicationClient.post(
            StubRequest.request("/throw")
                .body("<not json at all>"));

        Assert.assertEquals(
            TestHttpResponse.response(json()
                .put("message", "Invalid json request")
                .set("errors", json())
                .toString()).withStatusCode(400),
            response
        );
    }

    @Test
    public void shouldHandleItWhenThrowingAnExceptionWithinTheHandler() {
        TestHttpResponse response = testApplicationClient.post(
            StubRequest.request("/throw")
                .body(json().put("where", "complete").toString()));


        Assert.assertEquals(
            TestHttpResponse.response(json().put("message", "Something went wrong").toString()).withStatusCode(500),
            response
        );

        testApplicationClient.assertException("app error in complete");
    }

    @Test
    public void shouldHandleItWhenThrowingAnExceptionInMapHandler() {
        TestHttpResponse response = testApplicationClient.post(
            StubRequest.request("/throw")
                .body(json().put("where", "map").toString()));

        Assert.assertEquals(
            TestHttpResponse.response(json().put("message", "Something went wrong").toString()).withStatusCode(500),
            response
        );

        testApplicationClient.assertException("app error in map");
    }

    @Test
    public void shouldHandleItWhenThrowingAnExceptionInBlockingHandler() {
        TestHttpResponse response = testApplicationClient.post(
            StubRequest.request("/throw")
                .body(json().put("where", "blocking").toString()));

        Assert.assertEquals(
            TestHttpResponse.response(json().put("message", "Something went wrong").toString()).withStatusCode(500),
            response
        );

        testApplicationClient.assertException("app error in blocking");
    }

    @Test
    public void shouldUploadAFile() {

        TestHttpResponse response = testApplicationClient.post(
            StubRequest.request("/upload")
                .fileUpload("file1", "some bytes")
                .fileUpload("file2", "even more bytes"));

        Assert.assertEquals(
            TestHttpResponse.response(json()
                .set("results", json().put("file1", 10).put("file2", 15))
                .toString()),
            response
        );
    }

    @Test
    public void shouldDownloadFile() {
        TestHttpResponse response = testApplicationClient.get(StubRequest.request("/download"));

        Assert.assertEquals(
            TestHttpResponse.response(TestHelper.file("file contents"), "text/html; charset=utf-8")
                .withHeader("Transfer-Encoding", "chunked")
                .withHeader("Content-Disposition", "data.txt"),
            response
        );
    }


    @Test
    public void shouldSupportAsyncMap() {
        TestHttpResponse response = testApplicationClient.post(
                StubRequest.request("/asyncMap").body(json().put("value", 5).toString()));

        Assert.assertEquals(
                TestHttpResponse.response(json().put("result", 50).toString()),
                response);
    }

    @Test
    public void shouldSupportAsyncBlockingMap() {
        TestHttpResponse response = testApplicationClient.post(
                StubRequest.request("/asyncBlockingMap").body(json().put("value", 3).toString()));

        Assert.assertEquals(
                TestHttpResponse.response(json().put("result", 60).toString()),
                response);
    }

    @Test
    public void shouldSupportPathParam() {
        TestHttpResponse response = testApplicationClient.get(
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
                TestHttpResponse.response(expectedResponse),
                response
        );
    }

    @Test
    public void shouldPassValidationAndReturnResponse() {
        String body = json()
            .put("name", "Alice")
            .put("email", "alice@example.com")
            .put("age", 25)
            .set("address", json().put("city", "NYC").put("zip", "10001"))
            .toString();

        TestHttpResponse response = testApplicationClient.post(
            StubRequest.request("/validate/42")
                .body(body)
                .queryParam("page", "1"));

        String expected = json()
            .put("name", "Alice")
            .put("email", "alice@example.com")
            .put("age", 25)
            .put("city", "NYC")
            .put("page", "1")
            .put("userId", "42")
            .toString();

        Assert.assertEquals(TestHttpResponse.response(expected), response);
    }

    @Test
    public void shouldReturnValidationErrorForInvalidBodyField() {
        String body = json()
            .putNull("name")
            .put("email", "alice@example.com")
            .put("age", 25)
            .set("address", json().put("city", "NYC").put("zip", "10001"))
            .toString();

        TestHttpResponse response = testApplicationClient.post(
            StubRequest.request("/validate/42")
                .body(body)
                .queryParam("page", "1"));

        String expected = json()
            .put("message", "Validation failed")
            .set("errors", json()
                .set("name", TestHelper.MAPPER.createArrayNode().add("must not be blank")))
            .toString();

        Assert.assertEquals(TestHttpResponse.response(expected).withStatusCode(422), response);
    }

    @Test
    public void shouldReturnValidationErrorForInvalidEmail() {
        String body = json()
            .put("name", "Alice")
            .put("email", "not-an-email")
            .put("age", 25)
            .set("address", json().put("city", "NYC").put("zip", "10001"))
            .toString();

        TestHttpResponse response = testApplicationClient.post(
            StubRequest.request("/validate/42")
                .body(body)
                .queryParam("page", "1"));

        String expected = json()
            .put("message", "Validation failed")
            .set("errors", json()
                .set("email", TestHelper.MAPPER.createArrayNode().add("must be a valid email address")))
            .toString();

        Assert.assertEquals(TestHttpResponse.response(expected).withStatusCode(422), response);
    }

    @Test
    public void shouldReturnValidationErrorForNestedField() {
        String body = json()
            .put("name", "Alice")
            .put("email", "alice@example.com")
            .put("age", 25)
            .set("address", json().put("city", "NYC").put("zip", "bad"))
            .toString();

        TestHttpResponse response = testApplicationClient.post(
            StubRequest.request("/validate/42")
                .body(body)
                .queryParam("page", "1"));

        String expected = json()
            .put("message", "Validation failed")
            .set("errors", json()
                .set("address.zip", TestHelper.MAPPER.createArrayNode().add("must match pattern: [0-9]{5}")))
            .toString();

        Assert.assertEquals(TestHttpResponse.response(expected).withStatusCode(422), response);
    }

    @Test
    public void shouldReturnValidationErrorForMissingQueryParam() {
        String body = json()
            .put("name", "Alice")
            .put("email", "alice@example.com")
            .put("age", 25)
            .set("address", json().put("city", "NYC").put("zip", "10001"))
            .toString();

        TestHttpResponse response = testApplicationClient.post(
            StubRequest.request("/validate/42").body(body));

        String expected = json()
            .put("message", "Validation failed")
            .set("errors", json()
                .set("page", TestHelper.MAPPER.createArrayNode().add("must not be blank")))
            .toString();

        Assert.assertEquals(TestHttpResponse.response(expected).withStatusCode(422), response);
    }

    @Test
    public void shouldReturnApplicationState() {
        TestHttpResponse response = testApplicationClient.post(StubRequest.request("/state").body("{}"));

        Assert.assertEquals(TestHttpResponse.response(json().put("longValue", 55).toString()), response);
    }

    @Test
    public void shouldShortCircuitWhenFilterReturnsError() {
        TestHttpResponse response = testApplicationClient.get(StubRequest.request("/protected/resource"));

        Assert.assertEquals(
            new TestHttpResponse(json()
                .put("message", "filter blocked")
                .set("errors", json())
                .toString()).withStatusCode(401),
            response
        );
    }

    @Test
    public void shouldHandleBlockingFlatMapFailure() {
        TestHttpResponse response = testApplicationClient.post(
            StubRequest.request("/blockingFailing").body(json().put("numberToMultiply", 5).toString()));

        Assert.assertEquals(
            TestHttpResponse.response(json()
                .put("message", "blocking flat map failed")
                .set("errors", json())
                .toString()).withStatusCode(400),
            response
        );
    }

    @Test
    public void shouldHandleAsyncFlatMapFailure() {
        TestHttpResponse response = testApplicationClient.post(
            StubRequest.request("/asyncFailing").body(json().put("value", 5).toString()));

        Assert.assertEquals(
            TestHttpResponse.response(json()
                .put("message", "async flat map failed")
                .set("errors", json())
                .toString()).withStatusCode(400),
            response
        );
    }

    @Test
    public void shouldHandleExceptionInAsyncMapHandler() {
        TestHttpResponse response = testApplicationClient.post(
            StubRequest.request("/throw").body(json().put("where", "asyncMap").toString()));

        Assert.assertEquals(
            TestHttpResponse.response(json().put("message", "Something went wrong").toString()).withStatusCode(500),
            response
        );

        testApplicationClient.assertException("app error in asyncMap");
    }

    @Test
    public void shouldHandleExceptionInAsyncBlockingMapHandler() {
        TestHttpResponse response = testApplicationClient.post(
            StubRequest.request("/throw").body(json().put("where", "asyncBlockingMap").toString()));

        Assert.assertEquals(
            TestHttpResponse.response(json().put("message", "Something went wrong").toString()).withStatusCode(500),
            response
        );

        testApplicationClient.assertException("app error in asyncBlockingMap");
    }

    @Test
    public void shouldReturnValidationErrorForInvalidPathParam() {
        String body = json()
            .put("name", "Alice")
            .put("email", "alice@example.com")
            .put("age", 25)
            .set("address", json().put("city", "NYC").put("zip", "10001"))
            .toString();

        TestHttpResponse response = testApplicationClient.post(
            StubRequest.request("/validate/not-a-number")
                .body(body)
                .queryParam("page", "1"));

        String expected = json()
            .put("message", "Validation failed")
            .set("errors", json()
                .set("userId", TestHelper.MAPPER.createArrayNode().add("must match pattern: [0-9]+")))
            .toString();

        Assert.assertEquals(TestHttpResponse.response(expected).withStatusCode(422), response);
    }
}
