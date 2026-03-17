package io.kiw.web.application.routes;

import io.kiw.web.test.*;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

import static io.kiw.web.application.routes.TestApplicationClientCreator.createApplicationClient;
import static org.junit.Assert.fail;

public class WebSocketTest {

    @Test
    public void shouldEchoWebSocketMessage() {
        ApplicationClient client = createApplicationClient();

        WebSocketClient ws = client.webSocket(StubRequest.request("/ws/echo"));
        ws.send("{\"message\":\"hello\"}");

        List<String> received = ws.received();
        Assert.assertEquals(1, received.size());
        Assert.assertEquals("{\"echo\":\"echo: hello\"}", received.get(0));

        client.assertNoMoreExceptions();
    }

    @Test
    public void shouldHandleMultipleMessages() {
        ApplicationClient client = createApplicationClient();

        WebSocketClient ws = client.webSocket(StubRequest.request("/ws/echo"));
        ws.send("{\"message\":\"first\"}");
        ws.send("{\"message\":\"second\"}");

        List<String> received = ws.received();
        Assert.assertEquals(2, received.size());
        Assert.assertEquals("{\"echo\":\"echo: first\"}", received.get(0));
        Assert.assertEquals("{\"echo\":\"echo: second\"}", received.get(1));

        client.assertNoMoreExceptions();
    }

    @Test
    public void shouldSendMessageOnConnect() throws InterruptedException {
        ApplicationClient client = createApplicationClient();

        WebSocketClient ws = client.webSocket(StubRequest.request("/ws/chat/general"));

        List<String> received = ws.received();
        Assert.assertEquals(1, received.size());
        Assert.assertEquals("{\"echo\":\"connected\"}", received.get(0));

        client.assertNoMoreExceptions();
    }

    @Test
    public void shouldSendMessageOnClose() {
        ApplicationClient client = createApplicationClient();

        WebSocketClient ws = client.webSocket(StubRequest.request("/ws/chat/general"));
        ws.received(); // consume connect message
        ws.close();

        List<String> received = ws.received();
        Assert.assertEquals(1, received.size());
        Assert.assertEquals("{\"echo\":\"disconnected\"}", received.get(0));

        client.assertNoMoreExceptions();
    }

    @Test
    public void shouldSupportPathParams() {
        ApplicationClient client = createApplicationClient();

        WebSocketClient ws = client.webSocket(StubRequest.request("/ws/chat/general"));
        ws.received(); // consume connect message
        ws.send("{\"message\":\"hi\"}");

        List<String> received = ws.received();
        Assert.assertEquals(1, received.size());
        Assert.assertEquals("{\"echo\":\"general: hi\"}", received.get(0));

        client.assertNoMoreExceptions();
    }

    @Test
    public void shouldSupportQueryParams() {
        ApplicationClient client = createApplicationClient();

        WebSocketClient ws = client.webSocket(StubRequest.request("/ws/chat/lobby").queryParam("user", "alice"));
        ws.received(); // consume connect message
        ws.send("{\"message\":\"hi\"}");

        List<String> received = ws.received();
        Assert.assertEquals(1, received.size());
        Assert.assertEquals("{\"echo\":\"lobby/alice: hi\"}", received.get(0));

        client.assertNoMoreExceptions();
    }

    @Test
    public void shouldThrowWhenNoWebSocketRouteMatches() {
        ApplicationClient client = new TestApplicationClient(routesRegister -> TestApplicationRoutes.registerRoutes(routesRegister, new MyApplicationState()));

        try {
            client.webSocket(StubRequest.request("/ws/nonexistent"));
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            Assert.assertEquals("No WebSocket route registered for path: /ws/nonexistent", e.getMessage());
        }
    }

    @Test
    public void shouldHandleInvalidJsonGracefully() {
        ApplicationClient client = createApplicationClient();

        WebSocketClient ws = client.webSocket(StubRequest.request("/ws/echo"));
        ws.send("not valid json");

        List<String> received = ws.received();
        Assert.assertEquals(0, received.size());

        client.assertException("Unrecognized token 'not': was expecting (JSON String, Number, Array, Object or token 'null', 'true' or 'false')\n at [Source: REDACTED (`StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION` disabled); line: 1, column: 4]");

        client.assertNoMoreExceptions();
    }
}
