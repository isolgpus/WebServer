package io.kiw.luxis.web.application.routes;

import io.kiw.luxis.web.http.Method;
import io.kiw.luxis.web.test.ContextAsserter;
import io.kiw.luxis.web.test.InMemoryTransactionManager;
import io.kiw.luxis.web.test.StubRequest;
import io.kiw.luxis.web.test.TestClient;
import io.kiw.luxis.web.test.TestHttpResponse;
import io.kiw.luxis.web.test.handler.EchoRequest;
import io.kiw.luxis.web.test.handler.TransactionalHttpHandler;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static io.kiw.luxis.web.application.routes.TestApplicationClientCreator.REAL_MODE;
import static io.kiw.luxis.web.application.routes.TestApplicationClientCreator.assumeRealModeEnabled;
import static io.kiw.luxis.web.application.routes.TestApplicationClientCreator.createContextAsserter;
import static io.kiw.luxis.web.application.routes.TestApplicationClientCreator.createTestServerAndClient;
import static io.kiw.luxis.web.test.TestHelper.json;

@RunWith(Parameterized.class)
public class HttpTransactionTest {

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> modes() {
        return TestApplicationClientCreator.modes();
    }

    private final String mode;
    private TestClientAndServer testClientAndServer;

    public HttpTransactionTest(final String mode) {
        this.mode = mode;
    }

    @Before
    public void setUp() {
        if (REAL_MODE.equals(mode)) {
            assumeRealModeEnabled();
        }
    }

    @After
    public void tearDown() {
        if (testClientAndServer != null) {
            try {
                testClientAndServer.luxis().close();
            } catch (final Exception ignored) {
            }
        }
    }

    @Test
    public void shouldRunTransactionalSubChainAndFireCommitLifecycle() {
        final ContextAsserter asserter = createContextAsserter(mode);
        final InMemoryTransactionManager tm = new InMemoryTransactionManager();

        testClientAndServer = createTestServerAndClient(mode, (r, state) -> {
            r.jsonRoute("/tx", Method.POST, state, EchoRequest.class, new TransactionalHttpHandler(asserter));
        }, tm);
        final TestClient client = testClientAndServer.client();

        final TestHttpResponse response = client.post(
                StubRequest.request("/tx").body(json().put("stringExample", "hello").toString()));

        Assert.assertEquals(
                TestHttpResponse.response(json()
                        .put("intExample", 0)
                        .put("stringExample", "hello-queried-updated")
                        .putNull("pathExample")
                        .putNull("queryExample")
                        .putNull("requestHeaderExample")
                        .putNull("requestCookieExample")
                        .toString()),
                response);

        Assert.assertEquals(Arrays.asList("begin:1", "commit:1", "onCommitted:1"), tm.events());
        client.assertNoMoreExceptions();
    }

    @Test
    public void shouldRunIndependentTransactionsForSequentialRequests() {
        final ContextAsserter asserter = createContextAsserter(mode);
        final InMemoryTransactionManager tm = new InMemoryTransactionManager();

        testClientAndServer = createTestServerAndClient(mode, (r, state) -> {
            r.jsonRoute("/tx", Method.POST, state, EchoRequest.class, new TransactionalHttpHandler(asserter));
        }, tm);
        final TestClient client = testClientAndServer.client();

        client.post(StubRequest.request("/tx").body(json().put("stringExample", "first").toString()));
        client.post(StubRequest.request("/tx").body(json().put("stringExample", "second").toString()));

        Assert.assertEquals(
                Arrays.asList("begin:1", "commit:1", "onCommitted:1", "begin:2", "commit:2", "onCommitted:2"),
                tm.events());
        client.assertNoMoreExceptions();
    }

    @Test
    public void shouldRollbackWhenFlatMapReturnsResultError() {
        final ContextAsserter asserter = createContextAsserter(mode);
        final InMemoryTransactionManager tm = new InMemoryTransactionManager();

        testClientAndServer = createTestServerAndClient(mode, (r, state) -> {
            r.jsonRoute("/tx", Method.POST, state, EchoRequest.class, new RollbackOnErrorHttpHandler(asserter));
        }, tm);
        final TestClient client = testClientAndServer.client();

        final TestHttpResponse response = client.post(
                StubRequest.request("/tx").body(json().put("stringExample", "hello").toString()));

        Assert.assertEquals(
                TestHttpResponse.response(json()
                        .put("message", "sub-chain failed")
                        .set("errors", json())
                        .toString()).withStatusCode(400),
                response);

        Assert.assertEquals(Arrays.asList("begin:1", "rollback:1"), tm.events());
        client.assertNoMoreExceptions();
    }

    @Test
    public void shouldRollbackWhenAsyncMapReturnsFailedFuture() {
        final InMemoryTransactionManager tm = new InMemoryTransactionManager();

        testClientAndServer = createTestServerAndClient(mode, (r, state) -> {
            r.jsonRoute("/tx", Method.POST, state, EchoRequest.class, new AsyncMapThrowsTransactionalHttpHandler());
        }, tm);
        final TestClient client = testClientAndServer.client();

        final TestHttpResponse response = client.post(
                StubRequest.request("/tx").body(json().put("stringExample", "hello").toString()));

        Assert.assertEquals(
                TestHttpResponse.response(json().put("message", "Something went wrong").toString()).withStatusCode(500),
                response);

        Assert.assertEquals(Arrays.asList("begin:1", "rollback:1"), tm.events());
        client.assertException("async driver failed");
        client.assertNoMoreExceptions();
    }

    @Test
    public void shouldRollbackWhenSyncMapThrows() {
        final InMemoryTransactionManager tm = new InMemoryTransactionManager();

        testClientAndServer = createTestServerAndClient(mode, (r, state) -> {
            r.jsonRoute("/tx", Method.POST, state, EchoRequest.class, new MapThrowsTransactionalHttpHandler());
        }, tm);
        final TestClient client = testClientAndServer.client();

        final TestHttpResponse response = client.post(
                StubRequest.request("/tx").body(json().put("stringExample", "hello").toString()));

        Assert.assertEquals(
                TestHttpResponse.response(json().put("message", "Something went wrong").toString()).withStatusCode(500),
                response);

        Assert.assertEquals(Arrays.asList("begin:1", "rollback:1"), tm.events());
        client.assertException("sync map failed");
        client.assertNoMoreExceptions();
    }
}
