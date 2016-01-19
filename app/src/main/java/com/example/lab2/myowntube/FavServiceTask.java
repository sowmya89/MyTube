package com.example.lab2.myowntube;
import android.os.AsyncTask;
import android.util.Log;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.repackaged.com.google.common.base.Joiner;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Playlist;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemListResponse;
import com.google.api.services.youtube.model.PlaylistItemSnippet;
import com.google.api.services.youtube.model.PlaylistListResponse;
import com.example.lab2.myowntube.AppConstants;
import com.example.lab2.myowntube.ApplicationLoad;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FavServiceTask  extends AsyncTask<Object, Void, Object[]> implements
        ServiceTaskInterface {

    List<String> videoIds = new ArrayList<String>();
    private static final String TAG = FavServiceTask.class.getSimpleName();
    String pListId = null;
    private ServerResponseListener mServerResponseListener = null;
    private int mRequestCode = 0;
    private String _accessToken;
    private YouTube youtube;
    List<Playlist> pList;
    List<PlaylistItem> playlistItemList = new ArrayList<PlaylistItem>();
    GoogleCredential credential;
    PlaylistItem returnedPlaylistItem;

    private Credential createCredentialWithAccessTokenOnly(
            HttpTransport transport, JsonFactory jsonFactory, TokenResponse tokenResponse) {
        return new Credential(BearerToken.authorizationHeaderAccessMethod()).setFromTokenResponse(
                tokenResponse);
    }

    public void setmServerResponseListener(
            ServerResponseListener mServerResponseListener) {
        this.mServerResponseListener = mServerResponseListener;
    }

    public FavServiceTask(int iReqCode) {
        mRequestCode = iReqCode;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mServerResponseListener.prepareRequest(mRequestCode);
    }

    @Override
    protected Object[] doInBackground(Object... params) {
        if (params == null)
            throw new NullPointerException("Parameters to the async task can never be null");

        mServerResponseListener.goBackground();

        Object[] resultDetails = new Object[2];
        resultDetails[0] = mRequestCode;

        switch (mRequestCode) {
            case AppConstants.FAV_VIDEO:
                resultDetails[1] = addFavVideo((String) params[0],(String) params[0]);
                break;
            case AppConstants.DEL_VIDEO:
                delFavVideo((String) params[0]);
                break;
            case AppConstants.DELMULTI_VIDEO:
                delMultiFavVideo((List<String>) params[0]);
                break;
            case AppConstants.RET_VIDEO:
                resultDetails[1] = retrieveFavVideos();
                break;
        }


        return resultDetails;
    }

    @Override
    protected void onPostExecute(Object[] result) {
        super.onPostExecute(result);
        System.out.println("RESULT" + result);
        mServerResponseListener.completedRequest(result);
    }

    private List<Video> retrieveFavVideos() {
        Credential credential = createCredentialWithAccessTokenOnly(transport, jsonFactory, new GoogleTokenResponse().setAccessToken(ApplicationLoad.getAccessToken()));
        youtube = new YouTube.Builder(transport, jsonFactory, credential)
                .setApplicationName(ApplicationLoad.appName())
                .build();
        try {
            System.out.println("Access token for youtube" + ApplicationLoad.getAccessToken());


            YouTube.Playlists.List playLists = youtube.playlists().list("id,snippet");
            playLists.setMine(true);
            playLists.setFields("items(id,snippet(title))");
            playLists.setMaxResults(AppConstants.NUMBER_OF_VIDEOS_RETURNED);

            PlaylistListResponse playlistListResponse = playLists.execute();
            pList = playlistListResponse.getItems();


            for (int i = 0; i < playlistListResponse.getItems().size(); i++) {
                if (pList.get(i).getSnippet().getTitle().equals("SJSU-CMPE-277")) {
                    System.out.println("PLAYLISTS TITLE..." + pList.get(i).getSnippet().getTitle());
                    System.out.println("PLAYLISTS ID..." + pList.get(i).getId());
                    pListId = pList.get(i).getId();
                    System.out.println("PlayerListId" + pListId);
                }

            }

        } catch (GoogleJsonResponseException e) {
            System.err.println("There was a service error: " + e.getDetails().getCode() + " : "
                    + e.getDetails().getMessage());
        } catch (IOException e) {
            System.err.println("There was an IO error: " + e.getCause() + " : " + e.getMessage());
        } catch (Throwable t) {
            t.printStackTrace();
        }
        try {
            if (pList != null) {
                YouTube.PlaylistItems.List videoList = youtube.playlistItems().list("id,contentDetails,snippet");
                videoList.setPlaylistId(pListId);
                videoList.setFields("items(snippet/resourceId/videoId,snippet/title,snippet/publishedAt,snippet/thumbnails/default/url,snippet/thumbnails/medium/url)");
                videoList.setMaxResults(AppConstants.NUMBER_OF_VIDEOS_RETURNED);
                PlaylistItemListResponse playlistItemResult = videoList.execute();
                playlistItemList.addAll(playlistItemResult.getItems());
                for (int i = 0; i < playlistItemList.size(); i++) {
                    System.out.println("VIDEO TITLE..." + playlistItemList.get(i).getSnippet().getTitle());
                    System.out.println("VIDEO ID..." + playlistItemList.get(i).getSnippet().getResourceId().getVideoId());
                    System.out.println("VIDEO Thumbnail..." + playlistItemList.get(i).getSnippet().getThumbnails().getMedium().getUrl());
                    System.out.println("VIDEO publishedDate..." + playlistItemList.get(i).getSnippet().getPublishedAt());

                    videoIds.add(playlistItemList.get(i).getSnippet().getResourceId().getVideoId());

                }

                Joiner stringJoiner = Joiner.on(',');
                String vId = stringJoiner.join(videoIds);
                //     System.out.println("VIDEOIDS"+videoId);
                YouTube.Videos.List listVideosRequest = youtube.videos().list("snippet,statistics").setId(vId).setKey(AppConstants.KEY);
                listVideosRequest.setMaxResults(AppConstants.NUMBER_OF_VIDEOS_RETURNED);
                VideoListResponse listResponse = listVideosRequest.execute();


                List<Video> favVideoList = listResponse.getItems();
                for (int i = 0; i < favVideoList.size(); i++) {
                    System.out.println("PRINTING FAVOURITE VIDEO LIST\n");
                    System.out.println(favVideoList.get(i));
                }
                if (favVideoList != null) {
                    return favVideoList;
                }
                //          return playlistItemList;
            }
        } catch (GoogleJsonResponseException e) {
            System.err.println("There was a service error: " + e.getDetails().getCode() + " : "
                    + e.getDetails().getMessage());
        } catch (IOException e) {
            System.err.println("There was an IO error: " + e.getCause() + " : " + e.getMessage());
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return null;
    }


    private String addFavVideo(String videoId, String title) {
        Credential credential = createCredentialWithAccessTokenOnly(transport, jsonFactory, new GoogleTokenResponse().setAccessToken(ApplicationLoad.getAccessToken()));
        youtube = new YouTube.Builder(transport, jsonFactory, credential)
                .setApplicationName(ApplicationLoad.appName())
                .build();
        try {
            System.out.println("Access token for youtube" + ApplicationLoad.getAccessToken());


            YouTube.Playlists.List playLists = youtube.playlists().list("id,snippet");
            playLists.setMine(true);
            playLists.setFields("items(id,snippet(title))");


            PlaylistListResponse playlistListResponse = playLists.execute();
            pList = playlistListResponse.getItems();


            for (int i = 0; i < playlistListResponse.getItems().size(); i++) {
                if (pList.get(i).getSnippet().getTitle().equals("SJSU-CMPE-277")) {
                    System.out.println("PLAYLISTS TITLE..." + pList.get(i).getSnippet().getTitle());
                    System.out.println("PLAYLISTS ID..." + pList.get(i).getId());
                    pListId = pList.get(i).getId();
                    System.out.println("PlayerListId" + pListId);
                }

            }

        } catch (GoogleJsonResponseException e) {
            System.err.println("There was a service error: " + e.getDetails().getCode() + " : "
                    + e.getDetails().getMessage());
        } catch (IOException e) {
            System.err.println("There was an IO error: " + e.getCause() + " : " + e.getMessage());
        } catch (Throwable t) {
            t.printStackTrace();
        }
        try {
            if (pList != null) {

                ResourceId resourceId = new ResourceId();
                resourceId.setKind("youtube#video");
                resourceId.setVideoId(videoId);

                PlaylistItemSnippet playlistItemSnippet = new PlaylistItemSnippet();
                playlistItemSnippet.setTitle(title);
                playlistItemSnippet.setPlaylistId(pListId);
                playlistItemSnippet.setResourceId(resourceId);

                PlaylistItem playlistItem = new PlaylistItem();
                playlistItem.setSnippet(playlistItemSnippet);

                YouTube.PlaylistItems.Insert playlistItemsInsertCommand =
                        youtube.playlistItems().insert("snippet,contentDetails", playlistItem);
                returnedPlaylistItem = playlistItemsInsertCommand.execute();
                System.out.println("New PlaylistItem video name: " + returnedPlaylistItem.getSnippet().getTitle());
                System.out.println(" - Video id: " + returnedPlaylistItem.getSnippet().getResourceId().getVideoId());
                System.out.println(" - Posted: " + returnedPlaylistItem.getSnippet().getPublishedAt());
                System.out.println(" - Channel: " + returnedPlaylistItem.getSnippet().getChannelId());
                System.out.println("PlayerListItemId" + returnedPlaylistItem.getId());


            }
            return returnedPlaylistItem.getId();

        } catch (GoogleJsonResponseException e) {
            System.err.println("There was a service error: " + e.getDetails().getCode() + " : "
                    + e.getDetails().getMessage());
        } catch (IOException e) {
            System.err.println("There was an IO error: " + e.getCause() + " : " + e.getMessage());
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return null;
    }

    private void delFavVideo(String videoId) {

        System.out.println("To delete"+videoId);
        Credential credential = createCredentialWithAccessTokenOnly(transport, jsonFactory, new GoogleTokenResponse().setAccessToken(ApplicationLoad.getAccessToken()));
        youtube = new YouTube.Builder(transport, jsonFactory, credential)
                .setApplicationName(ApplicationLoad.appName())
                .build();
        try {
            System.out.println("Access token for youtube" + ApplicationLoad.getAccessToken());


            YouTube.Playlists.List playLists = youtube.playlists().list("id,snippet");
            playLists.setMine(true);
            playLists.setFields("items(id,snippet(title))");


            PlaylistListResponse playlistListResponse = playLists.execute();
            pList = playlistListResponse.getItems();


            for (int i = 0; i < playlistListResponse.getItems().size(); i++) {
                if (pList.get(i).getSnippet().getTitle().equals("SJSU-CMPE-277")) {
                    System.out.println("PLAYLISTS TITLE..." + pList.get(i).getSnippet().getTitle());
                    System.out.println("PLAYLISTS ID..." + pList.get(i).getId());
                    pListId = pList.get(i).getId();
                    System.out.println("PlayerListId" + pListId);
                }

            }

        } catch (GoogleJsonResponseException e) {
            System.err.println("There was a service error: " + e.getDetails().getCode() + " : "
                    + e.getDetails().getMessage());
        } catch (IOException e) {
            System.err.println("There was an IO error: " + e.getCause() + " : " + e.getMessage());
        } catch (Throwable t) {
            t.printStackTrace();
        }
        try {
            if (pList != null) {

                System.out.println("To delete" + videoId);
                YouTube.PlaylistItems.List videoList = youtube.playlistItems().list("id,contentDetails,snippet");
                videoList.setPlaylistId(pListId);
                videoList.setVideoId( videoId);
                videoList.setFields("items(id)");
                PlaylistItemListResponse playlistItemResult = videoList.execute();
                List<PlaylistItem> playlistItemId = playlistItemResult.getItems();
                String delId = playlistItemId.get(0).getId();
                System.out.println("Video sent"+videoId);
                System.out.println("TO be deleted" + delId);
                if (delId != null) {
                    YouTube.PlaylistItems.Delete playlistItemDeleteCommand = youtube.playlistItems().delete(delId);
                    playlistItemDeleteCommand.execute();
                }

            }

        } catch (GoogleJsonResponseException e) {
            System.err.println("There was a service error: " + e.getDetails().getCode() + " : "
                    + e.getDetails().getMessage());
        } catch (IOException e) {
            System.err.println("There was an IO error: " + e.getCause() + " : " + e.getMessage());
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private void delMultiFavVideo(List<String> videoIdList) {
        Credential credential = createCredentialWithAccessTokenOnly(transport, jsonFactory, new GoogleTokenResponse().setAccessToken(ApplicationLoad.getAccessToken()));
        youtube = new YouTube.Builder(transport, jsonFactory, credential)
                .setApplicationName(ApplicationLoad.appName())
                .build();
        try {
            System.out.println("Access token for youtube" + ApplicationLoad.getAccessToken());


            YouTube.Playlists.List playLists = youtube.playlists().list("id,snippet");
            playLists.setMine(true);
            playLists.setFields("items(id,snippet(title))");


            PlaylistListResponse playlistListResponse = playLists.execute();
            pList = playlistListResponse.getItems();


            for (int i = 0; i < playlistListResponse.getItems().size(); i++) {
                if (pList.get(i).getSnippet().getTitle().equals("SJSU-CMPE-277")) {
                    System.out.println("PLAYLISTS TITLE..." + pList.get(i).getSnippet().getTitle());
                    System.out.println("PLAYLISTS ID..." + pList.get(i).getId());
                    pListId = pList.get(i).getId();
                    System.out.println("PlayerListId" + pListId);
                }

            }

        } catch (GoogleJsonResponseException e) {
            System.err.println("There was a service error: " + e.getDetails().getCode() + " : "
                    + e.getDetails().getMessage());
        } catch (IOException e) {
            System.err.println("There was an IO error: " + e.getCause() + " : " + e.getMessage());
        } catch (Throwable t) {
            t.printStackTrace();
        }
        try {
            if (pList != null) {

                for ( int i = 0; i< videoIdList.size(); i++) {
                    System.out.println("To delete" + videoIdList);
                    YouTube.PlaylistItems.List videoList = youtube.playlistItems().list("id,contentDetails,snippet");
                    videoList.setPlaylistId(pListId);
                    videoList.setVideoId(videoIdList.get(i));
                    videoList.setFields("items(id,contentDetails/videoId,snippet/title)");
                    videoList.setOauthToken(ApplicationLoad.getAccessToken());
                    PlaylistItemListResponse playlistItemResult = videoList.execute();
                    List<PlaylistItem> playlistItemId = playlistItemResult.getItems();
                    String delId = playlistItemId.get(0).getId();
                    System.out.println("TO be deleted" + delId);
                    if (delId != null) {
                        YouTube.PlaylistItems.Delete playlistItemDeleteCommand = youtube.playlistItems().delete(delId);
                        playlistItemDeleteCommand.execute();
                    }

                }

            }

        } catch (GoogleJsonResponseException e) {
            System.err.println("There was a service error: " + e.getDetails().getCode() + " : "
                    + e.getDetails().getMessage());
        } catch (IOException e) {
            System.err.println("There was an IO error: " + e.getCause() + " : " + e.getMessage());
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}