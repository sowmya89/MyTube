package com.example.lab2.myowntube;

import android.os.AsyncTask;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.util.Joiner;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Thumbnail;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import com.example.lab2.myowntube.AppConstants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ServiceTask extends AsyncTask<Object, Void, Object[]> implements
        ServiceTaskInterface {
    private static final String TAG = ServiceTask.class.getSimpleName();
    private ServerResponseListener mServerResponseListener = null;
    private int mRequestCode = 0;
    List<SearchResult> searchResultList;

    public void setmServerResponseListener(
            ServerResponseListener mServerResponseListener) {
        this.mServerResponseListener = mServerResponseListener;
    }

    public ServiceTask(int iReqCode) {
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
            case AppConstants.SEARCH_VIDEO:
                resultDetails[1] = loadVideos((String) params[0]);
                break;
        }

        return resultDetails;
    }

    @Override
    protected void onPostExecute(Object[] result) {
        super.onPostExecute(result);
        System.out.println("RESULT"+result);
        mServerResponseListener.completedRequest(result);
    }


    /**
     * Method
     * @see <a>https://developers.google.com/youtube/v3/code_samples/java#search_by_keyword</a>
     * @param queryTerm
     */
    private List<Video> loadVideos(String queryTerm) {
        try {
            // This object is used to make YouTube Data API requests. The last
            // argument is required, but since we don't need anything
            // initialized when the HttpRequest is initialized, we override
            // the interface and provide a no-op function.
            YouTube youtube = new YouTube.Builder(transport, jsonFactory, new HttpRequestInitializer() {
                public void initialize(HttpRequest request) throws IOException {
                }
            }).setApplicationName(AppConstants.APP_NAME).build();

            // Define the API request for retrieving search results.
            YouTube.Search.List search = youtube.search().list("id,snippet");

            // Set your developer key from the Google Developers Console for
            // non-authenticated requests. See:
            // https://console.developers.google.com/
            search.setKey(AppConstants.KEY);
            search.setQ(queryTerm);
            System.out.println("TERM" + queryTerm);

            // Restrict the search results to only include videos. See:
            // https://developers.google.com/youtube/v3/docs/search/list#type
            search.setType("video");

            // To increase efficiency, only retrieve the fields that the
            // application uses.
            search.setFields("items(id/kind,id/videoId,snippet/title,snippet/publishedAt,snippet/thumbnails/default/url,snippet/thumbnails/medium/url)");
            search.setMaxResults(AppConstants.NUMBER_OF_VIDEOS_RETURNED);

            // Call the API and print results.
            SearchListResponse searchResponse = search.execute();
            searchResultList = searchResponse.getItems();
            List<String> videoIds = new ArrayList<String>();

//            for ( int i = 0; i< searchResultList.size(); i++)
//            {
//                System.out.println("PINTING LIST\n");
//                System.out.println(searchResultList.get(i));
//            }


            if (searchResultList != null) {

                for (SearchResult searchResult : searchResultList) {
                    videoIds.add(searchResult.getId().getVideoId());
                }
                Joiner stringJoiner = Joiner.on(',');
                String videoId = stringJoiner.join(videoIds);
                //     System.out.println("VIDEOIDS"+videoId);
                YouTube.Videos.List listVideosRequest = youtube.videos().list("snippet,statistics").setId(videoId).setKey(AppConstants.KEY);
                VideoListResponse listResponse = listVideosRequest.execute();

                List<Video> videoList = listResponse.getItems();

//                for ( int i = 0; i< videoList.size(); i++)
//                {
//                    System.out.println("PRINTING VIDEO LIST\n");
//                    System.out.println(videoList.get(i));
//                }
//
                if (videoList != null) {
                    return videoList;
                }
//            return searchResultList;
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



}
