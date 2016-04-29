package com.krrrr38.jabot.config;

import java.util.Collections;
import java.util.Map;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class PluginConfig {
    private String plugin;
    private String namespace;
    private Map<String, String> options;

    public static final PluginConfig NONE;
    static {
        NONE = new PluginConfig();
        NONE.setPlugin("NONE");
        NONE.setNamespace("NONE");
        NONE.setOptions(Collections.emptyMap());
    }

    /**
     * plugin(adapter/handler/brain) class name with package (required)
     *
     * @return plugin class name with package
     */
    public String getPlugin() {
        if (plugin == null) {
            throw new NullPointerException("plugin required");
        }
        return plugin;
    }

    public void setPlugin(String plugin) {
        this.plugin = plugin;
    }

    /**
     * plugin namespace for brain (required)
     *
     * @return plugin namespace
     */
    public String getNamespace() {
        if (namespace == null) {
            throw new NullPointerException("namespace required");
        }
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    /**
     * plugin options (nullable)
     *
     * @return plugin options
     */
    public Map<String, String> getOptions() {
        return options != null ? options : Collections.emptyMap();
    }

    public void setOptions(Map<String, String> options) {
        this.options = options;
    }
}
