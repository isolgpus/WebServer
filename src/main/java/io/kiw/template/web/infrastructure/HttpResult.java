package io.kiw.template.web.infrastructure;

public class HttpResult<S> {


    private final boolean successful;
    public final S successValue;
    public final MessageResponse errorMessageValue;
    public final int statusCode;

    public HttpResult(boolean successful, S successValue, MessageResponse errorMessageValue, int statusCode) {
        this.successful = successful;

        this.successValue = successValue;
        this.errorMessageValue = errorMessageValue;
        this.statusCode = statusCode;
    }

    public static <S> HttpResult<S> error(int statusCode, MessageResponse messageResponse) {
        return new HttpResult<>(false, null, messageResponse, statusCode);
    }

    public static <S> HttpResult<S> success(S success) {
        return new HttpResult<>(true, success, null, 200);
    }

    public boolean isSuccessful() {
        return successful;
    }
}
