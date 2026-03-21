package io.kiw.luxis.web.application.routes;

import io.kiw.luxis.web.http.Method;
import io.kiw.luxis.web.test.*;
import io.kiw.luxis.web.test.handler.*;
import io.kiw.luxis.web.http.HttpCookie;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;

import static io.kiw.luxis.web.application.routes.TestApplicationClientCreator.*;
import static io.kiw.luxis.web.test.TestHelper.json;
import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class JsonHttpInfrastructureTest {

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> modes() {
        return TestApplicationClientCreator.modes();
    }

    private final String mode;
    private TestApplicationClient testApplicationClient;
    private static final String DEFAULT_POST_RESPONSE = json()
            .put("intExample", 0)
            .putNull("stringExample")
            .putNull("pathExample")
            .putNull("queryExample")
            .putNull("requestHeaderExample")
            .putNull("requestCookieExample")
            .toString();

    public JsonHttpInfrastructureTest(String mode) {
        this.mode = mode;
    }

    @Before
    public void setUp() throws Exception {
        if (REAL_MODE.equals(mode)) {
            assumeRealModeEnabled();
        }
    }

    @After
    public void tearDown() throws Exception {
        if (testApplicationClient != null) {
            testApplicationClient.assertNoMoreExceptions();
            testApplicationClient.stop();
        }
    }

    @Test
    public void shouldHandlePopulatingJsonValues() {
        testApplicationClient = createClient(mode, (r, state) -> {
            r.jsonRoute("/echo", Method.POST, state, new PostEchoHandler());
        });

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
        testApplicationClient = createClient(mode, (r, state) -> {
            r.jsonRoute("/echo", Method.PUT, state, new PostEchoHandler());
        });

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
        testApplicationClient = createClient(mode, (r, state) -> {
            r.jsonRoute("/echo", Method.POST, state, new PostEchoHandler());
        });

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
        testApplicationClient = createClient(mode, (r, state) -> {
            r.jsonRoute("/echo", Method.PUT, state, new PostEchoHandler());
        });

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
        testApplicationClient = createClient(mode, (r, state) -> {
            r.jsonRoute("/echo", Method.DELETE, state, new PostEchoHandler());
        });

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
        testApplicationClient = createClient(mode, (r, state) -> {
            r.jsonRoute("/echo", Method.PATCH, state, new PostEchoHandler());
        });

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
        testApplicationClient = createClient(mode, (r, state) -> {
            r.jsonRoute("/echo", Method.GET, state, new GetEchoHandler());
        });

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
        testApplicationClient = createClient(mode, (r, state) -> {
            r.jsonRoute("/echo", Method.GET, state, new GetEchoHandler());
        });

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
        testApplicationClient = createClient(mode, (r, state) -> {
            r.jsonRoute("/echo", Method.POST, state, new PostEchoHandler());
        });

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
        testApplicationClient = createClient(mode, (r, state) -> {
            r.jsonRoute("/echo", Method.POST, state, new PostEchoHandler());
        });

        TestHttpResponse response = testApplicationClient.post(StubRequest.request("/echo"));

        final String expectedResponse = json()
                .put("message", "Invalid json request")
                .set("errors", json())
                .toString();

        Assert.assertEquals(TestHttpResponse.response(expectedResponse).withStatusCode(400), response);
    }

    @Test
    public void shouldCallGetRoute() {
        testApplicationClient = createClient(mode, (r, state) -> {
            r.jsonRoute("/echo", Method.GET, state, new GetEchoHandler());
        });

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
        testApplicationClient = createClient(mode, (r, state) -> {
            r.jsonRoute("/echo", Method.POST, state, new PostEchoHandler());
        });

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
        testApplicationClient = createClient(mode, (r, state) -> {
            r.jsonRoute("/echo", Method.POST, state, new PostEchoHandler());
        });

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
        testApplicationClient = createClient(mode, (r, state) -> {
            r.jsonRoute("/echo", Method.POST, state, new PostEchoHandler());
        });

        TestHttpResponse response = testApplicationClient.post(
                StubRequest.request("/echo")
                        .body(json().put("responseCookieExample", "responseCookieTest").toString()));

        Assert.assertEquals(TestHttpResponse.response(DEFAULT_POST_RESPONSE)
                .withCookie(new HttpCookie("responseCookieExample", "responseCookieTest")), response);
    }

    @Test
    public void shouldMapThroughABlockingCall() {
        testApplicationClient = createClient(mode, (r, state) -> {
            r.jsonRoute("/blocking", Method.POST, state, new BlockingTestHandler());
        });

        TestHttpResponse response = testApplicationClient.post(
            StubRequest.request("/blocking")
                .body(json().put("numberToMultiply", 22).toString()));

        Assert.assertEquals(TestHttpResponse.response(json().put("multipliedNumber", 44).toString()), response);
    }

    @Test
    public void shouldMapThroughABlockingCompleteCall() {
        testApplicationClient = createClient(mode, (r, state) -> {
            r.jsonRoute("/blockingComplete", Method.POST, state, new BlockingCompleteTestHandler());
        });

        TestHttpResponse response = testApplicationClient.post(
                StubRequest.request("/blockingComplete")
                        .body(json().put("numberToMultiply", 22).toString()));

        Assert.assertEquals(TestHttpResponse.response(json().put("multipliedNumber", 44).toString()), response);
    }

    @Test
    public void shouldReturnWithErrorOnBadRequest() {
        testApplicationClient = createClient(mode, (r, state) -> {
            r.jsonRoute("/failing", Method.POST, state, new FailingTestHandler());
        });

        TestHttpResponse response = testApplicationClient.post(
            StubRequest.request("/failing")
                .body(json().put("numberToMultiply", 22).toString()));

        Assert.assertEquals(TestHttpResponse.response(json()
                .put("message", "intentionally failed")
                .set("errors", json())
                .toString()).withStatusCode(400), response);
    }


    @Test
    public void shouldAccessRequestBodyInHandlerWhenRoutedThroughFilter() {
        testApplicationClient = createClient(mode, (r, state) -> {
            r.jsonFilter("/root/*", state, new TestFilter("rootFilter"));
            r.jsonFilter("/root/filter/*", state, new TestFilter("pathFilter"));
            r.jsonRoute("/root/filter/echo", Method.POST, state, new PostEchoHandler());
        });

        String requestBody = json()
            .put("intExample", 42)
            .put("stringExample", "through filter")
            .toString();

        TestHttpResponse response = testApplicationClient.post(
            StubRequest.request("/root/filter/echo").body(requestBody));

        String expectedResponse = json()
            .put("intExample", 42)
            .put("stringExample", "through filter")
            .putNull("pathExample")
            .putNull("queryExample")
            .putNull("requestHeaderExample")
            .putNull("requestCookieExample")
            .toString();

        Assert.assertEquals(
            TestHttpResponse.response(expectedResponse)
                .withCookie(new HttpCookie("rootFilter", "hitfilter"))
                .withCookie(new HttpCookie("pathFilter", "hitfilter")),
            response);
    }

    @Test
    public void shouldApplyFilterBeforeHandle() {
        testApplicationClient = createClient(mode, (r, state) -> {
            r.jsonFilter("/root/*", state, new TestFilter("rootFilter"));
            r.jsonFilter("/root/filter/*", state, new TestFilter("pathFilter"));
            r.jsonRoute("/root/filter/test", Method.POST, state, new TestFilterHandler());
        });

        TestHttpResponse response = testApplicationClient.post(
            StubRequest.request("/root/filter/test")
                .body(json().toString()));

        Assert.assertEquals(
            TestHttpResponse.response(json().put("filterMessage", "hit handler").toString())
            .withCookie(new HttpCookie("rootFilter", "hitfilter"))
            .withCookie(new HttpCookie("pathFilter", "hitfilter")),
            response
        );
    }

    @Test
    public void shouldApplyFilterBeforeHandleOnGet() {
        testApplicationClient = createClient(mode, (r, state) -> {
            r.jsonFilter("/root/*", state, new TestFilter("rootFilter"));
            r.jsonFilter("/root/filter/*", state, new TestFilter("pathFilter"));
            r.jsonRoute("/root/filter/test", Method.GET, state, new GetTestFilterHandler());
        });

        TestHttpResponse response = testApplicationClient.get(
            StubRequest.request("/root/filter/test"));

        Assert.assertEquals(
            TestHttpResponse.response(json().put("filterMessage", "hit handler").toString())
            .withCookie(new HttpCookie("rootFilter", "hitfilter"))
            .withCookie(new HttpCookie("pathFilter", "hitfilter")),
            response
        );
    }

    @Test
    public void shouldApplyFilterBeforeHandleOnPut() {
        testApplicationClient = createClient(mode, (r, state) -> {
            r.jsonFilter("/root/*", state, new TestFilter("rootFilter"));
            r.jsonFilter("/root/filter/*", state, new TestFilter("pathFilter"));
            r.jsonRoute("/root/filter/test", Method.PUT, state, new TestFilterHandler());
        });

        TestHttpResponse response = testApplicationClient.put(
            StubRequest.request("/root/filter/test")
                .body(json().toString()));

        Assert.assertEquals(
            TestHttpResponse.response(json().put("filterMessage", "hit handler").toString())
            .withCookie(new HttpCookie("rootFilter", "hitfilter"))
            .withCookie(new HttpCookie("pathFilter", "hitfilter")),
            response
        );
    }

    @Test
    public void shouldApplyFilterBeforeHandleOnDelete() {
        testApplicationClient = createClient(mode, (r, state) -> {
            r.jsonFilter("/root/*", state, new TestFilter("rootFilter"));
            r.jsonFilter("/root/filter/*", state, new TestFilter("pathFilter"));
            r.jsonRoute("/root/filter/test", Method.DELETE, state, new TestFilterHandler());
        });

        TestHttpResponse response = testApplicationClient.delete(
            StubRequest.request("/root/filter/test")
                .body(json().toString()));

        Assert.assertEquals(
            TestHttpResponse.response(json().put("filterMessage", "hit handler").toString())
            .withCookie(new HttpCookie("rootFilter", "hitfilter"))
            .withCookie(new HttpCookie("pathFilter", "hitfilter")),
            response
        );
    }

    @Test
    public void shouldApplyFilterBeforeHandleOnPatch() {
        testApplicationClient = createClient(mode, (r, state) -> {
            r.jsonFilter("/root/*", state, new TestFilter("rootFilter"));
            r.jsonFilter("/root/filter/*", state, new TestFilter("pathFilter"));
            r.jsonRoute("/root/filter/test", Method.PATCH, state, new TestFilterHandler());
        });

        TestHttpResponse response = testApplicationClient.patch(
            StubRequest.request("/root/filter/test")
                .body(json().toString()));

        Assert.assertEquals(
            TestHttpResponse.response(json().put("filterMessage", "hit handler").toString())
            .withCookie(new HttpCookie("rootFilter", "hitfilter"))
            .withCookie(new HttpCookie("pathFilter", "hitfilter")),
            response
        );
    }

    @Test
    public void shouldHandleMalformedJsonRequest() {
        testApplicationClient = createClient(mode, (r, state) -> {
            r.jsonRoute("/throw", Method.POST, state, new ThrowTestHandler());
        });

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
        testApplicationClient = createClient(mode, (r, state) -> {
            r.jsonRoute("/throw", Method.POST, state, new ThrowTestHandler());
        });

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
        testApplicationClient = createClient(mode, (r, state) -> {
            r.jsonRoute("/throw", Method.POST, state, new ThrowTestHandler());
        });

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
        testApplicationClient = createClient(mode, (r, state) -> {
            r.jsonRoute("/throw", Method.POST, state, new ThrowTestHandler());
        });

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
        testApplicationClient = createClient(mode, (r, state) -> {
            r.uploadFileRoute("/upload", Method.POST, state, new FileUploaderHandler());
        });

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
        testApplicationClient = createClient(mode, (r, state) -> {
            r.downloadFileRoute("/download", Method.GET, state, new FileDownloaderHandler(), "text/html; charset=utf-8");
        });

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
        testApplicationClient = createClient(mode, (r, state) -> {
            r.jsonRoute("/asyncMap", Method.POST, state, new AsyncMapTestHandler());
        });

        TestHttpResponse response = testApplicationClient.post(
                StubRequest.request("/asyncMap").body(json().put("value", 5).toString()));

        Assert.assertEquals(
                TestHttpResponse.response(json().put("result", 50).toString()),
                response);
    }

    @Test
    public void shouldSupportAsyncBlockingMap() {
        testApplicationClient = createClient(mode, (r, state) -> {
            r.jsonRoute("/asyncBlockingMap", Method.POST, state, new AsyncBlockingMapTestHandler());
        });

        TestHttpResponse response = testApplicationClient.post(
                StubRequest.request("/asyncBlockingMap").body(json().put("value", 3).toString()));

        Assert.assertEquals(
                TestHttpResponse.response(json().put("result", 60).toString()),
                response);
    }

    @Test
    public void shouldSupportPathParam() {
        testApplicationClient = createClient(mode, (r, state) -> {
            r.jsonRoute("/echo/:pathExample", Method.GET, state, new GetEchoHandler());
        });

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
        testApplicationClient = createClient(mode, (r, state) -> {
            r.jsonRoute("/validate/:userId", Method.POST, state, new ValidationTestHandler());
        });

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
        testApplicationClient = createClient(mode, (r, state) -> {
            r.jsonRoute("/validate/:userId", Method.POST, state, new ValidationTestHandler());
        });

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
        testApplicationClient = createClient(mode, (r, state) -> {
            r.jsonRoute("/validate/:userId", Method.POST, state, new ValidationTestHandler());
        });

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
        testApplicationClient = createClient(mode, (r, state) -> {
            r.jsonRoute("/validate/:userId", Method.POST, state, new ValidationTestHandler());
        });

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
        testApplicationClient = createClient(mode, (r, state) -> {
            r.jsonRoute("/validate/:userId", Method.POST, state, new ValidationTestHandler());
        });

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
        testApplicationClient = createClient(mode, (r, state) -> {
            r.jsonRoute("/state", Method.POST, state, new StateTestHandler());
        });

        TestHttpResponse response = testApplicationClient.post(StubRequest.request("/state").body("{}"));

        Assert.assertEquals(TestHttpResponse.response(json().put("longValue", 55).toString()), response);
    }

    @Test
    public void shouldShortCircuitWhenFilterReturnsError() {
        testApplicationClient = createClient(mode, (r, state) -> {
            r.jsonFilter("/protected/*", state, new ErrorFilter());
            r.jsonRoute("/protected/resource", Method.GET, state, new GetEchoHandler());
        });

        TestHttpResponse response = testApplicationClient.get(StubRequest.request("/protected/resource"));

        Assert.assertEquals(
            TestHttpResponse.response(json()
                .put("message", "filter blocked")
                .set("errors", json())
                .toString()).withStatusCode(401),
            response
        );
    }

    @Test
    public void shouldHandleBlockingFlatMapFailure() {
        testApplicationClient = createClient(mode, (r, state) -> {
            r.jsonRoute("/blockingFailing", Method.POST, state, new BlockingFlatMapFailHandler());
        });

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
        testApplicationClient = createClient(mode, (r, state) -> {
            r.jsonRoute("/asyncFailing", Method.POST, state, new AsyncFlatMapFailHandler());
        });

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
        testApplicationClient = createClient(mode, (r, state) -> {
            r.jsonRoute("/throw", Method.POST, state, new ThrowTestHandler());
        });

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
        testApplicationClient = createClient(mode, (r, state) -> {
            r.jsonRoute("/throw", Method.POST, state, new ThrowTestHandler());
        });

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
        testApplicationClient = createClient(mode, (r, state) -> {
            r.jsonRoute("/validate/:userId", Method.POST, state, new ValidationTestHandler());
        });

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
