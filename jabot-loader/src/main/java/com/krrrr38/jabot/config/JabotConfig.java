package com.krrrr38.jabot.config;

import java.util.List;

import lombok.Data;

@Data
public class JabotConfig {
    private String name;
    private PluginConfig adapter;
    private PluginConfig brain;
    private List<PluginConfig> handlers;
}
