package io.kiw.luxis.web.test;

import io.kiw.luxis.web.handler.VertxJsonFilter;
import io.kiw.luxis.web.internal.RequestPipeline;
import io.kiw.luxis.web.jwt.JwtProvider;
import io.kiw.luxis.web.pipeline.HttpStream;

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
