package com.krrrr38.jabot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.krrrr38.jabot.config.JabotConfig;

public class Jabot {
    private static final Logger logger = LoggerFactory.getLogger(Jabot.class);

    private final JabotContext context;

    private Jabot(JabotContext context) {
        this.context = context;
    }

    public static Jabot init(JabotConfig jabotConfig) {
        logger.debug("Initialize application");
        JabotContext context = new JabotContext();
        PluginLoader.load(jabotConfig, context);
        return new Jabot(context);
    }

    public void start() {
        context.listenAdapter();
    }

    public void stop() {
        context.destroy();
    }
}