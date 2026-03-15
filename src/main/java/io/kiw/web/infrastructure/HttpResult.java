package io.kiw.web.infrastructure;

import io.kiw.result.Result;

public abstract class HttpResult<S> {


    public static <S> Result<HttpErrorResponse, S> from(Result<String, S> result, int statusCodeOnFailure) {
        return result.fold(
            e -> Result.error(new HttpErrorResponse(new ErrorMessageResponse(e), statusCodeOnFailure)),
            Result::success);
    }

    public static <S> Result<HttpErrorResponse, S> from(Result<String, S> result) {
        return from(result, 400);
    }

    public static <S> Result<HttpErrorResponse, S> error(int statusCode, ErrorMessageResponse messageResponse) {
        return Result.error(new HttpErrorResponse(messageResponse, statusCode));
    }

    public static <S> Result<HttpErrorResponse, S> success(S response) {
        return Result.success(response);
    }

    @SuppressWarnings("unchecked")
    public static <S> Result<HttpErrorResponse, S> success(S response, int statusCode) {
        return (Result<HttpErrorResponse, S>) Result.success(new HttpSuccessResponse<>(response, statusCode));
    }

    public static <S> Result<HttpErrorResponse, S> success() {
        return Result.success(null);
    }
}
