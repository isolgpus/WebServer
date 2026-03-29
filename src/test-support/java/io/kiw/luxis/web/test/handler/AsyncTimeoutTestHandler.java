package io.kiw.luxis.web.test.handler;

import io.kiw.luxis.web.TestLuxis;
import io.kiw.luxis.web.handler.JsonHandler;
import io.kiw.luxis.web.http.HttpResult;
import io.kiw.luxis.web.internal.RequestPipeline;
import io.kiw.luxis.web.pipeline.HttpStream;
import io.kiw.luxis.web.test.MyApplicationState;

public class AsyncTimeoutTestHandler extends JsonHandler<AsyncMapRequest, AsyncMapResponse, MyApplicationState> {

    private TestLuxis<?> testLuxis;

    @Override
    public RequestPipeline<AsyncMapResponse> handle(final HttpStream<AsyncMapRequest, MyApplicationState> httpStream) {
        return httpStream
                .<Integer>asyncMap(ctx -> {
                    // Deliberately do NOT call handleAsyncResponse — simulate missing response
                    // Advance time past the 30s timeout to trigger the timeout handler
                    testLuxis.advanceTimeBy(30_001);
                })
                .map(ctx -> new AsyncMapResponse(ctx.in()))
                .complete(ctx -> HttpResult.success(ctx.in()));
    }

    public void evillyReferenceLuxis(final TestLuxis<?> testLuxis) {
        this.testLuxis = testLuxis;
    }
}
