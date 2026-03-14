package io.kiw.web.infrastructure;

public interface HttpControlStreamBlockingMapper<REQ, RES> {
    RES handle(BlockingContext<REQ> ctx);
}
