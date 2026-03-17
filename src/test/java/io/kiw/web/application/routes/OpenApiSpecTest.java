package io.kiw.web.application.routes;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.kiw.web.infrastructure.Method;
import io.kiw.web.infrastructure.RoutesRegister;
import io.kiw.web.infrastructure.openapi.OpenApiSpecGenerator;
import io.kiw.web.test.MyApplicationState;
import io.kiw.web.test.TestHttpResponse;
import io.kiw.web.test.StubRequest;
import io.kiw.web.test.StubRouter;
import io.kiw.web.test.handler.*;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class OpenApiSpecTest {

    private RoutesRegister routesRegister;
    private StubRouter router;
    private MyApplicationState state;

    @Before
    public void setUp() {
        router = new StubRouter(e -> {});
        routesRegister = new RoutesRegister(router);
        state = new MyApplicationState();
    }

    @Test
    public void shouldGenerateSpecWithCorrectVersion() {
        routesRegister.jsonRoute("/echo", Method.POST, state, new PostEchoHandler());

        OpenApiSpecGenerator generator = new OpenApiSpecGenerator(
            routesRegister.getOpenApiCollector(), routesRegister.getObjectMapper());
        ObjectNode spec = generator.title("Test API").version("1.0.0").description("A test API").generate();

        assertEquals("3.0.3", spec.get("openapi").asText());
        assertEquals("Test API", spec.get("info").get("title").asText());
        assertEquals("1.0.0", spec.get("info").get("version").asText());
        assertEquals("A test API", spec.get("info").get("description").asText());
    }

    @Test
    public void shouldGeneratePathsForRegisteredRoutes() {
        routesRegister.jsonRoute("/echo", Method.POST, state, new PostEchoHandler());
        routesRegister.jsonRoute("/echo", Method.GET, state, new GetEchoHandler());
        routesRegister.serveOpenApiSpec("/openapi.json", "Test API", "1.0.0", null);

        ObjectNode spec = getSpec();
        JsonNode paths = spec.get("paths");

        assertNotNull(paths.get("/echo"));
        assertNotNull(paths.get("/echo").get("post"));
        assertNotNull(paths.get("/echo").get("get"));
    }

    @Test
    public void shouldConvertPathParamsToOpenApiFormat() {
        routesRegister.jsonRoute("/echo/:pathExample", Method.GET, state, new GetEchoHandler());
        routesRegister.serveOpenApiSpec("/openapi.json", "Test API", "1.0.0", null);

        ObjectNode spec = getSpec();
        JsonNode paths = spec.get("paths");

        assertNotNull(paths.get("/echo/{pathExample}"));
        assertNull(paths.get("/echo/:pathExample"));

        JsonNode parameters = paths.get("/echo/{pathExample}").get("get").get("parameters");
        assertNotNull(parameters);
        assertEquals(1, parameters.size());
        assertEquals("pathExample", parameters.get(0).get("name").asText());
        assertEquals("path", parameters.get(0).get("in").asText());
        assertTrue(parameters.get(0).get("required").asBoolean());
    }

    @Test
    public void shouldGenerateRequestBodySchemaFromInputType() {
        routesRegister.jsonRoute("/echo", Method.POST, state, new PostEchoHandler());
        routesRegister.serveOpenApiSpec("/openapi.json", "Test API", "1.0.0", null);

        ObjectNode spec = getSpec();
        JsonNode requestBody = spec.get("paths").get("/echo").get("post").get("requestBody");

        assertNotNull(requestBody);
        assertTrue(requestBody.get("required").asBoolean());

        JsonNode schema = requestBody.get("content").get("application/json").get("schema");
        assertEquals("object", schema.get("type").asText());

        JsonNode properties = schema.get("properties");
        assertNotNull(properties.get("intExample"));
        assertEquals("integer", properties.get("intExample").get("type").asText());
        assertNotNull(properties.get("stringExample"));
        assertEquals("string", properties.get("stringExample").get("type").asText());
    }

    @Test
    public void shouldGenerateResponseSchemaFromOutputType() {
        routesRegister.jsonRoute("/echo", Method.POST, state, new PostEchoHandler());
        routesRegister.serveOpenApiSpec("/openapi.json", "Test API", "1.0.0", null);

        ObjectNode spec = getSpec();
        JsonNode response = spec.get("paths").get("/echo").get("post").get("responses").get("200");

        assertNotNull(response);
        assertEquals("Successful response", response.get("description").asText());

        JsonNode schema = response.get("content").get("application/json").get("schema");
        assertEquals("object", schema.get("type").asText());

        JsonNode properties = schema.get("properties");
        assertNotNull(properties.get("intExample"));
        assertNotNull(properties.get("stringExample"));
        assertNotNull(properties.get("pathExample"));
        assertNotNull(properties.get("queryExample"));
    }

    @Test
    public void shouldNotIncludeRequestBodyForGetRoutes() {
        routesRegister.jsonRoute("/echo", Method.GET, state, new GetEchoHandler());
        routesRegister.serveOpenApiSpec("/openapi.json", "Test API", "1.0.0", null);

        ObjectNode spec = getSpec();
        JsonNode getOperation = spec.get("paths").get("/echo").get("get");

        assertNull(getOperation.get("requestBody"));
    }

    @Test
    public void shouldApplyOpenApiMetadata() {
        routesRegister.jsonRoute("/echo", Method.POST, state, new PostEchoHandler(),
            new RouteConfigBuilder()
                .openApi()
                    .summary("Echo the input")
                    .description("Echoes back the provided values")
                    .tag("echo")
                    .responseDescription("The echoed response")
                .build()
        );
        routesRegister.serveOpenApiSpec("/openapi.json", "Test API", "1.0.0", null);

        ObjectNode spec = getSpec();
        JsonNode operation = spec.get("paths").get("/echo").get("post");

        assertEquals("Echo the input", operation.get("summary").asText());
        assertEquals("Echoes back the provided values", operation.get("description").asText());
        assertEquals("echo", operation.get("tags").get(0).asText());
        assertEquals("The echoed response", operation.get("responses").get("200").get("description").asText());
    }

    @Test
    public void shouldApplyParamDescriptions() {
        routesRegister.jsonRoute("/echo/:pathExample", Method.GET, state, new GetEchoHandler(),
            new RouteConfigBuilder()
                .openApi()
                    .paramDescription("pathExample", "An example path param")
                .build()
        );
        routesRegister.serveOpenApiSpec("/openapi.json", "Test API", "1.0.0", null);

        ObjectNode spec = getSpec();
        JsonNode param = spec.get("paths").get("/echo/{pathExample}").get("get").get("parameters").get(0);

        assertEquals("An example path param", param.get("description").asText());
    }

    @Test
    public void shouldHideRoutesMarkedAsHidden() {
        routesRegister.jsonRoute("/echo", Method.POST, state, new PostEchoHandler());
        routesRegister.jsonRoute("/internal", Method.GET, state, new GetEchoHandler(),
            new RouteConfigBuilder()
                .openApi()
                    .hidden()
                .build()
        );
        routesRegister.serveOpenApiSpec("/openapi.json", "Test API", "1.0.0", null);

        ObjectNode spec = getSpec();
        JsonNode paths = spec.get("paths");

        assertNotNull(paths.get("/echo"));
        assertNull(paths.get("/internal"));
        assertNull(paths.get("/openapi.json"));
    }

    @Test
    public void shouldGenerateOperationIds() {
        routesRegister.jsonRoute("/echo", Method.POST, state, new PostEchoHandler());
        routesRegister.serveOpenApiSpec("/openapi.json", "Test API", "1.0.0", null);

        ObjectNode spec = getSpec();
        JsonNode operation = spec.get("paths").get("/echo").get("post");

        assertNotNull(operation.get("operationId"));
        assertEquals("post_echo", operation.get("operationId").asText());
    }

    @Test
    public void shouldMarkPrimitiveFieldsAsRequired() {
        routesRegister.jsonRoute("/echo", Method.POST, state, new PostEchoHandler());
        routesRegister.serveOpenApiSpec("/openapi.json", "Test API", "1.0.0", null);

        ObjectNode spec = getSpec();
        JsonNode schema = spec.get("paths").get("/echo").get("post")
            .get("requestBody").get("content").get("application/json").get("schema");

        JsonNode required = schema.get("required");
        assertNotNull(required);
        assertTrue(required.isArray());
        boolean hasIntExample = false;
        for (JsonNode node : required) {
            if ("intExample".equals(node.asText())) {
                hasIntExample = true;
            }
        }
        assertTrue("intExample should be in required", hasIntExample);
    }

    @Test
    public void shouldServeSpecViaStubRouter() {
        routesRegister.jsonRoute("/echo", Method.POST, state, new PostEchoHandler());
        routesRegister.serveOpenApiSpec("/openapi.json", "Test API", "1.0.0", "A test API");

        TestHttpResponse response = router.handle(StubRequest.request("/openapi.json"), Method.GET);

        assertEquals(200, response.statusCode);
        assertNotNull(response.responseBody);
        assertTrue(response.responseBody.contains("\"openapi\""));
        assertTrue(response.responseBody.contains("3.0.3"));
        assertTrue(response.responseBody.contains("Test API"));
    }

    @Test
    public void shouldGenerateSpecDirectlyFromCollector() {
        routesRegister.jsonRoute("/echo", Method.POST, state, new PostEchoHandler(),
            new RouteConfigBuilder()
                .openApi()
                    .summary("Echo input")
                    .tag("echo")
                .build()
        );

        OpenApiSpecGenerator generator = new OpenApiSpecGenerator(
            routesRegister.getOpenApiCollector(), routesRegister.getObjectMapper());
        ObjectNode spec = generator.title("Direct API").version("2.0.0").generate();

        assertEquals("3.0.3", spec.get("openapi").asText());
        assertEquals("Direct API", spec.get("info").get("title").asText());
        assertEquals("2.0.0", spec.get("info").get("version").asText());
        assertEquals("Echo input", spec.get("paths").get("/echo").get("post").get("summary").asText());
    }

    @Test
    public void shouldChainRouteConfigWithTimeoutAndOpenApi() {
        routesRegister.jsonRoute("/echo", Method.POST, state, new PostEchoHandler(),
            new RouteConfigBuilder()
                .timeout(5000)
                .openApi()
                    .summary("Echo with timeout")
                .done()
                .build()
        );
        routesRegister.serveOpenApiSpec("/openapi.json", "Test API", "1.0.0", null);

        ObjectNode spec = getSpec();
        assertEquals("Echo with timeout", spec.get("paths").get("/echo").get("post").get("summary").asText());
    }

    private ObjectNode getSpec() {
        OpenApiSpecGenerator generator = new OpenApiSpecGenerator(
            routesRegister.getOpenApiCollector(), routesRegister.getObjectMapper());
        generator.title("Test API").version("1.0.0");
        return generator.generate();
    }
}
