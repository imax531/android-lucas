package com.max.lucas.adapters.act_playlists;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.max.lucas.R;

/**
 * Created by imax5 on 10-Dec-16.
 */

class ViewHolder extends RecyclerView.ViewHolder {

    TextView tvUsername;
    TextView tvTitle;
    ImageView ivCover;
    ImageView ivDownload;
    TextView tvTrackCount;

    public ViewHolder(View itemView) {
        super(itemView);
        tvUsername = (TextView) itemView.findViewById(R.id.tvUploader);
        tvTitle = (TextView) itemView.findViewById(R.id.tvTitle);
        ivCover = (ImageView) itemView.findViewById(R.id.ivCover);
        ivDownload = (ImageView) itemView.findViewById(R.id.ivDownload);
        tvTrackCount = (TextView) itemView.findViewById(R.id.tvTrackCount);
    }
}
