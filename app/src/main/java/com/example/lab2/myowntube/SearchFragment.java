package com.example.lab2.myowntube;

import android.app.ProgressDialog;
import android.content.Context;

import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import com.example.lab2.myowntube.R;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubeStandalonePlayer;
import com.google.api.services.youtube.model.Video;
import com.example.lab2.myowntube.YtAdapter;
import com.example.lab2.myowntube.ServerResponseListener;

import com.example.lab2.myowntube.ServiceTask;
import com.google.api.services.youtube.model.SearchResult;
import com.squareup.picasso.Picasso;

import java.util.List;

public class SearchFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemClickListener
        ,ServerResponseListener {

    private static final int REQ_START_STANDALONE_PLAYER = 1;
    private static final int REQ_RESOLVE_SERVICE_MISSING = 2;

    private EditText searchInput;
    private ListView videosFound;
    private Button search;
    private List<Video> searchResults;
    Context thisContext;
    private ServiceTask mYtServiceTask = null;
    private ProgressDialog mLoadingDialog = null;
    private YtAdapter mYtAdapter = null;


    private Handler handler;

    public static SearchFragment newInstance() {
        return new SearchFragment();
    }

    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search,
                container, false);

        thisContext = getContext();
        searchInput = (EditText)view.findViewById(R.id.search_input);
        search = (Button) view.findViewById(R.id.search);
        videosFound = (ListView) view.findViewById(R.id.videos_found);
        search.setOnClickListener(this);
        videosFound.setOnItemClickListener(this);
        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.search:
                final String keyWord = searchInput.getText().toString().trim();
                if (keyWord.length() > 0) {
                    System.out.println("KEYWORD"+keyWord);

                    // Service to search video
                    mYtServiceTask = new ServiceTask(SEARCH_VIDEO);
                    mYtServiceTask.setmServerResponseListener(this);
                    mYtServiceTask.execute(new String[]{keyWord});
                } else {
                    Toast.makeText(thisContext.getApplicationContext(),"Empty field", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

    }

    @Override
    public void prepareRequest(Object... objects) {
        // Parse the response based upon type of request
        Integer reqCode = (Integer) objects[0];

        if(reqCode==null || reqCode == 0)
            throw new NullPointerException("Request Code's value is Invalid.");
        String dialogMsg = null;
        switch (reqCode)
        {
            case SEARCH_VIDEO:
                dialogMsg = SEARCH_VIDEO_MSG;
                break;
        }

        if (mLoadingDialog != null && !mLoadingDialog.isShowing())
            mLoadingDialog = ProgressDialog.show(this.thisContext, DIALOG_TITLE, dialogMsg, true, true);
    }

    @Override
    public void goBackground(Object... objects) {

    }

    @Override
    public void completedRequest(Object... objects) {
        // Dismiss the dialog
        if (mLoadingDialog != null && mLoadingDialog.isShowing())
            mLoadingDialog.dismiss();

        // Parse the response based upon type of request
        Integer reqCode = (Integer) objects[0];

        if(reqCode==null || reqCode == 0)
            throw new NullPointerException("Request Code's value is Invalid.");

        switch (reqCode) {
            case SEARCH_VIDEO:

                if (mYtAdapter == null) {
                    mYtAdapter = new YtAdapter(thisContext,this);
                    mYtAdapter.setmVideoList((List<Video>) objects[1]);
                    videosFound.setAdapter(mYtAdapter);
                } else {
                    mYtAdapter.setmVideoList((List<Video>) objects[1]);
                    mYtAdapter.notifyDataSetChanged();
                }

                break;
        }
    }

    public void fullScreen(String videoId){

        Intent intent = YouTubeStandalonePlayer.createVideoIntent(getActivity(), AppConstants.KEY, videoId, 0, true, false);

        if (intent != null) {
            if (canResolveIntent(intent)) {
                startActivityForResult(intent, REQ_START_STANDALONE_PLAYER);
            } else {
                // Could not resolve the intent - must need to install or update the YouTube API service.
                YouTubeInitializationResult.SERVICE_MISSING
                        .getErrorDialog(getActivity(), REQ_RESOLVE_SERVICE_MISSING).show();
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQ_START_STANDALONE_PLAYER && resultCode != getActivity().RESULT_OK) {
            YouTubeInitializationResult errorReason =
                    YouTubeStandalonePlayer.getReturnedInitializationResult(data);
            if (errorReason.isUserRecoverableError()) {
                errorReason.getErrorDialog(getActivity(), 0).show();
            } else {
                String errorMessage =
                        String.format(getString(R.string.error_player), errorReason.toString());
                Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_LONG).show();
            }
        }
    }

    private boolean canResolveIntent(Intent intent) {
        List<ResolveInfo> resolveInfo = getContext().getPackageManager().queryIntentActivities(intent, 0);
        return resolveInfo != null && !resolveInfo.isEmpty();
    }
}