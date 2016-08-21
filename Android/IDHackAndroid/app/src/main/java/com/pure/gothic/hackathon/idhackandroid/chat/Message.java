package com.pure.gothic.hackathon.idhackandroid.chat;

/**
 * Created by quki on 2016-08-21.
 */
public class Message {

    private String sender;
    private String receiver;
    private String text;

    /**
     * setter
     */
    public void setSender(String sender) {
        this.sender = sender;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public void setText(String text) {
        this.text = text;
    }

    /**
     * getter
     */
    public String getSender() {
        return sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public String getText() {
        return text;
    }

}
