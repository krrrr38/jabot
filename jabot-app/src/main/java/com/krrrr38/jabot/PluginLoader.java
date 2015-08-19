package com.krrrr38.jabot;

import com.krrrr38.jabot.config.JabotConfig;
import com.krrrr38.jabot.config.PluginConfig;
import com.krrrr38.jabot.plugin.Plugin;
import com.krrrr38.jabot.plugin.adapter.Adapter;
import com.krrrr38.jabot.plugin.brain.Brain;
import com.krrrr38.jabot.plugin.brain.InmemoryBrain;
import com.krrrr38.jabot.plugin.brain.JabotBrainException;
import com.krrrr38.jabot.plugin.handler.Handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class PluginLoader {
    private static final Logger logger = LoggerFactory.getLogger(PluginLoader.class);

    /**
     * load plugins into context based on config
     *
     * @param jabotConfig jabot config
     * @param context     jabot context which would be
     */
    public static void load(JabotConfig jabotConfig, JabotContext context) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Adapter adapter = loadAdapter(classLoader, jabotConfig.getAdapterConfig(), jabotConfig.getName(), context::receive);
        Brain brain = loadBrain(classLoader, jabotConfig.getBrainConfig(), jabotConfig.getName());
        List<Handler> handlers = loadHandlers(classLoader, jabotConfig.getHandlers(), brain, context::send);

        // after registring hook
        handlers.forEach(handler -> handler.afterRegister(handlers));

        context.setAdapter(adapter);
        context.setHandlers(handlers);
    }

    private static Adapter loadAdapter(
            ClassLoader classLoader, PluginConfig config, String botName, Consumer<String> receiver
    ) {
        logger.info("Load adapter plugin: {}", config.getPlugin());
        return with(() -> {
            Adapter adapter = (Adapter) classLoader.loadClass(config.getPlugin()).newInstance();
            adapter.setup(botName, receiver, config.getOptions());
            return adapter;
        }, e -> {
            logger.error(String.format("Failed to load adapter plugin [%s]", config.getPlugin()), e);
            throw e; // no fallback
        });
    }

    // fallback with inmemory brain
    private static Brain loadBrain(ClassLoader classLoader, PluginConfig config, String botName) {
        if (config == null) {
            logger.warn("No brain definition. Fallback to inmemory brain.");
            return getDefaultInmemoryBrain();
        }

        logger.info("Load brain plugin: {}", config.getPlugin());
        Brain brain = with(() -> {
            return (Brain) classLoader.loadClass(config.getPlugin()).newInstance();
        }, e -> {
            logger.warn("Failed to load brain plugin. Fallback to inmemory brain.", e);
            return getDefaultInmemoryBrain();
        });
        try {
            brain.setup(botName, config.getOptions());
        } catch (JabotBrainException e) {
            throw new RuntimeException(e);
        }
        return brain;
    }

    private static List<Handler> loadHandlers(ClassLoader classLoader, List<PluginConfig> configs, Brain brain, Consumer<String> sender) {
        return configs.stream().map(config -> with(() -> {
            logger.info("Load handler plugin: {}", config.getPlugin());
            Handler handler = (Handler) classLoader.loadClass(config.getPlugin()).newInstance();
            handler.setup(config.getNamespace(), brain, sender, config.getOptions());
            return handler;
        }, e -> {
            logger.error(String.format("Failed to load handler plugin [%s]", config.getPlugin()), e);
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
