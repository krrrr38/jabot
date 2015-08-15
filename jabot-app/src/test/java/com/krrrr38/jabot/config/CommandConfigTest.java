package com.krrrr38.jabot.config;

import org.junit.Test;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class CommandConfigTest {
    private CommandConfig commandConfig = new CommandConfig();

    @Test
    public void testTojabotConfig() throws Exception {
        setConfigFile(commandConfig, "valid.yml");
        JabotConfig JabotConfig = commandConfig.tojabotConfig();

        // name
        assertThat(JabotConfig.getName(), is("jabot"));

        // adapter
        assertThat(JabotConfig.getAdapterConfig(), is(notNullValue()));
        PluginConfig adapter = JabotConfig.getAdapterConfig();
        assertThat(adapter, is(notNullValue()));
        assertThat(adapter.getPlugin(), is("com.krrrr38.jabot.adapter.ShellAdapter"));
        assertThat(adapter.getNamespace(), is("shell-adapter"));
        assertThat(adapter.getOptions().get("prompt"), is("> "));
        assertThat(adapter.getOptions().get("foo"), is("bar"));

        // handlers
        List<PluginConfig> handlers = JabotConfig.getHandlers();
        assertThat(handlers, is(notNullValue()));
        assertThat(handlers.size(), is(2));
        assertThat(handlers.get(0).getPlugin(), is("com.krrrr38.jabot.handler.PingHandler"));
        assertThat(handlers.get(0).getNamespace(), is("ping-handler"));
        assertThat(handlers.get(0).getOptions(), is(Collections.emptyMap()));

        // brain
        PluginConfig brain = JabotConfig.getBrainConfig();
        assertThat(brain, is(nullValue()));
    }

    private void setConfigFile(CommandConfig commandConfig, String resourceConfigName) throws Exception {
        String resourcePath = "config/" + resourceConfigName;
        final URL resource = Thread.currentThread().getContextClassLoader().getResource(resourcePath);
        File configFile = new File(resource.toURI());
        Class<CommandConfig> clazz = CommandConfig.class;
        Field field = clazz.getDeclaredField("pluginConfig");
        field.setAccessible(true);
        field.set(commandConfig, configFile);
    }
}