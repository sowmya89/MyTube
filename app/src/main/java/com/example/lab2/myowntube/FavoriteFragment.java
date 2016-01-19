package com.example.lab2.myowntube;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lab2.myowntube.AppConstants;
import com.example.lab2.myowntube.R;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubeStandalonePlayer;
import com.google.android.youtube.player.YouTubeThumbnailLoader;
import com.google.android.youtube.player.YouTubeThumbnailView;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.Video;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ramyap on 10/16/15.
 */
public class FavoriteFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemClickListener
        ,ServerResponseListener {

    private static final int REQ_START_STANDALONE_PLAYER = 1;
    private static final int REQ_RESOLVE_SERVICE_MISSING = 2;

    private RecyclerView favVideo;
    private CheckBox delCheckBox;
    List<String> checked = new ArrayList();

    Context thisContext;
    private FavServiceTask favServiceTask = null;
    private ProgressDialog mLoadingDialog = null;
    private FavAdapter favAdapter = null;
    private ActionMode mActionMode;
    private List<String> delIds = new ArrayList<>();

    public static FavoriteFragment newInstance() {
        return new FavoriteFragment();
    }

    public FavoriteFragment() {
        // Required empty public constructor
    }
//
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorite, container, false);

        thisContext = getContext();

        favVideo = (RecyclerView) view.findViewById(R.id.fav_videos);
        myUIUpdate();
        favVideo.setLayoutManager(new LinearLayoutManager(getContext()));
        favVideo.setItemAnimator(new DefaultItemAnimator());

//        MyPlayList pl = new MyPlayList(getContext());
//
//        listFragment = (VideoListFragment) getFragmentManager().findFragmentById(R.id.list_fragment);
        return view;
    }

    public void myUIUpdate(){
        favServiceTask = new FavServiceTask(AppConstants.RET_VIDEO);
        favServiceTask.setmServerResponseListener(this);
        favServiceTask.execute();
    }
//
//    @Override
//    public void onHiddenChanged(boolean hidden) {
//        super.onHiddenChanged(hidden);
//        if (hidden) {
//            //do when hidden
//        } else {
//            //do when show
//        }
//    }

    @Override
    public void setUserVisibleHint(boolean visible){
        super.setUserVisibleHint(visible);
        if (visible && isResumed()) {
            myUIUpdate();
        }

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
            case RET_VIDEO:
                dialogMsg = RET_VIDEO_MSG;
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
            case RET_VIDEO:
                if (favAdapter == null) {
                    favAdapter = new FavAdapter(this,(List<Video>) objects[1]);
                    favVideo.setAdapter(favAdapter);
                } else {
                    favAdapter.setFavVideoList((List<Video>) objects[1]);
                    favAdapter.notifyDataSetChanged();
                }
                break;
            case DELMULTI_VIDEO:
                myUIUpdate();
                favAdapter.getCheckedItems().removeAll(favAdapter.getCheckedItems());
//                favAdapter.uncheckAll();
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

    public void showActionMode(){
        mActionMode = getActivity().startActionMode( new ActionModeCallback());

    }

    public void closeActionMode(){
        mActionMode.finish();
    }
    @Override
    public void onClick(View v) {

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    private class ActionModeCallback implements ActionMode.Callback {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // inflate contextual menu
            menu.add(Menu.NONE,R.id.delete,Menu.NONE,"Delete")
                    .setTitle("Delete")
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.delete:
                    favServiceTask = new FavServiceTask(AppConstants.DELMULTI_VIDEO);
                    favServiceTask.setmServerResponseListener(FavoriteFragment.this);
                    favServiceTask.execute(favAdapter.getCheckedItems());
                    mode.finish(); // Action picked, so close the CAB
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            // remove selection
//            favAdapter.notifyDataSetChanged();
//            mActionMode = null;
        }
    }
}

