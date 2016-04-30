package com.krrrr38.jabot.plugin.adapter;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.krrrr38.jabot.plugin.message.ReceiveMessage;
import com.krrrr38.jabot.plugin.message.SendMessage;
import com.krrrr38.jabot.plugin.message.Sender;

import com.linecorp.bot.client.LineBotAPIHeaders;
import com.linecorp.bot.client.LineBotClient;
import com.linecorp.bot.client.LineBotClientBuilder;
import com.linecorp.bot.client.exception.LineBotAPIException;
import com.linecorp.bot.model.callback.CallbackRequest;
import com.linecorp.bot.model.content.Content;
import com.linecorp.bot.model.content.TextContent;

import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderValues;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LineBotApiAdapter extends Adapter {
    private static final String DEFAULT_HOST = "localhost";
    private static final String OPTIONS_CHANNEL_ID = "channelId"; // required
    private static final String OPTIONS_CHANNEL_SECRET = "channelSecret"; // required
    private static final String OPTIONS_BOT_MID = "botMid"; // required
    private static final String OPTIONS_WEBHOOK_PORT = "webhookPort"; // optional (default 4000)
    private static final int DEFAULT_PORT = 4000;

    private Undertow server;
    private Queue<ReceiveMessage> queue = new ConcurrentLinkedQueue<>();
    private LineBotClient client;

    @Override
    public void afterSetup(Map<String, String> options) {
        String channelId = requireString(options, OPTIONS_CHANNEL_ID);
        String channelSecret = requireString(options, OPTIONS_CHANNEL_SECRET);
        String botMid = requireString(options, OPTIONS_BOT_MID);
        int port = optionInteger(options, OPTIONS_WEBHOOK_PORT, DEFAULT_PORT);

        client = LineBotClientBuilder.create(channelId, channelSecret, botMid)
                                     .build();
        server = Undertow.builder()
                         .addHttpListener(port, DEFAULT_HOST)
                         .setHandler(new LineBotWebhookHandler(queue, client))
                         .build();
        server.start();
        log.info("LineBot Webhook Server Start: host={}, port={}", DEFAULT_HOST, port);
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
        try {
            client.sendText(sendMessage.getReplyId(), sendMessage.getMessage());
        } catch (LineBotAPIException e) {
            log.error("Failed to send message: " + e.getMessage(), e);
        }
    }

    @Override
    public void connectAction() {
        post(new SendMessage("Hello!!"));
    }

    static class LineBotWebhookHandler implements HttpHandler {
        private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        private static final Executor EXECUTOR = new Executor() {
            private final ExecutorService executorService = Executors.newCachedThreadPool();

            @Override
            public void execute(Runnable command) {
                executorService.execute(command);
            }
        };

        private final Queue<ReceiveMessage> messageQueue;
        private final LineBotClient client;

        public LineBotWebhookHandler(Queue<ReceiveMessage> messageQueue, LineBotClient client) {
            this.messageQueue = messageQueue;
            this.client = client;
        }

        @Override
        public void handleRequest(HttpServerExchange httpServerExchange) throws Exception {
            httpServerExchange.startBlocking();
            if (httpServerExchange.isInIoThread()) {
                httpServerExchange.dispatch(EXECUTOR, (exchange) -> {
                    // same with line-bot-servlet process
                    final HeaderValues signatureHeaderValues = exchange.getRequestHeaders().get(
                            LineBotAPIHeaders.X_LINE_CHANNEL_SIGNATURE);
                    if (signatureHeaderValues == null) {
                        log.info("Receive no signature request");
                        return;
                    }
                    String signatureHeader = signatureHeaderValues.getFirst();

                    String json = IOUtils.toString(exchange.getInputStream(), StandardCharsets.UTF_8);
                    try {
                        if (!client.validateSignature(json, signatureHeader)) {
                            log.info("Receive invalid signature request: " + signatureHeader);
                            return;
                        }
                    } catch (LineBotAPIException e) {
                        log.info(
                                "Receive invalid signature request: " + signatureHeader + ": " + e.getMessage(),
                                e);
                        return;
                    }

                    CallbackRequest callbackRequest = OBJECT_MAPPER.readValue(json, CallbackRequest.class);
                    callbackRequest.getResult().forEach(message -> {
                        Content content = message.getContent();
                        if (content instanceof TextContent) {
                            TextContent textContent = (TextContent) content;
                            Sender sender = new Sender(textContent.getFrom(), null, null, null);
                            messageQueue.add(new ReceiveMessage(sender, textContent.getText()));
                        }
                    });
                });
            }
        }
    }
}
