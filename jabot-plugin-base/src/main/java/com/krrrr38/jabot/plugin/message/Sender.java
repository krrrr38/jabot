package com.krrrr38.jabot.plugin.message;

import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@AllArgsConstructor
public class Sender {
    private String id;
    private String mentionId;
    private String name;
    private String email;

    public Optional<String> getId() {
        return Optional.ofNullable(id);
    }

    public Optional<String> getMentionId() {
        return Optional.ofNullable(mentionId);
    }

    public Optional<String> getName() {
        return Optional.ofNullable(name);
    }

    public Optional<String> getEmail() {
        return Optional.ofNullable(name);
    }
}
