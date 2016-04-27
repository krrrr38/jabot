package com.krrrr38.jabot.plugin.adapter;

import java.io.IOException;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

import org.apache.http.HttpHeaders;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.http.message.BasicHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.krrrr38.jabot.plugin.adapter.model.PostMessage;
import com.krrrr38.jabot.plugin.adapter.model.Webhook;

import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

public class HipChatAdapter extends Adapter {
    private static final Logger logger = LoggerFactory.getLogger(HipChatAdapter.class);

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);

    private static final String DEFAULT_HOST = "localhost";
    private static final String DEFAULT_MESSAGE_FORMAT = "text";

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

    private Queue<String> queue = new ConcurrentLinkedQueue<>();
    private String postUrl;
    private String messageColor;
    private boolean messageNotify;
    private String slashCommand;

    @Override
    protected void build(Map<String, String> options) {
        String botName = getBotName();
        postUrl = requireString(options, OPTIONS_POST_URL);
        messageColor = optionString(options, OPTIONS_MESSAGE_COLOR, DEFAULT_MESSAGE_COLOR,
                                    MESSAGE_COLOR_PATTERN);
        messageNotify = optionBoolean(options, OPTIONS_MESSAGE_NOTIFY, DEFAULT_MESSAGE_NOTIFY);
        slashCommand = optionString(options, OPTIONS_SLASH_COMMAND, String.format("/%s", botName));
        int port = optionInteger(options, OPTIONS_WEBHOOK_PORT, DEFAULT_PORT);

        Undertow server = Undertow.builder()
                                  .addHttpListener(port, DEFAULT_HOST)
                                  .setHandler(new HipChatWebhookHandler(queue))
                                  .build();
        server.start();
        logger.info("HipChat Webhook Server Start: host={}, port={}", DEFAULT_HOST, port);
    }

    @Override
    public String receive() {
        while (true) {
            synchronized (queue) {
                if (!queue.isEmpty()) {
                    return normalize(queue.poll());
                }
            }
        }
    }

    private String normalize(String message) {
        // message contains slashCommand(/jabot), so remove this
        return message.replaceFirst(slashCommand, "").trim();
    }

    @Override
    public void post(String message) {
        PostMessage postMessage = new PostMessage(messageColor, message, messageNotify, DEFAULT_MESSAGE_FORMAT);
        try {
            Request.Post(postUrl)
                   .addHeader(new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/json"))
                   .bodyByteArray(OBJECT_MAPPER.writeValueAsBytes(postMessage), ContentType.APPLICATION_JSON)
                   .execute()
                   .discardContent();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void connectAction() {
        post("Hello!!");
    }

    static class HipChatWebhookHandler implements HttpHandler {
        private static final Executor EXECUTOR = new Executor() {
            private final ExecutorService executorService = Executors.newCachedThreadPool();
            @Override
            public void execute(Runnable command) {
                executorService.execute(command);
            }
        };

        private final Queue<String> messageQueue;

        public HipChatWebhookHandler(Queue<String> messageQueue) {
            this.messageQueue = messageQueue;
        }

        @Override
        public void handleRequest(HttpServerExchange httpServerExchange) throws Exception {
            httpServerExchange.startBlocking();
            if (httpServerExchange.isInIoThread()) {
                httpServerExchange.dispatch(EXECUTOR, (exchange) -> {
                    try {
                        Webhook webhook = OBJECT_MAPPER.readValue(exchange.getInputStream(), Webhook.class);
                        logger.debug("receive webhook: {}", webhook);
                        messageQueue.add(webhook.getItem().getMessage().getMessage());
                    } catch (IOException e) {
                        logger.error(e.getMessage(), e);
                    }
                });
            }
        }
    }
}
