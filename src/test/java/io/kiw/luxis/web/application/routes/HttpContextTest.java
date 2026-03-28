package io.kiw.luxis.web.application.routes;

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
public class HttpContextTest {

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> modes() {
        return TestApplicationClientCreator.modes();
    }

    private final String mode;
    private TestClientAndServer testClientAndServer;

    public HttpContextTest(String mode) {
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
    public void shouldRunAllPipelineStagesOnCorrectContext() {
        final ContextAsserter asserter = TestApplicationClientCreator.createContextAsserter(mode);
        testClientAndServer = createClient(mode, (r, state) -> {
            r.jsonRoute("/context", Method.POST, state, new ContextAssertingHttpHandler(asserter));
        });
        TestClient client = testClientAndServer.client();

        final String requestBody = json()
                .put("message", "hello")
                .toString();

        TestHttpResponse response = client.post(StubRequest.request("/context").body(requestBody));

        final String expectedResponse = json()
                .put("result", "hello flatMap async blockingFlatMap map blocking2 async2")
                .toString();

        Assert.assertEquals(TestHttpResponse.response(expectedResponse), response);
    }

    @Test
    public void shouldRunAsyncBlockingMapOnCorrectContext() {
        final ContextAsserter asserter = TestApplicationClientCreator.createContextAsserter(mode);
        testClientAndServer = createClient(mode, (r, state) -> {
            r.jsonRoute("/context-async-blocking", Method.POST, state, new ContextAssertingAsyncBlockingHttpHandler(asserter));
        });
        TestClient client = testClientAndServer.client();

        final String requestBody = json()
                .put("message", "hello")
                .toString();

        TestHttpResponse response = client.post(StubRequest.request("/context-async-blocking").body(requestBody));

        final String expectedResponse = json()
                .put("result", "hello asyncBlocking map blocking asyncBlocking2 async")
                .toString();

        Assert.assertEquals(TestHttpResponse.response(expectedResponse), response);
    }

    @Test
    public void shouldRunCorrelatedAsyncOnCorrectContext() {
        final ContextAsserter asserter = TestApplicationClientCreator.createContextAsserter(mode);
        final ContextAssertingCorrelatedAsyncHttpHandler handler = new ContextAssertingCorrelatedAsyncHttpHandler(asserter);
        testClientAndServer = createClient(mode, (r, state) -> {
            r.jsonRoute("/context-correlated", Method.POST, state, handler);
        });
        handler.evillyReferenceLuxis(testClientAndServer.luxis());
        TestClient client = testClientAndServer.client();

        final String requestBody = json()
                .put("message", "hello")
                .toString();

        TestHttpResponse response = client.post(StubRequest.request("/context-correlated").body(requestBody));

        final String expectedResponse = json()
                .put("result", "hello correlatedAsync blocking correlatedAsyncBlocking map")
                .toString();

        Assert.assertEquals(TestHttpResponse.response(expectedResponse), response);
    }
}
