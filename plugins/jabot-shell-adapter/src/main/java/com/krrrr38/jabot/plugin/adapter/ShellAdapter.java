package com.krrrr38.jabot.plugin.adapter;

import java.util.Map;
import java.util.Scanner;

public class ShellAdapter extends Adapter {
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_CYAN = "\u001B[36m";

    private static final String OPTIONS_PROMPT = "prompt";
    private static final String DEFAULT_PROMPT = ANSI_YELLOW + "> " + ANSI_RESET;
    private static final String EXIT_COMMAND = "exit";

    private String prompt;
    private Scanner scanner;

    @Override
    protected void build(Map<String, String> options) {
        prompt = optionString(options, OPTIONS_PROMPT, DEFAULT_PROMPT);
        scanner = new Scanner(System.in);
    }

    @Override
    public String receive() {
        post(prompt, false);
        String line = scanner.nextLine().trim();
        if (EXIT_COMMAND.equals(line)) {
            stop();
        }
        return line;
    }

    @Override
    public void post(String message) {
        post(String.format("%s[%s]\n%s%s", ANSI_CYAN, getBotName(), message, ANSI_RESET), true);
    }

    @Override
    public void connectAction() {
        post("Welcome!! (type `exit` to interrupt)");
    }

    private void post(String message, boolean withBreakLine) {
        if (withBreakLine) {
            System.out.println(message);
        } else {
            System.out.print(message);
        }
    }
}
