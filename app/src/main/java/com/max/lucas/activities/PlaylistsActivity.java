package com.max.lucas.activities;

import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.util.AsyncListUtil;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.max.lucas.R;
import com.max.lucas.adapters.act_playlists.PlaylistsRecyclerAdapter;
import com.max.lucas.models.Playlist;
import com.max.lucas.sync.Accessor;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class PlaylistsActivity extends AppCompatActivity {

    public static final String EXTRA_USER_ID = "user_id";
    private TextView tvLoading;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlists);

        tvLoading = (TextView) findViewById(R.id.tvLoading);

        Bundle b = getIntent().getExtras();
        long userId = -1;
        if (b != null)
            userId = b.getLong(EXTRA_USER_ID);

        new FillPlaylistsTask((RecyclerView) findViewById(R.id.rvList), userId).execute();
    }

    class FillPlaylistsTask extends AsyncTask <Void, Void, String> {
        RecyclerView recyclerView;
        long userId;

        FillPlaylistsTask (RecyclerView rv, long id) {
            this.recyclerView = rv;
            this.userId = id;
        }

        @Override
        protected String doInBackground(Void... params) {
            Uri uri1 = Accessor.getUserPlaylists(String.valueOf(userId), 0, 9999);
            Uri uri2 = Accessor.getUserLikedPlaylists(String.valueOf(userId), 0, 9999);
            return "[" + Accessor.getJSON(uri1.toString()) + ", " + Accessor.getJSON(uri2.toString()) + "]";
        }

        @Override
        protected void onPostExecute(String strings) {
            try {
                ArrayList<Playlist> parr = new ArrayList<>();
                JSONArray arrboth = new JSONArray(strings);
                for (int j = 0; j < 2; j++) {
                    JSONArray arr = arrboth.getJSONArray(j);
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject obj = arr.getJSONObject(i);
                        if (j == 1)
                            obj = obj.getJSONObject("playlist");
                        parr.add(new Playlist(
                                obj.getString("title"),
                                obj.getJSONObject("user").getString("username"),
                                obj.getJSONArray("tracks").length(),
                                obj.getString("artwork_url"),
                                obj.getJSONArray("tracks")));
                    }
                }
                recyclerView.setLayoutManager(new LinearLayoutManager(PlaylistsActivity.this));
                recyclerView.setAdapter(new PlaylistsRecyclerAdapter(PlaylistsActivity.this, parr, userId));
                tvLoading.setVisibility(View.GONE);
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(PlaylistsActivity.this, e.toString(), Toast.LENGTH_LONG).show();
            }
        }
    }
}
