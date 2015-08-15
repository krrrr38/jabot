package com.krrrr38.jabot;

import com.krrrr38.jabot.config.CommandConfig;
import com.krrrr38.jabot.config.JabotConfig;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

public class JabotRunner {

    public static void main(String[] args) {
        CommandConfig commandConfig = new CommandConfig();
        CmdLineParser cmdLineParser = new CmdLineParser(commandConfig);
        try {
            cmdLineParser.parseArgument(args);
        } catch (CmdLineException e) {
            cmdLineParser.printUsage(System.err);
            return;
        }

        JabotConfig JabotConfig = commandConfig.tojabotConfig();
        Jabot.init(JabotConfig).start();
    }

}
