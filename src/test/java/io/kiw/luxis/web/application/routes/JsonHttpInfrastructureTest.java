package io.kiw.luxis.web.application.routes;

import io.kiw.luxis.web.http.HttpCookie;
import io.kiw.luxis.web.http.Method;
import io.kiw.luxis.web.test.*;
import io.kiw.luxis.web.test.handler.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;

import static io.kiw.luxis.web.application.routes.TestApplicationClientCreator.*;
import static io.kiw.luxis.web.test.TestHelper.json;

@RunWith(Parameterized.class)
public class JsonHttpInfrastructureTest {

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> modes() {
        return TestApplicationClientCreator.modes();
    }

    private final String mode;
    private TestClientAndServer testClientAndServer;
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
        if (testClientAndServer != null) {
            testClientAndServer.client().assertNoMoreExceptions();
            testClientAndServer.close();
        }
    }

    @Test
    public void shouldHandlePopulatingJsonValues() {
        testClientAndServer = createClient(mode, (r, state) -> {
            r.jsonRoute("/echo", Method.POST, state, new PostEchoHandler());
        });
        TestClient luxisTestClient = testClientAndServer.client();

        final String requestBody = json()
                .put("intExample", 17)
                .put("stringExample", "hiya")
                .toString();

        TestHttpResponse response = luxisTestClient.post(StubRequest.request("/echo").body(requestBody));

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
        testClientAndServer = createClient(mode, (r, state) -> {
            r.jsonRoute("/echo", Method.PUT, state, new PostEchoHandler());
        });
        TestClient luxisTestClient = testClientAndServer.client();

        TestHttpResponse response = luxisTestClient.put(
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
        testClientAndServer = createClient(mode, (r, state) -> {
            r.jsonRoute("/echo", Method.POST, state, new PostEchoHandler());
        });
        TestClient luxisTestClient = testClientAndServer.client();

        TestHttpResponse response = luxisTestClient.post(
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
        testClientAndServer = createClient(mode, (r, state) -> {
            r.jsonRoute("/echo", Method.PUT, state, new PostEchoHandler());
        });
        TestClient luxisTestClient = testClientAndServer.client();

        TestHttpResponse response = luxisTestClient.put(
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
        testClientAndServer = createClient(mode, (r, state) -> {
            r.jsonRoute("/echo", Method.DELETE, state, new PostEchoHandler());
        });
        TestClient luxisTestClient = testClientAndServer.client();

        TestHttpResponse response = luxisTestClient.delete(
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
        testClientAndServer = createClient(mode, (r, state) -> {
            r.jsonRoute("/echo", Method.PATCH, state, new PostEchoHandler());
        });
        TestClient luxisTestClient = testClientAndServer.client();

        TestHttpResponse response = luxisTestClient.patch(
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
        testClientAndServer = createClient(mode, (r, state) -> {
            r.jsonRoute("/echo", Method.GET, state, new GetEchoHandler());
        });
        TestClient luxisTestClient = testClientAndServer.client();

        TestHttpResponse response = luxisTestClient.get(
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
        testClientAndServer = createClient(mode, (r, state) -> {
            r.jsonRoute("/echo", Method.GET, state, new GetEchoHandler());
        });
        TestClient luxisTestClient = testClientAndServer.client();

        TestHttpResponse response = luxisTestClient.get(
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
        testClientAndServer = createClient(mode, (r, state) -> {
            r.jsonRoute("/echo", Method.POST, state, new PostEchoHandler());
        });
        TestClient luxisTestClient = testClientAndServer.client();

        final String requestBody = json()
                .put("intExample", 17)
                .put("stringExample", "hiya")
                .putNull("pathExample")
                .put("something", "else")
                .toString();

        TestHttpResponse response = luxisTestClient.post(
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
        testClientAndServer = createClient(mode, (r, state) -> {
            r.jsonRoute("/echo", Method.POST, state, new PostEchoHandler());
        });
        TestClient luxisTestClient = testClientAndServer.client();

        TestHttpResponse response = luxisTestClient.post(StubRequest.request("/echo"));

        final String expectedResponse = json()
                .put("message", "Invalid json request")
                .set("errors", json())
                .toString();

        Assert.assertEquals(TestHttpResponse.response(expectedResponse).withStatusCode(400), response);
    }

    @Test
    public void shouldCallGetRoute() {
        testClientAndServer = createClient(mode, (r, state) -> {
            r.jsonRoute("/echo", Method.GET, state, new GetEchoHandler());
        });
        TestClient luxisTestClient = testClientAndServer.client();

        TestHttpResponse response = luxisTestClient.get(StubRequest.request("/echo"));

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
        testClientAndServer = createClient(mode, (r, state) -> {
            r.jsonRoute("/echo", Method.POST, state, new PostEchoHandler());
        });
        TestClient luxisTestClient = testClientAndServer.client();

        final String request = json()
                .put("responseHeaderExample", "responseTest")
                .toString();

        TestHttpResponse response = luxisTestClient.post(StubRequest.request("/echo")
                .body(request));

        Assert.assertEquals(TestHttpResponse.response(DEFAULT_POST_RESPONSE)
                .withHeader("responseHeaderExample", "responseTest"), response);
    }


    @Test
    public void shouldReadRequestCookies() {
        testClientAndServer = createClient(mode, (r, state) -> {
            r.jsonRoute("/echo", Method.POST, state, new PostEchoHandler());
        });
        TestClient luxisTestClient = testClientAndServer.client();

        TestHttpResponse response = luxisTestClient.post(
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
        testClientAndServer = createClient(mode, (r, state) -> {
            r.jsonRoute("/echo", Method.POST, state, new PostEchoHandler());
        });
        TestClient luxisTestClient = testClientAndServer.client();

        TestHttpResponse response = luxisTestClient.post(
                StubRequest.request("/echo")
                        .body(json().put("responseCookieExample", "responseCookieTest").toString()));

        Assert.assertEquals(TestHttpResponse.response(DEFAULT_POST_RESPONSE)
                .withCookie(new HttpCookie("responseCookieExample", "responseCookieTest")), response);
    }

    @Test
    public void shouldMapThroughABlockingCall() {
        testClientAndServer = createClient(mode, (r, state) -> {
            r.jsonRoute("/blocking", Method.POST, state, new BlockingTestHandler());
        });
        TestClient luxisTestClient = testClientAndServer.client();

        TestHttpResponse response = luxisTestClient.post(
            StubRequest.request("/blocking")
                .body(json().put("numberToMultiply", 22).toString()));

        Assert.assertEquals(TestHttpResponse.response(json().put("multipliedNumber", 44).toString()), response);
    }

    @Test
    public void shouldMapThroughABlockingCompleteCall() {
        testClientAndServer = createClient(mode, (r, state) -> {
            r.jsonRoute("/blockingComplete", Method.POST, state, new BlockingCompleteTestHandler());
        });
        TestClient luxisTestClient = testClientAndServer.client();

        TestHttpResponse response = luxisTestClient.post(
                StubRequest.request("/blockingComplete")
                        .body(json().put("numberToMultiply", 22).toString()));

        Assert.assertEquals(TestHttpResponse.response(json().put("multipliedNumber", 44).toString()), response);
    }

    @Test
    public void shouldReturnWithErrorOnBadRequest() {
        testClientAndServer = createClient(mode, (r, state) -> {
            r.jsonRoute("/failing", Method.POST, state, new FailingTestHandler());
        });
        TestClient luxisTestClient = testClientAndServer.client();

        TestHttpResponse response = luxisTestClient.post(
            StubRequest.request("/failing")
                .body(json().put("numberToMultiply", 22).toString()));

        Assert.assertEquals(TestHttpResponse.response(json()
                .put("message", "intentionally failed")
                .set("errors", json())
                .toString()).withStatusCode(400), response);
    }


    @Test
    public void shouldAccessRequestBodyInHandlerWhenRoutedThroughFilter() {
        testClientAndServer = createClient(mode, (r, state) -> {
            r.jsonFilter("/root/*", state, new TestFilter("rootFilter"));
            r.jsonFilter("/root/filter/*", state, new TestFilter("pathFilter"));
            r.jsonRoute("/root/filter/echo", Method.POST, state, new PostEchoHandler());
        });
        TestClient luxisTestClient = testClientAndServer.client();

        String requestBody = json()
            .put("intExample", 42)
            .put("stringExample", "through filter")
            .toString();

        TestHttpResponse response = luxisTestClient.post(
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
        testClientAndServer = createClient(mode, (r, state) -> {
            r.jsonFilter("/root/*", state, new TestFilter("rootFilter"));
            r.jsonFilter("/root/filter/*", state, new TestFilter("pathFilter"));
            r.jsonRoute("/root/filter/test", Method.POST, state, new TestFilterHandler());
        });
        TestClient luxisTestClient = testClientAndServer.client();

        TestHttpResponse response = luxisTestClient.post(
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
        testClientAndServer = createClient(mode, (r, state) -> {
            r.jsonFilter("/root/*", state, new TestFilter("rootFilter"));
            r.jsonFilter("/root/filter/*", state, new TestFilter("pathFilter"));
            r.jsonRoute("/root/filter/test", Method.GET, state, new GetTestFilterHandler());
        });
        TestClient luxisTestClient = testClientAndServer.client();

        TestHttpResponse response = luxisTestClient.get(
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
        testClientAndServer = createClient(mode, (r, state) -> {
            r.jsonFilter("/root/*", state, new TestFilter("rootFilter"));
            r.jsonFilter("/root/filter/*", state, new TestFilter("pathFilter"));
            r.jsonRoute("/root/filter/test", Method.PUT, state, new TestFilterHandler());
        });
        TestClient luxisTestClient = testClientAndServer.client();

        TestHttpResponse response = luxisTestClient.put(
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
        testClientAndServer = createClient(mode, (r, state) -> {
            r.jsonFilter("/root/*", state, new TestFilter("rootFilter"));
            r.jsonFilter("/root/filter/*", state, new TestFilter("pathFilter"));
            r.jsonRoute("/root/filter/test", Method.DELETE, state, new TestFilterHandler());
        });
        TestClient luxisTestClient = testClientAndServer.client();

        TestHttpResponse response = luxisTestClient.delete(
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
        testClientAndServer = createClient(mode, (r, state) -> {
            r.jsonFilter("/root/*", state, new TestFilter("rootFilter"));
            r.jsonFilter("/root/filter/*", state, new TestFilter("pathFilter"));
            r.jsonRoute("/root/filter/test", Method.PATCH, state, new TestFilterHandler());
        });
        TestClient luxisTestClient = testClientAndServer.client();

        TestHttpResponse response = luxisTestClient.patch(
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
        testClientAndServer = createClient(mode, (r, state) -> {
            r.jsonRoute("/throw", Method.POST, state, new ThrowTestHandler());
        });
        TestClient luxisTestClient = testClientAndServer.client();

        TestHttpResponse response = luxisTestClient.post(
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
        testClientAndServer = createClient(mode, (r, state) -> {
            r.jsonRoute("/throw", Method.POST, state, new ThrowTestHandler());
        });
        TestClient luxisTestClient = testClientAndServer.client();

        TestHttpResponse response = luxisTestClient.post(
            StubRequest.request("/throw")
                .body(json().put("where", "complete").toString()));

        Assert.assertEquals(
            TestHttpResponse.response(json().put("message", "Something went wrong").toString()).withStatusCode(500),
            response
        );

        luxisTestClient.assertException("app error in complete");
    }

    @Test
    public void shouldHandleItWhenThrowingAnExceptionInMapHandler() {
        testClientAndServer = createClient(mode, (r, state) -> {
            r.jsonRoute("/throw", Method.POST, state, new ThrowTestHandler());
        });
        TestClient luxisTestClient = testClientAndServer.client();

        TestHttpResponse response = luxisTestClient.post(
            StubRequest.request("/throw")
                .body(json().put("where", "map").toString()));

        Assert.assertEquals(
            TestHttpResponse.response(json().put("message", "Something went wrong").toString()).withStatusCode(500),
            response
        );

        luxisTestClient.assertException("app error in map");
    }

    @Test
    public void shouldHandleItWhenThrowingAnExceptionInBlockingHandler() {
        testClientAndServer = createClient(mode, (r, state) -> {
            r.jsonRoute("/throw", Method.POST, state, new ThrowTestHandler());
        });
        TestClient luxisTestClient = testClientAndServer.client();

        TestHttpResponse response = luxisTestClient.post(
            StubRequest.request("/throw")
                .body(json().put("where", "blocking").toString()));

        Assert.assertEquals(
            TestHttpResponse.response(json().put("message", "Something went wrong").toString()).withStatusCode(500),
            response
        );

        luxisTestClient.assertException("app error in blocking");
    }

    @Test
    public void shouldUploadAFile() {
        testClientAndServer = createClient(mode, (r, state) -> {
            r.uploadFileRoute("/upload", Method.POST, state, new FileUploaderHandler());
        });
        TestClient luxisTestClient = testClientAndServer.client();

        TestHttpResponse response = luxisTestClient.post(
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
        testClientAndServer = createClient(mode, (r, state) -> {
            r.downloadFileRoute("/download", Method.GET, state, new FileDownloaderHandler(), "text/html; charset=utf-8");
        });
        TestClient luxisTestClient = testClientAndServer.client();

        TestHttpResponse response = luxisTestClient.get(StubRequest.request("/download"));

        Assert.assertEquals(
            TestHttpResponse.response(TestHelper.file("file contents"), "text/html; charset=utf-8")
                .withHeader("Transfer-Encoding", "chunked")
                .withHeader("Content-Disposition", "data.txt"),
            response
        );
    }


    @Test
    public void shouldSupportAsyncMap() {
        testClientAndServer = createClient(mode, (r, state) -> {
            r.jsonRoute("/asyncMap", Method.POST, state, new AsyncMapTestHandler());
        });
        TestClient luxisTestClient = testClientAndServer.client();

        TestHttpResponse response = luxisTestClient.post(
                StubRequest.request("/asyncMap").body(json().put("value", 5).toString()));

        Assert.assertEquals(
                TestHttpResponse.response(json().put("result", 50).toString()),
                response);
    }

    @Test
    public void shouldSupportAsyncBlockingMap() {
        testClientAndServer = createClient(mode, (r, state) -> {
            r.jsonRoute("/asyncBlockingMap", Method.POST, state, new AsyncBlockingMapTestHandler());
        });
        TestClient luxisTestClient = testClientAndServer.client();

        TestHttpResponse response = luxisTestClient.post(
                StubRequest.request("/asyncBlockingMap").body(json().put("value", 3).toString()));

        Assert.assertEquals(
                TestHttpResponse.response(json().put("result", 60).toString()),
                response);
    }

    @Test
    public void shouldSupportPathParam() {
        testClientAndServer = createClient(mode, (r, state) -> {
            r.jsonRoute("/echo/:pathExample", Method.GET, state, new GetEchoHandler());
        });
        TestClient luxisTestClient = testClientAndServer.client();

        TestHttpResponse response = luxisTestClient.get(
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
        testClientAndServer = createClient(mode, (r, state) -> {
            r.jsonRoute("/validate/:userId", Method.POST, state, new ValidationTestHandler());
        });
        TestClient luxisTestClient = testClientAndServer.client();

        String body = json()
            .put("name", "Alice")
            .put("email", "alice@example.com")
            .put("age", 25)
            .set("address", json().put("city", "NYC").put("zip", "10001"))
            .toString();

        TestHttpResponse response = luxisTestClient.post(
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
        testClientAndServer = createClient(mode, (r, state) -> {
            r.jsonRoute("/validate/:userId", Method.POST, state, new ValidationTestHandler());
        });
        TestClient luxisTestClient = testClientAndServer.client();

        String body = json()
            .putNull("name")
            .put("email", "alice@example.com")
            .put("age", 25)
            .set("address", json().put("city", "NYC").put("zip", "10001"))
            .toString();

        TestHttpResponse response = luxisTestClient.post(
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
        testClientAndServer = createClient(mode, (r, state) -> {
            r.jsonRoute("/validate/:userId", Method.POST, state, new ValidationTestHandler());
        });
        TestClient luxisTestClient = testClientAndServer.client();

        String body = json()
            .put("name", "Alice")
            .put("email", "not-an-email")
            .put("age", 25)
            .set("address", json().put("city", "NYC").put("zip", "10001"))
            .toString();

        TestHttpResponse response = luxisTestClient.post(
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
        testClientAndServer = createClient(mode, (r, state) -> {
            r.jsonRoute("/validate/:userId", Method.POST, state, new ValidationTestHandler());
        });
        TestClient luxisTestClient = testClientAndServer.client();

        String body = json()
            .put("name", "Alice")
            .put("email", "alice@example.com")
            .put("age", 25)
            .set("address", json().put("city", "NYC").put("zip", "bad"))
            .toString();

        TestHttpResponse response = luxisTestClient.post(
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
        testClientAndServer = createClient(mode, (r, state) -> {
            r.jsonRoute("/validate/:userId", Method.POST, state, new ValidationTestHandler());
        });
        TestClient luxisTestClient = testClientAndServer.client();

        String body = json()
            .put("name", "Alice")
            .put("email", "alice@example.com")
            .put("age", 25)
            .set("address", json().put("city", "NYC").put("zip", "10001"))
            .toString();

        TestHttpResponse response = luxisTestClient.post(
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
        testClientAndServer = createClient(mode, (r, state) -> {
            r.jsonRoute("/state", Method.POST, state, new StateTestHandler());
        });
        TestClient luxisTestClient = testClientAndServer.client();

        TestHttpResponse response = luxisTestClient.post(StubRequest.request("/state").body("{}"));

        Assert.assertEquals(TestHttpResponse.response(json().put("longValue", 55).toString()), response);
    }

    @Test
    public void shouldShortCircuitWhenFilterReturnsError() {
        testClientAndServer = createClient(mode, (r, state) -> {
            r.jsonFilter("/protected/*", state, new ErrorFilter());
            r.jsonRoute("/protected/resource", Method.GET, state, new GetEchoHandler());
        });
        TestClient luxisTestClient = testClientAndServer.client();

        TestHttpResponse response = luxisTestClient.get(StubRequest.request("/protected/resource"));

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
        testClientAndServer = createClient(mode, (r, state) -> {
            r.jsonRoute("/blockingFailing", Method.POST, state, new BlockingFlatMapFailHandler());
        });
        TestClient luxisTestClient = testClientAndServer.client();

        TestHttpResponse response = luxisTestClient.post(
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
        testClientAndServer = createClient(mode, (r, state) -> {
            r.jsonRoute("/asyncFailing", Method.POST, state, new AsyncFlatMapFailHandler());
        });
        TestClient luxisTestClient = testClientAndServer.client();

        TestHttpResponse response = luxisTestClient.post(
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
        testClientAndServer = createClient(mode, (r, state) -> {
            r.jsonRoute("/throw", Method.POST, state, new ThrowTestHandler());
        });
        TestClient luxisTestClient = testClientAndServer.client();

        TestHttpResponse response = luxisTestClient.post(
            StubRequest.request("/throw").body(json().put("where", "asyncMap").toString()));

        Assert.assertEquals(
            TestHttpResponse.response(json().put("message", "Something went wrong").toString()).withStatusCode(500),
            response
        );

        luxisTestClient.assertException("app error in asyncMap");
    }

    @Test
    public void shouldHandleExceptionInAsyncBlockingMapHandler() {
        testClientAndServer = createClient(mode, (r, state) -> {
            r.jsonRoute("/throw", Method.POST, state, new ThrowTestHandler());
        });
        TestClient luxisTestClient = testClientAndServer.client();

        TestHttpResponse response = luxisTestClient.post(
            StubRequest.request("/throw").body(json().put("where", "asyncBlockingMap").toString()));

        Assert.assertEquals(
            TestHttpResponse.response(json().put("message", "Something went wrong").toString()).withStatusCode(500),
            response
        );

        luxisTestClient.assertException("app error in asyncBlockingMap");
    }

    @Test
    public void shouldReturnValidationErrorForInvalidPathParam() {
        testClientAndServer = createClient(mode, (r, state) -> {
            r.jsonRoute("/validate/:userId", Method.POST, state, new ValidationTestHandler());
        });
        TestClient luxisTestClient = testClientAndServer.client();

        String body = json()
            .put("name", "Alice")
            .put("email", "alice@example.com")
            .put("age", 25)
            .set("address", json().put("city", "NYC").put("zip", "10001"))
            .toString();

        TestHttpResponse response = luxisTestClient.post(
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
