package io.kiw.template.web.test;

import io.kiw.template.web.infrastructure.*;

import java.util.Optional;

public class ValidateQueryParamsHandler extends VertxJsonRoute<EmptyRequest, ValidateQueryParamsResponse, MyApplicationState> {
    @Override
    public Flow<ValidateQueryParamsResponse> handle(HttpControlStream<EmptyRequest, MyApplicationState> e) {
        return e.flatMap((request, httpContext, applicationState) -> {
            return httpContext.getQueryParamValidator()
                            .required("required").validate(ra -> ra.equals("IAMREQUIRED"), "Did not match expected value").next()
                            .required("rangedInt")
                                .attemptMap(Integer::parseInt, "is not a valid number")
                                .validate(i -> 20 <= i && i <= 80, "is not in expected range 20 - 78")
                                .next()
                            .optional("defaultedInt", "22").attemptMap(Integer::parseInt, "is not a valid number").validate(i -> true, "should not fail").next()
                            .optional("optionalInt").attemptMap(Integer::parseInt, "is not a valid number").validate(i -> true, "should not fail").next()
                            .optional("optionalFlatMapInt").attemptFlatMap(s -> Optional.of(Integer.parseInt(s)), "is not a valid number").validate(i -> true, "should not fail").next()
                            .optional("optionalToEmptyFlatMapInt").attemptFlatMap(v -> Optional.empty(), "is not a valid number").validate(i -> true, "should not fail").next()
                            .toHttpResult(ValidateQueryParamsResponse::new);
                }
        ).complete((validateQueryParamsResponse, context, app) -> HttpResult.success(validateQueryParamsResponse));
    }
}
