package io.kiw.luxis.web.application.routes;

import io.kiw.luxis.web.WebSocketRouteConfigBuilder;
import io.kiw.luxis.web.pipeline.DisconnectSession;
import io.kiw.luxis.web.pipeline.JustSendValidationError;
import io.kiw.luxis.web.pipeline.SendErrorResponse;
import io.kiw.luxis.web.pipeline.SendValidationErrorsAndDisconnectSession;
import io.kiw.luxis.web.test.ContextAsserter;
import io.kiw.luxis.web.test.StubRequest;
import io.kiw.luxis.web.test.TestClient;
import io.kiw.luxis.web.test.TestHelper;
import io.kiw.luxis.web.test.TestWebSocketClient;
import io.kiw.luxis.web.TestLuxis;
import io.kiw.luxis.web.pipeline.AsyncMapConfigBuilder;
import io.kiw.luxis.web.test.handler.AsyncBlockingMapWebSocketHandler;
import io.kiw.luxis.web.test.handler.AsyncFlatMapFailWebSocketHandler;
import io.kiw.luxis.web.test.handler.AsyncMapWebSocketHandler;
import io.kiw.luxis.web.test.handler.WebSocketCustomTimeoutHandler;
import io.kiw.luxis.web.test.handler.BlockingFlatMapFailWebSocketHandler;
import io.kiw.luxis.web.test.handler.BlockingMapWebSocketHandler;
import io.kiw.luxis.web.test.handler.ContextAssertingAsyncWebSocketHandler;
import io.kiw.luxis.web.test.handler.ContextAssertingWebSocketHandler;
import io.kiw.luxis.web.test.handler.EchoWebSocketHandler;
import io.kiw.luxis.web.test.handler.FlatMapFailWebSocketHandler;
import io.kiw.luxis.web.test.handler.NoResponseWebSocketHandler;
import io.kiw.luxis.web.test.handler.OnCloseTrackingWebSocketHandler;
import io.kiw.luxis.web.test.handler.StatefulWebSocketHandler;
import io.kiw.luxis.web.test.handler.ThrowWebSocketHandler;
import io.kiw.luxis.web.test.handler.ValidationWebSocketHandler;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;

import static io.kiw.luxis.web.application.routes.TestApplicationClientCreator.REAL_MODE;
import static io.kiw.luxis.web.application.routes.TestApplicationClientCreator.STUB_MODE;
import static io.kiw.luxis.web.application.routes.TestApplicationClientCreator.assumeRealModeEnabled;
import static io.kiw.luxis.web.application.routes.TestApplicationClientCreator.createClient;
import static io.kiw.luxis.web.test.TestHelper.json;
import static org.junit.Assert.fail;

@RunWith(Parameterized.class)
public class WebSocketTest {

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> modes() {
        return TestApplicationClientCreator.modes();
    }

    private final String mode;
    private TestWebSocketClient ws;
    private TestClientAndServer testClientAndServer;

    public WebSocketTest(String mode) {
        this.mode = mode;
    }

    @Before
    public void setUp() {
        if (REAL_MODE.equals(mode)) {
            assumeRealModeEnabled();
        }
    }

    @Test
    public void shouldEchoWebSocketMessage() {
        testClientAndServer = createClient(mode, (r, state) -> {
            r.webSocketRoute("/ws/echo", state, new EchoWebSocketHandler());
        });
        TestClient client = testClientAndServer.client();

        ws = client.webSocket(StubRequest.request("/ws/echo"));
        ws.send("{\"message\":\"hello\"}");

        ws.onResponses(received -> {
            Assert.assertEquals(1, received.size());
            Assert.assertEquals("{\"echo\":\"echo: hello\"}", received.get(0));

            client.assertNoMoreExceptions();
        });
    }

    @Test
    public void shouldHandleMultipleMessages() {
        testClientAndServer = createClient(mode, (r, state) -> {
            r.webSocketRoute("/ws/echo", state, new EchoWebSocketHandler());
        });
        TestClient client = testClientAndServer.client();

        ws = client.webSocket(StubRequest.request("/ws/echo"));
        ws.send("{\"message\":\"first\"}");
        ws.send("{\"message\":\"second\"}");

        ws.onResponses((received -> {
            Assert.assertEquals(2, received.size());
            Assert.assertEquals("{\"echo\":\"echo: first\"}", received.get(0));
            Assert.assertEquals("{\"echo\":\"echo: second\"}", received.get(1));

            client.assertNoMoreExceptions();
        }));
    }

    @Test
    public void shouldSendMessageOnConnect() {
        testClientAndServer = createClient(mode, (r, state) -> {
            r.webSocketRoute("/ws/chat/:room", state, new StatefulWebSocketHandler());
        });
        TestClient client = testClientAndServer.client();

        ws = client.webSocket(StubRequest.request("/ws/chat/general"));

        ws.onResponses(received -> {
            Assert.assertEquals(1, received.size());
            Assert.assertEquals("{\"echo\":\"connected\"}", received.get(0));

            client.assertNoMoreExceptions();
        });
    }

    @Test
    public void shouldCloseWebSocket() {
        testClientAndServer = createClient(mode, (r, state) -> {
            r.webSocketRoute("/ws/chat/:room", state, new StatefulWebSocketHandler());
        });
        TestClient client = testClientAndServer.client();

        ws = client.webSocket(StubRequest.request("/ws/chat/general"));
        ws.onResponses(received -> {
            Assert.assertEquals(1, received.size());
            Assert.assertEquals("{\"echo\":\"connected\"}", received.get(0));
        });
        Assert.assertFalse(ws.isClosed());
        ws.close();

        Assert.assertTrue(ws.isClosed());

        client.assertNoMoreExceptions();
    }

    @Test
    public void shouldThrowWhenNoWebSocketRouteMatches() {
        testClientAndServer = createClient(mode, (r, state) -> {
            r.webSocketRoute("/ws/echo", state, new EchoWebSocketHandler());
        });
        TestClient client = testClientAndServer.client();

        try {
            client.webSocket(StubRequest.request("/ws/nonexistent"));
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            Assert.assertEquals("No WebSocket route registered for path: /ws/nonexistent", e.getMessage());
        }
        catch (RuntimeException e) {
            Assert.assertEquals("WebSocket connection failed", e.getMessage());
        }
    }

    @Test
    public void shouldHandleInvalidJsonGracefully() {
        testClientAndServer = createClient(mode, (r, state) -> {
            r.webSocketRoute("/ws/echo", state, new EchoWebSocketHandler());
        });
        TestClient client = testClientAndServer.client();

        ws = client.webSocket(StubRequest.request("/ws/echo"));
        ws.send("not valid json");

        ws.onResponses(received -> {
            Assert.assertEquals(0, received.size());
            Assert.assertTrue(ws.isClosed());
            client.assertNoMoreExceptions();
        });
    }

    @Test
    public void shouldMapThroughBlockingCall() {
        testClientAndServer = createClient(mode, (r, state) -> {
            r.webSocketRoute("/ws/blocking", state, new BlockingMapWebSocketHandler());
        });
        TestClient client = testClientAndServer.client();

        ws = client.webSocket(StubRequest.request("/ws/blocking"));
        ws.send("{\"value\":22}");

        ws.onResponses(received -> {
            Assert.assertEquals(1, received.size());
            Assert.assertEquals("{\"result\":44}", received.get(0));

            client.assertNoMoreExceptions();
        });
    }

    @Test
    public void shouldMapThroughAsyncMap() {
        final AsyncMapWebSocketHandler handler = new AsyncMapWebSocketHandler();
        testClientAndServer = createClient(mode, (r, state) -> {
            r.webSocketRoute("/ws/asyncMap", state, handler);
        });
        handler.evillyReferenceLuxis(testClientAndServer.luxis());
        TestClient client = testClientAndServer.client();

        ws = client.webSocket(StubRequest.request("/ws/asyncMap"));
        ws.send("{\"value\":5}");

        ws.onResponses(received -> {
            Assert.assertEquals(1, received.size());
            Assert.assertEquals("{\"result\":50}", received.get(0));

            client.assertNoMoreExceptions();
        });
    }

    @Test
    public void shouldMapThroughAsyncBlockingMap() {
        final AsyncBlockingMapWebSocketHandler handler = new AsyncBlockingMapWebSocketHandler();
        testClientAndServer = createClient(mode, (r, state) -> {
            r.webSocketRoute("/ws/asyncBlockingMap", state, handler);
        });
        handler.evillyReferenceLuxis(testClientAndServer.luxis());
        TestClient client = testClientAndServer.client();

        ws = client.webSocket(StubRequest.request("/ws/asyncBlockingMap"));
        ws.send("{\"value\":3}");

        ws.onResponses(received -> {
            Assert.assertEquals(1, received.size());
            Assert.assertEquals("{\"result\":60}", received.get(0));

            client.assertNoMoreExceptions();
        });
    }

    @Test
    @Ignore
    public void shouldReturnErrorOnFlatMapFailure() {
        testClientAndServer = createClient(mode, (r, state) -> {
            r.webSocketRoute("/ws/flatMapFail", state, new FlatMapFailWebSocketHandler());
        });
        TestClient client = testClientAndServer.client();

        ws = client.webSocket(StubRequest.request("/ws/flatMapFail"));
        ws.send("{\"message\":\"hello\"}");

        ws.onResponses(received -> {
            Assert.assertEquals(1, received.size());
            Assert.assertEquals(
                json().put("message", "flatMap failed").set("errors", json()).toString(),
                received.get(0));

            client.assertNoMoreExceptions();
        });
    }

    @Test
    @Ignore
    public void shouldReturnErrorOnBlockingFlatMapFailure() {
        testClientAndServer = createClient(mode, (r, state) -> {
            r.webSocketRoute("/ws/blockingFlatMapFail", state, new BlockingFlatMapFailWebSocketHandler());
        });
        TestClient client = testClientAndServer.client();

        ws = client.webSocket(StubRequest.request("/ws/blockingFlatMapFail"));
        ws.send("{\"message\":\"hello\"}");

        ws.onResponses(received -> {
            Assert.assertEquals(1, received.size());
            Assert.assertEquals(
                json().put("message", "blocking flatMap failed").set("errors", json()).toString(),
                received.get(0));

            client.assertNoMoreExceptions();
        });
    }

    @Test
    @Ignore
    public void shouldReturnErrorOnAsyncFlatMapFailure() {
        final AsyncFlatMapFailWebSocketHandler handler = new AsyncFlatMapFailWebSocketHandler();
        testClientAndServer = createClient(mode, (r, state) -> {
            r.webSocketRoute("/ws/asyncFlatMapFail", state, handler);
        });
        handler.evillyReferenceLuxis(testClientAndServer.luxis());
        TestClient client = testClientAndServer.client();

        ws = client.webSocket(StubRequest.request("/ws/asyncFlatMapFail"));
        ws.send("{\"message\":\"hello\"}");

        ws.onResponses(received -> {
            Assert.assertEquals(1, received.size());
            Assert.assertEquals(
                json().put("message", "async flatMap failed").set("errors", json()).toString(),
                received.get(0));

            client.assertNoMoreExceptions();
        });
    }

    @Test
    public void shouldHandleExceptionInMapHandler() {
        final ThrowWebSocketHandler handler = new ThrowWebSocketHandler();
        testClientAndServer = createClient(mode, (r, state) -> {
            r.webSocketRoute("/ws/throw", state, handler);
        });
        handler.evillyReferenceLuxis(testClientAndServer.luxis());
        TestClient client = testClientAndServer.client();

        ws = client.webSocket(StubRequest.request("/ws/throw"));
        ws.send("{\"where\":\"map\"}");

        ws.onResponses(received -> {
            Assert.assertEquals(0, received.size());

            client.assertException("app error in map");
            client.assertNoMoreExceptions();
        });
    }

    @Test
    public void shouldHandleExceptionInBlockingHandler() {
        final ThrowWebSocketHandler handler = new ThrowWebSocketHandler();
        testClientAndServer = createClient(mode, (r, state) -> {
            r.webSocketRoute("/ws/throw", state, handler);
        });
        handler.evillyReferenceLuxis(testClientAndServer.luxis());
        TestClient client = testClientAndServer.client();

        ws = client.webSocket(StubRequest.request("/ws/throw"));
        ws.send("{\"where\":\"blocking\"}");

        ws.onResponses(received -> {
            Assert.assertEquals(0, received.size());

            client.assertException("app error in blocking");
            client.assertNoMoreExceptions();
        });
    }

    @Test
    public void shouldHandleExceptionInAsyncMapHandler() {
        final ThrowWebSocketHandler handler = new ThrowWebSocketHandler();
        testClientAndServer = createClient(mode, (r, state) -> {
            r.webSocketRoute("/ws/throw", state, handler);
        });
        handler.evillyReferenceLuxis(testClientAndServer.luxis());
        TestClient client = testClientAndServer.client();

        ws = client.webSocket(StubRequest.request("/ws/throw"));
        ws.send("{\"where\":\"asyncMap\"}");

        ws.onResponses(received -> {
            Assert.assertEquals(0, received.size());

            client.assertException("app error in asyncMap");
            client.assertNoMoreExceptions();
        });
    }

    @Test
    public void shouldHandleExceptionInAsyncBlockingMapHandler() {
        final ThrowWebSocketHandler handler = new ThrowWebSocketHandler();
        testClientAndServer = createClient(mode, (r, state) -> {
            r.webSocketRoute("/ws/throw", state, handler);
        });
        handler.evillyReferenceLuxis(testClientAndServer.luxis());
        TestClient client = testClientAndServer.client();

        ws = client.webSocket(StubRequest.request("/ws/throw"));
        ws.send("{\"where\":\"asyncBlockingMap\"}");

        ws.onResponses(received -> {
            Assert.assertEquals(0, received.size());

            client.assertException("app error in asyncBlockingMap");
            client.assertNoMoreExceptions();
        });
    }

    @Test
    public void shouldHandleExceptionInCompleteHandler() {
        final ThrowWebSocketHandler handler = new ThrowWebSocketHandler();
        testClientAndServer = createClient(mode, (r, state) -> {
            r.webSocketRoute("/ws/throw", state, handler);
        });
        handler.evillyReferenceLuxis(testClientAndServer.luxis());
        TestClient client = testClientAndServer.client();

        ws = client.webSocket(StubRequest.request("/ws/throw"));
        ws.send("{\"where\":\"complete\"}");

        ws.onResponses(received -> {
            Assert.assertEquals(0, received.size());

            client.assertException("app error in complete");
            client.assertNoMoreExceptions();
        });
    }

    @Test
    public void shouldNotSendResponseWhenCompleteWithNoResponse() {
        final NoResponseWebSocketHandler handler = new NoResponseWebSocketHandler();
        testClientAndServer = createClient(mode, (r, state) -> {
            r.webSocketRoute("/ws/noresponse", state, handler);
        });
        TestClient client = testClientAndServer.client();

        ws = client.webSocket(StubRequest.request("/ws/noresponse"));
        ws.send("{\"message\":\"hello\"}");

        ws.onResponses(received -> {
            Assert.assertEquals(0, received.size());
            Assert.assertTrue(handler.messageReceived);

            client.assertNoMoreExceptions();
        });
    }

    @Test
    public void shouldPassThroughAllStagesWhenNoException() {
        final ThrowWebSocketHandler handler = new ThrowWebSocketHandler();
        testClientAndServer = createClient(mode, (r, state) -> {
            r.webSocketRoute("/ws/throw", state, handler);
        });
        handler.evillyReferenceLuxis(testClientAndServer.luxis());
        TestClient client = testClientAndServer.client();

        ws = client.webSocket(StubRequest.request("/ws/throw"));
        ws.send("{\"where\":\"none\"}");

        ws.onResponses(received -> {
            Assert.assertEquals(1, received.size());
            Assert.assertEquals("{\"echo\":\"ok\"}", received.get(0));

            client.assertNoMoreExceptions();
        });
    }

    @Test
    public void shouldPassValidationAndReturnResponse() {
        testClientAndServer = createClient(mode, (r, state) -> {
            r.webSocketRoute("/ws/validate", state, new ValidationWebSocketHandler());
        });
        TestClient client = testClientAndServer.client();

        ws = client.webSocket(StubRequest.request("/ws/validate"));

        String body = json()
            .put("name", "Alice")
            .put("email", "alice@example.com")
            .put("age", 25)
            .set("address", json().put("city", "NYC").put("zip", "10001"))
            .toString();

        ws.send(body);

        ws.onResponses(received -> {
            Assert.assertEquals(1, received.size());
            Assert.assertEquals(
                json()
                    .put("name", "Alice")
                    .put("email", "alice@example.com")
                    .put("age", 25)
                    .put("city", "NYC")
                    .toString(),
                received.get(0));

            client.assertNoMoreExceptions();
        });
    }

    @Test
    public void shouldReturnValidationErrorForInvalidBodyField() {
        testClientAndServer = createClient(mode, (r, state) -> {
            r.webSocketRoute("/ws/validate", state, new ValidationWebSocketHandler());
        });
        TestClient client = testClientAndServer.client();

        ws = client.webSocket(StubRequest.request("/ws/validate"));

        String body = json()
            .putNull("name")
            .put("email", "alice@example.com")
            .put("age", 25)
            .set("address", json().put("city", "NYC").put("zip", "10001"))
            .toString();

        ws.send(body);

        ws.onResponses(received -> {
            Assert.assertEquals(1, received.size());
            Assert.assertEquals(
                json()
                    .put("message", "Validation failed")
                    .set("errors", json()
                        .set("name", TestHelper.MAPPER.createArrayNode().add("must not be blank")))
                    .toString(),
                received.get(0));

            client.assertNoMoreExceptions();
        });
    }

    @Test
    public void shouldReturnValidationErrorForInvalidEmail() {
        testClientAndServer = createClient(mode, (r, state) -> {
            r.webSocketRoute("/ws/validate", state, new ValidationWebSocketHandler());
        });
        TestClient client = testClientAndServer.client();

        ws = client.webSocket(StubRequest.request("/ws/validate"));

        String body = json()
            .put("name", "Alice")
            .put("email", "not-an-email")
            .put("age", 25)
            .set("address", json().put("city", "NYC").put("zip", "10001"))
            .toString();

        ws.send(body);

        ws.onResponses(received -> {
            Assert.assertEquals(1, received.size());
            Assert.assertEquals(
                json()
                    .put("message", "Validation failed")
                    .set("errors", json()
                        .set("email", TestHelper.MAPPER.createArrayNode().add("must be a valid email address")))
                    .toString(),
                received.get(0));

            client.assertNoMoreExceptions();
        });
    }

    @Test
    public void shouldReturnValidationErrorForNestedField() {
        testClientAndServer = createClient(mode, (r, state) -> {
            r.webSocketRoute("/ws/validate", state, new ValidationWebSocketHandler());
        });
        TestClient client = testClientAndServer.client();

        ws = client.webSocket(StubRequest.request("/ws/validate"));

        String body = json()
            .put("name", "Alice")
            .put("email", "alice@example.com")
            .put("age", 25)
            .set("address", json().put("city", "NYC").put("zip", "bad"))
            .toString();

        ws.send(body);

        ws.onResponses(received -> {
            Assert.assertEquals(1, received.size());
            Assert.assertEquals(
                json()
                    .put("message", "Validation failed")
                    .set("errors", json()
                        .set("address.zip", TestHelper.MAPPER.createArrayNode().add("must match pattern: [0-9]{5}")))
                    .toString(),
                received.get(0));

            client.assertNoMoreExceptions();
        });
    }

    @Test
    public void shouldReturnValidationErrorForMultipleFields() {
        testClientAndServer = createClient(mode, (r, state) -> {
            r.webSocketRoute("/ws/validate", state, new ValidationWebSocketHandler());
        });
        TestClient client = testClientAndServer.client();

        ws = client.webSocket(StubRequest.request("/ws/validate"));

        String body = json()
            .putNull("name")
            .put("email", "not-an-email")
            .put("age", 25)
            .set("address", json().put("city", "NYC").put("zip", "10001"))
            .toString();

        ws.send(body);

        ws.onResponses(received -> {
            Assert.assertEquals(1, received.size());
            Assert.assertEquals(
                json()
                    .put("message", "Validation failed")
                    .set("errors", json()
                        .set("name", TestHelper.MAPPER.createArrayNode().add("must not be blank"))
                        .set("email", TestHelper.MAPPER.createArrayNode().add("must be a valid email address")))
                    .toString(),
                received.get(0));

            client.assertNoMoreExceptions();
        });
    }

    @Test
    public void shouldJustSendValidationErrorByDefault() {
        testClientAndServer = createClient(mode, (r, state) -> {
            r.webSocketRoute("/ws/validate", state, new ValidationWebSocketHandler(),
                new WebSocketRouteConfigBuilder()
                    .failedValidationStrategy(JustSendValidationError.INSTANCE)
                    .build());
        });
        TestClient client = testClientAndServer.client();

        ws = client.webSocket(StubRequest.request("/ws/validate"));

        String body = json()
            .putNull("name")
            .put("email", "alice@example.com")
            .put("age", 25)
            .set("address", json().put("city", "NYC").put("zip", "10001"))
            .toString();

        ws.send(body);

        ws.onResponses(received -> {
            Assert.assertEquals(1, received.size());
            Assert.assertEquals(
                json()
                    .put("message", "Validation failed")
                    .set("errors", json()
                        .set("name", TestHelper.MAPPER.createArrayNode().add("must not be blank")))
                    .toString(),
                received.get(0));

            Assert.assertFalse(ws.isClosed());
            client.assertNoMoreExceptions();
        });
    }

    @Test
    public void shouldDisconnectSessionOnValidationFailure() {
        testClientAndServer = createClient(mode, (r, state) -> {
            r.webSocketRoute("/ws/validate", state, new ValidationWebSocketHandler(),
                new WebSocketRouteConfigBuilder()
                    .failedValidationStrategy(DisconnectSession.INSTANCE)
                    .build());
        });
        TestClient client = testClientAndServer.client();

        ws = client.webSocket(StubRequest.request("/ws/validate"));

        String body = json()
            .putNull("name")
            .put("email", "alice@example.com")
            .put("age", 25)
            .set("address", json().put("city", "NYC").put("zip", "10001"))
            .toString();

        ws.send(body);

        ws.onResponses(received -> {
            Assert.assertEquals(0, received.size());
            Assert.assertTrue(ws.isClosed());
            client.assertNoMoreExceptions();
        });
    }

    @Test
    public void shouldSendValidationErrorsAndDisconnectSession() {
        testClientAndServer = createClient(mode, (r, state) -> {
            r.webSocketRoute("/ws/validate", state, new ValidationWebSocketHandler(),
                new WebSocketRouteConfigBuilder()
                    .failedValidationStrategy(SendValidationErrorsAndDisconnectSession.INSTANCE)
                    .build());
        });
        TestClient client = testClientAndServer.client();

        ws = client.webSocket(StubRequest.request("/ws/validate"));

        String body = json()
            .putNull("name")
            .put("email", "alice@example.com")
            .put("age", 25)
            .set("address", json().put("city", "NYC").put("zip", "10001"))
            .toString();

        ws.send(body);

        ws.onResponses(received -> {
            Assert.assertEquals(1, received.size());
            Assert.assertEquals(
                json()
                    .put("message", "Validation failed")
                    .set("errors", json()
                        .set("name", TestHelper.MAPPER.createArrayNode().add("must not be blank")))
                    .toString(),
                received.get(0));

            Assert.assertTrue(ws.isClosed());
            client.assertNoMoreExceptions();
        });
    }

    @Test
    public void shouldRunMapAndBlockingMapOnCorrectContext() {
        final ContextAsserter asserter = TestApplicationClientCreator.createContextAsserter(mode);
        testClientAndServer = createClient(mode, (r, state) -> {
            r.webSocketRoute("/ws/context", state, new ContextAssertingWebSocketHandler(asserter));
        });
        TestClient client = testClientAndServer.client();

        ws = client.webSocket(StubRequest.request("/ws/context"));
        ws.send("{\"message\":\"hello\"}");

        ws.onResponses(received -> {
            Assert.assertEquals(1, received.size());
            Assert.assertEquals("{\"echo\":\"hello blocked\"}", received.get(0));

            client.assertNoMoreExceptions();
        });
    }

    @Test
    public void shouldRunAsyncMapOnCorrectContext() {
        final ContextAsserter asserter = TestApplicationClientCreator.createContextAsserter(mode);
        final ContextAssertingAsyncWebSocketHandler handler = new ContextAssertingAsyncWebSocketHandler(asserter);
        testClientAndServer = createClient(mode, (r, state) -> {
            r.webSocketRoute("/ws/context-async", state, handler);
        });
        handler.evillyReferenceLuxis(testClientAndServer.luxis());
        TestClient client = testClientAndServer.client();

        ws = client.webSocket(StubRequest.request("/ws/context-async"));
        ws.send("{\"message\":\"hello\"}");

        ws.onResponses(received -> {
            Assert.assertEquals(1, received.size());
            Assert.assertEquals("{\"echo\":\"hello asyncmap async2 async3 blocking\"}", received.get(0));

            client.assertNoMoreExceptions();
        });
    }

    @Test
    public void shouldTriggerOnCloseWhenClientDisconnects() {
        final OnCloseTrackingWebSocketHandler handler = new OnCloseTrackingWebSocketHandler();
        testClientAndServer = createClient(mode, (r, state) -> {
            r.webSocketRoute("/ws/lifecycle", state, handler);
        });
        TestClient client = testClientAndServer.client();

        ws = client.webSocket(StubRequest.request("/ws/lifecycle"));
        Assert.assertTrue(handler.onOpenCalled);
        Assert.assertFalse(handler.onCloseCalled);

        ws.close();

        Assert.assertTrue(handler.onCloseCalled);

        ws = null;
        client.assertNoMoreExceptions();
    }

    @Test
    public void shouldSendErrorResponseOnCorruptInputWhenConfigured() {
        testClientAndServer = createClient(mode, (r, state) -> {
            r.webSocketRoute("/ws/echo", state, new EchoWebSocketHandler(),
                new WebSocketRouteConfigBuilder()
                    .corruptInputStrategy(new SendErrorResponse("{\"error\":\"bad json\"}"))
                    .build());
        });
        TestClient client = testClientAndServer.client();

        ws = client.webSocket(StubRequest.request("/ws/echo"));
        ws.send("not valid json");

        ws.onResponses(received -> {
            Assert.assertEquals(1, received.size());
            Assert.assertEquals("{\"error\":\"bad json\"}", received.get(0));
            Assert.assertFalse(ws.isClosed());

            client.assertNoMoreExceptions();
        });
    }

    @Test
    public void shouldDisconnectOnCorruptInputByDefault() {
        testClientAndServer = createClient(mode, (r, state) -> {
            r.webSocketRoute("/ws/echo", state, new EchoWebSocketHandler());
        });
        TestClient client = testClientAndServer.client();

        ws = client.webSocket(StubRequest.request("/ws/echo"));
        ws.send("not valid json");

        ws.onResponses(received -> {
            Assert.assertEquals(0, received.size());
            Assert.assertTrue(ws.isClosed());
            client.assertNoMoreExceptions();
        });
    }

    @Test
    public void shouldTimeoutWebSocketWithCustomOneSecondTimeout() {
        final WebSocketCustomTimeoutHandler handler = new WebSocketCustomTimeoutHandler();

        testClientAndServer = createClient(mode, (r, state) -> {
            r.webSocketRoute("/ws/customTimeout", state, handler);
        });

        if (STUB_MODE.equals(mode)) {
            handler.setOnRegistered(() -> ((TestLuxis<?>) testClientAndServer.luxis()).advanceTimeBy(1_001));
        }

        TestClient client = testClientAndServer.client();

        ws = client.webSocket(StubRequest.request("/ws/customTimeout"));
        ws.send("{\"value\":1}");

        ws.onResponses(received -> {
            Assert.assertEquals(1, received.size());
            Assert.assertEquals(
                json().put("message", "Something went wrong").set("errors", json()).toString(),
                received.get(0));

            client.assertException("Correlated async response timed out");
        });
    }

    @After
    public void tearDown() throws Exception {
        if (ws != null) {
            ws.close();
        }
        if (testClientAndServer != null) {
            testClientAndServer.client().assertNoMoreExceptions();
            testClientAndServer.close();
        }
    }
}
