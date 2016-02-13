package com.pure.gothic.hackathon.idhackandroid;

import android.util.Log;

public class ChatMessage {
    public int left;
    public String phoneNumber;
    public String message;
   // public String date;

    public ChatMessage(String left, String phoneNumber,String message) {
        super();
        this.left = Integer.parseInt(left);
        Log.e("lslslslsl",this.left+"");
        this.phoneNumber=phoneNumber;
        this.message = message;
    }
}