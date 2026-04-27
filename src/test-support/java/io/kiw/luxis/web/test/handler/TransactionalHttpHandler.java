package io.kiw.luxis.web.test.handler;

import io.kiw.luxis.result.Result;
import io.kiw.luxis.web.handler.JsonHandler;
import io.kiw.luxis.web.http.HttpResult;
import io.kiw.luxis.web.http.client.LuxisAsync;
import io.kiw.luxis.web.internal.LuxisPipeline;
import io.kiw.luxis.web.pipeline.HttpStream;
import io.kiw.luxis.web.test.ContextAsserter;
import io.kiw.luxis.web.test.MyApplicationState;

public class TransactionalHttpHandler implements JsonHandler<EchoRequest, EchoResponse, MyApplicationState> {

    private final ContextAsserter asserter;

    public TransactionalHttpHandler(final ContextAsserter asserter) {
        this.asserter = asserter;
    }

    @Override
    public LuxisPipeline<EchoResponse> handle(final HttpStream<EchoRequest, MyApplicationState> e) {
        return e.map(ctx -> {
                    asserter.notInTransaction();
                    return ctx.in().stringExample;
                })
                .inTransaction(tx -> tx
                        .map(ctx -> {
                            asserter.inTransaction();
                            return ctx.in() + "-queried";
                        })
                        .asyncMap(ctx -> {
                            asserter.inTransaction();
                            return LuxisAsync.completed(ctx.in() + "-updated");
                        })
                        .flatMap(ctx -> {
                            asserter.inTransaction();
                            return Result.success(ctx.in());
                        })
                        .peek(ctx -> asserter.inTransaction())
                        .onCompletion(ctx -> {
                            asserter.notInTransaction();
                            ctx.app().setLongValue(99);
                        })
                        .commit())
                .complete(ctx -> {
                    asserter.notInTransaction();
                    return HttpResult.success(new EchoResponse(0, ctx.in(), null, null, null, null));
                });
    }
}
