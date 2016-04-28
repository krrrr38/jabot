package com.krrrr38.jabot.plugin.adapter.model;

import lombok.Value;

@Value
public class PostMessage {
    private String color;
    private String message;
    private boolean notify;
    private String messageFormat;
}
