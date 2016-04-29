package com.krrrr38.jabot;

import com.krrrr38.jabot.config.JabotConfig;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Jabot {
    private final JabotContext context;

    private Jabot(JabotContext context) {
        this.context = context;
    }

    public static Jabot init(JabotConfig jabotConfig) {
        log.debug("Initialize application");
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
