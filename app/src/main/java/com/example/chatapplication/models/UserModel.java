package com.example.chatapplication.models;

import java.io.Serializable;

public class UserModel implements Serializable {

    public String user_id;
    public String username;
    public String message_body;
    public String date;
    public String imageURL;

    public UserModel() {
    }

    public UserModel(String name, String message_body, String date) {
        this.username = name;
        this.message_body = message_body;
        this.date = date;
    }
}