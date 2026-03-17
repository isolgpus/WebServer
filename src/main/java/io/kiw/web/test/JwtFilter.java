package io.kiw.web.test;

import io.kiw.web.infrastructure.HttpStream;
import io.kiw.web.infrastructure.RequestPipeline;
import io.kiw.web.infrastructure.VertxJsonFilter;
import io.kiw.web.infrastructure.jwt.JwtProvider;

public class JwtFilter implements VertxJsonFilter<MyApplicationState>
{

    private final JwtProvider jwtProvider;

    public JwtFilter(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    @Override
    public RequestPipeline<Void> handle(HttpStream<Void, MyApplicationState> e) {
        return e.requireJwt(jwtProvider)
            .complete();
    }
}
