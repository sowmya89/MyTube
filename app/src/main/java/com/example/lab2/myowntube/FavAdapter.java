package com.example.lab2.myowntube;

import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.Video;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by ramyap on 10/16/15.
 */
public class FavAdapter extends RecyclerView.Adapter<FavAdapter.ViewHolder> implements View.OnClickListener,ServerResponseListener{
    private FavoriteFragment favFrag = null;
    private List<Video> favVideoList = null;
    private LayoutInflater mLayoutInflater = null;
    private FavServiceTask mYtFavTask = null;
    private ProgressDialog mLoadingDialog = null;
    private String videoId = null;
    ViewHolder viewHolder;

    private List<String> checkedItems = new ArrayList<String>();
    public List<String> getCheckedItems() {
        return checkedItems;
    }



    public FavAdapter(FavoriteFragment iFrag, List<Video> mVideoList) {
        this.favFrag = iFrag;
        mLayoutInflater = LayoutInflater.from(favFrag.getActivity());
        this.favVideoList = mVideoList;
    }

    public void setFavVideoList(List<Video> mVideoList) {
        this.favVideoList = mVideoList;
    }


//    @Override
//    public int getCount() {
//        return (favVideoList==null)?(0):(favVideoList.size());
//    }
//
//    @Override
//    public Video getItem(int i) {
//        return (favVideoList!=null && favVideoList.size()>i)?(favVideoList.get(i)):(null);
//    }

    @Override
    public FavAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = mLayoutInflater.inflate(R.layout.video_list_item, null);
        viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder favHolder, int position) {
        Video result = favVideoList.get(position);
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy HH:mm");
        Date resultdate = new Date(result.getSnippet().getPublishedAt().getValue());
        System.out.println("resultDate" + resultdate);
        String mDateTime = sdf.format(resultdate);
        System.out.println("mDateTime" + mDateTime);
        favHolder.favVideoDate.setText(mDateTime);

        favHolder.favVideoTitleTxv.setText(result.getSnippet().getTitle());
        System.out.println("VIEWS" + result.getStatistics().getViewCount());
        String views = result.getStatistics().getViewCount().toString();
        System.out.println("view in string:\n" + views);
        favHolder.favVideoViews.setText(views);

        videoId = result.getId();
        favHolder.favVideoId.setText(videoId);
        System.out.println("video id:\n"+videoId);


        //Load images
        Picasso.with(favFrag.getActivity()).load(result.getSnippet().getThumbnails().getMedium().getUrl()).into(favHolder.favVideoThumbnail);


        favHolder.favVideoThumbnail.setOnClickListener(this);
        favHolder.delCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                View parentView = (View) buttonView.getParent();
                String videoId = ((TextView) parentView.findViewById(R.id.videoId)).getText().toString();
                if (isChecked) {
                    checkedItems.add(videoId);
                    favFrag.showActionMode();
                } else {
                    if (checkedItems.contains(videoId)) {
                        checkedItems.remove(videoId);
                    }
                }
            }
        });

    }

//    public void uncheckAll(){
//        viewHolder.delCheckBox.setChecked(false);
//    }
    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public int getItemCount() {
        return (favVideoList==null)?(0):(favVideoList.size());
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView favVideoTitleTxv = null;
        private TextView favVideoViews = null;
        private TextView favVideoDate = null;
        private ImageView favVideoThumbnail = null;
        private CheckBox delCheckBox = null;
        private TextView favVideoId = null;


        public ViewHolder(View v) {
            super(v);

            favVideoThumbnail = (ImageView) v.findViewById(R.id.video_thumbnail_imv);
            favVideoTitleTxv = (TextView) v.findViewById(R.id.video_title_txv);
            favVideoDate = (TextView) v.findViewById(R.id.published_date);
            favVideoViews = (TextView) v.findViewById(R.id.video_views);
            favVideoId = (TextView) v.findViewById(R.id.videoId);


            delCheckBox = (CheckBox) v.findViewById(R.id.del_checkbox);
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
            case FAV_VIDEO:
                dialogMsg = FAV_VIDEO_MSG;
                break;
        }

        if (mLoadingDialog != null && !mLoadingDialog.isShowing())
            mLoadingDialog = ProgressDialog.show(favFrag.getContext(), DIALOG_TITLE, dialogMsg, true, true);
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
        View parentView = (View) v.getParent();
        videoId = ((TextView) parentView.findViewById(R.id.videoId)).getText().toString();
        favFrag.fullScreen(videoId);
    }

}
