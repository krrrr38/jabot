package com.krrrr38.jabot.plugin.adapter.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(suppressConstructorProperties = true)
public class PostMessage {
    private String color;
    private String message;
    private boolean notify;
    @JsonProperty("message_format")
    private String messageFormat;
}
