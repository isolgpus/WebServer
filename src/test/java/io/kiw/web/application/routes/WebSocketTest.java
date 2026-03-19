package io.kiw.web.application.routes;

import io.kiw.web.test.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;

import static io.kiw.web.application.routes.TestApplicationClientCreator.*;
import static org.junit.Assert.fail;

@RunWith(Parameterized.class)
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
        client = createClient(mode);

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
        client = createClient(mode);

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
        client = createClient(mode);

        ws = client.webSocket(StubRequest.request("/ws/chat/general"));

        ws.onResponses(received -> {
            Assert.assertEquals(1, received.size());
            Assert.assertEquals("{\"echo\":\"connected\"}", received.get(0));

            client.assertNoMoreExceptions();
        });
    }

    @Test
    public void shouldCloseWebSocket() {
        client = createClient(mode);

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
        client = createClient(mode);

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
        client = createClient(mode);

        ws = client.webSocket(StubRequest.request("/ws/echo"));
        ws.send("not valid json");

        ws.onResponses(received -> {
            Assert.assertEquals(0, received.size());

            client.assertException("Unrecognized token 'not': was expecting (JSON String, Number, Array, Object or token 'null', 'true' or 'false')\n at [Source: REDACTED (`StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION` disabled); line: 1, column: 4]");

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
