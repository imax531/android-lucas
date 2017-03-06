package com.max.lucas.models;

import org.json.JSONArray;

/**
 * Created by imax5 on 10-Dec-16.
 */

public class Playlist {
    private String naame;
    private String username;
    private int trackCount;
    private String artworkUrl;
    private JSONArray tracks;

    public Playlist(String naame, String username, int trackCount, String artworkUrl, JSONArray tracks) {
        this.artworkUrl = artworkUrl;
        this.naame = naame;
        this.trackCount = trackCount;
        this.username = username;
        this.tracks = tracks;

    }

    public String getArtworkUrl() {
        return artworkUrl;
    }

    public String getNaame() {
        return naame;
    }

    public int getTrackCount() {
        return trackCount;
    }

    public String getUsername() {
        return username;
    }

    public JSONArray getTracks() {
        return tracks;
    }
}
