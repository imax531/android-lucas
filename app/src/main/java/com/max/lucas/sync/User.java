package com.max.lucas.sync;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.max.lucas.R;

/**
 * Created by imax5 on 29-Nov-15.
 */
public class User {
    private long id;
    private String username;
    private String imageurl;

    //settings
    private boolean mSyncPlaylists;

    public String getPermalink() {
        return permalink;
    }

    public void setPermalink(String permalink) {
        this.permalink = permalink;
    }

    private String permalink;

    public User() {}

    public User(long id, String username, String permalink) {
        this.id = id;
        this.username = username;
        this.permalink = permalink;
    }

    public User(int id, String username, String imageurl, String permalink) {
        this.id = id;
        this.username = username;
        this.imageurl = imageurl;
        this.permalink = permalink;
    }

    public long getId() {
        return id;
    }

    public String getImageurl() {
        return imageurl;
    }

    public String getUsername() {
        return username;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setImageurl(String imageurl) {
        this.imageurl = imageurl;
    }

    public void setUsername(String username) { this.username = username; }

    public boolean getSettingPlaylistSync(Activity context) {
        SharedPreferences sharedPref = context.getPreferences(Context.MODE_PRIVATE);
        return sharedPref.getBoolean(context.getString(R.string.pref_sync_user_playlists_key, id),
                Boolean.valueOf(context.getString(R.string.pref_sync_user_playlists_default)));
    }

    public void setSettingPlaylistSync(Activity context, boolean value) {
        SharedPreferences.Editor editor = context.getPreferences(Context.MODE_PRIVATE).edit();
        editor.putBoolean(context.getString(R.string.pref_sync_user_playlists_key, id), value);
        editor.apply();
    }
}
