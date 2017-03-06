package com.max.lucas.adapters.act_playlists;

import android.app.Activity;
import android.database.Cursor;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.max.lucas.R;
import com.max.lucas.data.DbContract;
import com.max.lucas.models.Playlist;
import com.max.lucas.sync.LoadAvatarTask;
import com.max.lucas.sync.SyncTask;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by imax5 on 10-Dec-16.
 */

public class PlaylistsRecyclerAdapter extends RecyclerView.Adapter<ViewHolder> {

    private Activity context;
    private Playlist[] dataset;
    private Long[] downloadedTracks;
    private long userId;

    public PlaylistsRecyclerAdapter(Activity c, ArrayList<Playlist> ds, long userId) {
        this.context = c;
        dataset = ds.toArray(new Playlist[ds.size()]);
        this.userId = userId;
        initDownloadedTracksArray();
    }

    private void initDownloadedTracksArray() {
        ArrayList<Long> dtracks = new ArrayList<>();
        String[] proj = { DbContract.TrackEntry.COLUMN_ID};
        Cursor c = context.getContentResolver().query(DbContract.TrackEntry.CONTENT_URI, proj, null, null, null);

        final int plidColumnIndex = c.getColumnIndex(DbContract.TrackEntry.COLUMN_ID);
        c.moveToFirst();
        while (c.moveToNext()) {
            dtracks.add(c.getLong(plidColumnIndex));
        }
        c.close();

        Arrays.sort(downloadedTracks = dtracks.toArray(new Long[dtracks.size()]));
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_playlist, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.tvUsername.setText(dataset[position].getUsername());
        holder.tvTitle.setText(dataset[position].getNaame());
        new LoadAvatarTask(dataset[position].getArtworkUrl(), holder.ivCover).execute();
        if (dataset[position].getTrackCount() == 0) {
            holder.ivDownload.setImageDrawable(ResourcesCompat.getDrawable(context .getResources(), R.drawable.ic_close_black_24dp, null));
            holder.tvTrackCount.setText("No tracks available :(");
        } else {
            holder.tvTrackCount.setText(dataset[position].getTrackCount() + " tracks");
            if (areAllTracksDownloaded(dataset[position].getTracks())) {
                holder.ivDownload.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_check_black_24dp, null));
                holder.ivDownload.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        holder.ivDownload.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_file_download_black_24dp, null));
                        holder.ivDownload.setOnClickListener(getPlaylistDownloadOnClickListener(dataset[holder.getAdapterPosition()].getTracks(), userId, holder.ivDownload));
                    }
                });
            } else {
                holder.ivDownload.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_file_download_black_24dp, null));
                holder.ivDownload.setOnClickListener(getPlaylistDownloadOnClickListener(dataset[position].getTracks(), userId, holder.ivDownload));
            }
        }
    }

    private View.OnClickListener getPlaylistDownloadOnClickListener(final JSONArray playlistid, final long userid, final ImageView iv) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SyncTask(context, null, userid, playlistid).execute();
                iv.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_check_black_24dp, null));
                iv.setOnClickListener(null);
            }
        };
    }

    private boolean areAllTracksDownloaded(JSONArray arr) {
        try {
            for (int i = 0; i < arr.length(); i++)
                if (!binarySearch(downloadedTracks, arr.getJSONObject(i).getLong("id")))
                    return false;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return true;
    }

    private boolean binarySearch(Long[] arr, Long itm) {
        int i = 0, j = arr.length - 1, k = 0;
        while (i <= j && !arr[k].equals(itm)) {
            k = (i + j) / 2;
            if (arr[k] > itm) j = k - 1;
            else if (arr[k] < itm) i = k + 1;
        }
        return arr[k].equals(itm);
    }

    @Override
    public int getItemCount() {
        return dataset.length;
    }
}
