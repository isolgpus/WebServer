# Luxis

A type-safe, functional web framework built on Vert.x for Java 21.

## Why Luxis?

Build your application with fast feedback with high coverage — a combination that traditional testing strategies struggle to deliver.

Unit tests are fast but test components in isolation, offering low coverage of real behaviour. End-to-end tests give you near-full coverage but are slow to set up and slow to run. You're always trading speed for confidence.

Luxis eliminates that trade-off in two ways:

1. **In-memory test layer** — Your application logic runs without any IO, network stack, or web server. The same route definitions you register in production plug straight into an in-memory stub router, giving you near-full coverage at unit-test speed. When you're ready to test against the real stack, swap in the real implementation with zero code changes to your tests.

2. **Compiler-enforced concurrency safety** — The functional pipeline API forces you to declare whether each step is non-blocking, blocking, or async. Run the wrong kind of work on the wrong thread and the code won't compile. Concurrency bugs that would otherwise surface as confusing, hard-to-diagnose production errors become compiler-level feedback — the fastest feedback loop there is.


## Usage

### Getting Started


Create a handler by extending `VertxJsonRoute`:

```java
public class HelloWorldHandler extends VertxJsonRoute<HelloWorldRequest, HelloWorldResponse, AppState> {

    @Override
    public RequestPipeline<HelloWorldResponse> handle(HttpStream<HelloWorldRequest, AppState> e) {
        return e.complete(ctx -> HttpResult.success(new HelloWorldResponse()));
    }
}
```

Start the server and register your routes:

```java
        WebServer.start(routesRegister -> {
            AppState appState = new AppState();
            routesRegister.jsonRoute("/hello/world", Method.POST, appState.helloWorldState, new HelloWorldHandler());
            return appState;
        });
```

### Handler Pipeline

Handlers are typed transformation chains built by calling methods on `HttpStream`. Each step receives a context object (`ctx`) that provides access to the current value (`ctx.in()`), the HTTP context (`ctx.http()`), and — for non-blocking steps — the application state (`ctx.app()`).

#### Pipeline Methods

| Method | Use when you need to… | Thread | Context |
|---|---|---|---|
| `map` | Transform a value without error handling | Event loop | `in()`, `http()`, `app()` |
| `flatMap` | Transform a value and potentially short-circuit with an error | Event loop | `in()`, `http()`, `app()` |
| `blockingMap` | Perform synchronous blocking I/O (DB reads, file I/O) | Worker | `in()`, `http()` |
| `blockingFlatMap` | Perform synchronous blocking I/O that can fail | Worker | `in()`, `http()` |
| `asyncMap` | Call an async API (Kafka, HTTP client) | Event loop | `in()`, `http()`, `app()` |
| `asyncFlatMap` | Call an async API that can fail | Event loop | `in()`, `http()`, `app()` |
| `asyncBlockingMap` | Blocking setup followed by an async result | Worker | `in()`, `http()` |
| `asyncBlockingFlatMap` | Blocking setup followed by an async result that can fail | Worker | `in()`, `http()` |
| `validate` | Declaratively validate fields (see [Validation](#validation)) | Event loop | `in()`, `http()` |
| `requireJwt` | Require and validate a JWT Bearer token | Event loop | `in()`, `http()` |
| `complete` | **Terminal** — produce the final response | Event loop | `in()`, `http()`, `app()` |
| `blockingComplete` | **Terminal** — produce the final response from a blocking operation | Worker | `in()`, `http()` |

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
