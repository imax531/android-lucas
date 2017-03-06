package com.max.lucas.adapters.frg_home;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.support.v4.util.ArrayMap;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.max.lucas.ActiveDownloads;
import com.max.lucas.R;
import com.max.lucas.data.DbContract;
import com.max.lucas.frangments.Settings;
import com.max.lucas.models.HomeTrack;
import com.max.lucas.sync.Accessor;
import com.max.lucas.sync.DownloadAndSetArtwork;
import com.pkmmte.view.CircularImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;


/**
 * Created by imax5 on 07-May-16.
 */
public class MainHomeTrackRecyclerAdapter extends RecyclerView.Adapter<MainHomeTrackRecyclerAdapter.MyViewHolder> {

    private ArrayList<HomeTrack> mOriginalDataset;
    private ArrayList<HomeTrack> mDataset;
    private Context c;
    private String prevFilter = "";
    private Map<Long, File> avatars = new ArrayMap<Long, File>();

    private interface filterCond {

        boolean test(String str, HomeTrack track);
    }
    private final filterCond defaultFilter = new filterCond() {
        @Override
        public boolean test(String str, HomeTrack track) {
            return track.getTitle().toLowerCase().contains(str.toLowerCase()) ||
                    track.getUploader().toLowerCase().contains(str.toLowerCase());
        }
    };

    private final filterCond pendingFilter = new filterCond() {
        @Override
        public boolean test(String str, HomeTrack track) {
            return !track.isDownloaded();
        }
    };

    private filterCond currentFilter = defaultFilter;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    static class MyViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        ImageView mArtworkImageView;
        ImageView mCompletedImageView;
        TextView mUploaderTextView;
        TextView mTitleTextView;
        LinearLayout mLikesLinearLayout;

        MyViewHolder(View v) {
            super(v);
            this.mArtworkImageView = (ImageView) v.findViewById(R.id.imgArtwork);
            this.mCompletedImageView = (ImageView) v.findViewById(R.id.imgDownloadedFlag);
            this.mUploaderTextView = (TextView) v.findViewById(R.id.tvUploader);
            this.mTitleTextView = (TextView) v.findViewById(R.id.tvTitle);
            this.mLikesLinearLayout = (LinearLayout) v.findViewById(R.id.llUsers);
        }
    }

    public MainHomeTrackRecyclerAdapter(ArrayList<HomeTrack> dataset, Context context) {
        mOriginalDataset = new ArrayList<>(dataset);
        mDataset = mOriginalDataset;
        c = context;
    }

    // This method is used to update the datasets,
    // as well as the recyclerview presenting it, neatly
    public void update(ArrayList<HomeTrack> newDataset) {
        int i = 0, j = 0, k = 0;
        HomeTrack orgTrack, newTrack;
        while (i < mOriginalDataset.size() || j < newDataset.size()) {
            orgTrack = mOriginalDataset.get(i);
            newTrack = newDataset.get(j);
            if (orgTrack.getId() == newTrack.getId()) {
                // update fields
                boolean modified = orgTrack.update(newTrack);
                if (modified && k < mDataset.size() && orgTrack.getId() == mDataset.get(k).getId()) {
                    notifyItemChanged(k);
                    k++;
                }
            } else {
                mOriginalDataset.add(i, newTrack);
                if (currentFilter.test(prevFilter, newTrack)) {
                    mDataset.add(k, newTrack);
                    notifyItemInserted(k);
                }
            }

            if (k < mDataset.size() && orgTrack.getId() == mDataset.get(k).getId())
                k++;
            i++; j++;
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_track_homescreen, parent, false);
        RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        v.setLayoutParams(layoutParams);

        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        HomeTrack dsEntry = mDataset.get(position);
        if (dsEntry.isDownloaded()) {
            holder.mCompletedImageView.setVisibility(View.VISIBLE);
        } else {
            holder.mCompletedImageView.setVisibility(View.INVISIBLE);
            holder.mArtworkImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String searchString = mDataset.get(holder.getAdapterPosition()).getUploader() + " - " + mDataset.get(holder.getAdapterPosition()).getTitle();
                    ClipboardManager clipboard = (ClipboardManager) c.getSystemService(c.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText(searchString, searchString);
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(c, "Title copied to clipboard. Loading Soundcloud...", Toast.LENGTH_LONG).show();
                    try {
                        Intent launchIntent = c.getPackageManager().getLaunchIntentForPackage("com.soundcloud.android");
                        c.startActivity(launchIntent);
                    } catch (Exception ex) {
                        Toast.makeText(c, "Soundcloud app not installed", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        holder.mArtworkImageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                new AlertDialog.Builder(c)
                        .setTitle("Downloaded track")
                        .setMessage("Set track as downloaded?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                ContentValues cv = new ContentValues(1);
                                cv.put(DbContract.TrackEntry.COLUMN_DOWNLOADED, 1);
                                String[] whereValues = { String.valueOf(mDataset.get(holder.getAdapterPosition()).getId()) };
                                c.getContentResolver().update(
                                        DbContract.TrackEntry.CONTENT_URI,
                                        cv,
                                        DbContract.TrackEntry.COLUMN_ID + " = ?",
                                        whereValues
                                );
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
                return true;
            }
        });
        holder.mUploaderTextView.setText(dsEntry.getUploader());
        holder.mTitleTextView.setText(dsEntry.getTitle());

        if (!avatars.containsKey(dsEntry.getId()))
            avatars.put(dsEntry.getId(), new File(ActiveDownloads.getFullArtworkFolder(c, dsEntry.getId())));
        if (avatars.get(dsEntry.getId()).exists()) {
            setCompletedIconColor(holder.mCompletedImageView, avatars.get(dsEntry.getId()));
            holder.mArtworkImageView.setImageURI(Uri.parse(avatars.get(dsEntry.getId()).getAbsolutePath()));
        } else {
            holder.mArtworkImageView.setImageResource(R.drawable.default_artwork);
            holder.mCompletedImageView.setImageResource(R.drawable.ic_check_circle_black_24dp);
            if (!Settings.getBooleanPreference(c, R.string.pref_only_wifi_key) || Settings.isConnectedToWIFI(c)) {
                new DownloadAndSetArtwork(c,
                        Accessor.getTrackDetails(dsEntry.getId()).toString(),
                        dsEntry.getId(),
                        dsEntry.getTitle(),
                        holder.mArtworkImageView).execute();
            }
        }

        holder.mLikesLinearLayout.removeAllViews();
        for (int i = 0; i < dsEntry.getUsers().size(); i++) {
            CircularImageView circleImageView = new CircularImageView(c);
            final int size = c.getResources().getDimensionPixelOffset(R.dimen.home_avatar_size);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
            params.setMargins(0, 2, 5, 0);
            circleImageView.setImageURI(Uri.parse(ActiveDownloads.getFullAvatarsFolder(c, dsEntry.getUsers().get(i))));
            holder.mLikesLinearLayout.addView(circleImageView, params);
        }
    }

    private void setCompletedIconColor(ImageView imgView, File f) {
        Bitmap bitmap = BitmapFactory.decodeFile(f.getAbsolutePath());
        int avgColor = getAverageBitmapColor(bitmap, 0.3, 0.3);

        if (isColorDark(avgColor))
            imgView.setImageResource(R.drawable.ic_check_circle_white_24dp);
        else
            imgView.setImageResource(R.drawable.ic_check_circle_black_24dp);
    }

    private int getAverageBitmapColor(Bitmap bitmap, double width, double height) {
        height = Math.min(height, 1);
        width = Math.min(width, 1);

        int redBucket = 0;
        int greenBucket = 0;
        int blueBucket = 0;
        int pixelCount = 0;

        for (int y = 0; y < bitmap.getHeight() * height; y++) {
            for (int x = 0; x < bitmap.getWidth() * width; x++) {
                int color = bitmap.getPixel(x, y);
                pixelCount++;
                redBucket += Color.red(color);
                greenBucket += Color.green(color);
                blueBucket += Color.blue(color);
                // does alpha matter?
            }
        }

        return Color.rgb(redBucket / pixelCount,
                         greenBucket / pixelCount,
                         blueBucket / pixelCount);
    }

    private boolean isColorDark(int color) {
        double darkness = 1 - (0.299*Color.red(color) + 0.587*Color.green(color) + 0.114*Color.blue(color)) / 255;
        return (darkness >= 0.5);
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }
    public int getCompleteItemCount() { return mOriginalDataset.size(); }

    public void clear() {
        mDataset = new ArrayList<>();
        mOriginalDataset = new ArrayList<>();
        notifyDataSetChanged();
    }

    public void filter(String str) {
        if (str.equals("")) {
            mDataset = new ArrayList<>(mOriginalDataset);
        } else if (str.equals("pending")) {
            mDataset = new ArrayList<>(mOriginalDataset);
            applyFilter(str, pendingFilter);
        } else if (prevFilter.equals(str.substring(0, str.length() - 1))) {
            applyFilter(str, defaultFilter);
        } else {
            applyAddingFilter(str, defaultFilter);
        }
        prevFilter = str;
        notifyDataSetChanged();
    }

    private void applyFilter(String str, filterCond filter) {
        for (int i = 0; i < mDataset.size();) {
            if (filter.test(str, mDataset.get(i))) i++;
            else mDataset.remove(i);
        }
    }

    private void applyAddingFilter(String str, filterCond filter) {
        mDataset.clear();
        for (int i = 0; i < mOriginalDataset.size(); i++) {
            if (filter.test(str, mOriginalDataset.get(i)))
                mDataset.add(mOriginalDataset.get(i));
        }
    }
}