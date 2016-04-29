package com.krrrr38.jabot.plugin.message;

import lombok.Value;

@Value
public class ReceiveMessage {
    private Sender sender;
    private String message;
}
