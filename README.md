# Luxis

A type-safe, functional web framework built on Vert.x. It provides two things:

1. **A declarative HTTP pipeline** — define handlers as typed transformation chains with automatic JSON serialization, error propagation via a Result monad, and clean separation of blocking and non-blocking operations.
2. **A test layer that removes Vert.x entirely** — the same route definitions run against an in-memory stub router, so you can unit test your handlers instantly without starting a server.

## Usage

### Getting Started

Define your request, response, and state as plain Java classes:

```java
public class HelloWorldRequest {
}

public class HelloWorldResponse {
    public String response = "hello World";
}

public class HelloWorldState {
}
```

Create a handler by extending `VertxJsonRoute`:

```java
public class HelloWorldHandler extends VertxJsonRoute<HelloWorldRequest, HelloWorldResponse, HelloWorldState> {

    @Override
    public RequestPipeline<HelloWorldResponse> handle(HttpStream<HelloWorldRequest, HelloWorldState> e) {
        return e.complete(ctx -> HttpResult.success(new HelloWorldResponse()));
    }
}
```

Start the server and register your routes:

```java
public class Main {
    public static void main(String[] args) {
        WebServer.start(routesRegister -> {
            AppState appState = new AppState();
            routesRegister.jsonRoute("/hello/world", Method.POST, appState.helloWorldState, new HelloWorldHandler());
            return appState;
        });
    }
}
```

### Handler Pipeline

Handlers are typed transformation chains built by calling methods on `HttpStream`. Each step receives a context object (`ctx`) that provides access to the current value (`ctx.in()`), the HTTP context (`ctx.http()`), and — for non-blocking steps — the application state (`ctx.app()`).

#### Pipeline Methods

| Method | Thread | Context | Returns | Use when you need to… |
|---|---|---|---|---|
| `map` | Event loop | `in()`, `http()`, `app()` | Value | Transform a value without error handling |
| `flatMap` | Event loop | `in()`, `http()`, `app()` | `Result` | Transform a value and potentially short-circuit with an error |
| `blockingMap` | Worker | `in()`, `http()` | Value | Perform blocking I/O (DB reads, file I/O) |
| `blockingFlatMap` | Worker | `in()`, `http()` | `Result` | Perform blocking I/O that can fail |
| `asyncMap` | Event loop | `in()`, `http()`, `app()` | `CompletableFuture<Value>` | Call an async API (Kafka, HTTP client) |
| `asyncFlatMap` | Event loop | `in()`, `http()`, `app()` | `CompletableFuture<Result>` | Call an async API that can fail |
| `asyncBlockingMap` | Worker | `in()`, `http()` | `CompletableFuture<Value>` | Blocking setup followed by an async result |
| `asyncBlockingFlatMap` | Worker | `in()`, `http()` | `CompletableFuture<Result>` | Blocking setup followed by an async result that can fail |
| `validate` | Event loop | `in()`, `http()` | `Result` | Declaratively validate fields (see [Validation](#validation)) |
| `requireJwt` | Event loop | `in()`, `http()` | `Result` | Require and validate a JWT Bearer token |
| `complete` | Event loop | `in()`, `http()`, `app()` | `Result` | **Terminal** — produce the final response |
| `blockingComplete` | Worker | `in()`, `http()` | `Result` | **Terminal** — produce the final response from a blocking operation |

#### Example

```java
public class AddUserHandler extends VertxJsonRoute<AddUserRequest, AddUserResponse, AppState> {

    private final UserDatabase database;
    private final KafkaProducer kafka;

    public AddUserHandler(UserDatabase database, KafkaProducer kafka) {
        this.database = database;
        this.kafka = kafka;
    }

    @Override
    public RequestPipeline<AddUserResponse> handle(HttpStream<AddUserRequest, AppState> stream) {
        return stream
            .validate(v -> {
                v.jsonField("name", r -> r.name).required().minLength(2);
                v.jsonField("email", r -> r.email).required().email();
            })
            .map(this::updateApplicationState)
            .blockingFlatMap(this::validateAgainstDatabase)
            .blockingMap(this::writeToDatabase)
            .asyncMap(this::sendKafkaEvent)
            .flatMap(this::handleKafkaResponse)
            .complete(this::toResponse);
    }
}
```

### Error Handling

Use `flatMap` with `HttpResult.error()` to short-circuit the pipeline:

```java
.flatMap(ctx -> {
    if (ctx.in() < 0) {
        return HttpResult.error(ErrorStatusCode.BAD_REQUEST, new ErrorMessageResponse("Number must be positive"));
    }
    return Result.success(ctx.in());
})
```

### Accessing HTTP Context

Use `ctx.http()` to access query parameters, headers, and cookies:

```java
@Override
public RequestPipeline<EchoResponse> handle(HttpStream<EchoRequest, MyState> e) {
    return e.complete(ctx -> {
        // Read from request
        String query = ctx.http().getQueryParam("search");
        String header = ctx.http().getRequestHeader("X-Request-Id");
        Cookie cookie = ctx.http().getRequestCookie("session");

        // Write to response
        ctx.http().addResponseHeader("X-Response-Id", "abc");
        ctx.http().addResponseCookie(new CookieImpl("session", "new-value"));

        return HttpResult.success(new EchoResponse(query, header));
    });
}
```

### Validation

`validate()` evaluates all field rules together and short-circuits with a `422` response if any fail. You can validate JSON body fields, query parameters, and path parameters:

```java
v.jsonField("name", r -> r.name).required().minLength(2);
v.jsonField("email", r -> r.email).required().email();
v.jsonField("age", r -> r.age).required().min(0).max(150);
v.queryParam("page").required().matches("[0-9]+");
v.pathParam("userId").required().matches("[0-9]+");
```

Nested objects use a `jsonField` overload with a validation block:

```java
v.jsonField("address", r -> r.address, a -> {
    a.jsonField("city", x -> x.city).required();
    a.jsonField("zip", x -> x.zip).required().matches("[0-9]{5}");
});
```

On failure the response body is:

```json
{
  "message": "Validation failed",
  "errors": {
    "name": ["must not be blank"],
    "email": ["must be a valid email address"],
    "address.zip": ["must match pattern: [0-9]{5}"]
  }
}
```

Lists are validated with `listField`, which supports size constraints and per-element validation via `each()`:

```java
v.listField("addresses", r -> r.addresses)
    .required()
    .minSize(1)
    .maxSize(10)
    .each(a -> {
        a.jsonField("city", x -> x.city).required();
        a.jsonField("zip", x -> x.zip).required().matches("[0-9]{5}");
    });
```

Element errors are keyed by index, e.g. `addresses[0].city`.

**String rules:** `required()`, `minLength(n)`, `maxLength(n)`, `email()`, `matches(regex)`

**Numeric rules:** `required()`, `min(n)`, `max(n)`

**List rules:** `required()`, `minSize(n)`, `maxSize(n)`, `each(block)`

### Filters

Filters are middleware that run before matching routes. Register them with wildcard paths:

```java
public class AuthFilter implements VertxJsonFilter<MyState> {

    @Override
    public RequestPipeline<Void> handle(HttpStream<Void, MyState> e) {
        return e.complete(ctx -> {
            ctx.http().addResponseCookie(new CookieImpl("visited", "true"));
            return HttpResult.success();
        });
    }
}
```

```java
// Applies to all routes under /api/
routesRegister.jsonFilter("/api/*", appState, new AuthFilter());
```

### File Upload and Download

Upload routes receive a `Map<String, Buffer>` of uploaded files:

```java
public class UploadHandler extends VertxFileUploadRoute<UploadResponse, MyState> {

    @Override
    public RequestPipeline<UploadResponse> handle(HttpStream<Map<String, Buffer>, MyState> e) {
        return e.complete(ctx -> {
            int totalBytes = ctx.in().values().stream().mapToInt(b -> b.getBytes().length).sum();
            return HttpResult.success(new UploadResponse(totalBytes));
        });
    }
}

routesRegister.uploadFileRoute("/upload", Method.POST, appState, new UploadHandler());
```

Download routes return a `DownloadFileResponse`:

```java
public class DownloadHandler implements VertxFileDownloadRoute<String, MyState> {

    @Override
    public RequestPipeline<DownloadFileResponse> handle(HttpStream<String, MyState> e) {
        return e.complete(ctx ->
            Result.success(new DownloadFileResponse(Buffer.buffer("file contents"), "report.csv")));
    }
}

routesRegister.downloadFileRoute("/download", Method.GET, appState, new DownloadHandler(), "text/csv");
```

### Configuration

Customize the server with `WebServiceConfigBuilder`:

```java
WebServer.start(MyApp::registerRoutes, new WebServiceConfigBuilder()
    .setPort(8080)
    .setDefaultBlockingTimeoutMillis(5000)
    .setExceptionHandler(Throwable::printStackTrace)
    .setMaxBodySize(1_048_576)
    .build());
```

## Testing Your Handlers

### Setting Up a Test Client

Register the same routes against a `StubRouter` to run handlers in-memory without Vert.x:

```java
public class TestApplicationClient {

    private List<Exception> seenExceptions = new ArrayList<>();
    private final StubRouter router = new StubRouter(seenExceptions::add);

    public TestApplicationClient() {
        RoutesRegister routesRegister = new RoutesRegister(router);
        registerRoutes(routesRegister);
    }

    public static MyState registerRoutes(RoutesRegister routesRegister) {
        MyState state = new MyState();
        routesRegister.jsonRoute("/echo", Method.POST, state, new PostEchoHandler());
        routesRegister.jsonRoute("/echo", Method.GET, state, new GetEchoHandler());
        return state;
    }

    public TestHttpResponse post(StubRequest stubRequest) {
        return router.handle(stubRequest, Method.POST);
    }

    public TestHttpResponse get(StubRequest stubRequest) {
        return router.handle(stubRequest, Method.GET);
    }

    public void assertNoMoreExceptions() { /* ... */ }
    public void assertException(String message) { /* ... */ }
}
```

The same `registerRoutes` method can be used for both the real server and tests:

```java
// Production
WebServer.start(TestApplicationClient::registerRoutes);

// Test
TestApplicationClient client = new TestApplicationClient();
```

### Building Requests

Use `StubRequest` to build test requests fluently:

```java
StubRequest.request("/echo")
    .body("{\"name\": \"test\"}")
    .queryParam("search", "hello")
    .headerParam("X-Request-Id", "abc")
    .cookie("session", "xyz")
    .fileUpload("document", "file contents")
```

### Building JSON Test Data

`TestHelper.json()` returns a Jackson `ObjectNode` you can build fluently and convert to a string:

```java
String json = json()
    .put("intExample", 17)
    .put("stringExample", "hiya")
    .putNull("optionalField")
    .toString();
// {"intExample":17,"stringExample":"hiya","optionalField":null}
```

Import it with `import static io.kiw.luxis.web.test.TestHelper.json;`.

### Asserting Responses

`TestHttpResponse` supports equality checks on body, status code, headers, and cookies:

```java
TestHttpResponse expected = TestHttpResponse.response(expectedJson)
    .withStatusCode(400)
    .withHeader("X-Custom", "value")
    .withCookie(new CookieImpl("session", "abc"));

Assert.assertEquals(expected, actual);
```

### Complete Test Example

```java
public class MyHandlerTest {

    private TestApplicationClient client;

    @Before
    public void setUp() {
        client = new TestApplicationClient();
    }

    @After
    public void tearDown() {
        client.assertNoMoreExceptions();
    }

    @Test
    public void shouldEchoJsonValues() {
        String requestBody = json()
            .put("intExample", 17)
            .put("stringExample", "hiya")
            .toString();

        TestHttpResponse response = client.post(
            StubRequest.request("/echo").body(requestBody));

        String expectedResponse = json()
            .put("intExample", 17)
            .put("stringExample", "hiya")
            .putNull("queryExample")
            .putNull("requestHeaderExample")
            .putNull("requestCookieExample")
            .toString();

        Assert.assertEquals(
            TestHttpResponse.response(expectedResponse),
            response);
    }

    @Test
    public void shouldReturnErrorForMissingBody() {
        TestHttpResponse response = client.post(StubRequest.request("/echo"));

        String expectedResponse = json()
            .put("message", "Invalid json request")
            .set("errors", json())
            .toString();

        Assert.assertEquals(
            TestHttpResponse.response(expectedResponse).withStatusCode(400),
            response);
    }

    @Test
    public void shouldHandleQueryParamsAndHeaders() {
        TestHttpResponse response = client.post(
            StubRequest.request("/echo")
                .body("{}")
                .queryParam("queryExample", "hi")
                .headerParam("requestHeaderExample", "test"));

        String expectedResponse = json()
            .put("intExample", 0)
            .putNull("stringExample")
            .put("queryExample", "hi")
            .put("requestHeaderExample", "test")
            .putNull("requestCookieExample")
            .toString();

        Assert.assertEquals(
            TestHttpResponse.response(expectedResponse),
            response);
    }

    @Test
    public void shouldCatchHandlerExceptions() {
        TestHttpResponse response = client.post(
            StubRequest.request("/throw")
                .body(json().put("where", "complete").toString()));

        Assert.assertEquals(
            TestHttpResponse.response(
                json().put("message", "Something went wrong").toString())
                .withStatusCode(500),
            response);

        client.assertException("app error in complete");
    }
}
```
