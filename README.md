# Luxis

Ship faster. Break less. The Java web framework that makes high test coverage effortless.

## Why Luxis?

Your test suite is either fast or thorough — never both.

Unit tests run in milliseconds but mock away so much that they barely prove your app works. Integration and end-to-end tests give real confidence, but they're slow to write, slow to run, and brittle to maintain. So your team makes a choice: wait ten minutes for the build, or ship with gaps in coverage and hope for the best.

Neither option is great. One slows you down. The other lets bugs through.

### What that costs you

Slow feedback loops don't just waste developer time — they change how your team works, and not for the better.

- **Developers stop running the full suite locally.** They push and wait for CI. Context-switching kills focus. Flow state disappears.
- **Bugs ship to production.** Low-coverage tests miss real behaviour. You find out from your users, not your build.
- **Releases slow down.** When the team doesn't trust the test suite, every deploy needs manual verification. Confidence drops, velocity drops with it.
- **Debugging gets expensive.** Concurrency bugs surface as intermittent failures in production — the hardest, most time-consuming category of issue to diagnose.

This isn't a tooling problem. It's an architecture problem. Most frameworks make it structurally difficult to test your actual application logic quickly.

### How Luxis fixes it

Luxis is built around one idea: **your tests should exercise real application behaviour at the speed of a unit test.**

**Near-full coverage, near-instant feedback** — Your route handlers, filters, and application logic run in-memory with no web server, no network stack, no IO. The same code you deploy to production plugs directly into a stub router for testing. You get the coverage of an integration test with the speed of a unit test. When you're ready to test against the real stack, swap in the real implementation. Zero test code changes.

**Concurrency bugs caught at compile time** — The pipeline API forces you to declare whether each step is blocking, non-blocking, or async. Attempt to access application state on a different thread? It won't compile. The category of bug that causes 2am pages — thread starvation, blocked event loops, race conditions — becomes a red squiggle in your IDE.

**Scales across your entire architecture** — The in-memory test layer doesn't stop at a single service. Because every Luxis service runs without IO, you can wire dozens — even hundreds — of microservices together in a single test and get feedback in milliseconds. No Docker Compose. No shared test environments. No waiting for deploys to validate cross-service behaviour. As your architecture grows, your feedback loop stays instant.

### What this means for your team

**For developers:** Tests run in milliseconds. You get feedback before you've left your editor. You write more tests because it's easy, not because someone told you to.

**For tech leads and architects:** The type system enforces your concurrency model. New team members can't accidentally introduce threading bugs. Code review focuses on logic, not "did you remember to run this off the event loop?"

**For CTOs and engineering leaders:** Faster feedback means faster delivery. Fewer production incidents. Less time firefighting, more time building. The kind of developer experience that retains engineers.


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

The same test code runs against an in-memory stub **and** a real Vert.x HTTP server. Write your tests once, choose the execution mode.

### Two modes, one test

| | In-memory (stub) | Real server (Vert.x) |
|---|---|---|
| **Factory** | `Luxis.test(routes)` | `Luxis.start(routes)` |
| **Speed** | Milliseconds | Seconds |
| **Network** | None — everything in-process | Real HTTP over localhost |
| **When to use** | Day-to-day development, CI | Final verification |

Both give you a `TestClient` with the same API. Your test code doesn't know or care which mode it's running in.

### Register your routes once

This is the same function your production server calls:

```java
public static MyState registerRoutes(RoutesRegister routesRegister) {
    MyState state = new MyState();
    routesRegister.jsonRoute("/echo", Method.POST, state, new PostEchoHandler());
    routesRegister.jsonRoute("/echo", Method.GET, state, new GetEchoHandler());
    return state;
}
```

### Create a test client

**In-memory:**

```java
Luxis<MyState> luxis = Luxis.test(MyApp::registerRoutes);
TestClient client = new StubTestClient("127.0.0.1", 8080, luxis);
```

No server starts. No ports are opened. Your handlers execute synchronously in-process.

**Real server:**

```java
Luxis<MyState> luxis = Luxis.start(MyApp::registerRoutes,
    new WebServiceConfigBuilder().setPort(8080).build());
TestClient client = new VertxTestClient("127.0.0.1", 8080);
```

A real HTTP server starts on port 8080. Requests go over the network through the full Vert.x stack.

**The test code is identical from here on:**

```java
TestHttpResponse response = client.post(
    StubRequest.request("/echo")
        .body(json().put("name", "Alice").toString()));

Assert.assertEquals(TestHttpResponse.response(expectedJson), response);
```

Same request builder. Same response type. Same assertions. The only difference is how you created the `Luxis` instance — one line.

### A complete test

```java
public class EchoTest {

    private Luxis<MyState> luxis;
    private TestClient client;

    @Before
    public void setUp() {
        luxis = Luxis.test(MyApp::registerRoutes);
        client = new StubTestClient("127.0.0.1", 8080, luxis);
    }

    @After
    public void tearDown() throws Exception {
        client.assertNoMoreExceptions();
        client.close();
        luxis.close();
    }

    @Test
    public void shouldEchoBackJsonValues() {
        String requestBody = json()
                .put("intExample", 17)
                .put("stringExample", "hiya")
                .toString();

        TestHttpResponse response = client.post(
                StubRequest.request("/echo").body(requestBody));

        String expectedResponse = json()
                .put("intExample", 17)
                .put("stringExample", "hiya")
                .toString();

        Assert.assertEquals(
                TestHttpResponse.response(expectedResponse),
                response);
    }
}
```

To run this same test against the real Vert.x server, change two lines in `setUp()`:

```java
luxis = Luxis.start(MyApp::registerRoutes,
    new WebServiceConfigBuilder().setPort(8080).build());
client = new VertxTestClient("127.0.0.1", 8080);
```

Everything else stays exactly the same.

### Building requests

```java
StubRequest.request("/echo")
    .body("{\"name\": \"test\"}")
    .queryParam("search", "hello")
    .headerParam("X-Request-Id", "abc")
    .cookie("session", "xyz")
    .fileUpload("document", "file contents")
```

### Building JSON test data

```java
import static io.kiw.luxis.web.test.TestHelper.json;

String body = json()
    .put("intExample", 17)
    .put("stringExample", "hiya")
    .putNull("optionalField")
    .toString();
```

### Asserting responses

```java
TestHttpResponse expected = TestHttpResponse.response(expectedJson)
    .withStatusCode(400)
    .withHeader("X-Custom", "value")
    .withCookie(new HttpCookie("session", "abc"));

Assert.assertEquals(expected, actual);
```


## License

Licensed under the [Apache License, Version 2.0](LICENSE).
