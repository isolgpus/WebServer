package io.kiw.web.application.routes;

import io.kiw.web.infrastructure.Method;
import io.kiw.web.test.*;
import io.kiw.web.test.handler.StatusCodeTestHandler;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;

import static io.kiw.web.application.routes.TestApplicationClientCreator.*;
import static io.kiw.web.test.TestHelper.json;

@RunWith(Parameterized.class)
public class CustomStatusCodeTest {

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> modes() {
        return TestApplicationClientCreator.modes();
    }

    private final String mode;
    private TestApplicationClient client;

    public CustomStatusCodeTest(String mode) {
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
        if (client != null) {
            client.assertNoMoreExceptions();
            client.stop();
        }
    }

    @Test
    public void shouldAllowHandlerToSetCreatedStatusCode() {
        client = createClient(mode, r -> {
            MyApplicationState state = new MyApplicationState();
            r.jsonRoute("/statusCode", Method.POST, state, new StatusCodeTestHandler());
        });

        TestHttpResponse response = client.post(
            StubRequest.request("/statusCode")
                .body(json().put("statusCode", "CREATED").put("value", "created").toString()));

        Assert.assertEquals(
            TestHttpResponse.response(json().put("value", "created").toString()).withStatusCode(201),
            response);
    }

    @Test
    public void shouldAllowHandlerToSetNoContentStatusCode() {
        client = createClient(mode, r -> {
            MyApplicationState state = new MyApplicationState();
            r.jsonRoute("/statusCode", Method.POST, state, new StatusCodeTestHandler());
        });

        TestHttpResponse response = client.post(
            StubRequest.request("/statusCode")
                .body(json().put("statusCode", "NO_CONTENT").put("value", "done").toString()));

        Assert.assertEquals(
            TestHttpResponse.response("").withStatusCode(204),
            response);
    }

    @Test
    public void shouldDefaultToOkWhenStatusCodeNotSet() {
        client = createClient(mode, r -> {
            MyApplicationState state = new MyApplicationState();
            r.jsonRoute("/statusCode", Method.POST, state, new StatusCodeTestHandler());
        });

        TestHttpResponse response = client.post(
            StubRequest.request("/statusCode")
                .body(json().put("statusCode", "OK").put("value", "ok").toString()));

        Assert.assertEquals(
            TestHttpResponse.response(json().put("value", "ok").toString()),
            response);
    }
}
