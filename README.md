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

## Documentation

Full documentation is available at **[isolgpus.github.io/Luxis](https://isolgpus.github.io/Luxis)**.

- [Getting Started](https://isolgpus.github.io/Luxis/getting-started/)
- [Handler Pipeline](https://isolgpus.github.io/Luxis/guides/handler-pipeline/)
- [Validation](https://isolgpus.github.io/Luxis/guides/validation/)
- [Testing Your Handlers](https://isolgpus.github.io/Luxis/testing/)

## License

Licensed under the [Apache License, Version 2.0](LICENSE).
