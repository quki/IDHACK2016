package com.pure.gothic.hackathon.idhackandroid;

public class ChatMessage {
    public boolean left;
    public String phoneNumber;
    public String message;
   // public String date;

    public ChatMessage(String left, String phoneNumber,String message) {
        super();
        this.left = Boolean.parseBoolean(left);
        this.phoneNumber=phoneNumber;
        this.message = message;
    }
}