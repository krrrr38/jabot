package com.krrrr38.jabot.plugin.adapter.model;

import java.util.List;

import lombok.Data;

/**
 * https://www.hipchat.com/docs/apiv2/webhooks#room_message
 */
@Data
public class Webhook {
    private String event;
    private Item item;
    private String oauthClientId;
    private String webhookId;

    @Data
    public static class Item {
        private Message message;
        private Room room;

        @Data
        public static class Message {
            private String id;
            private User from;
            private List<User> mentions;
            private String message;

            @Data
            public static class User {
                private int id;
                private String mentionName;
                private String name;
            }
        }

        @Data
        public static class Room {
            private int id;
            private String name;
        }
    }
}

