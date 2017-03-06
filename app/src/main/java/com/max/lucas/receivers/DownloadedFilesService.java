package com.max.lucas.receivers;

import android.Manifest;
import android.app.DownloadManager;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.provider.DocumentFile;
import android.util.Log;
import android.widget.Toast;

import com.max.lucas.ActiveDownloads;
import com.max.lucas.R;
import com.max.lucas.TrackAnalizer;
import com.max.lucas.data.DbContract;
import com.max.lucas.frangments.Settings;
import com.max.lucas.ActiveDownloads.DownloadItem;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.NotSupportedException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DownloadedFilesService extends Service {

    private static final String TAG = "lucas.DownloadCompleted";

    public static final String ACTION_KEY = "action_extra";
    public static final int NO_ACTION = 0;
    public static final int DOWNLOAD_COMPLETE_ACTION = 1;
    public static final int NEW_DOWNLOAD_ACTION = 2;
    public static final String NEW_DOWNLOAD_URL_KEY = "download_url";
    public static final String NEW_DOWNLOAD_FILENAME_KEY = "filename";
    public static final String NEW_DOWNLOAD_TRACKID_KEY = "trackid";
    public static final String NEW_DOWNLOAD_TRACK_ANALIZER_TITLE_KEY = "track_analizer_title";
    public static final String NEW_DOWNLOAD_TRACK_ANALIZER_ARTIST_KEY = "track_analizer_artist";
    public static final String NEW_DOWNLOAD_TRACK_ANALIZER_ALBUM_KEY = "track_analizer_album";
    public static final String NEW_DOWNLOAD_TRACK_URL_KEY = "track_url";
    public static final String NEW_DOWNLOAD_TRACK_IMAGE_KEY = "image";
    public static final int CLEAR_LISTS_ACTION = 3;

    private final ConcurrentLinkedQueue<DownloadItem> activeDownloads = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<DownloadItem> completedDownloads = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<DownloadManager.Request> requestQueue = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<DownloadItem> requestQueueDownloadItems = new ConcurrentLinkedQueue<>();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public synchronized int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            int actionId = intent.getIntExtra(ACTION_KEY, NO_ACTION);
            switch (actionId) {
                case NO_ACTION:
                    break;
                case DOWNLOAD_COMPLETE_ACTION:
                    completedDownloadAction(intent);
                    break;
                case NEW_DOWNLOAD_ACTION:
                    newDownloadAction(intent);
                    break;
                case CLEAR_LISTS_ACTION:
                    clearListsAction();
                    break;
            }
        }

        return Service.START_NOT_STICKY;
    }

    private boolean completedDownloadAction(Intent intent) {
        // Check for runtime permissions
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck == PackageManager.PERMISSION_DENIED) return true;

        // Get downloaded item data
        long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0L);
        try {
            // Mark as completed
            DownloadItem item = downloadCompleted(this, id);

            // Find path of downloaded file
            String downloadPath = getDownloadedFilePath(this, id);
            if (item == null || downloadPath == null) return true;
            String[] arr = downloadPath.split(File.separator);
            String filename = java.net.URLDecoder.decode(arr[arr.length - 1], "UTF-8");

            // Get trackAnalizer data and future storage location)
            TrackAnalizer ta = item.getTrackAnalizer();
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            String storageLocation = sharedPreferences.getString(getString(R.string.pref_storage_location_key), ActiveDownloads.DEFAULT_DOWNLOAD_LOCATION);

            // Set up Mp3File w/ tags
            File downloadedFile = new File(getExternalFilesDir(null), filename);
            Mp3File mp3File = new Mp3File(downloadedFile);
            mp3File.removeCustomTag();
            mp3File.removeId3v1Tag();
            mp3File.removeId3v2Tag();
            mp3File.setId3v2Tag(ta.getTags(item.getImage()));

            // Copy file w/ tags to storage location.
            // If fails, saves to default location.
            try {
                documentFileSave(this, downloadedFile, mp3File, ta, storageLocation);
            } catch (Exception e) {
                mp3File.save(ActiveDownloads.DEFAULT_DOWNLOAD_LOCATION + File.separator + ta.toString() + ".mp3");
                if (!storageLocation.equals(ActiveDownloads.DEFAULT_DOWNLOAD_LOCATION)) {
                    Toast.makeText(this, "Couldn't store file in requested location. Reverting to default download location", Toast.LENGTH_SHORT).show();
                    Settings.setStorageLocationToDefault(this);
                }
            }

            // Deletes original file, saves the id and scans for the new one
            downloadedFile.delete();

            String[] str = {storageLocation + File.separator + filename};
            MediaScannerConnection.scanFile(this, str, null, null);
            Runtime.getRuntime().exec("am broadcast -a android.intent.action.MEDIA_MOUNTED -d file://" + str[0]);

            ContentValues cv = new ContentValues(1);
            cv.put(DbContract.TrackEntry.COLUMN_DOWNLOADED, 1);
            String[] whereValues = {String.valueOf(item.getTrackId())};
            getContentResolver().update(
                    DbContract.TrackEntry.CONTENT_URI,
                    cv,
                    DbContract.TrackEntry.COLUMN_ID + " = ?",
                    whereValues
            );
            Log.e(TAG, "Finished processing completed download broadcast of " + ta.toString());

            if (activeDownloads.size() > 0 &&
                    requestQueue.size() > 0 &&
                    requestQueueDownloadItems.size() > 0)
                stopSelf();

        } catch (Exception e) {
            Log.e(TAG, "Could not process completed download broadcast", e);
        }
        return false;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "destroyed");
        super.onDestroy();
    }

    private String getDownloadedFilePath(Context context, long id) throws Exception {
        // Search for the item in the download manager
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(id);
        Cursor cursor = downloadManager.query(query);
        if (!cursor.moveToFirst())
            return null; //throw new Exception("cursor.moveToFirst() failed");
        int statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
        if (DownloadManager.STATUS_SUCCESSFUL != cursor.getInt(statusIndex))
            return null; //throw new Exception("download was not successful");
        int uriIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI);

        // Found String!
        return cursor.getString(uriIndex);
    }

    private void documentFileSave(Context context, File file, Mp3File mp3File, TrackAnalizer ta, String storageLocation) throws IOException, NotSupportedException {
        DocumentFile f = DocumentFile.fromTreeUri(context, Uri.parse(storageLocation)).createFile("audio/mp3", ta.toString() + ".mp3");

        //Convert file into Converted directory
        new File(file.getParent() + "/converted/").mkdirs();
        File converted = new File(file.getParent() + "/converted/" + file.getName());
        mp3File.save(converted.getAbsolutePath());

        OutputStream out = context.getContentResolver().openOutputStream(f.getUri());
        if (out != null) {
            out.write(fileToByteArray(converted));
            out.close();
        }

        converted.delete();
    }

    private byte[] fileToByteArray(File file) throws IOException {
        ByteArrayOutputStream ous = null;
        InputStream ios = null;
        try {
            byte[] buffer = new byte[4096];
            ous = new ByteArrayOutputStream();
            ios = new FileInputStream(file);
            int read;
            while ((read = ios.read(buffer)) != -1)
                ous.write(buffer, 0, read);
        } finally {
            if (ous != null) ous.close();
            if (ios != null) ios.close();
        }
        return ous.toByteArray();
    }

    public static void newDownload(Context c, String downloadUrl, String filename, long trackId, TrackAnalizer trackAnalizer, String url, byte[] image) {
        Intent i = new Intent(c, DownloadedFilesService.class);
        i.putExtra(ACTION_KEY, NEW_DOWNLOAD_ACTION);
        i.putExtra(NEW_DOWNLOAD_URL_KEY, downloadUrl);
        i.putExtra(NEW_DOWNLOAD_FILENAME_KEY, filename);
        i.putExtra(NEW_DOWNLOAD_TRACKID_KEY, trackId);
        i.putExtra(NEW_DOWNLOAD_TRACK_ANALIZER_TITLE_KEY, trackAnalizer.getTitle());
        i.putExtra(NEW_DOWNLOAD_TRACK_ANALIZER_ARTIST_KEY, trackAnalizer.getArtist());
        i.putExtra(NEW_DOWNLOAD_TRACK_ANALIZER_ALBUM_KEY, trackAnalizer.getAlbum());
        i.putExtra(NEW_DOWNLOAD_TRACK_URL_KEY, url);
        i.putExtra(NEW_DOWNLOAD_TRACK_IMAGE_KEY, image);
        c.startService(i);
    }

    public static void clearLists(Context c) {
        Intent i = new Intent(c, DownloadedFilesService.class);
        i.putExtra(ACTION_KEY, CLEAR_LISTS_ACTION);
        c.startService(i);
    }

    private void newDownloadAction(Intent intent) {
        String downloadUrl = intent.getStringExtra(NEW_DOWNLOAD_URL_KEY);
        String filename = intent.getStringExtra(NEW_DOWNLOAD_FILENAME_KEY) + ".mp3";
        long trackId = intent.getLongExtra(NEW_DOWNLOAD_TRACKID_KEY, 0);
        TrackAnalizer trackAnalizer = new TrackAnalizer(
                intent.getStringExtra(NEW_DOWNLOAD_TRACK_ANALIZER_TITLE_KEY),
                intent.getStringExtra(NEW_DOWNLOAD_TRACK_ANALIZER_ARTIST_KEY),
                intent.getStringExtra(NEW_DOWNLOAD_TRACK_ANALIZER_ALBUM_KEY)
        );
        String url = intent.getStringExtra(NEW_DOWNLOAD_TRACK_URL_KEY);
        byte[] image = intent.getByteArrayExtra(NEW_DOWNLOAD_TRACK_IMAGE_KEY);

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(downloadUrl));
        request.setDestinationInExternalFilesDir(this, null, filename);

        if (isIdAlreadyDownloading(trackId)) return;

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean onlyWIFI = sharedPreferences.getBoolean(getString(R.string.pref_only_wifi_key),
                Boolean.valueOf(getString(R.string.pref_only_wifi_default)));

        request.setVisibleInDownloadsUi(true);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
        request.setTitle(trackAnalizer.toString());
        if (onlyWIFI)
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);

        Log.i(TAG, "New download request received for " + trackAnalizer.getTitle());

        requestQueue.add(request);
        requestQueueDownloadItems.add(new DownloadItem(-1, "", 0, trackAnalizer, trackId, url, image));
        attemptToStartANewDownload();
    }

    private boolean isIdAlreadyDownloading(long trackid) {
        // Search through all three lists for the trackID and return whether it exists in one of them
        Iterator<DownloadItem> it = requestQueueDownloadItems.iterator();
        for (int i = 0; i < requestQueueDownloadItems.size(); i++)
            if (it.next().getTrackId() == trackid) return true;

        it = activeDownloads.iterator();
        for (int i = 0; i < activeDownloads.size(); i++)
            if (it.next().getTrackId() == trackid) return true;

        it = completedDownloads.iterator();
        for (int i = 0; i < completedDownloads.size(); i++)
            if (it.next().getTrackId() == trackid) return true;

        return false;
    }

    private synchronized void attemptToStartANewDownload() {
        String maxConcurentString = Settings.getStringPreference(this, R.string.pref_download_limit_key);
        int maxConcurent = Integer.valueOf(maxConcurentString.equals("") ? getString(R.string.pref_download_limit_default) : maxConcurentString);
        DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        if (activeDownloads.size() < maxConcurent && requestQueue.size() > 0) {
            long id = downloadManager.enqueue(requestQueue.poll());
            DownloadItem downloadItem = requestQueueDownloadItems.poll();
            downloadItem.setDownloadId(id);
            activeDownloads.add(downloadItem);
            Log.i(TAG, "Download started: " + downloadItem.getTrackAnalizer().getTitle());
            attemptToStartANewDownload();
        }
    }

    public DownloadItem downloadCompleted(Context c, long id) {
        for (DownloadItem item : activeDownloads)
            if (item.getDownloadId() == id) {
                activeDownloads.remove(item);
                completedDownloads.add(item);
                Log.i(TAG, "Download completed: " + item.getTrackAnalizer().getTitle());

                attemptToStartANewDownload();
                return item;
            }
        return null;
    }

    private void clearListsAction() {
        activeDownloads.clear();
        completedDownloads.clear();
        requestQueue.clear();
        requestQueueDownloadItems.clear();
    }
}