package com.max.lucas.sync;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONObject;

/**
 * Created by imax5 on 20-Jun-16.
 */
public class DownloadAndSetArtwork extends AsyncTask<Void, Void, String> {

    private String mUrl;
    private long trackId;
    private ImageView avatar;
    private Context c;

    public DownloadAndSetArtwork(Context context, String url, long trackId, String title, ImageView ivAvatar) {
        mUrl = url;
        this.trackId = trackId;
        avatar = ivAvatar;
        c = context;
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

            final String imageUrl = json.getString("artwork_url");
            if (imageUrl.equals("null")) return;
            new LoadAvatarTask(imageUrl, avatar).execute();
            Accessor.downloadTrackArtwork(c, String.valueOf(trackId), imageUrl);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(c, e.toString(), Toast.LENGTH_LONG).show();
        }
    }
}