package com.krrrr38.jabot.config;

import java.util.List;

public class JabotConfig {
    private String name;
    private PluginConfig adapter;
    private PluginConfig brain;
    private List<PluginConfig> handlers;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PluginConfig getAdapterConfig() {
        return adapter;
    }

    public void setAdapter(PluginConfig adapter) {
        this.adapter = adapter;
    }

    public PluginConfig getBrainConfig() {
        return brain;
    }

    public void setBrain(PluginConfig brain) {
        this.brain = brain;
    }

    public List<PluginConfig> getHandlers() {
        // If change getHandlerConfigs, yaml is never mapped.
        return handlers;
    }

    public void setHandlers(List<PluginConfig> handlers) {
        this.handlers = handlers;
    }

    @Override
    public String toString() {
        return "jabotConfig{" +
                "adapter=" + adapter +
                ", brain=" + brain +
                ", handlers=" + handlers +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JabotConfig)) return false;

        JabotConfig that = (JabotConfig) o;

        if (adapter != null ? !adapter.equals(that.adapter) : that.adapter != null) return false;
        if (brain != null ? !brain.equals(that.brain) : that.brain != null) return false;
        return !(handlers != null ? !handlers.equals(that.handlers) : that.handlers != null);
    }

    @Override
    public int hashCode() {
        int result = adapter != null ? adapter.hashCode() : 0;
        result = 31 * result + (brain != null ? brain.hashCode() : 0);
        result = 31 * result + (handlers != null ? handlers.hashCode() : 0);
        return result;
    }
}
