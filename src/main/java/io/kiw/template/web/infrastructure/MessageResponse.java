package io.kiw.template.web.infrastructure;

import java.util.Map;

public class MessageResponse {
    public final String message;
    public final Map<String, String> messages;

    public MessageResponse(String message) {
        this.message = message;
        this.messages = null;
    }
    public MessageResponse(String message, Map<String, String> messages) {
        this.message = message;
        this.messages = messages;
    }

}
