package com.krrrr38.jabot.plugin.adapter;

import java.io.IOException;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.krrrr38.jabot.plugin.message.ReceiveMessage;
import com.krrrr38.jabot.plugin.message.SendMessage;
import com.krrrr38.jabot.plugin.message.Sender;
import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackPersona;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.SlackUser;
import com.ullink.slack.simpleslackapi.impl.SlackChatConfiguration;
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SlackAdapter extends Adapter {
    private static final String OPTIONS_TOKEN = "token";
    private static final String OPTIONS_CHANNEL = "channel";

    private SlackSession slack;
    private SlackChannel slackChannel;
    private SlackChatConfiguration slackChatConfiguration;
    private Queue<ReceiveMessage> queue = new ConcurrentLinkedQueue<>();

    @Override
    public void afterSetup(Map<String, String> options) {
        String botName = getBotName();
        String token = requireString(options, OPTIONS_TOKEN);
        String channelName = requireString(options, OPTIONS_CHANNEL);
        slack = SlackSessionFactory.createWebSocketSlackSession(token);

        // connecting
        try {
            log.info("Connect to channel: {}", channelName);
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
                                String message = String.format(
                                        "Cannot find target bot: %s\nPlease set `same` name with plugins.yml "
                                        + "name and slack Customize name.",
                                        botName);
                                return new RuntimeException(message);
                            });
        log.info("Found Slack bot: {}", botId);

        // register listener
        slack.addMessagePostedListener((slackMessagePosted, slackSession) -> {
            log.debug("Received Slack Message: {}", slackMessagePosted);
            String receivedMessage = slackMessagePosted.getMessageContent().trim();
            if (channelName.equals(slackMessagePosted.getChannel().getName())
                && isBotMention(receivedMessage, botName, botId)
                && !isSelfMessage(slackMessagePosted.getSender(), botId)) {
                final SlackUser sender = slackMessagePosted.getSender();
                queue.add(new ReceiveMessage(
                        new Sender(sender.getId(), sender.getUserName(), sender.getUserName(), sender.getUserMail()),
                        omitBotInfo(receivedMessage, botName, botId)));
            }
        });
    }

    @Override
    public void beforeDestroy() {
        // slack.disconnect();
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
    public ReceiveMessage receive() {
        while (true) {
            synchronized (queue) {
                if (!queue.isEmpty()) {
                    return queue.poll();
                }
            }
        }
    }

    @Override
    public void post(SendMessage sendMessage) {
        String replyMessage = sendMessage.getReplyId() != null
                              ? String.format("<@%s> ", sendMessage.getReplyId())
                              : "";
        String message = replyMessage + sendMessage.getMessage();
        slack.sendMessage(slackChannel, message, null);
    }

    @Override
    public void connectAction() {
        post(new SendMessage("Hello!!"));
    }
}
