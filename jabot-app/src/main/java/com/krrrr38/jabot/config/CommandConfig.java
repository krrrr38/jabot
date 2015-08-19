package com.krrrr38.jabot.config;

import org.kohsuke.args4j.Option;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class CommandConfig {
    @Option(name = "-c", usage = "path to plugins.yml")
    private File pluginConfig = new File("./plugins.yml");

    /**
     * read yaml to jabotConfig
     *
     * @return jabot config
     */
    public JabotConfig tojabotConfig() {
        try (InputStream input = new FileInputStream(pluginConfig)) {
            Yaml yaml = new Yaml(new Constructor(JabotConfig.class));
            return (JabotConfig) yaml.load(input);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
