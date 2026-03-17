package io.kiw.web.infrastructure;

public interface VertxJsonFilter<APP>{

    RequestPipeline handle(HttpStream<Void, APP> e);

}
