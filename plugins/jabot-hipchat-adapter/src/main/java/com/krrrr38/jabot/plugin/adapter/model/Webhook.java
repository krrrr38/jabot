package com.krrrr38.jabot.plugin.adapter.model;

import java.util.List;

/**
 * https://www.hipchat.com/docs/apiv2/webhooks#room_message
 */
public class Webhook {
    private String event;
    private Item item;
    private String oauthClientId;
    private String webhookId;

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public String getOauthClientId() {
        return oauthClientId;
    }

    public void setOauthClientId(String oauthClientId) {
        this.oauthClientId = oauthClientId;
    }

    public String getWebhookId() {
        return webhookId;
    }

    public void setWebhookId(String webhookId) {
        this.webhookId = webhookId;
    }

    @Override
    public String toString() {
        return "Webhook{" +
               "event='" + event + '\'' +
               ", item=" + item +
               ", oauthClientId='" + oauthClientId + '\'' +
               ", webhookId='" + webhookId + '\'' +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (!(o instanceof Webhook)) { return false; }

        Webhook webhook = (Webhook) o;

        if (event != null ? !event.equals(webhook.event) : webhook.event != null) { return false; }
        if (item != null ? !item.equals(webhook.item) : webhook.item != null) { return false; }
        if (oauthClientId != null ? !oauthClientId.equals(webhook.oauthClientId) :
            webhook.oauthClientId != null) {
            return false;
        }
        return webhookId != null ? webhookId.equals(webhook.webhookId) : webhook.webhookId == null;

    }

    @Override
    public int hashCode() {
        int result = event != null ? event.hashCode() : 0;
        result = 31 * result + (item != null ? item.hashCode() : 0);
        result = 31 * result + (oauthClientId != null ? oauthClientId.hashCode() : 0);
        result = 31 * result + (webhookId != null ? webhookId.hashCode() : 0);
        return result;
    }

    public static class Item {
        private Message message;
        private Room room;

        public Message getMessage() {
            return message;
        }

        public void setMessage(Message message) {
            this.message = message;
        }

        public Room getRoom() {
            return room;
        }

        public void setRoom(Room room) {
            this.room = room;
        }

        @Override
        public String toString() {
            return "Item{" +
                   "message=" + message +
                   ", room=" + room +
                   '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) { return true; }
            if (!(o instanceof Item)) { return false; }

            Item item = (Item) o;

            if (message != null ? !message.equals(item.message) : item.message != null) { return false; }
            return room != null ? room.equals(item.room) : item.room == null;

        }

        @Override
        public int hashCode() {
            int result = message != null ? message.hashCode() : 0;
            result = 31 * result + (room != null ? room.hashCode() : 0);
            return result;
        }

        public static class Message {
            private String id;
            private User from;
            private List<User> mentions;
            private String message;

            public String getId() {
                return id;
            }

            public void setId(String id) {
                this.id = id;
            }

            public User getFrom() {
                return from;
            }

            public void setFrom(User from) {
                this.from = from;
            }

            public List<User> getMentions() {
                return mentions;
            }

            public void setMentions(
                    List<User> mentions) {
                this.mentions = mentions;
            }

            public String getMessage() {
                return message;
            }

            public void setMessage(String message) {
                this.message = message;
            }

            @Override
            public String toString() {
                return "Message{" +
                       "id='" + id + '\'' +
                       ", from=" + from +
                       ", mentions=" + mentions +
                       ", message='" + message + '\'' +
                       '}';
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) { return true; }
                if (!(o instanceof Message)) { return false; }

                Message message1 = (Message) o;

                if (id != null ? !id.equals(message1.id) : message1.id != null) { return false; }
                if (from != null ? !from.equals(message1.from) : message1.from != null) { return false; }
                if (mentions != null ? !mentions.equals(message1.mentions) : message1.mentions != null) {
                    return false;
                }
                return message != null ? message.equals(message1.message) : message1.message == null;

            }

            @Override
            public int hashCode() {
                int result = id != null ? id.hashCode() : 0;
                result = 31 * result + (from != null ? from.hashCode() : 0);
                result = 31 * result + (mentions != null ? mentions.hashCode() : 0);
                result = 31 * result + (message != null ? message.hashCode() : 0);
                return result;
            }

            public static class User {
                private int id;
                private String mentionName;
                private String name;

                public int getId() {
                    return id;
                }

                public void setId(int id) {
                    this.id = id;
                }

                public String getMentionName() {
                    return mentionName;
                }

                public void setMentionName(String mentionName) {
                    this.mentionName = mentionName;
                }

                public String getName() {
                    return name;
                }

                public void setName(String name) {
                    this.name = name;
                }

                @Override
                public String toString() {
                    return "User{" +
                           "id=" + id +
                           ", mentionName='" + mentionName + '\'' +
                           ", name='" + name + '\'' +
                           '}';
                }

                @Override
                public boolean equals(Object o) {
                    if (this == o) { return true; }
                    if (!(o instanceof User)) { return false; }

                    User user = (User) o;

                    if (id != user.id) { return false; }
                    if (mentionName != null ? !mentionName.equals(user.mentionName) :
                        user.mentionName != null) {
                        return false;
                    }
                    return name != null ? name.equals(user.name) : user.name == null;

                }

                @Override
                public int hashCode() {
                    int result = id;
                    result = 31 * result + (mentionName != null ? mentionName.hashCode() : 0);
                    result = 31 * result + (name != null ? name.hashCode() : 0);
                    return result;
                }
            }
        }

        public static class Room {
            private int id;
            private String name;

            public int getId() {
                return id;
            }

            public void setId(int id) {
                this.id = id;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            @Override
            public String toString() {
                return "Room{" +
                       "id=" + id +
                       ", name='" + name + '\'' +
                       '}';
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) { return true; }
                if (!(o instanceof Room)) { return false; }

                Room room = (Room) o;

                if (id != room.id) { return false; }
                return name != null ? name.equals(room.name) : room.name == null;

            }

            @Override
            public int hashCode() {
                int result = id;
                result = 31 * result + (name != null ? name.hashCode() : 0);
                return result;
            }
        }

    }
}

