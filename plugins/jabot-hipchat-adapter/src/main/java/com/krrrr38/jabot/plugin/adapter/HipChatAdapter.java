package com.krrrr38.jabot.plugin.adapter;

import java.io.IOException;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.http.util.EntityUtils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.krrrr38.jabot.plugin.adapter.model.PostMessage;
import com.krrrr38.jabot.plugin.adapter.model.Webhook;
import com.krrrr38.jabot.plugin.adapter.model.Webhook.Item.Message.User;
import com.krrrr38.jabot.plugin.message.ReceiveMessage;
import com.krrrr38.jabot.plugin.message.SendMessage;
import com.krrrr38.jabot.plugin.message.Sender;

import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HipChatAdapter extends Adapter {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);

    private static final String DEFAULT_HOST = "localhost";
    private static final String DEFAULT_MESSAGE_FORMAT = "text";
    private static final int MAX_RETRY_POST_COUNT = 3;

    private static final String OPTIONS_POST_URL = "postUrl"; // required
    private static final String OPTIONS_MESSAGE_COLOR = "messageColor"; // optional (default "gray")
    private static final String DEFAULT_MESSAGE_COLOR = "gray";
    private static final String OPTIONS_MESSAGE_NOTIFY = "messageNotify"; // optional (default false)
    private static final boolean DEFAULT_MESSAGE_NOTIFY = false;
    private static final String OPTIONS_WEBHOOK_PORT = "webhookPort"; // optional (default 4000)
    private static final int DEFAULT_PORT = 4000;
    private static final String OPTIONS_SLASH_COMMAND = "slashCommand";
    // optional (default "/" + your bot name)
    private static final Pattern MESSAGE_COLOR_PATTERN =
            Pattern.compile("yellow|red|green|purple|gray|random");

    private Undertow server;
    private Queue<ReceiveMessage> queue = new ConcurrentLinkedQueue<>();
    private String postUrl;
    private String messageColor;
    private boolean messageNotify;

    @Override
    public void afterSetup(Map<String, String> options) {
        String botName = getBotName();
        postUrl = requireString(options, OPTIONS_POST_URL);
        messageColor = optionString(options, OPTIONS_MESSAGE_COLOR, DEFAULT_MESSAGE_COLOR,
                                    MESSAGE_COLOR_PATTERN);
        messageNotify = optionBoolean(options, OPTIONS_MESSAGE_NOTIFY, DEFAULT_MESSAGE_NOTIFY);
        String slashCommand = optionString(options, OPTIONS_SLASH_COMMAND, String.format("/%s", botName));
        int port = optionInteger(options, OPTIONS_WEBHOOK_PORT, DEFAULT_PORT);

        server = Undertow.builder()
                         .addHttpListener(port, DEFAULT_HOST)
                         .setHandler(new HipChatWebhookHandler(queue, slashCommand))
                         .build();
        server.start();
        log.info("HipChat Webhook Server Start: host={}, port={}", DEFAULT_HOST, port);
    }

    @Override
    public void beforeDestroy() {
        server.stop();
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
                              ? String.format("@%s ", sendMessage.getReplyId())
                              : "";
        PostMessage postMessage = new PostMessage(
                messageColor,
                replyMessage + sendMessage.getMessage(),
                messageNotify,
                DEFAULT_MESSAGE_FORMAT
        );
        post(postMessage, 1);
    }

    private void post(PostMessage postMessage, int retryCount) {
        if (retryCount > MAX_RETRY_POST_COUNT) {
            log.error("Failed to post message, aborted");
            return;
        }

        try {
            HttpResponse res = Request.Post(postUrl)
                                      .bodyByteArray(OBJECT_MAPPER.writeValueAsBytes(postMessage),
                                                     ContentType.APPLICATION_JSON)
                                      .execute()
                                      .returnResponse();
            if (res.getStatusLine().getStatusCode() > 299) {
                String responseBody = res.getEntity() != null ? EntityUtils.toString(res.getEntity()) : "";
                log.error("Failed to post message: statusLine={}, body={}", res.getStatusLine(), responseBody);
            }
        } catch (NoHttpResponseException e) {
            log.warn("Failed to post message, will retry: " + e.getMessage(), e);
            post(postMessage, retryCount + 1);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public void connectAction() {
        post(new SendMessage("Hello!!"));
    }

    static class HipChatWebhookHandler implements HttpHandler {
        private static final Executor EXECUTOR = new Executor() {
            private final ExecutorService executorService = Executors.newCachedThreadPool();

            @Override
            public void execute(Runnable command) {
                executorService.execute(command);
            }
        };

        private final Queue<ReceiveMessage> messageQueue;
        private final String slashCommand;

        public HipChatWebhookHandler(Queue<ReceiveMessage> messageQueue, String slashCommand) {
            this.messageQueue = messageQueue;
            this.slashCommand = slashCommand;
        }

        @Override
        public void handleRequest(HttpServerExchange httpServerExchange) throws Exception {
            httpServerExchange.startBlocking();
            if (httpServerExchange.isInIoThread()) {
                httpServerExchange.dispatch(EXECUTOR, (exchange) -> {
                    try {
                        Webhook webhook = OBJECT_MAPPER.readValue(exchange.getInputStream(), Webhook.class);
                        log.debug("receive webhook: {}", webhook);
                        Webhook.Item.Message hipchatMessage = webhook.getItem().getMessage();
                        User user = hipchatMessage.getFrom();
                        String message = normalize(hipchatMessage.getMessage());
                        Sender sender = new Sender(String.valueOf(user.getId()), user.getMentionName(),
                                                   user.getName(), null);
                        messageQueue.add(new ReceiveMessage(sender, message));
                    } catch (IOException e) {
                        log.error(e.getMessage(), e);
                    }
                });
            }
        }

        private String normalize(String message) {
            // message contains slashCommand(/jabot), so remove this
            return message.replaceFirst(slashCommand, "").trim();
        }
    }
}
