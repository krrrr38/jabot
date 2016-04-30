package com.krrrr38.jabot.plugin.message;

import lombok.NonNull;
import lombok.Value;

@Value
public class ReceiveMessage {
    @NonNull
    private Sender sender;
    @NonNull
    private String message;
}
