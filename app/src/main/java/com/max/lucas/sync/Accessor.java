package com.max.lucas.sync;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.max.lucas.ActiveDownloads;
import com.max.lucas.R;
import com.max.lucas.TrackAnalizer;
import com.max.lucas.frangments.Settings;
import com.max.lucas.receivers.DownloadedFilesService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by Max on 27-Oct-15.
 * Just a big fucking rul-string builder
 */
public class Accessor {

    public static final String cid = "&client_id=" + Keys.soundcloud.clientId;

    static final String api_base = "https://api.soundcloud.com/";
    public static final String r = "resolve";
    public static final String u = "users/";

    static final Uri uriBuilder = Uri.parse(api_base).buildUpon()
            .appendQueryParameter("client_id", Keys.soundcloud.clientId).build();

    public static Uri resolveUrl(String stringURL) {
        //return api_base + r + "?url=" + stringURL + cid;
        return uriBuilder.buildUpon()
                .appendPath("resolve")
                .appendQueryParameter("url", stringURL).build();
    }

    static Uri getUserFavorites(String user, int offset, int limit) {
        //return api_base + u + user + "/favorites?offset=" + offset + "&limit=" + limit + cid;
        return uriBuilder.buildUpon()
                .appendPath("users")
                .appendPath(user)
                .appendPath("favorites")
                .appendQueryParameter("offset", String.valueOf(offset))
                .appendQueryParameter("limit", String.valueOf(limit))
                .build();
    }

    public static Uri getUserLikedPlaylists(String user, int offset, int limit) {
        return uriBuilder.buildUpon()
                .appendPath("e1")
                .appendPath("users")
                .appendPath(user)
                .appendPath("playlist_likes")
                .appendQueryParameter("offset", String.valueOf(offset))
                .appendQueryParameter("limit", String.valueOf(limit))
                .build();
    }

    public static Uri getUserPlaylists(String user, int offset, int limit) {
        return uriBuilder.buildUpon()
                .appendPath("users")
                .appendPath(user)
                .appendPath("playlists")
                .appendQueryParameter("offset", String.valueOf(offset))
                .appendQueryParameter("limit", String.valueOf(limit))
                .build();
    }

    public static Uri getPlaylist(long id) {
        return uriBuilder.buildUpon()
                .appendPath("playlists")
                .appendPath(String.valueOf(id))
                .build();
    }

    public static Uri getTrackDetails(long id) {
        return uriBuilder.buildUpon()
                .appendPath("tracks")
                .appendPath(String.valueOf(id)).build();
    }

    public static Uri getUserDetails(String id) {
        //return api_base + u + id + "?" + cid;
        return uriBuilder.buildUpon()
                .appendPath("users")
                .appendPath(id).build();
    }

    static int getNumberOfUserFavourites (String id) {
        try {
            JSONObject obj = new JSONObject(getJSON(getUserDetails(id).toString()));
            return obj.getInt("public_favorites_count");
        } catch (JSONException e) {
            Log.e("SoundcloudAccessor", "Couldn't get user " + id + " favourites");
            e.printStackTrace();
        }
        return 0;
    }

    public static String getJSON(String uri) {
        HttpURLConnection c = null;
        try {
            URL u = new URL(uri);
            c = (HttpURLConnection) u.openConnection();
            c.connect();
            int status = c.getResponseCode();
            switch (status) {
                case 200:
                case 201:
                    BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line+"\n");
                    }
                    br.close();
                    return sb.toString();
            }

        } catch (Exception ex) {
            return ex.toString();
        } finally {
            if (c != null) {
                try {
                    c.disconnect();
                } catch (Exception ex) {
                    //disconnect error
                }
            }
        }
        return null;
    }

    public static boolean downloadUrl (Context context, String url, TrackAnalizer trackAnalizer, long trackId, String soundcloudUrl, String artworkUrl, byte[] image) {
        try {
            if (url.contains("https://api.soundcloud.com/tracks/"))
                url += "?" + Accessor.cid;

            URLRedirectTask testAsyncTask = new URLRedirectTask(context, url, trackAnalizer, trackId, soundcloudUrl, artworkUrl, image);
            testAsyncTask.execute();

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static String downloadURL;

    static class URLRedirectTask extends AsyncTask<Void, Void, Void> {
        private Context mContext;
        private String mUrl;
        private TrackAnalizer mTrackAnalizer;
        private long mTrackId;
        private String mSoundcloudUrl;
        private String mArtworkUrl;
        private byte[] image;

        URLRedirectTask(Context context, String url, TrackAnalizer trackAnalizer, long trackId, String soundcloudUrl, String artworkUrl, byte[] image) {
            mContext = context;
            mUrl = url;
            mTrackAnalizer = trackAnalizer;
            mTrackId = trackId;
            mSoundcloudUrl = soundcloudUrl;
            mArtworkUrl = artworkUrl;
            this.image = image;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                if (mUrl.contains("https://api.soundcloud.com/tracks/")) {
                    URLConnection con = new URL(mUrl).openConnection();
                    con.connect();
                    downloadURL = con.getURL().toString();
                } else {
                    downloadURL = mUrl;
                }
                String fileName = mTrackAnalizer.toString();
                DownloadedFilesService.newDownload(mContext, downloadURL, fileName, mTrackId, mTrackAnalizer, mSoundcloudUrl, image);
                downloadTrackArtwork(mContext, String.valueOf(mTrackId), mArtworkUrl);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    static void downloadTrackArtwork(Context c, String trackId, String artworkUrl) throws JSONException {
        if (new File(ActiveDownloads.getFullArtworkFolder(c, Long.valueOf(trackId))).exists()) return;

        (new File(ActiveDownloads.getFullArtworkFolder(c))).mkdirs();
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(artworkUrl));
        request.setDestinationInExternalFilesDir(c, ActiveDownloads.ARTWORK_FOLDER, trackId + ".jpg");
        request.setVisibleInDownloadsUi(true);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
        request.setTitle("Track " + trackId + " artwork");
        request.setDescription("Track " + trackId + " artwork download");
        boolean onlyWifi = Settings.getBooleanPreference(c, R.string.pref_only_wifi_key);
        if (onlyWifi)
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
        ((DownloadManager) c.getSystemService(Context.DOWNLOAD_SERVICE)).enqueue(request);
    }
}