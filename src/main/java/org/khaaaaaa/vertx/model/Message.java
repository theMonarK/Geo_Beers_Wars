package org.khaaaaaa.vertx.model;

/**
 * Created by anthony on 06/06/2016.
 */
public class Message {
    private int id;

    public Message(int id, String from, String to, String message) {
        this.id = id;
        this.from = from;
        this.to = to;
        this.message = message;
    }

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

    public int getId() {
        return id;
    }
}
