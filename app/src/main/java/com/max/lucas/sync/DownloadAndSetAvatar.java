package com.max.lucas.sync;

import android.app.DownloadManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.max.lucas.ActiveDownloads;
import com.max.lucas.R;

import org.json.JSONObject;

import java.io.File;

/**
 * Created by imax5 on 20-Jun-16.
 */
public class DownloadAndSetAvatar extends AsyncTask<Void, Void, String> {
    private String  mUrl;
    private long userId;
    private ImageView avatar;
    private Context c;
    private String mDownloadDesc;

    public DownloadAndSetAvatar(Context context, String url, long userId, ImageView ivAvatar, String downloadDesc) {
        mUrl = url;
        this.userId = userId;
        avatar = ivAvatar;
        c = context;
        mDownloadDesc = downloadDesc;
    }

    @Override
    protected String doInBackground(Void... params) {
        return Accessor.getJSON(mUrl);
    }

    @Override
    protected void onPostExecute(String strings) {
        super.onPostExecute(strings);
        try {
            JSONObject json = new JSONObject(strings);
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(c);
            boolean onlyWIFI = sharedPreferences.getBoolean(c.getString(R.string.pref_only_wifi_key), Boolean.valueOf(c.getString(R.string.pref_only_wifi_default)));

            final String imageUrl = json.getString("avatar_url");
            new LoadAvatarTask(imageUrl, avatar).execute();

            // Download the avatar for offline usage
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(imageUrl));
            request.setVisibleInDownloadsUi(true);
            (new File(ActiveDownloads.getFullAvatarsFolder(c))).mkdirs();
            request.setDestinationInExternalFilesDir(c, ActiveDownloads.AVATARS_FOLDER, userId + ".jpg");
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
            request.setDescription(mDownloadDesc);
            request.setAllowedNetworkTypes(onlyWIFI ? DownloadManager.Request.NETWORK_WIFI :
                    (DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI));
            ((DownloadManager) c.getSystemService(Context.DOWNLOAD_SERVICE)).enqueue(request);

            // Code for loading a missing user's avatar from the downloaded image (after downloading of course).
            // Slower than current method for src-ing from the download url and downloading in the background.
                /*c.registerReceiver(new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0L);
                        try {
                            if (getDescription(id).equals(getImageDownloadDesc(userId))) {
                                bindUrlToImageViewSrc(avatar, new File(getImageUrl(context, userId)), true);
                                c.unregisterReceiver(this);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    private String getDescription(long id) throws Exception {
                        // Search for the item in the download manager
                        DownloadManager downloadManager = (DownloadManager) c.getSystemService(Context.DOWNLOAD_SERVICE);
                        DownloadManager.Query query = new DownloadManager.Query();
                        query.setFilterById(id);
                        Cursor cursor = downloadManager.query(query);
                        if (!cursor.moveToFirst())
                            throw new Exception("cursor.moveToFirst() failed");
                        int statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                        if (DownloadManager.STATUS_SUCCESSFUL != cursor.getInt(statusIndex))
                            throw new Exception("download was not successful");
                        int descIndex = cursor.getColumnIndex(DownloadManager.COLUMN_DESCRIPTION);

                        // Found String!
                        String toReturn = cursor.getString(descIndex);
                        cursor.close();
                        return toReturn;
                    }
                }, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)); */
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(c, e.toString(), Toast.LENGTH_LONG).show();
        }
    }
}
