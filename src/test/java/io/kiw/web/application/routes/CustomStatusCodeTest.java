package io.kiw.web.application.routes;

import io.kiw.web.test.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static io.kiw.web.test.TestHelper.json;

public class CustomStatusCodeTest {

    private TestApplicationClient testApplicationClient;

    @Before
    public void setUp() {
        testApplicationClient = new TestApplicationClient(routesRegister -> TestApplicationRoutes.registerRoutes(routesRegister, new MyApplicationState()));
    }

    @After
    public void tearDown() {
        testApplicationClient.assertNoMoreExceptions();
    }

    @Test
    public void shouldAllowHandlerToSetCreatedStatusCode() {
        TestHttpResponse response = testApplicationClient.post(
            StubRequest.request("/statusCode")
                .body(json().put("statusCode", "CREATED").put("value", "created").toString()));

        Assert.assertEquals(
            TestHttpResponse.response(json().put("value", "created").toString()).withStatusCode(201),
            response);
    }

    @Test
    public void shouldAllowHandlerToSetNoContentStatusCode() {
        TestHttpResponse response = testApplicationClient.post(
            StubRequest.request("/statusCode")
                .body(json().put("statusCode", "NO_CONTENT").put("value", "done").toString()));

        Assert.assertEquals(
            TestHttpResponse.response(json().put("value", "done").toString()).withStatusCode(204),
            response);
    }

    @Test
    public void shouldDefaultToOkWhenStatusCodeNotSet() {
        TestHttpResponse response = testApplicationClient.post(
            StubRequest.request("/statusCode")
                .body(json().put("statusCode", "OK").put("value", "ok").toString()));

        Assert.assertEquals(
            TestHttpResponse.response(json().put("value", "ok").toString()),
            response);
    }
}
