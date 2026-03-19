package io.kiw.web.infrastructure;

import io.kiw.result.Result;

public abstract class WebSocketResult<S> {

    public static <S> Result<ErrorMessageResponse, S> from(Result<String, S> result) {
        return result.fold(
            e -> Result.error(new ErrorMessageResponse(e)),
            Result::success);
    }

    public static <S> Result<ErrorMessageResponse, S> error(ErrorMessageResponse messageResponse) {
        return Result.error(messageResponse);
    }

    public static <S> Result<ErrorMessageResponse, S> error(String message) {
        return Result.error(new ErrorMessageResponse(message));
    }

    public static <S> Result<ErrorMessageResponse, S> success(S response) {
        return Result.success(response);
    }

    public static <S> Result<ErrorMessageResponse, S> success() {
        return Result.success(null);
    }
}
