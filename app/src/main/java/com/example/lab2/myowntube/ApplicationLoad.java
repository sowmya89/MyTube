package com.example.lab2.myowntube;

/**
 * Created by ramyap on 10/16/15.
 */

import android.app.Application;
import android.content.Context;

public class ApplicationLoad extends Application{

    private static Context sContext = null;
    private static String accessToken = null;
    private static String userEmail = null;

    public static String getUserEmail() {
        return userEmail;
    }

    public static void setUserEmail(String userEmail) {
        ApplicationLoad.userEmail = userEmail;
    }

    public static Context getAppContext() {
        return sContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = this;
    }

    public static String appName() {
        return "MyOwnTube";
    }

    public static String getAccessToken() {
        return accessToken;
    }

    public static void setAccessToken(String accessToken) {
        ApplicationLoad.accessToken = accessToken;
    }
}
