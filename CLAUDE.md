# CLAUDE.md

## Project Overview

Luxis — a type-safe, functional web framework built on Vert.x (Java 21). Provides declarative HTTP pipelines with automatic JSON serialization, a Result monad for error handling, and an in-memory test layer that removes Vert.x entirely from unit tests.

## Build & Test Commands

```bash
./lux test # compile and test
```

CI runs `mvn test` on PRs to `master` using JDK 21 (Temurin).

> **Cloud mode:** Do NOT run `mvn test` (or `mvn verify`/`mvn integration-test`) or ./lux test in cloud mode — Maven cannot download JARs due to restricted network access. A PreToolUse hook automatically blocks these commands when `CLAUDE_CODE_REMOTE=1`. Tests will run via GitHub Actions CI when the PR is raised.
When running locally to get feedback on changes, just run `./lux test`. It will run the whole suite but it will be quick 


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

## Documentation

Detailed framework docs live in `docs/src/content/docs/` as `.mdx` files. Treat these as authoritative. Before making non-trivial changes to pipelines, validation, filters, error handling, file upload/download, websockets, the HTTP client, or configuration, read the relevant guide in `docs/src/content/docs/guides/`. Testing behaviour is documented in `docs/src/content/docs/testing/index.mdx`.

## Project-Specific Rules

- **Application State** is only to ever be accessed within the non-blocking `HttpStream` map methods of the pipelines, or when calling a Luxis method directly.
- **Test client tests** should only ever take the perspective of the client. Despite having access to the server, we continue to treat the server as a black box, only ever asserting the behaviour the client gives. The only exception to this is asserting the exceptionHandler.
- **Documentation** should only ever refer to code in the main module. Both test-support and test are for the benefit of testing this framework, not for other who want to use the framework

## Dependencies

- Vert.x 4.5.1 (core, web, auth-jwt)
- Jackson 2.16.0 (JSON serialization)
- JUnit 4.12
