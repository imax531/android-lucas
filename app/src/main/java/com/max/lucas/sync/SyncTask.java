package com.max.lucas.sync;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.max.lucas.R;
import com.max.lucas.data.DbContract;
import com.max.lucas.frangments.Settings;
import com.max.lucas.helpers.DatabaseAbstractor;
import com.max.lucas.TrackAnalizer;
import com.max.lucas.receivers.DownloadedFilesService;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created by imax5 on 22-Apr-16.
 */
public class SyncTask extends AsyncTask<Void, Void, Void> {

    public static final String TAG = "com.max.lucas.sync.SyTa";

    // Abstraction
    private Activity context;
    private SyncListerner listerner;

    // Progress
    private int tracksDone = 0, totalTracks = 0;
    private int usersDone = 0, totalUsers = 0;
    private int newTracks = 0;
    private long[] userIds = null;
    private JSONArray playlistId = null;
    private Map<Long, Integer> userLimit;
    private final ArrayList<Long> downloadedTracks = new ArrayList<>();
    private final ArrayList<Long> redundencyList = new ArrayList<>();

    private boolean isActive = true;
    private boolean progressing = true;

    // Sync
    private static final int LIKES_ITERATOR_OFFSET = 100;
    private static final int PLAYLIST_ITERATOR_OFFSET = 10;

    public SyncTask(Activity context, SyncListerner listerner) {
        this.context = context;
        this.listerner = listerner;
    }

    public SyncTask(Activity context, SyncListerner listerner, long[] userIds) {
        this(context, listerner);
        this.userIds = userIds;
    }

    public SyncTask(Activity context, SyncListerner listerner, long userId, JSONArray playlistId) {
        this(context, listerner);
        this.userIds = new long[] {userId};
        this.playlistId = playlistId;
    }

    @Override
    protected void onPreExecute() {
        if (listerner != null)
            listerner.preSync();
    }

    @Override
    protected Void doInBackground(Void... params) {
        downloadedTracks.addAll(DatabaseAbstractor.getDownloadedTracksIds(context));
        if (playlistId == null) {

            if (userIds == null)
                userIds = DatabaseAbstractor.getUsersIds(context);

            totalUsers = userIds.length;
            userLimit = new android.support.v4.util.ArrayMap<>(userIds.length);

            String userLimitString = Settings.getStringPreference(context, R.string.pref_track_sync_limit_key);
            final int userLimitInt = userLimitString.equals("") ? Integer.MAX_VALUE : Integer.valueOf(userLimitString);

            for (int j = 0; j < userIds.length; j++) {
                long id = userIds[j];
                totalTracks += Math.min(userLimitInt, Accessor.getNumberOfUserFavourites(String.valueOf(id)));
                if (listerner != null) // capacity update
                    context.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            listerner.trackCompleted(tracksDone, totalTracks, newTracks);
                        }
                    });
            }

            for (int j = 0; j < userIds.length; j++) {
                long id = userIds[j];
                Log.i(TAG, "Started user id " + id + " downloads");
                syncUser(id, userLimitInt);
                usersDone++;
                if (totalUsers == usersDone) {
                    if (listerner != null)
                        context.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                listerner.trackCompleted(totalTracks, totalTracks, newTracks);
                            }
                        });

                    progressing = false;
                }
            }
        } else {
            userLimit = new android.support.v4.util.ArrayMap<>(1);
            userLimit.put(userIds[0], Integer.MAX_VALUE);
            handleFavouritesJson(userIds[0], playlistId, null);
        }
        return null;
    }

    private void syncUser(long userId, int userLimitInt) {
        userLimit.put(userId, Integer.MAX_VALUE);
        boolean playlistFlag = Settings.getUserPreferences(context, userId).getPlaylistDownload();
        // Liked playlists
        for (int i = 0; playlistFlag && isActive; i += PLAYLIST_ITERATOR_OFFSET) {
            String _uri = Accessor.getUserLikedPlaylists(String.valueOf(userId), i, PLAYLIST_ITERATOR_OFFSET).toString();
            String jsonResponse = Accessor.getJSON(_uri);
            playlistFlag = handlePlaylistsJson(userId, jsonResponse);
        }

        // Tracks
        userLimit.put(userId, userLimitInt);
        int numOfFavourites = Accessor.getNumberOfUserFavourites(String.valueOf(userId));
        for (int i = 0; i < numOfFavourites && isActive && i < userLimit.get(userId); i += LIKES_ITERATOR_OFFSET) {
            String _uri = Accessor.getUserFavorites(String.valueOf(userId), i, LIKES_ITERATOR_OFFSET).toString();
            String jsonResponse = Accessor.getJSON(_uri);
            handleFavouritesJson(userId, jsonResponse);
        }
    }

    @Override
    protected void onPostExecute(Void v) { }

    private boolean handlePlaylistsJson(final long userId, String strings) {
        try {
            if (!isActive) return false;
            if (strings == null) {
                Log.w(TAG, "No tracks, strings null");
                return false;
            } else {
                JSONArray arr = new JSONArray(strings);
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject playlist = arr.getJSONObject(i).getJSONObject("playlist");
                    handleFavouritesJson(userId, playlist.getString("tracks"), playlist.getString("title"));
                }
                return true;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    private boolean handleFavouritesJson(final long userId, String strings) {
        return handleFavouritesJson(userId, strings, null);
    }

    private synchronized boolean handleFavouritesJson(final long userId, String strings, String album) {
        try {
            return handleFavouritesJson(userId, strings == null ? null : new JSONArray(strings), album);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    private synchronized boolean handleFavouritesJson(final long userId, JSONArray arr, String album) {
        try {
            if (!isActive) return false;

            if (arr == null || arr.length() == 0) return false;

            ArrayList<ContentValues> cvLikes = new ArrayList<>();
            ArrayList<ContentValues> cvTracks = new ArrayList<>();
            for (int i = 0; i < arr.length() &&
                    isActive &&
                    userLimit.get(userId) > 0; i++) {
                JSONObject obj = arr.getJSONObject(i);
                long trackId = obj.getLong("id");

                boolean alreadySeen = listSearch(redundencyList, trackId);
                if (alreadySeen)
                    continue;
                redundencyList.add(trackId);

                userLimit.put(userId, userLimit.get(userId) - 1);

                boolean previouslyProccessed;
                synchronized (downloadedTracks) {
                    previouslyProccessed = listSearch(downloadedTracks, trackId);
                    if (!previouslyProccessed)
                        downloadedTracks.add(trackId);
                }

                if (!previouslyProccessed) {
                    boolean streamable = obj.getBoolean("streamable");
                    ContentValues cvNewTrack = new ContentValues(streamable ? 8 : 7);

                    TrackAnalizer trackAnalizer = new TrackAnalizer(obj.getString("title"), obj.getJSONObject("user").getString("username"));
                    if (album != null) trackAnalizer.album = album;
                    addTrackToContentValues(cvNewTrack, obj, trackAnalizer);

                    if (streamable) {
                        cvNewTrack.put(DbContract.TrackEntry.COLUMN_STREAM_URL, obj.getString("stream_url"));
                        downloadUrl(obj, trackId, trackAnalizer);
                        newTracks++;
                    }

                    cvTracks.add(cvNewTrack);
                }

                // Even if we have already processed the track, let's keep the likes updated
                ContentValues cvNewLike = new ContentValues(2);
                cvNewLike.put(DbContract.LikeEntry.COLUMN_USER_ID, userId);
                cvNewLike.put(DbContract.LikeEntry.COLUMN_TRACK_ID, trackId);
                cvLikes.add(cvNewLike);


                if (listerner != null) {
                    final int fTracksDone = tracksDone++;
                    context.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            listerner.trackCompleted(fTracksDone, totalTracks, newTracks);
                        }
                    });
                }
            }
            context.getContentResolver().bulkInsert(DbContract.TrackEntry.CONTENT_URI, cvTracks.toArray(new ContentValues[cvTracks.size()]));
            context.getContentResolver().bulkInsert(DbContract.LikeEntry.CONTENT_URI, cvLikes.toArray(new ContentValues[cvLikes.size()]));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (listerner != null) {
            context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    listerner.userCompleted(userId);
                }
            });
        }

        return true;
    }

    private void addTrackToContentValues(ContentValues cvTrack, JSONObject obj, TrackAnalizer trackAnalizer) throws JSONException {
        cvTrack.put(DbContract.TrackEntry.COLUMN_ID, obj.getLong("id"));
        cvTrack.put(DbContract.TrackEntry.COLUMN_API_UPLOADER_USERNAME, obj.getJSONObject("user").getString("username"));
        cvTrack.put(DbContract.TrackEntry.COLUMN_API_TITLE, obj.getString("title"));
        cvTrack.put(DbContract.TrackEntry.COLUMN_URL, obj.getString("permalink_url"));
        cvTrack.put(DbContract.TrackEntry.COLUMN_TITLE, trackAnalizer.title);
        cvTrack.put(DbContract.TrackEntry.COLUMN_ARTIST, trackAnalizer.artist);
        cvTrack.put(DbContract.TrackEntry.COLUMN_ALBUM, trackAnalizer.album);
    }

    private void downloadUrl(JSONObject obj, long trackId, TrackAnalizer trackAnalizer) throws JSONException {
        String streamUrl;
        if (obj.getBoolean("downloadable")) {
            streamUrl = obj.getString("download_url");
        } else {
            streamUrl = obj.getString("stream_url");
        }
        String soundcloudUrl = obj.getString("permalink_url");
        String artworkUrl = obj.getString("artwork_url");
        artworkUrl = artworkUrl.replace("-large.jpg?", "-t500x500.jpg?");

        boolean useImage = Settings.getBooleanPreference(context, R.string.pref_use_track_artwork_key);
        byte[] image = useImage ? getImageFromUrl(artworkUrl) : null;

        boolean success = Accessor.downloadUrl(context, streamUrl, trackAnalizer, trackId, soundcloudUrl, artworkUrl, image);
        if (!success) {
            Toast.makeText(context, "Can't download " + trackAnalizer.toString(), Toast.LENGTH_LONG).show();
            Log.i(TAG, "Download of " + obj.toString() + " failed");
        } else {
            Log.i(TAG, "Download of " + obj.toString() + " succeeded");
        }
    }

    private byte[] getImageFromUrl(String url) {
        try {
            InputStream inputStream = (InputStream) new URL(url).getContent();
            return IOUtils.toByteArray(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private boolean listSearch(ArrayList<Long> arr, long searched) {
        for (int i = 0; i < arr.size(); i++)
            if (arr.get(i) == searched)
                return true;
        return false;
    }

    public interface SyncListerner {
        void preSync();
        void userCompleted(long current);
        void trackCompleted(int current, int amount, int newTracks);
    }

    public void cancelSyncing(Context c) {
        isActive = false;
        DownloadedFilesService.clearLists(c);
        listerner.trackCompleted(totalTracks + 1, totalTracks, newTracks);
    }

    public boolean isActive() {
        return isActive && progressing;
    }

    public void updateListener() {
        listerner.trackCompleted(tracksDone, totalTracks, newTracks);
    }
}