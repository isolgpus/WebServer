package io.kiw.template.web.test;

import io.kiw.template.web.infrastructure.*;

public class ValidateQueryParamsHandler extends VertxJsonRoute<EmptyRequest, ValidateQueryParamsResponse, MyApplicationState> {
    @Override
    public Flow<ValidateQueryParamsResponse> handle(HttpControlStream<EmptyRequest, MyApplicationState> e) {
        return e.flatMap((request, httpContext, applicationState) ->
                httpContext.getQueryParamValidator()
                        .required("required").validate(ra -> ra.equals("IAMREQUIRED"), "Did not match expected value").next()
                        .required("rangedInt").attemptMap(Integer::parseInt, "Was not a valid number").next()
                        .toHttpResult(ValidateQueryParamsResponse::new)
        ).complete((validateQueryParamsResponse, context, app) -> HttpResult.success(validateQueryParamsResponse));
    }
}
