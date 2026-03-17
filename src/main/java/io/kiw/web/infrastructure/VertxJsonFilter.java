package io.kiw.web.infrastructure;

public interface VertxJsonFilter<APP>{

    RequestPipeline<Void> handle(HttpStream<Void, APP> e);

}
