package io.kiw.web.infrastructure;

import io.kiw.result.Result;
import io.kiw.web.test.StubVertxContext;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class ValidatorTest {

    private static class Body {
        String name;
        String email;
        Integer age;
        Address address;

        Body(String name, String email, Integer age, Address address) {
            this.name = name;
            this.email = email;
            this.age = age;
            this.address = address;
        }
    }

    private static class Address {
        String city;
        String zip;

        Address(String city, String zip) {
            this.city = city;
            this.zip = zip;
        }
    }

    private Validator<Body> validator(Body body) {
        return new Validator<>(body, null, "");
    }

    private Validator<Body> validatorWithHttp(Body body, Map<String, String> queryParams, Map<String, String> pathParams) {
        StubVertxContext ctx = new StubVertxContext("{}", queryParams, Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
        ctx.setPathParams(pathParams);
        return new Validator<>(body, new HttpContext(ctx), "");
    }

    // --- required ---

    @Test
    public void required_failsOnNull() {
        Validator<Body> v = validator(new Body(null, "a@b.com", 1, null));
        v.jsonField("name", r -> r.name).required();
        assertError(v, "name", "must not be blank");
    }

    @Test
    public void required_failsOnBlank() {
        Validator<Body> v = validator(new Body("  ", "a@b.com", 1, null));
        v.jsonField("name", r -> r.name).required();
        assertError(v, "name", "must not be blank");
    }

    @Test
    public void required_passesOnNonBlank() {
        Validator<Body> v = validator(new Body("Alice", "a@b.com", 1, null));
        v.jsonField("name", r -> r.name).required();
        assertNoErrors(v);
    }

    // --- minLength ---

    @Test
    public void minLength_failsBelowMin() {
        Validator<Body> v = validator(new Body("Al", null, null, null));
        v.jsonField("name", r -> r.name).minLength(3);
        assertError(v, "name", "must be at least 3 characters");
    }

    @Test
    public void minLength_passesAtMin() {
        Validator<Body> v = validator(new Body("Ali", null, null, null));
        v.jsonField("name", r -> r.name).minLength(3);
        assertNoErrors(v);
    }

    @Test
    public void minLength_skipsNull() {
        Validator<Body> v = validator(new Body(null, null, null, null));
        v.jsonField("name", r -> r.name).minLength(3);
        assertNoErrors(v);
    }

    // --- maxLength ---

    @Test
    public void maxLength_failsAboveMax() {
        Validator<Body> v = validator(new Body("Alicia", null, null, null));
        v.jsonField("name", r -> r.name).maxLength(3);
        assertError(v, "name", "must be at most 3 characters");
    }

    @Test
    public void maxLength_passesAtMax() {
        Validator<Body> v = validator(new Body("Ali", null, null, null));
        v.jsonField("name", r -> r.name).maxLength(3);
        assertNoErrors(v);
    }

    // --- email ---

    @Test
    public void email_failsWithoutAt() {
        Validator<Body> v = validator(new Body(null, "notanemail", null, null));
        v.jsonField("email", r -> r.email).email();
        assertError(v, "email", "must be a valid email address");
    }

    @Test
    public void email_failsWithSpaces() {
        Validator<Body> v = validator(new Body(null, "a b@c.com", null, null));
        v.jsonField("email", r -> r.email).email();
        assertError(v, "email", "must be a valid email address");
    }

    @Test
    public void email_passesOnValid() {
        Validator<Body> v = validator(new Body(null, "user@example.com", null, null));
        v.jsonField("email", r -> r.email).email();
        assertNoErrors(v);
    }

    @Test
    public void email_skipsNull() {
        Validator<Body> v = validator(new Body(null, null, null, null));
        v.jsonField("email", r -> r.email).email();
        assertNoErrors(v);
    }

    @Test
    public void email_skipsBlank() {
        Validator<Body> v = validator(new Body(null, "  ", null, null));
        v.jsonField("email", r -> r.email).email();
        assertNoErrors(v);
    }

    // --- matches ---

    @Test
    public void matches_failsOnNonMatch() {
        Validator<Body> v = validator(new Body("abc", null, null, null));
        v.jsonField("name", r -> r.name).matches("[0-9]+");
        assertError(v, "name", "must match pattern: [0-9]+");
    }

    @Test
    public void matches_passesOnMatch() {
        Validator<Body> v = validator(new Body("123", null, null, null));
        v.jsonField("name", r -> r.name).matches("[0-9]+");
        assertNoErrors(v);
    }

    // --- numeric min/max ---

    @Test
    public void min_failsBelowMin() {
        Validator<Body> v = validator(new Body(null, null, -1, null));
        v.jsonField("age", r -> r.age).min(0);
        assertError(v, "age", "must be at least 0.0");
    }

    @Test
    public void min_passesAtMin() {
        Validator<Body> v = validator(new Body(null, null, 0, null));
        v.jsonField("age", r -> r.age).min(0);
        assertNoErrors(v);
    }

    @Test
    public void max_failsAboveMax() {
        Validator<Body> v = validator(new Body(null, null, 200, null));
        v.jsonField("age", r -> r.age).max(150);
        assertError(v, "age", "must be at most 150.0");
    }

    @Test
    public void max_passesAtMax() {
        Validator<Body> v = validator(new Body(null, null, 150, null));
        v.jsonField("age", r -> r.age).max(150);
        assertNoErrors(v);
    }

    @Test
    public void numericRequired_failsOnNull() {
        Validator<Body> v = validator(new Body(null, null, null, null));
        v.jsonField("age", r -> r.age).required();
        assertError(v, "age", "must not be blank");
    }

    // --- multiple errors accumulate ---

    @Test
    public void multipleRulesOnSameField_accumulateErrors() {
        Validator<Body> v = validator(new Body(null, null, null, null));
        v.jsonField("name", r -> r.name).required();
        v.jsonField("email", r -> r.email).required();
        assertEquals(2, v.errors.size());
        assertTrue(v.errors.containsKey("name"));
        assertTrue(v.errors.containsKey("email"));
    }

    @Test
    public void combinedRequiredAndFormat_bothRecorded() {
        Validator<Body> v = validator(new Body(null, "bad", null, null));
        v.jsonField("name", r -> r.name).required();
        v.jsonField("email", r -> r.email).required().email();
        List<String> emailErrors = v.errors.get("email");
        assertNotNull(emailErrors);
        assertEquals("must be a valid email address", emailErrors.get(0));
    }

    // --- nested body field ---

    @Test
    public void jsonField_prefixesErrorKeys() {
        Body body = new Body(null, null, null, new Address(null, "bad"));
        Validator<Body> v = validator(body);
        v.jsonField("address", r -> r.address, a -> {
            a.jsonField("city", x -> x.city).required();
            a.jsonField("zip", x -> x.zip).matches("[0-9]{5}");
        });
        assertTrue(v.errors.containsKey("address.city"));
        assertTrue(v.errors.containsKey("address.zip"));
    }

    @Test
    public void jsonField_skipsBlockWhenNull() {
        Body body = new Body(null, null, null, null);
        Validator<Body> v = validator(body);
        v.jsonField("address", r -> r.address, a -> {
            a.jsonField("city", x -> x.city).required();
        });
        assertNoErrors(v);
    }

    @Test
    public void jsonField_passesWhenValid() {
        Body body = new Body(null, null, null, new Address("NYC", "12345"));
        Validator<Body> v = validator(body);
        v.jsonField("address", r -> r.address, a -> {
            a.jsonField("city", x -> x.city).required();
            a.jsonField("zip", x -> x.zip).required().matches("[0-9]{5}");
        });
        assertNoErrors(v);
    }

    // --- query param ---

    @Test
    public void queryParam_failsWhenMissing() {
        Validator<Body> v = validatorWithHttp(new Body(null, null, null, null),
            Collections.emptyMap(), Collections.emptyMap());
        v.queryParam("page").required();
        assertError(v, "page", "must not be blank");
    }

    @Test
    public void queryParam_failsPatternMismatch() {
        Validator<Body> v = validatorWithHttp(new Body(null, null, null, null),
            Map.of("page", "abc"), Collections.emptyMap());
        v.queryParam("page").matches("[0-9]+");
        assertError(v, "page", "must match pattern: [0-9]+");
    }

    @Test
    public void queryParam_passesWhenValid() {
        Validator<Body> v = validatorWithHttp(new Body(null, null, null, null),
            Map.of("page", "5"), Collections.emptyMap());
        v.queryParam("page").required().matches("[0-9]+");
        assertNoErrors(v);
    }

    // --- path param ---

    @Test
    public void pathParam_failsWhenMissing() {
        Validator<Body> v = validatorWithHttp(new Body(null, null, null, null),
            Collections.emptyMap(), Collections.emptyMap());
        v.pathParam("userId").required();
        assertError(v, "userId", "must not be blank");
    }

    @Test
    public void pathParam_passesWhenPresent() {
        Validator<Body> v = validatorWithHttp(new Body(null, null, null, null),
            Collections.emptyMap(), Map.of("userId", "42"));
        v.pathParam("userId").required().matches("[0-9]+");
        assertNoErrors(v);
    }

    // --- toResult ---

    @Test
    public void toResult_successWhenNoErrors() {
        Body body = new Body("Alice", "a@b.com", 25, null);
        Validator<Body> v = validator(body);
        v.jsonField("name", r -> r.name).required();
        Result<HttpErrorResponse, Body> result = v.toResult();
        boolean[] isSuccess = {false};
        result.consume(err -> {}, val -> isSuccess[0] = true);
        assertTrue(isSuccess[0]);
    }

    @Test
    public void toResult_errorWhenErrors() {
        Validator<Body> v = validator(new Body(null, null, null, null));
        v.jsonField("name", r -> r.name).required();
        Result<HttpErrorResponse, Body> result = v.toResult();
        boolean[] isError = {false};
        int[] statusCode = {0};
        result.consume(err -> {
            isError[0] = true;
            statusCode[0] = err.statusCode;
        }, val -> {});
        assertTrue(isError[0]);
        assertEquals(422, statusCode[0]);
    }

    // --- helpers ---

    private void assertError(Validator<?> v, String field, String message) {
        List<String> errs = v.errors.get(field);
        assertNotNull("Expected error for field: " + field, errs);
        assertTrue("Expected message '" + message + "' in " + errs, errs.contains(message));
    }

    private void assertNoErrors(Validator<?> v) {
        assertTrue("Expected no errors but got: " + v.errors, v.errors.isEmpty());
    }
}
