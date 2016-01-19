package com.example.lab2.myowntube;

import android.util.Log;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.plusDomains.PlusDomains;

public class GooglePlusDomainApi {
    private static PlusDomains plusDomains = null;

    public GooglePlusDomainApi(GoogleCredential credential){
        Log.i("weird_error:", credential.toString());
        if(this.plusDomains == null) {
            this.plusDomains = new PlusDomains.Builder(new NetHttpTransport(),
                    new JacksonFactory(), credential).setApplicationName("MyYouTube").build();
        }
    }

    public static PlusDomains getPlusDomains(){
        return plusDomains;
    }
}
