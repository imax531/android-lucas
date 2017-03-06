package com.max.lucas.sync;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by imax5 on 11-May-16.
 */
public class RedirectTask extends AsyncTask<Void, Void, URL> {

    public static final String TAG = "lucas.RedirectTask";

    private Context mContext;
    private String mUrl;

    public RedirectTask(Context context, String url) {
        mContext = context;
        mUrl = url;
    }

    @Override
    protected URL doInBackground(Void... params) {
        InputStream is = null;
        try {
            URLConnection con = new URL(mUrl).openConnection();
            con.connect();
            is = con.getInputStream();
            return con.getURL();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Failed to get the redirected url");
        } finally {
            if (is != null)
                try {
                    is.close();
                } catch (IOException e) {
                    // shouldn't happen
                    e.printStackTrace();
                }
        }
        return null;
    }
}
