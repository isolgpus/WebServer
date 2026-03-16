# Test Coverage Analysis

## Current State

The project has **7 test classes** with ~84 test methods covering: HTTP infrastructure, CORS, validation, JWT auth, WebSockets, custom status codes, and OpenAPI spec generation. All tests run in-memory via `StubRouter`/`TestApplicationClient` — no Vert.x server needed.

## Coverage Gaps & Recommendations

### 1. `Result<E, S>` monad — NO dedicated tests

The `Result` class (`io.kiw.result.Result`) is a foundational building block used throughout the entire framework, yet it has **zero dedicated unit tests**. It is only exercised indirectly through HTTP pipeline tests.

**What to test:**
- `Result.success(value)` — `map`, `flatMap`, `mapError`, `fold`, `consume` all follow the success path
- `Result.error(value)` — `map`, `flatMap`, `mapError`, `fold`, `consume` all follow the error path
- `Result.collapse(List<Result>)` — all successes yields `Success<List<S>>`, mixed yields `Error<Map<Integer, E>>` with correct indices, all errors, empty list
- Verify that `map` on an error short-circuits (returns the same error without calling the mapper)
- Verify that `flatMap` on a success can produce either success or error
- Verify that `mapError` on a success is a no-op

**Priority: HIGH** — this is a core abstraction; a bug here silently breaks everything.

---

### 2. Validation edge cases — partial coverage

`ValidatorTest` covers the happy and basic sad paths but misses several edge cases in `FieldChain`, `Validator`, and `ListValidator`:

**What to test:**
- **`FieldChain.validate(Predicate, message)`** — the custom predicate validator has no tests at all
- **`FieldChain.minLength`/`maxLength` on non-String values** — these silently pass when the value isn't a `String` (e.g., passing a number). This may be intentional but should be tested to document the behavior
- **`FieldChain.min`/`max` on non-Number values** — same silent pass-through behavior
- **`FieldChain.email()` on null values** — the check skips blank strings but what about null?
- **`FieldChain.matches()` on null values** — does it silently pass or NPE?
- **Nested object validation when the nested object is null** — `Validator.jsonField` with a nested block skips validation when null, but this is never tested
- **`ListValidator.each()` when list is null** — should be a no-op, worth verifying
- **Multiple validation rules accumulating errors on a single field** — partially tested but could be more thorough

**Priority: MEDIUM** — validation is user-facing; subtle edge cases can cause unexpected 422s or silent pass-throughs.

---

### 3. Pipeline chaining with multiple steps — lightly tested

Most pipeline tests use a single `complete()` step. The framework supports multi-step chains like `.map(...).flatMap(...).blockingMap(...).complete(...)`, but this composition is barely tested.

**What to test:**
- A chain of 3+ transformations where an intermediate step returns an error — verify the chain short-circuits correctly
- A chain mixing `map` → `flatMap` → `blockingMap` → `complete` — verify the value flows through
- A chain where a `flatMap` in the middle returns an error with a custom status code
- Pipeline with `validate()` followed by `map()` — verify validation errors prevent the map from running
- Pipeline with `requireJwt()` followed by `validate()` followed by business logic

**Priority: MEDIUM** — this is the primary user-facing API pattern; complex chains are where subtle bugs hide.

---

### 4. Error/exception handling in async pipelines — gaps exist

`asyncFlatMap` error paths are only tested for the "handler throws exception" scenario. The `CompletableFuture` failure path (where the future completes exceptionally rather than the handler itself throwing) is not tested.

**What to test:**
- `asyncMap` where the `CompletableFuture` completes exceptionally (not the handler throwing)
- `asyncFlatMap` where the future completes with an error `Result`
- `asyncBlockingFlatMap` where the future completes exceptionally
- Verify that exceptions in async pipelines still return 500 with `{"message":"Something went wrong"}`

**Priority: MEDIUM** — async error handling is notoriously tricky; a missed path means unhandled exceptions in production.

---

### 5. `PathMatcher` — NO dedicated tests

`PathMatcher` is a non-trivial routing tree with wildcard support, path parameter extraction, and all-method routes. It is only tested indirectly through higher-level HTTP tests.

**What to test:**
- Exact path matching: `/a/b` matches `/a/b`, does not match `/a/c`
- Path parameter extraction: `/users/:id` matches `/users/42` and extracts `id=42`
- Multiple path parameters: `/users/:userId/posts/:postId`
- Wildcard routes: `/api/*` matches `/api/anything`
- Priority between exact match and param match (e.g., `/users/admin` vs `/users/:id`)
- All-method routes interact correctly with method-specific routes
- No match returns empty flows list
- Trailing slashes, empty segments

**Priority: MEDIUM** — path matching bugs cause routes to silently not match or match the wrong handler.

---

### 6. WebSocket edge cases

`WebSocketTest` covers the basics but misses:

**What to test:**
- **Multiple concurrent clients** on the same WebSocket route
- **`session.close()`** initiated by the server
- **Exception thrown inside `onMessage` handler** — verify the exception handler is called and the connection stays alive
- **Exception thrown inside `onOpen` handler**
- **Large messages or rapid message sequences**

**Priority: LOW-MEDIUM** — depends on how heavily WebSockets are used.

---

### 7. CORS edge cases

The CORS tests are thorough but miss:

**What to test:**
- **Multiple origins configured** — verify only the matching origin is reflected (not all of them)
- **Case sensitivity** in origin matching
- **CORS on error responses** — if a handler returns 400/500, are CORS headers still added?
- **CORS headers on filter-blocked responses** (e.g., a 401 from JWT auth)

**Priority: LOW** — current tests are good; these are hardening tests.

---

### 8. JWT authentication edge cases

**What to test:**
- **Token with no `sub` claim** — `getSubject()` returns null; is this handled by consumers?
- **`getJwtClaims()` called when no JWT middleware was applied** — returns null, could NPE downstream
- **Token with special characters in claims**
- **Empty bearer token** (`Authorization: Bearer `)

**Priority: LOW** — existing JWT tests are solid; these are edge-case hardening.

---

### 9. OpenAPI spec generation — nested/complex types

**What to test:**
- **Nested object types** in request/response schemas
- **List/array fields** in generated schemas
- **Multiple routes with the same path but different methods** (e.g., GET and POST on `/users`)
- **Routes with multiple path parameters**

**Priority: LOW** — current spec tests cover the main paths well.

---

## Summary: Recommended Priority Order

| # | Area | Priority | Effort |
|---|------|----------|--------|
| 1 | `Result` monad dedicated tests | HIGH | Low |
| 2 | Validation edge cases (`validate()`, null handling, non-String types) | MEDIUM | Low |
| 3 | Multi-step pipeline chains | MEDIUM | Medium |
| 4 | Async pipeline error paths (exceptional futures) | MEDIUM | Low |
| 5 | `PathMatcher` dedicated tests | MEDIUM | Medium |
| 6 | WebSocket edge cases | LOW-MED | Medium |
| 7 | CORS on error responses | LOW | Low |
| 8 | JWT edge cases | LOW | Low |
| 9 | OpenAPI nested types | LOW | Low |

Starting with items 1, 2, and 4 would give the highest coverage improvement for the least effort.
