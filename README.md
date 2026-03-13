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
    public Flow<HelloWorldResponse> handle(HttpResponseStream<HelloWorldRequest, HelloWorldState> e) {
        return e.complete((request, httpContext, applicationState) -> {
            return HttpResult.success(new HelloWorldResponse());
        });
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

Handlers build a processing chain using `map`, `flatMap`, `blockingMap`, and `complete`. Each step transforms the data flowing through the pipeline:

```java
public class MultiplyHandler extends VertxJsonRoute<MultiplyRequest, MultiplyResponse, MyState> {

    @Override
    public Flow<MultiplyResponse> handle(HttpResponseStream<MultiplyRequest, MyState> stream) {
        return stream
            .map((request, httpContext, appState) -> request.numberToMultiply)    // extract on event loop
            .blockingMap((number, httpContext) -> number * 2)                      // compute on worker thread
            .complete((result, httpContext, appState) ->                           // build response
                HttpResult.success(new MultiplyResponse(result)));
    }
}
```

- `map` / `flatMap` — run on the event loop
- `blockingMap` / `blockingFlatMap` — run on a worker thread (for database calls, file I/O, etc.)
- `complete` / `blockingComplete` — terminal operation that produces the final response

### Error Handling

Use `flatMap` with `HttpResult.error()` to short-circuit the pipeline. Once an error is returned, subsequent steps are skipped and the error response is sent immediately:

```java
@Override
public Flow<Response> handle(HttpResponseStream<Request, MyState> stream) {
    return stream
        .map((request, httpContext, appState) -> request.numberToMultiply)
        .flatMap((number, httpContext, appState) -> {
            if (number < 0) {
                return HttpResult.error(400, new ErrorMessageResponse("Number must be positive"));
            }
            return Result.success(number);
        })
        .complete((number, httpContext, appState) ->
            HttpResult.success(new Response(number * 2)));
}
```

### Accessing HTTP Context

The `httpContext` parameter gives you access to query parameters, headers, and cookies:

```java
@Override
public Flow<EchoResponse> handle(HttpResponseStream<EchoRequest, MyState> e) {
    return e.complete((request, httpContext, appState) -> {
        // Read from request
        String query = httpContext.getQueryParam("search");
        String header = httpContext.getRequestHeader("X-Request-Id");
        Cookie cookie = httpContext.getRequestCookie("session");

        // Write to response
        httpContext.addResponseHeader("X-Response-Id", "abc");
        httpContext.addResponseCookie(new CookieImpl("session", "new-value"));

        return HttpResult.success(new EchoResponse(query, header));
    });
}
```

### Filters

Filters are middleware that run before matching routes. Register them with wildcard paths:

```java
public class AuthFilter implements VertxJsonFilter<MyState> {

    @Override
    public Flow handle(HttpResponseStream<Void, MyState> e) {
        return e.complete((request, httpContext, appState) -> {
            httpContext.addResponseCookie(new CookieImpl("visited", "true"));
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
    public Flow<UploadResponse> handle(HttpResponseStream<Map<String, Buffer>, MyState> e) {
        return e.complete((files, httpContext, appState) -> {
            int totalBytes = files.values().stream().mapToInt(b -> b.getBytes().length).sum();
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
    public Flow<DownloadFileResponse> handle(HttpResponseStream<String, MyState> e) {
        return e.complete((request, httpContext, appState) ->
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
