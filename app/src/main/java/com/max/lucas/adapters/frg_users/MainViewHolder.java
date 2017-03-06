package com.max.lucas.adapters.frg_users;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.max.lucas.R;

/**
 * Created by imax5 on 17-Aug-16.
 */
public class MainViewHolder extends RecyclerView.ViewHolder {
    public ImageView mAvater;
    public TextView mUsername;
    public long extraUserId;

    public String extraUsername;

    public MainViewHolder(View v) {
        super(v);
        this.mAvater = (ImageView) v.findViewById(R.id.ivSoundcloudUserRowProfile);
        this.mUsername = (TextView) v.findViewById(R.id.tvSoundcloudUserRowUsername);
    }
}