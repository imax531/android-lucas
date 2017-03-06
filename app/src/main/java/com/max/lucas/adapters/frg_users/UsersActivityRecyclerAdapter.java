package com.max.lucas.adapters.frg_users;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.max.lucas.ActiveDownloads;
import com.max.lucas.Auxiliary;
import com.max.lucas.R;
import com.max.lucas.activities.Main2Activity;
import com.max.lucas.activities.PlaylistsActivity;
import com.max.lucas.data.DbContract;
import com.max.lucas.frangments.Settings;
import com.max.lucas.frangments.Users;
import com.max.lucas.sync.Accessor;
import com.max.lucas.sync.DownloadAndSetAvatar;
import com.max.lucas.sync.SyncTask;
import com.max.lucas.sync.User;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by imax5 on 07-May-16.
 */
public class UsersActivityRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "adapters.UsersAdapter";
    private ArrayList<User> mDataset, mOriginalDataset;
    private Activity c;
    private String prevFilter = "";
    private int userOpenPrefPosition = -1;
    private Users containingFragment;

    public UsersActivityRecyclerAdapter(Activity context, ArrayList<User> users, Users frag) {
        this.c = context;
        mDataset = users;
        mOriginalDataset = new ArrayList<>(mDataset);
        containingFragment = frag;
    }

    public ArrayList<User> getDataset() {
        return mDataset;
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, final int userPosition) {
        if (userPosition < 0) { // User
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.adapter_soundcloud_user, parent, false);

            final MainViewHolder vh = new MainViewHolder(v);
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (userOpenPrefPosition == vh.getAdapterPosition()) return;

                    if (userOpenPrefPosition > 0) {
                        mDataset.remove(userOpenPrefPosition);
                        notifyItemRemoved(userOpenPrefPosition);
                    }

                    if (userOpenPrefPosition - 1 != vh.getAdapterPosition()) {
                        userOpenPrefPosition = vh.getAdapterPosition() + 1;
                        mDataset.add(userOpenPrefPosition, null);
                        notifyItemInserted(userOpenPrefPosition);
                    } else userOpenPrefPosition = -1;
                }
            });
            return vh;
        } else { // Prefs
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.pref_user, parent, false);

            long userId = mDataset.get(userPosition).getId();
            final PrefViewHolder vh = new PrefViewHolder(v, userId);
            return vh;
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (mDataset.get(position) == null)
            return position - 1;
        return -1;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        if (mDataset.get(position) != null) {
            final User user = mDataset.get(position);
            final long userId = user.getId();
            MainViewHolder vh = (MainViewHolder) holder;
            vh.mUsername.setText(user.getUsername());
            vh.extraUserId = userId;
            vh.extraUsername = user.getUsername();

            final File f = new File(ActiveDownloads.getFullAvatarsFolder(c, userId));
            if (f.exists()) {
                vh.mAvater.setImageURI(Uri.parse(f.getAbsolutePath()));
            } else if (Settings.canUseInternet(c)) {
                vh.mAvater.setImageResource(R.drawable.default_profile);
                new DownloadAndSetAvatar(c,
                        Accessor.getUserDetails(String.valueOf(userId)).toString(),
                        userId,
                        vh.mAvater,
                        getImageDownloadDesc(userId)).execute();
            }
        } else {
            PrefViewHolder vh = (PrefViewHolder) holder;
            final User user = mDataset.get(position - 1);
            setSettingsValues(vh, user);
            setSettingsChangeListeners(vh, user);
        }
    }

    private void deleteUser(RecyclerView.ViewHolder holder) {
        int j = holder.getAdapterPosition();
        if (j == userOpenPrefPosition) {
            j--;
            userOpenPrefPosition = -1;
        }
        final int i = j; // i=index of view-holder, j=index of user to delete
        final User user = mDataset.get(i);

        c.getContentResolver().delete(
                DbContract.UserEntry.CONTENT_URI,
                DbContract.UserEntry.COLUMN_ID + " = ?",
                new String[]{String.valueOf(mDataset.get(i).getId())}
        );
        mDataset.remove(i);
        notifyItemRemoved(i);

        if (mDataset.get(i) == null) {
            mDataset.remove(i);
            notifyItemRemoved(i);
        }

        Snackbar.make(c.findViewById(R.id.fab), "User removed", Snackbar.LENGTH_SHORT).setAction("UNDO", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContentValues cv = new ContentValues(3);
                cv.put(DbContract.UserEntry.COLUMN_ID, user.getId());
                cv.put(DbContract.UserEntry.COLUMN_USERNAME, user.getUsername());
                cv.put(DbContract.UserEntry.COLUMN_PERMALINK_URL, user.getPermalink());
                c.getContentResolver().insert(DbContract.UserEntry.CONTENT_URI, cv);

                mDataset.add(i, user);
                notifyItemInserted(i);
            }
        }).show();

        containingFragment.updateEmptyDatasetMsg();
    }

    private static String getImageDownloadDesc(long userId) {
        return "User " + userId + " avatar download";
    }

    public void filter(String str) {
        if (str.equals(""))
            mDataset = new ArrayList<>(mOriginalDataset);
        else {
            if (!prevFilter.equals(str.substring(0, str.length() - 1)))
                mDataset = new ArrayList<>(mOriginalDataset);
            int i = 0;
            while (i < mDataset.size()) {
                if (mDataset.get(i).getUsername().toLowerCase().contains(str.toLowerCase())) i++;
                else mDataset.remove(i);
            }
        }
        prevFilter = str;
        notifyDataSetChanged();
    }

    private void setSettingsValues(PrefViewHolder holder, User user) {
        holder.mSwitchSyncPlaylists.setChecked(user.getSettingPlaylistSync(c));
    }

    private void setSettingsChangeListeners(final PrefViewHolder holder, final User user) {
        holder.mFLSyncPlaylist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.mSwitchSyncPlaylists.setChecked(!holder.mSwitchSyncPlaylists.isChecked()); // flip the switch
            }
        });
        holder.mSwitchSyncPlaylists.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                user.setSettingPlaylistSync(c, holder.mSwitchSyncPlaylists.isChecked());
            }
        });

        holder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteUser(holder);
            }
        });
        holder.btnSync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long[] arr = {holder.extraUserId};
                if (Auxiliary.checkConnectivity(c)) {
                    final SyncTask syncTask = new SyncTask(c, ((Main2Activity) c).SYNC_LISTENER, arr);
                    syncTask.execute();
                    Snackbar.make(((Main2Activity) c).fab, "Sync started", Snackbar.LENGTH_LONG).setAction("Cancel", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            syncTask.cancelSyncing(c);
                            Snackbar.make(((Main2Activity) c).fab, "Sync cancelled", Snackbar.LENGTH_SHORT).show();
                        }
                    }).show();
                    ((Main2Activity) c).tvProgress.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            syncTask.cancelSyncing(c);
                        }
                    });
                } else
                    Toast.makeText(c, "No internet", Toast.LENGTH_LONG).show();

            }
        });
        holder.btnPlaylists.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(c, PlaylistsActivity.class);
                i.putExtra(PlaylistsActivity.EXTRA_USER_ID, holder.extraUserId);
                c.startActivity(i);
            }
        });
    }
}