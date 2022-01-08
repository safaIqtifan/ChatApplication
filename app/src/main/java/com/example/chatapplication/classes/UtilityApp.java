package com.example.chatapplication.classes;

import com.example.chatapplication.models.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;

public class UtilityApp {

    public static void setUserData(UserModel user) {
        String userData = new Gson().toJson(user);
        RootApplication.getInstance().getSharedPManger().SetData(Constants.KEY_MEMBER, userData);
    }

    public static UserModel getUserData() {
        String userJsonData = RootApplication.getInstance().getSharedPManger().getDataString(Constants.KEY_MEMBER);
        return new Gson().fromJson(userJsonData, UserModel.class);
    }

    public static boolean isLogin() {
        return FirebaseAuth.getInstance().getCurrentUser() != null;
    }

    public static void logout() {
        FirebaseAuth.getInstance().signOut();
        RootApplication.getInstance().getSharedPManger().SetData(Constants.KEY_MEMBER, null);
    }

}
