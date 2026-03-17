package io.kiw.web.application.routes;

import io.kiw.web.test.*;
import org.junit.Assert;
import org.junit.Test;


import static io.kiw.web.application.routes.TestApplicationClientCreator.createApplicationClient;
import static org.junit.Assert.fail;

public class WebSocketTest {

    @Test
    public void shouldEchoWebSocketMessage() {
        TestApplicationClient client = createApplicationClient();

        TestWebSocketClient ws = client.webSocket(StubRequest.request("/ws/echo"));
        ws.send("{\"message\":\"hello\"}");

        ws.onResponses(received -> {
            Assert.assertEquals(1, received.size());
            Assert.assertEquals("{\"echo\":\"echo: hello\"}", received.get(0));

            client.assertNoMoreExceptions();
        });
    }

    @Test
    public void shouldHandleMultipleMessages() {
        TestApplicationClient client = createApplicationClient();

        TestWebSocketClient ws = client.webSocket(StubRequest.request("/ws/echo"));
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
        TestApplicationClient client = createApplicationClient();

        TestWebSocketClient ws = client.webSocket(StubRequest.request("/ws/chat/general"));

        ws.onResponses(received -> {
            Assert.assertEquals(1, received.size());
            Assert.assertEquals("{\"echo\":\"connected\"}", received.get(0));

            client.assertNoMoreExceptions();
        });
    }

    @Test
    public void shouldCloseWebSocket() {
        TestApplicationClient client = createApplicationClient();

        TestWebSocketClient ws = client.webSocket(StubRequest.request("/ws/chat/general"));
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
    public void shouldSupportPathParams() {
        TestApplicationClient client = createApplicationClient();

        TestWebSocketClient ws = client.webSocket(StubRequest.request("/ws/chat/general"));
        ws.onResponses(received -> {
            Assert.assertEquals(1, received.size());
            Assert.assertEquals("{\"echo\":\"connected\"}", received.get(0));
        });
        ws.send("{\"message\":\"hi\"}");

        ws.onResponses(received -> {
            Assert.assertEquals(1, received.size());
            Assert.assertEquals("{\"echo\":\"general: hi\"}", received.get(0));

            client.assertNoMoreExceptions();
        });
    }

    @Test
    public void shouldSupportQueryParams() {
        TestApplicationClient client = createApplicationClient();

        TestWebSocketClient ws = client.webSocket(StubRequest.request("/ws/chat/lobby").queryParam("user", "alice"));
        ws.onResponses(received -> {
            Assert.assertEquals(1, received.size());
            Assert.assertEquals("{\"echo\":\"connected\"}", received.get(0));
        });
        ws.send("{\"message\":\"hi\"}");

        ws.onResponses(received -> {
            Assert.assertEquals(1, received.size());
            Assert.assertEquals("{\"echo\":\"lobby/alice: hi\"}", received.get(0));

            client.assertNoMoreExceptions();
        });
    }

    @Test
    public void shouldThrowWhenNoWebSocketRouteMatches() {
        TestApplicationClient client = createApplicationClient();

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
        TestApplicationClient client = createApplicationClient();

        TestWebSocketClient ws = client.webSocket(StubRequest.request("/ws/echo"));
        ws.send("not valid json");

        ws.onResponses(received -> {
            Assert.assertEquals(0, received.size());

            client.assertException("Unrecognized token 'not': was expecting (JSON String, Number, Array, Object or token 'null', 'true' or 'false')\n at [Source: REDACTED (`StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION` disabled); line: 1, column: 4]");

            client.assertNoMoreExceptions();
        });
    }
}
