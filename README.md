# WebServer

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
    public RequestPipeline<HelloWorldResponse> handle(HttpResponseStream<HelloWorldRequest, HelloWorldState> e) {
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

Handlers build a processing chain using `map`, `flatMap`, `blockingMap`, and `complete`. Each step receives a context object (`ctx`) that exposes the current value via `ctx.in()`, the HTTP context via `ctx.http()`, and the application state via `ctx.app()`:

```java
public class MultiplyHandler extends VertxJsonRoute<MultiplyRequest, MultiplyResponse, MyState> {

    @Override
    public RequestPipeline<MultiplyResponse> handle(HttpResponseStream<MultiplyRequest, MyState> stream) {
        return stream
            .map(ctx -> ctx.in().numberToMultiply)      // extract on event loop
            .blockingMap(ctx -> ctx.in() * 2)           // compute on worker thread
            .complete(ctx ->                             // build response
                HttpResult.success(new MultiplyResponse(ctx.in())));
    }
}
```

- `map` / `flatMap` — run on the event loop; context provides `ctx.in()`, `ctx.http()`, `ctx.app()`
- `blockingMap` / `blockingFlatMap` — run on a worker thread (for database calls, file I/O, etc.); context provides `ctx.in()` and `ctx.http()` only (no app state access by design)
- `asyncMap` / `asyncFlatMap` — run asynchronously, returning a `CompletableFuture`; context provides `ctx.in()`, `ctx.http()`, `ctx.app()`
- `complete` / `blockingComplete` — terminal operation that produces the final response

### Realistic Pipeline Example

A handler that validates input, updates application state, checks and writes to a database, publishes a Kafka event, and handles the async result — each step extracted as a method reference:

```java
public class AddUserHandler extends VertxJsonRoute<AddUserRequest, AddUserResponse, AppState> {

    private final UserDatabase database;
    private final KafkaProducer kafka;

    public AddUserHandler(UserDatabase database, KafkaProducer kafka) {
        this.database = database;
        this.kafka = kafka;
    }

    @Override
    public RequestPipeline<AddUserResponse> handle(HttpResponseStream<AddUserRequest, AppState> stream) {
        return stream
            .flatMap(this::validateRequest)
            .map(this::updateApplicationState)
            .blockingFlatMap(this::validateAgainstDatabase)
            .blockingMap(this::writeToDatabase)
            .asyncMap(this::sendKafkaEvent)
            .flatMap(this::handleKafkaResponse)
            .complete(this::toResponse);
    }

    private Result<HttpErrorResponse, AddUserRequest> validateRequest(RouteContext<AddUserRequest, AppState> ctx) {
        if (ctx.in().name == null || ctx.in().name.isBlank()) {
            return HttpResult.error(ErrorStatusCode.BAD_REQUEST, new ErrorMessageResponse("Name is required"));
        }
        return HttpResult.success(ctx.in());
    }

    private User updateApplicationState(RouteContext<AddUserRequest, AppState> ctx) {
        User user = new User(ctx.in().name, ctx.in().email);
        ctx.app().getUserCache().add(user);
        return user;
    }

    private Result<HttpErrorResponse, User> validateAgainstDatabase(BlockingContext<User> ctx) {
        if (database.existsByEmail(ctx.in().email)) {
            return HttpResult.error(ErrorStatusCode.UNPROCESSABLE_ENTITY, new ErrorMessageResponse("Email already registered"));
        }
        return HttpResult.success(ctx.in());
    }

    private User writeToDatabase(BlockingContext<User> ctx) {
        return database.save(ctx.in());
    }

    private CompletableFuture<KafkaResult> sendKafkaEvent(RouteContext<User, AppState> ctx) {
        return kafka.send("user-events", new UserCreatedEvent(ctx.in().id));
    }

    private Result<HttpErrorResponse, User> handleKafkaResponse(RouteContext<KafkaResult, AppState> ctx) {
        if (ctx.in().failed()) {
            return HttpResult.error(ErrorStatusCode.INTERNAL_SERVER_ERROR, new ErrorMessageResponse("Failed to publish event"));
        }
        return HttpResult.success(ctx.in().user());
    }

    private Result<HttpErrorResponse, AddUserResponse> toResponse(RouteContext<User, AppState> ctx) {
        return HttpResult.success(new AddUserResponse(ctx.in().id, ctx.in().name));
    }
}
```

### Error Handling

Use `flatMap` with `HttpResult.error()` to short-circuit the pipeline. Once an error is returned, subsequent steps are skipped and the error response is sent immediately:

```java
@Override
public RequestPipeline<Response> handle(HttpResponseStream<Request, MyState> stream) {
    return stream
        .map(ctx -> ctx.in().numberToMultiply)
        .flatMap(ctx -> {
            if (ctx.in() < 0) {
                return HttpResult.error(ErrorStatusCode.BAD_REQUEST, new ErrorMessageResponse("Number must be positive"));
            }
            return Result.success(ctx.in());
        })
        .complete(ctx -> HttpResult.success(new Response(ctx.in() * 2)));
}
```

### Accessing HTTP Context

Use `ctx.http()` to access query parameters, headers, and cookies:

```java
@Override
public RequestPipeline<EchoResponse> handle(HttpResponseStream<EchoRequest, MyState> e) {
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

Use `validate()` in your pipeline to declare field-level rules declaratively. All rules are evaluated together and, if any fail, the pipeline short-circuits with a `422` response containing a map of field errors.

```java
@Override
public RequestPipeline<Response> handle(HttpResponseStream<Request, MyState> stream) {
    return stream
        .validate(v -> {
            v.jsonField("name", r -> r.name).required().minLength(2);
            v.jsonField("email", r -> r.email).required().email();
            v.numericBodyField("age", r -> r.age).required().min(0).max(150);
            v.queryParam("page").required().matches("[0-9]+");
            v.pathParam("userId").required().matches("[0-9]+");
        })
        .complete(ctx -> HttpResult.success(new Response(ctx.in().name)));
}
```

Nested objects are validated with `nestedBodyField`:

```java
v.nestedBodyField("address", r -> r.address, a -> {
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

**String rules:** `required()`, `minLength(n)`, `maxLength(n)`, `email()`, `matches(regex)`

**Numeric rules:** `required()`, `min(n)`, `max(n)`

### Filters

Filters are middleware that run before matching routes. Register them with wildcard paths:

```java
public class AuthFilter implements VertxJsonFilter<MyState> {

    @Override
    public RequestPipeline handle(HttpResponseStream<Void, MyState> e) {
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
    public RequestPipeline<UploadResponse> handle(HttpResponseStream<Map<String, Buffer>, MyState> e) {
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
    public RequestPipeline<DownloadFileResponse> handle(HttpResponseStream<String, MyState> e) {
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

The framework ships with a stub router that executes your handlers in-memory without Vert.x. Your route registration code is shared between production and test — the only difference is which router implementation it runs against.

### Setting Up a Test Client

Create a `TestApplicationClient` that registers the same routes against a `StubRouter`:

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

    public StubHttpResponse post(StubRequest stubRequest) {
        return router.handle(stubRequest, Method.POST);
    }

    public StubHttpResponse get(StubRequest stubRequest) {
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

Import it with `import static io.kiw.web.test.TestHelper.json;`.

### Asserting Responses

`StubHttpResponse` supports equality checks on body, status code, headers, and cookies:

```java
StubHttpResponse expected = StubHttpResponse.response(expectedJson)
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

        StubHttpResponse response = client.post(
            StubRequest.request("/echo").body(requestBody));

        String expectedResponse = json()
            .put("intExample", 17)
            .put("stringExample", "hiya")
            .putNull("queryExample")
            .putNull("requestHeaderExample")
            .putNull("requestCookieExample")
            .toString();

        Assert.assertEquals(
            StubHttpResponse.response(expectedResponse),
            response);
    }

    @Test
    public void shouldReturnErrorForMissingBody() {
        StubHttpResponse response = client.post(StubRequest.request("/echo"));

        String expectedResponse = json()
            .put("message", "Invalid json request")
            .toString();

        Assert.assertEquals(
            StubHttpResponse.response(expectedResponse).withStatusCode(400),
            response);
    }

    @Test
    public void shouldHandleQueryParamsAndHeaders() {
        StubHttpResponse response = client.post(
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
            StubHttpResponse.response(expectedResponse),
            response);
    }

    @Test
    public void shouldCatchHandlerExceptions() {
        StubHttpResponse response = client.post(
            StubRequest.request("/throw")
                .body(json().put("where", "complete").toString()));

        Assert.assertEquals(
            StubHttpResponse.response(
                json().put("message", "Something went wrong").toString())
                .withStatusCode(500),
            response);

        client.assertException("app error in complete");
    }
}
```
