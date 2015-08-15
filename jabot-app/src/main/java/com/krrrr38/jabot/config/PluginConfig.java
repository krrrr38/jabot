package com.krrrr38.jabot.config;

import java.util.Collections;
import java.util.Map;

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
     * @return
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
     * @return
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
     * @return
     */
    public Map<String, String> getOptions() {
        return options != null ? options : Collections.emptyMap();
    }

    public void setOptions(Map<String, String> options) {
        this.options = options;
    }

    @Override
    public String toString() {
        return "PluginConfig{" +
                "plugin='" + plugin + '\'' +
                ", namespace='" + namespace + '\'' +
                ", options=" + options +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PluginConfig)) return false;

        PluginConfig that = (PluginConfig) o;

        if (!plugin.equals(that.plugin)) return false;
        if (!namespace.equals(that.namespace)) return false;
        return !(options != null ? !options.equals(that.options) : that.options != null);

    }

    @Override
    public int hashCode() {
        int result = plugin.hashCode();
        result = 31 * result + namespace.hashCode();
        result = 31 * result + (options != null ? options.hashCode() : 0);
        return result;
    }
}
