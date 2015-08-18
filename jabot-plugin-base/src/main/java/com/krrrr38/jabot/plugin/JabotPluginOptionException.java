package com.krrrr38.jabot.plugin;

public class JabotPluginOptionException extends RuntimeException {
    public JabotPluginOptionException() {
    }

    public JabotPluginOptionException(String message) {
        super(message);
    }

    public JabotPluginOptionException(String message, Throwable cause) {
        super(message, cause);
    }

    public JabotPluginOptionException(Throwable cause) {
        super(cause);
    }

    public JabotPluginOptionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
