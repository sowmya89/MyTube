package com.example.lab2.myowntube;
import com.example.lab2.myowntube.ApplicationLoad;
import com.google.android.gms.common.Scopes;
import com.google.api.services.youtube.YouTubeScopes;

public interface AppConstants {
    public static final int SEARCH_VIDEO = 1;
    public static final int FAV_VIDEO = 1;
    public static final int DEL_VIDEO = 2;
    public static final int DELMULTI_VIDEO = 3;

    public static final int RET_VIDEO = 4;


    public static final String SEARCH_VIDEO_MSG = "Searching Videos";
    public static final String FAV_VIDEO_MSG = "Adding Favourite Video";

    public static final String RET_VIDEO_MSG = "Retrieving Favourite Video";

    public static final String DIALOG_TITLE = "Loading";

    public static final long NUMBER_OF_VIDEOS_RETURNED = 25;
    public static final String APP_NAME = ApplicationLoad.appName();

    // Register an API key here: https://code.google.com/apis/console
    // Note : This is the browser key instead of android key as Android key was generating Service config errors (403)
    public static final String KEY = "AIzaSyApTmppk6XvhM1Ndywt3W8ymG-PjyAsyyA";

    public static final String[] SCOPES = {Scopes.PROFILE, YouTubeScopes.YOUTUBE};
}
