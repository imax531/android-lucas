package com.max.lucas.sync;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by imax5 on 22-May-16.
 */
public class LoadAvatarTask extends AsyncTask<Void, Void, Bitmap> {

    private String url;
    private ImageView imageView;

    public LoadAvatarTask(String url, ImageView imageView) {
        this.url = url;
        this.imageView = imageView;
    }

    @Override
    protected Bitmap doInBackground(Void... params) {
        try {
            URL urlConnection = new URL(url);

            HttpURLConnection connection = (HttpURLConnection) urlConnection
                    .openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        super.onPostExecute(result);

        // Animation
        TransitionDrawable td = new TransitionDrawable(new Drawable[]{
                imageView.getDrawable(),
                new BitmapDrawable(result)
        });
        imageView.setImageDrawable(td);
        td.startTransition(400);
    }
}