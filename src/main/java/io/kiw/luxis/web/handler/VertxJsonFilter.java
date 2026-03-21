package io.kiw.luxis.web.handler;

import io.kiw.luxis.web.http.*;
import io.kiw.luxis.web.pipeline.*;
import io.kiw.luxis.web.websocket.*;
import io.kiw.luxis.web.internal.*;
import io.kiw.luxis.web.internal.ender.*;

public interface VertxJsonFilter<APP>{

    RequestPipeline<Void> handle(HttpStream<Void, APP> e);

}
