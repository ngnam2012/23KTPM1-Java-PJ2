package com.common;

import java.io.Serializable;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;

    private String type;
    private Object data;
    private String senderId;

    public Message(String type, Object data, String senderId) {
        this.type = type;
        this.data = data;
        this.senderId = senderId;
    }

    public String getType() { return type; }
    public Object getData() { return data; }
    public String getSenderId() { return senderId; }
}
