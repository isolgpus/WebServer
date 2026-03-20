package io.kiw.web.application.routes;

import io.kiw.web.test.*;
import io.kiw.web.test.handler.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;

import static io.kiw.web.application.routes.TestApplicationClientCreator.*;
import static io.kiw.web.test.TestHelper.json;
import static org.junit.Assert.fail;

@RunWith(Parameterized.class)
@Ignore
public class WebSocketTest {

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> modes() {
        return TestApplicationClientCreator.modes();
    }

    private final String mode;
    private TestWebSocketClient ws;
    private TestApplicationClient client;

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
        client = createClient(mode, (r, state) -> {
            r.webSocketRoute("/ws/echo", state, new EchoWebSocketHandler());
        });

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
        client = createClient(mode, (r, state) -> {
            r.webSocketRoute("/ws/echo", state, new EchoWebSocketHandler());
        });

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
        client = createClient(mode, (r, state) -> {
            r.webSocketRoute("/ws/chat/:room", state, new StatefulWebSocketHandler());
        });

        ws = client.webSocket(StubRequest.request("/ws/chat/general"));

        ws.onResponses(received -> {
            Assert.assertEquals(1, received.size());
            Assert.assertEquals("{\"echo\":\"connected\"}", received.get(0));

            client.assertNoMoreExceptions();
        });
    }

    @Test
    public void shouldCloseWebSocket() {
        client = createClient(mode, (r, state) -> {
            r.webSocketRoute("/ws/chat/:room", state, new StatefulWebSocketHandler());
        });

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
        client = createClient(mode, (r, state) -> {
            r.webSocketRoute("/ws/echo", state, new EchoWebSocketHandler());
        });

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
        client = createClient(mode, (r, state) -> {
            r.webSocketRoute("/ws/echo", state, new EchoWebSocketHandler());
        });

        ws = client.webSocket(StubRequest.request("/ws/echo"));
        ws.send("not valid json");

        ws.onResponses(received -> {
            Assert.assertEquals(0, received.size());

            client.assertException("Unrecognized token 'not': was expecting (JSON String, Number, Array, Object or token 'null', 'true' or 'false')\n at [Source: REDACTED (`StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION` disabled); line: 1, column: 4]");

            client.assertNoMoreExceptions();
        });
    }

    @Test
    public void shouldMapThroughBlockingCall() {
        client = createClient(mode, (r, state) -> {
            r.webSocketRoute("/ws/blocking", state, new BlockingMapWebSocketHandler());
        });

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
        client = createClient(mode, (r, state) -> {
            r.webSocketRoute("/ws/asyncMap", state, new AsyncMapWebSocketHandler());
        });

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
        client = createClient(mode, (r, state) -> {
            r.webSocketRoute("/ws/asyncBlockingMap", state, new AsyncBlockingMapWebSocketHandler());
        });

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
        client = createClient(mode, (r, state) -> {
            r.webSocketRoute("/ws/flatMapFail", state, new FlatMapFailWebSocketHandler());
        });

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
        client = createClient(mode, (r, state) -> {
            r.webSocketRoute("/ws/blockingFlatMapFail", state, new BlockingFlatMapFailWebSocketHandler());
        });

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
        client = createClient(mode, (r, state) -> {
            r.webSocketRoute("/ws/asyncFlatMapFail", state, new AsyncFlatMapFailWebSocketHandler());
        });

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
        client = createClient(mode, (r, state) -> {
            r.webSocketRoute("/ws/throw", state, new ThrowWebSocketHandler());
        });

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
        client = createClient(mode, (r, state) -> {
            r.webSocketRoute("/ws/throw", state, new ThrowWebSocketHandler());
        });

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
        client = createClient(mode, (r, state) -> {
            r.webSocketRoute("/ws/throw", state, new ThrowWebSocketHandler());
        });

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
        client = createClient(mode, (r, state) -> {
            r.webSocketRoute("/ws/throw", state, new ThrowWebSocketHandler());
        });

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
        client = createClient(mode, (r, state) -> {
            r.webSocketRoute("/ws/throw", state, new ThrowWebSocketHandler());
        });

        ws = client.webSocket(StubRequest.request("/ws/throw"));
        ws.send("{\"where\":\"complete\"}");

        ws.onResponses(received -> {
            Assert.assertEquals(0, received.size());

            client.assertException("app error in complete");
            client.assertNoMoreExceptions();
        });
    }

    @Test
    public void shouldPassThroughAllStagesWhenNoException() {
        client = createClient(mode, (r, state) -> {
            r.webSocketRoute("/ws/throw", state, new ThrowWebSocketHandler());
        });

        ws = client.webSocket(StubRequest.request("/ws/throw"));
        ws.send("{\"where\":\"none\"}");

        ws.onResponses(received -> {
            Assert.assertEquals(1, received.size());
            Assert.assertEquals("{\"echo\":\"ok\"}", received.get(0));

            client.assertNoMoreExceptions();
        });
    }

    @After
    public void tearDown() {
        if (ws != null) {
            ws.close();
        }
        if (client != null) {
            client.stop();
        }
    }
}
