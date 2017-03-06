package com.max.lucas.models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Created by imax5 on 07-May-16.
 */
public class HomeTrack {
    private long id;
    private boolean downloaded;
    private String uploader;
    private String title;
    private String artworkUrl;
    private ArrayList<Long> users;

    public HomeTrack(long id, String title, String uploader, String artworkUrl, boolean downloaded) {
        this.artworkUrl = artworkUrl;
        this.downloaded = downloaded;
        this.id = id;
        this.title = title;
        this.uploader = uploader;
        this.users = new ArrayList<>();
    }

    public boolean update(HomeTrack track) {
        boolean modified = false;

        if (this.downloaded != track.downloaded) {
            this.downloaded = track.downloaded;
            modified = true;
        }
        if (!this.uploader.equals(track.uploader)) {
            this.uploader = track.uploader;
            modified = true;
        }
        if (!this.title.equals(track.title)) {
            this.title = track.title;
            modified = true;
        }
        if (this.artworkUrl != null && !this.artworkUrl.equals(track.artworkUrl) ||
                this.artworkUrl == null && track.artworkUrl != null) {
            this.artworkUrl = track.artworkUrl;
            modified = true;
        }
        for (int i = 0; i < track.users.size(); i++) {
            if (!this.users.contains(track.users.get(i))) {
                this.users.add(track.users.get(i));
                modified = true;
            }
        }

        return modified;
    }

    public void addUser(long userId) {
        users.add(userId);
    }

    public boolean isDownloaded() {
        return downloaded;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getUploader() {
        return uploader;
    }

    public ArrayList<Long> getUsers() {
        return users;
    }

    public String getArtworkUrl() {
        return artworkUrl;
    }
}
