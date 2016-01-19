package com.example.lab2.myowntube;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.lab2.myowntube.FavServiceTask;
import com.google.api.client.util.DateTime;
import com.google.api.services.youtube.model.Video;
import com.example.lab2.myowntube.ServerResponseListener;
import com.example.lab2.myowntube.R;
import com.google.api.services.youtube.model.SearchResult;
import com.squareup.picasso.Picasso;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class YtAdapter extends BaseAdapter implements View.OnClickListener, CompoundButton.OnCheckedChangeListener, ServerResponseListener{

    private SearchFragment mFrag = null;
    private List<Video> mVideoList = null;
    private LayoutInflater mLayoutInflater = null;
    private FavServiceTask mYtFavTask = null;
    private ProgressDialog mLoadingDialog = null;
    private String videoId = null;
    private String videoTitle = null;
    Context context;


    public YtAdapter(Context context, SearchFragment iFrag) {
        this.context = context;
        mFrag = iFrag;
        mLayoutInflater = LayoutInflater.from(mFrag.getActivity());
    }

    public void setmVideoList(List<Video> mVideoList) {
        this.mVideoList = mVideoList;
    }


    @Override
    public int getCount() {
        return (mVideoList==null)?(0):(mVideoList.size());
    }

    @Override
    public Object getItem(int i) {
        return (mVideoList!=null && mVideoList.size()>i)?(mVideoList.get(i)):(null);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder mHolder = null;
        View v = null;
//        if (v != null) {
//            mHolder = (ViewHolder)view.getTag();
//        } else {
        if (v == null) {
            mHolder = new ViewHolder();
            v = mLayoutInflater.inflate(R.layout.search_item, null);
            mHolder.mVideoThumbnail = (ImageView)v.findViewById(R.id.video_thumbnail_imv);
            mHolder.mVideoTitleTxv = (TextView)v.findViewById(R.id.video_title_txv);
            mHolder.mVideoDate = (TextView)v.findViewById(R.id.published_date);
            mHolder.mVideoViews = (TextView)v.findViewById(R.id.video_views);
            mHolder.favButton = (ToggleButton)v.findViewById(R.id.toggleButton);
            mHolder.vId = (TextView)v.findViewById(R.id.video_id);
            mHolder.favButton.setOnCheckedChangeListener(this);
            mHolder.mVideoThumbnail.setOnClickListener(this);
            v.setTag(mHolder);

        }

        // Set the data
        Video result = mVideoList.get(i);
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy HH:mm");
        Date resultdate = new Date(result.getSnippet().getPublishedAt().getValue());
        System.out.println("resultDate" + resultdate);
        String mDateTime = sdf.format(resultdate);
        System.out.println("mDateTime" + mDateTime);
        mHolder.mVideoDate.setText(mDateTime);

        mHolder.mVideoTitleTxv.setText(result.getSnippet().getTitle());
        System.out.println("VIEWS" + result.getStatistics().getViewCount());
        String views = result.getStatistics().getViewCount().toString();
        System.out.println("view in string:\n" + views);
        mHolder.mVideoViews.setText(views);
        mHolder.vId.setText(result.getId());
        System.out.println("video id:\n"+mHolder.vId.getText().toString());


        //Load images
        Picasso.with(mFrag.getActivity()).load(result.getSnippet().getThumbnails().getMedium().getUrl()).into(mHolder.mVideoThumbnail);
        return v;
    }
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            // The toggle is enabled
            View parentView = (View) buttonView.getParent();
            View superParentView = (View) parentView.getParent();
            buttonView.setChecked(true);
            videoId = ((TextView) parentView.findViewById(R.id.video_id)).getText().toString();
            videoTitle = ((TextView) superParentView.findViewById(R.id.video_title_txv)).getText().toString();
            System.out.println("video id & videoTitle:\n"+videoId+"\n"+videoTitle);
            mYtFavTask = new FavServiceTask(AppConstants.FAV_VIDEO);
            mYtFavTask.setmServerResponseListener(this);
            mYtFavTask.execute(new String[]{videoId,videoTitle});
            Toast.makeText(mFrag.getActivity().getApplicationContext(), "Video Saved", Toast.LENGTH_LONG).show();
        } else {
            View parentView = (View) buttonView.getParent();
            buttonView.setChecked(false);
            videoId = ((TextView) parentView.findViewById(R.id.video_id)).getText().toString();
            mYtFavTask = new FavServiceTask(AppConstants.DEL_VIDEO);
            mYtFavTask.setmServerResponseListener(this);
            mYtFavTask.execute(new String[]{videoId});
            Toast.makeText(mFrag.getActivity().getApplicationContext(), "Video Removed from Playlist", Toast.LENGTH_LONG).show();
            // The toggle is disabled
        }
    }

//    @Override
//    public void onClick(View v) {
//
//        mYtFavTask = new FavServiceTask(AppConstants.FAV_VIDEO);
//        mYtFavTask.setmServerResponseListener(this);
//        mYtFavTask.execute(new String[]{videoId});
//        Toast.makeText(mFrag.getActivity().getApplicationContext(), "Video Saved", Toast.LENGTH_LONG).show();
//
//    }

    @Override
    public void prepareRequest(Object... objects) {
        // Parse the response based upon type of request
        Integer reqCode = (Integer) objects[0];

        if(reqCode==null || reqCode == 0)
            throw new NullPointerException("Request Code's value is Invalid.");
        String dialogMsg = null;
        switch (reqCode)
        {
            case FAV_VIDEO:
                dialogMsg = FAV_VIDEO_MSG;
                break;
        }

        if (mLoadingDialog != null && !mLoadingDialog.isShowing())
            mLoadingDialog = ProgressDialog.show(mFrag.getContext(), DIALOG_TITLE, dialogMsg, true, true);
    }

    @Override
    public void goBackground(Object... objects) {

    }

    @Override
    public void completedRequest(Object... objects) {
        // Dismiss the dialog
        if (mLoadingDialog != null && mLoadingDialog.isShowing())
            mLoadingDialog.dismiss();

    }

    @Override
    public void onClick(View v) {
//
        View parentView = (View) v.getParent();
        videoId = ((TextView) parentView.findViewById(R.id.video_id)).getText().toString();
        mFrag.fullScreen(videoId);
 }

    private class ViewHolder {
        private TextView mVideoTitleTxv = null;
        private TextView mVideoViews = null;
        private TextView mVideoDate = null;
        private ImageView mVideoThumbnail = null;
        private ToggleButton favButton = null;
        private TextView vId = null;
    }
    @Override

    public int getViewTypeCount() {

        return getCount();
    }

    @Override
    public int getItemViewType(int position) {

        return position;
    }

}
