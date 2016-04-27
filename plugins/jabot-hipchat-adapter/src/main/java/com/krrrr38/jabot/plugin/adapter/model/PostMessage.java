package com.krrrr38.jabot.plugin.adapter.model;

public class PostMessage {
    private String color;
    private String message;
    private boolean notify;
    private String messageFormat;

    public PostMessage(String color, String message, boolean notify, String messageFormat) {
        this.color = color;
        this.message = message;
        this.notify = notify;
        this.messageFormat = messageFormat;
    }

    public String getColor() {
        return color;
    }

    public String getMessage() {
        return message;
    }

    public boolean isNotify() {
        return notify;
    }

    public String getMessageFormat() {
        return messageFormat;
    }

    @Override
    public String toString() {
        return "PostMessage{" +
               "color='" + color + '\'' +
               ", message='" + message + '\'' +
               ", notify=" + notify +
               ", messageFormat='" + messageFormat + '\'' +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (!(o instanceof PostMessage)) { return false; }

        PostMessage that = (PostMessage) o;

        if (notify != that.notify) { return false; }
        if (color != null ? !color.equals(that.color) : that.color != null) { return false; }
        if (message != null ? !message.equals(that.message) : that.message != null) { return false; }
        return messageFormat != null ? messageFormat.equals(that.messageFormat) : that.messageFormat == null;

    }

    @Override
    public int hashCode() {
        int result = color != null ? color.hashCode() : 0;
        result = 31 * result + (message != null ? message.hashCode() : 0);
        result = 31 * result + (notify ? 1 : 0);
        result = 31 * result + (messageFormat != null ? messageFormat.hashCode() : 0);
        return result;
    }
}
