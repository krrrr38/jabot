package com.krrrr38.jabot.plugin.message;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
public class SendMessage {
    @NonNull
    private final String message;
    private final String replyId;

    public SendMessage(@NonNull String message) {
        this(message, null);
    }

    public SendMessage(@NonNull String message, String replyId) {
        this.message = message;
        this.replyId = replyId;
    }
}
