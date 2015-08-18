package com.krrrr38.jabot.plugin.brain;

public class JabotBrainException extends Exception {
    public JabotBrainException() {
    }

    public JabotBrainException(String message) {
        super(message);
    }

    public JabotBrainException(String message, Throwable cause) {
        super(message, cause);
    }

    public JabotBrainException(Throwable cause) {
        super(cause);
    }

    public JabotBrainException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
