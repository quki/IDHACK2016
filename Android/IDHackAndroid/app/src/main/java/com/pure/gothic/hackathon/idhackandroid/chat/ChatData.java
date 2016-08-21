package com.pure.gothic.hackathon.idhackandroid.chat;

/**
 * Created by quki on 2016-08-21.
 */
public class ChatData {

    private String sender;
    private String receiver;
    private String text;
    private int status; // status == 0 ? left : right

    public ChatData(){

    }
    public ChatData(String sender, String receiver, String text, int status){
        this.sender = sender;
        this.receiver = receiver;
        this.text = text;
        this.status = status;
    }

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
    public void setStatus(int status) {
        this.status = status;
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

    public int getStatus(){
        return status;
    }

}
