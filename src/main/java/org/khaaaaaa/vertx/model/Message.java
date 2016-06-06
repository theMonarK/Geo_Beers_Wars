package org.khaaaaaa.vertx.model;

/**
 * Created by anthony on 06/06/2016.
 */
public class Message {
    private String from;
    private String to;
    private String message;

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public String getMessage() {
        return message;
    }

    public Message(String from, String to, String message) {

        this.from = from;
        this.to = to;
        this.message = message;
    }
}
