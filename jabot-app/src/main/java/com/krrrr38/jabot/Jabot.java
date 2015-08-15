package com.krrrr38.jabot;

import com.krrrr38.jabot.config.JabotConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Jabot {
    private static final Logger logger = LoggerFactory.getLogger(Jabot.class);

    private final JabotContext context;

    private Jabot(JabotContext context) {
        this.context = context;
    }

    public static Jabot init(JabotConfig JabotConfig) {
        logger.debug("Initialize application");
        JabotContext context = new JabotContext();
        PluginLoader.load(JabotConfig, context);
        return new Jabot(context);
    }

    public void start() {
        logger.info("Start application");
        context.listenAdapter();
    }
}
