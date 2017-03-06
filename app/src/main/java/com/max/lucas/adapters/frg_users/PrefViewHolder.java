package com.max.lucas.adapters.frg_users;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;

import com.max.lucas.R;

/**
 * Created by imax5 on 17-Aug-16.
 */
public class PrefViewHolder extends RecyclerView.ViewHolder {
    public View mPreferences;

    public View btnDelete;
    public View btnSync;
    public TextView btnPlaylists;

    public View outerLayout;
    public View innerLayout;
    // Settings
    public View mFLSyncPlaylist;
    public Switch mSwitchSyncPlaylists;

    public long extraUserId;

    public PrefViewHolder(View v, long userId) {
        super(v);
        mPreferences = v;
        mFLSyncPlaylist = v.findViewById(R.id.flPlaylistSync);
        mSwitchSyncPlaylists = (Switch) v.findViewById(R.id.sPlaylistSyncValue);

        outerLayout = v.findViewById(R.id.outerLayout);
        innerLayout = v.findViewById(R.id.innerLayout);

        btnDelete = v.findViewById(R.id.btnDelete);
        btnSync = v.findViewById(R.id.btnSync);
        btnPlaylists = (TextView) v.findViewById(R.id.btnPlaylists);

        extraUserId = userId;
    }
}
