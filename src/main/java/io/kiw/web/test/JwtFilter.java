package io.kiw.web.test;

import io.kiw.web.pipeline.HttpStream;
import io.kiw.web.internal.RequestPipeline;
import io.kiw.web.handler.VertxJsonFilter;
import io.kiw.web.jwt.JwtProvider;

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
