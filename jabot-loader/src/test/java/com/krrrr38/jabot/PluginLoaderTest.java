package com.krrrr38.jabot;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.Collections;

import org.junit.Test;

import com.krrrr38.jabot.config.JabotConfig;
import com.krrrr38.jabot.config.PluginConfig;
import com.krrrr38.jabot.mock.MockAdapter;
import com.krrrr38.jabot.plugin.message.SendMessage;

public class PluginLoaderTest {

    @Test
    public void testLoad() throws Exception {
        // given
        JabotContext context = new JabotContext();
        JabotConfig config = new JabotConfig();

        PluginConfig adapterConfig = new PluginConfig();
        adapterConfig.setPlugin("com.krrrr38.jabot.mock.MockAdapter");
        adapterConfig.setNamespace("mock-adapter");

        PluginConfig handlerConfig = new PluginConfig();
        handlerConfig.setPlugin("com.krrrr38.jabot.plugin.handler.PingHandler");
        handlerConfig.setNamespace("ping-handler");

        config.setName("jabot");
        config.setAdapter(adapterConfig);
        config.setHandlers(Collections.singletonList(handlerConfig));
        config.setBrain(null); // using InMemoryBrain

        // when load successfully
        PluginLoader.load(config, context);
        context.send(new SendMessage("message", null));

        // then
        assertThat(MockAdapter.queue.peekLast().getMessage(), is("message"));
    }

    @Test
    public void testLoadFailed() throws Exception {
        // given not found class adapter
        JabotContext context = new JabotContext();
        JabotConfig config = new JabotConfig();

        PluginConfig adapterConfig = new PluginConfig();
        adapterConfig.setPlugin("com.krrrr38.jabot.mock.ClassNotFoundAdapter");
        adapterConfig.setNamespace("mock-adapter");

        PluginConfig handlerConfig = new PluginConfig();
        handlerConfig.setPlugin("com.krrrr38.jabot.plugin.handler.ClassNotFoundHandler");
        handlerConfig.setNamespace("ping-handler");

        config.setName("jabot");
        config.setAdapter(adapterConfig);
        config.setHandlers(Collections.singletonList(handlerConfig));
        config.setBrain(null); // using InMemoryBrain

        // when load not found class
        try {
            PluginLoader.load(config, context);
            fail();
        } catch (Exception e) {
            assertThat("ClassNotFound", e, instanceOf(IllegalArgumentException.class));
        }

        // when load handler into adapter
        adapterConfig.setPlugin("com.krrrr38.jabot.plugin.handler.PingHandler");
        try {
            PluginLoader.load(config, context);
            fail();
        } catch (Exception e) {
            assertThat(e, instanceOf(ClassCastException.class));
        }
    }
}
