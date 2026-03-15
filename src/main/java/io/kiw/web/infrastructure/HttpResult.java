package io.kiw.web.infrastructure;

import io.kiw.result.Result;

public abstract class HttpResult<S> {


    public static <S> Result<HttpErrorResponse, S> from(Result<String, S> result, ErrorStatusCode statusCodeOnFailure) {
        return result.fold(
            e -> Result.error(new HttpErrorResponse(new ErrorMessageResponse(e), statusCodeOnFailure)),
            Result::success);
    }

    public static <S> Result<HttpErrorResponse, S> from(Result<String, S> result) {
        return from(result, ErrorStatusCode.BAD_REQUEST);
    }

    public static <S> Result<HttpErrorResponse, S> error(ErrorStatusCode statusCode, ErrorMessageResponse messageResponse) {
        return Result.error(new HttpErrorResponse(messageResponse, statusCode));
    }

    public static <S> Result<HttpErrorResponse, S> success(S response) {
        return Result.success(response);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <S> Result<HttpErrorResponse, S> success(S response, SuccessStatusCode statusCode) {
        return (Result) Result.success(new HttpSuccessResponse<>(response, statusCode));
    }

    public static <S> Result<HttpErrorResponse, S> success() {
        return Result.success(null);
    }
}
