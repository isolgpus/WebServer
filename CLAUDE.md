# CLAUDE.md

## Project Overview

Luxis — a type-safe, functional web framework built on Vert.x (Java 21). Provides declarative HTTP pipelines with automatic JSON serialization, a Result monad for error handling, and an in-memory test layer that removes Vert.x entirely from unit tests.

## Build & Test Commands

```bash
mvn test          # Run all tests
mvn clean compile # Build without tests
```

CI runs `mvn test` on PRs to `master` using JDK 21 (Temurin).

> **Cloud mode:** Do NOT run `mvn test` (or `mvn verify`/`mvn integration-test`) in cloud mode — Maven cannot download JARs due to restricted network access. A PreToolUse hook automatically blocks these commands when `CLAUDE_CODE_REMOTE=1`. Tests will run via GitHub Actions CI when the PR is raised.

## Project Structure

- `src/main/java/io/kiw/luxis/result/` - Generic Result monad
- `src/main/java/io/kiw/luxis/web/` - Server entry point, config, route registration
- `src/main/java/io/kiw/luxis/web/internal/` - Core framework: pipelines, handlers, filters, JWT, mappers
- `src/main/java/io/kiw/luxis/web/test/` - In-memory test utilities (StubRouter, TestApplicationClient)
- `src/test/java/` - Unit tests

## Code Style

- Java 21, 4-space indentation
- PascalCase classes, camelCase methods
- Heavy use of generics, lambdas, and fluent APIs (method chaining)
- Functional pipeline pattern: `map()`, `flatMap()`, `blockingMap()`, `blockingFlatMap()`, `complete()`
- JUnit 4 for tests
- No linter configured; follow existing conventions

## Key Patterns

- **Route handlers** are typed transformation chains built via `HttpResponseStream`
- **Result monad** (`Result<S, F>`) propagates errors through pipelines without exceptions
- **StubRouter** enables unit testing routes without starting an HTTP server
- **Filters** (middleware) use `JsonFilter` with wildcard path matching
- **JWT auth** via `requireJwt()` in pipelines
- **Application State** is only to ever be accessed within the non blocking HttpStream map methods of the pipelines. Or when calling a luxis method directly.
- **Tests client tests** should only ever take the perspective of the client. Despite having access to the server, we continue to treat the server as a black box, only ever asserting the behaviour the client gives. The only exception to this is asserting the exceptionHandler.

## Dependencies

- Vert.x 4.5.1 (core, web, auth-jwt)
- Jackson 2.16.0 (JSON serialization)
- JUnit 4.12
