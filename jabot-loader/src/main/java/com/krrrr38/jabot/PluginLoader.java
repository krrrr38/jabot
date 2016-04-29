package com.krrrr38.jabot;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.krrrr38.jabot.config.JabotConfig;
import com.krrrr38.jabot.config.PluginConfig;
import com.krrrr38.jabot.plugin.Plugin;
import com.krrrr38.jabot.plugin.adapter.Adapter;
import com.krrrr38.jabot.plugin.brain.Brain;
import com.krrrr38.jabot.plugin.brain.InmemoryBrain;
import com.krrrr38.jabot.plugin.brain.JabotBrainException;
import com.krrrr38.jabot.plugin.handler.Handler;
import com.krrrr38.jabot.plugin.message.ReceiveMessage;
import com.krrrr38.jabot.plugin.message.SendMessage;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PluginLoader {

    /**
     * load plugins into context based on config
     *
     * @param jabotConfig jabot config
     * @param context     jabot context which would be
     */
    public static void load(JabotConfig jabotConfig, JabotContext context) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Adapter adapter = loadAdapter(classLoader, jabotConfig.getAdapter(), jabotConfig.getName(), context::receive);
        Brain brain = loadBrain(classLoader, jabotConfig.getBrain(), jabotConfig.getName());
        List<Handler> handlers = loadHandlers(classLoader, jabotConfig.getHandlers(), brain, context::send);

        // after registring hook
        handlers.forEach(handler -> handler.afterRegister(handlers));

        context.setBrain(brain);
        context.setAdapter(adapter);
        context.setHandlers(handlers);
    }

    private static Adapter loadAdapter(
            ClassLoader classLoader, PluginConfig config, String botName, Consumer<ReceiveMessage> receiver
    ) {
        log.info("Load adapter plugin: {}", config.getPlugin());
        return with(() -> {
            Adapter adapter = (Adapter) classLoader.loadClass(config.getPlugin()).newInstance();
            adapter.setup(config.getNamespace(), botName, receiver, config.getOptions());
            return adapter;
        }, e -> {
            log.error(String.format("Failed to load adapter plugin [%s]", config.getPlugin()), e);
            throw e; // no fallback
        });
    }

    // fallback with inmemory brain
    private static Brain loadBrain(ClassLoader classLoader, PluginConfig config, String botName) {
        if (config == null) {
            log.warn("No brain definition. Fallback to inmemory brain.");
            return getDefaultInmemoryBrain();
        }

        log.info("Load brain plugin: {}", config.getPlugin());
        Brain brain = with(() -> {
            return (Brain) classLoader.loadClass(config.getPlugin()).newInstance();
        }, e -> {
            log.warn("Failed to load brain plugin. Fallback to inmemory brain.", e);
            return getDefaultInmemoryBrain();
        });
        try {
            brain.setup(config.getNamespace(), botName, config.getOptions());
        } catch (JabotBrainException e) {
            throw new RuntimeException(e);
        }
        return brain;
    }

    private static List<Handler> loadHandlers(ClassLoader classLoader, List<PluginConfig> configs, Brain brain, Consumer<SendMessage> sender) {
        return configs.stream().map(config -> with(() -> {
            log.info("Load handler plugin: {}", config.getPlugin());
            Handler handler = (Handler) classLoader.loadClass(config.getPlugin()).newInstance();
            handler.setup(config.getNamespace(), brain, sender, config.getOptions());
            return handler;
        }, e -> {
            log.error(String.format("Failed to load handler plugin [%s]", config.getPlugin()), e);
            throw e; // no fallback
        })).collect(Collectors.toList());
    }

    private static Brain getDefaultInmemoryBrain() {
        return new InmemoryBrain();
    }

    private static <A extends Plugin> A with(PluginSupplier<A> loader, PluginFallback<A> fallback) {
        try {
            return loader.get();
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            try {
                return fallback.apply(e);
            } catch (Exception fe) {
                throw new IllegalArgumentException(fe);
            }
        }
    }

    @FunctionalInterface
    interface PluginSupplier<A extends Plugin> {
        A get() throws ClassNotFoundException, IllegalAccessException, InstantiationException;
    }

    @FunctionalInterface
    interface PluginFallback<A extends Plugin> {
        A apply(Exception e) throws Exception;
    }
}
