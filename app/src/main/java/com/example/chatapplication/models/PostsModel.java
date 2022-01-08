package com.example.chatapplication.models;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class PostsModel {

    public String post_id;
    public String description;
    public String photo;
    public String user_id;
    public String user_name;
    public String user_avatar;
    @ServerTimestamp
    public Date created_at;

    public PostsModel() {
    }

    public PostsModel(String description) {
        this.description = description;
    }
}
