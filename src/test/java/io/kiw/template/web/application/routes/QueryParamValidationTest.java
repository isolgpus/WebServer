package io.kiw.template.web.application.routes;

import io.kiw.template.web.test.StubHttpResponse;
import io.kiw.template.web.test.TestApplicationClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static io.kiw.template.web.test.StubHttpResponse.response;
import static io.kiw.template.web.test.StubRequest.request;
import static io.kiw.template.web.test.TestHelper.*;
import static org.junit.Assert.assertEquals;

public class QueryParamValidationTest {

    private TestApplicationClient testApplicationClient;

    @Before
    public void setUp() throws Exception {

        testApplicationClient = new TestApplicationClient();
    }

    @Test
    public void shouldValidateSuccessfully() {
        StubHttpResponse response = testApplicationClient.get(
                request("/validateQueryParams")
                        .queryParam("required", "IAMREQUIRED")
                        .queryParam("rangedInt", "55"));

        assertEquals(
                response(json(entry("required", "IAMREQUIRED"), entry("rangedInt", 55), entry("defaultedInt", 22))).withStatusCode(200),
                response
        );
    }

    @Test
    public void shouldComplainWhenRequiredParamIsNotPresent() {
        StubHttpResponse response = testApplicationClient.get(
                request("/validateQueryParams")
                        .queryParam("rangedInt", "55"));

        assertEquals(
                response(
                        json(
                                entry("message", "There were unexpected validation errors"),
                                entry("messages", jsonObject(entry("required", "is required")))
                        )
                ).withStatusCode(400),
                response
        );
    }

    @Test
    public void shouldComplainWhenMapIsInvalid() {
        StubHttpResponse response = testApplicationClient.get(
                request("/validateQueryParams")
                        .queryParam("rangedInt", "bob"));

        assertEquals(
                response(
                        json(
                                entry("message", "There were unexpected validation errors"),
                                entry("messages", jsonObject(entry("required", "is required"), entry("rangedInt", "is not a valid number")))
                        )
                ).withStatusCode(400),
                response
        );
    }

    @Test
    public void shouldComplainWhenValidateIsInvalid() {
        StubHttpResponse response = testApplicationClient.get(
                request("/validateQueryParams")
                        .queryParam("rangedInt", "19"));

        assertEquals(
                response(
                        json(
                                entry("message", "There were unexpected validation errors"),
                                entry("messages", jsonObject(entry("required", "is required"), entry("rangedInt", "is not in expected range 20 - 78")))
                        )
                ).withStatusCode(400),
                response
        );
    }

    @Test
    public void shouldMapAnOptionalParamWhenValidateIsInvalid() {
        StubHttpResponse response = testApplicationClient.get(
                request("/validateQueryParams")
                        .queryParam("required", "IAMREQUIRED")
                        .queryParam("rangedInt", "55")
                        .queryParam("optionalInt", "99"));

        assertEquals(
                response(json(entry("required", "IAMREQUIRED"), entry("rangedInt", 55), entry("defaultedInt", 22), entry("optionalInt", 99))).withStatusCode(200),
                response
        );
    }

    @After
    public void invariantAssertions()
    {
        testApplicationClient.assertNoMoreErrors();

    }
}
