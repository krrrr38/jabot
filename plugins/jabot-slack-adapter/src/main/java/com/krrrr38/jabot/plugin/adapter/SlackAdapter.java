package com.krrrr38.jabot.plugin.adapter;

import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackPersona;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.SlackUser;
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SlackAdapter extends Adapter {
    private static final Logger logger = LoggerFactory.getLogger(SlackAdapter.class);

    private SlackSession slack;
    private SlackChannel slackChannel;
    private Queue<String> queue = new ConcurrentLinkedQueue<>();

    private final String OPTIONS_TOKEN = "token";
    private final String OPTIONS_CHANNEL = "channel";

    @Override
    protected void build(Map<String, String> options) {
        String botName = getBotName();
        String token = requireString(options, OPTIONS_TOKEN);
        String channelName = requireString(options, OPTIONS_CHANNEL);
        slack = SlackSessionFactory.createWebSocketSlackSession(token);

        // connecting
        try {
            logger.info("Connect to channel: {}", channelName);
            slack.connect();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // find channel
        slackChannel = slack.findChannelByName(channelName);

        // find botId
        String botId = slack.getBots().stream()
                .filter(bot -> botName.equals(bot.getUserName()))
                .findFirst()
                .map(SlackPersona::getId)
                .orElseThrow(() -> {
                    String message = String.format("Cannot find target bot: %s\nPlease set `same` name with plugins.yml name and slack Customize name.", botName);
                    return new RuntimeException(message);
                });
        logger.info("Found Slack bot: {}", botId);

        // register listener
        slack.addMessagePostedListener((slackMessagePosted, slackSession) -> {
            logger.debug("Received Slack Message: {}", slackMessagePosted);
            String message = slackMessagePosted.getMessageContent().trim();
            if (channelName.equals(slackMessagePosted.getChannel().getName())
                    && isBotMention(message, botName, botId)
                    && !isSelfMessage(slackMessagePosted.getSender(), botId)) {
                queue.add(omitBotInfo(message, botName, botId));
            }
        });
    }

    private String omitBotInfo(String message, String botName, String botId) {
        message = message.replaceFirst(botName, "").replaceFirst(slackIdFormat(botId), "");
        // Slack app add ":" like `@jabot:`
        if (message.startsWith(":")) {
            message = message.substring(1, message.length());
        }
        return message.trim();
    }

    private boolean isBotMention(String message, String botName, String botId) {
        return message.startsWith(botName) || message.startsWith(slackIdFormat(botId));
    }

    private boolean isSelfMessage(SlackUser sender, String botId) {
        return botId.equals(sender.getId());
    }

    private String slackIdFormat(String id) {
        return String.format("<@%s>", id);
    }

    @Override
    public String receive() {
        while (true) {
            synchronized (queue) {
                if (!queue.isEmpty()) {
                    return queue.poll();
                }
            }
        }
    }

    @Override
    public void post(String message) {
        slack.sendMessage(slackChannel, message, null);
    }

    @Override
    public void connectAction() {
        post("Hello!!");
    }
}
