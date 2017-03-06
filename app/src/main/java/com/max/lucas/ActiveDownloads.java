package com.max.lucas;

import android.content.Context;
import android.os.Environment;

import java.io.File;

/**
 * Created by imax5 on 22-Nov-15.
 */
public class ActiveDownloads {

    public static final String TAG = "lucas.ActiveDownloads";
    public static final String DEFAULT_DOWNLOAD_LOCATION = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getPath();

    public static final String ARTWORK_FOLDER = "artwork";
    public static final String AVATARS_FOLDER = "avatars";

    public static String getFullArtworkFolder(Context c) {
        return c.getExternalFilesDir(null).getAbsolutePath()
                + File.separator + ARTWORK_FOLDER;
    }

    public static String getFullArtworkFolder(Context c, long id) {
        return c.getExternalFilesDir(null).getAbsolutePath()
                + File.separator + ARTWORK_FOLDER
                + File.separator + id + ".jpg";
    }

    public static String getFullAvatarsFolder(Context c) {
        return c.getExternalFilesDir(null).getAbsolutePath()
                + File.separator + AVATARS_FOLDER;
    }

    public static String getFullAvatarsFolder(Context c, long id) {
        return c.getExternalFilesDir(null).getAbsolutePath()
                + File.separator + AVATARS_FOLDER
                + File.separator + id + ".jpg";
    }

    public static class DownloadItem {
        private TrackAnalizer trackAnalizer;
        private long downloadId;
        private long trackId;
        private int progress;
        private String icon;
        private String date;
        private String url;
        private byte[] image;

        public DownloadItem() { }

        public DownloadItem(long downloadId, String icon, int progress, TrackAnalizer trackAnalizer, long trackId, String url, byte[] image) {
            this.downloadId = downloadId;
            this.icon = icon;
            this.progress = progress;
            this.trackAnalizer = trackAnalizer;
            this.trackId = trackId;
            this.url = url;
            this.image = image;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public long getDownloadId() {
            return downloadId;
        }

        public void setDownloadId(long downloadId) {
            this.downloadId = downloadId;
        }

        public String getIcon() {
            return icon;
        }

        public void setIcon(String icon) {
            this.icon = icon;
        }

        public int getProgress() {
            return progress;
        }

        public void setProgress(int progress) {
            this.progress = progress;
        }

        public long getTrackId() {
            return trackId;
        }

        public void setTrackId(long trackId) {
            this.trackId = trackId;
        }

        public TrackAnalizer getTrackAnalizer() {
            return trackAnalizer;
        }

        public void setTrackAnalizer(TrackAnalizer trackAnalizer) {
            this.trackAnalizer = trackAnalizer;
        }

        public byte[] getImage() {
            return image;
        }
    }
}
