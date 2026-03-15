package io.kiw.web.application.routes;

import io.kiw.web.test.StubHttpResponse;
import io.kiw.web.test.StubRequest;
import io.kiw.web.test.TestApplicationClient;
import io.kiw.web.test.TestHelper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static io.kiw.web.test.TestHelper.json;

public class CustomStatusCodeTest {

    private TestApplicationClient testApplicationClient;

    @Before
    public void setUp() {
        testApplicationClient = new TestApplicationClient();
    }

    @After
    public void tearDown() {
        testApplicationClient.assertNoMoreExceptions();
    }

    @Test
    public void shouldAllowHandlerToSetCreatedStatusCode() {
        StubHttpResponse response = testApplicationClient.post(
            StubRequest.request("/statusCode")
                .body(json().put("statusCode", 201).put("value", "created").toString()));

        Assert.assertEquals(
            StubHttpResponse.response(json().put("value", "created").toString()).withStatusCode(201),
            response);
    }

    @Test
    public void shouldAllowHandlerToSetNoContentStatusCode() {
        StubHttpResponse response = testApplicationClient.post(
            StubRequest.request("/statusCode")
                .body(json().put("statusCode", 204).put("value", "done").toString()));

        Assert.assertEquals(
            StubHttpResponse.response(json().put("value", "done").toString()).withStatusCode(204),
            response);
    }

    @Test
    public void shouldDefaultToOkWhenStatusCodeNotSet() {
        StubHttpResponse response = testApplicationClient.post(
            StubRequest.request("/statusCode")
                .body(json().put("statusCode", 200).put("value", "ok").toString()));

        Assert.assertEquals(
            StubHttpResponse.response(json().put("value", "ok").toString()),
            response);
    }
}
