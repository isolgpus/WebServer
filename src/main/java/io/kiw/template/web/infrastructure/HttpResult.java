package io.kiw.template.web.infrastructure;

import java.util.function.Function;

public class HttpResult<S> {


    private final boolean successful;
    final S successValue;
    final MessageResponse errorMessageValue;
    final int statusCode;

    private HttpResult(boolean successful, S successValue, MessageResponse errorMessageValue, int statusCode) {
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

    public static <S> HttpResult<S> success() {
        return new HttpResult<>(true, null, null, 200);
    }

    boolean isSuccessful() {
        return successful;
    }

    public <OUT> HttpResult<OUT> map(Function<S, OUT> mapper)
    {
        if(successful)
        {
            return HttpResult.success(mapper.apply(this.successValue));
        }
        else
        {
            return HttpResult.error(this.statusCode, errorMessageValue);
        }
    }

    public <OUT> HttpResult<OUT> flatmap(Function<S, HttpResult<OUT>> mapper)
    {
        if(successful)
        {
            return mapper.apply(this.successValue);
        }
        else
        {
            return HttpResult.error(this.statusCode, errorMessageValue);
        }
    }
}
