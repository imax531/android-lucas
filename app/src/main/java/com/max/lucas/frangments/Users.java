package com.max.lucas.frangments;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.max.lucas.models.FabListenerFragment;
import com.max.lucas.DividerItemDecoration;
import com.max.lucas.R;
import com.max.lucas.adapters.frg_users.UsersActivityRecyclerAdapter;
import com.max.lucas.data.DbContract;
import com.max.lucas.sync.Accessor;
import com.max.lucas.sync.User;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by imax5 on 29-Nov-15.
 */
public class Users extends FabListenerFragment implements android.support.v7.widget.SearchView.OnQueryTextListener {

    public static final String TAG = "com.max.lucas.users";

    private RecyclerView rvUsers;
    private UsersActivityRecyclerAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private View v;
    private View emptyDatasetMsg;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_users, container, false);
        emptyDatasetMsg = v.findViewById(R.id.tvNoUsersMsg);
        rvUsers = (RecyclerView) v.findViewById(R.id.rvUsers);

        setHasOptionsMenu(true);

        mLayoutManager = new LinearLayoutManager(getActivity());
        rvUsers.setLayoutManager(mLayoutManager);

        rvUsers.addItemDecoration(new DividerItemDecoration(getActivity(), null));

        Cursor cursor = getActivity().getContentResolver().query(
                DbContract.UserEntry.CONTENT_URI,
                null,
                null,
                null,
                "Lower(" + DbContract.UserEntry.COLUMN_USERNAME + ")"
        );
        ArrayList<User> users = new ArrayList<>();
        assert cursor != null;
        final int idIndex = cursor.getColumnIndex(DbContract.UserEntry.COLUMN_ID);
        final int usernameIndex = cursor.getColumnIndex(DbContract.UserEntry.COLUMN_USERNAME);
        final int permalinkIndex = cursor.getColumnIndex(DbContract.UserEntry.COLUMN_PERMALINK_URL);
        while (cursor.moveToNext()) {
            users.add(new User(cursor.getLong(idIndex),
                    cursor.getString(usernameIndex),
                    cursor.getString(permalinkIndex)));
        }
        cursor.close();

        mAdapter = new UsersActivityRecyclerAdapter(getActivity(), users, this);
        rvUsers.setAdapter(mAdapter);

        updateEmptyDatasetMsg();

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.users, menu);

        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(this);
    }

    @Override
    public boolean onQueryTextSubmit(String query) { return true; }

    @Override
    public boolean onQueryTextChange(String newText) {
        mAdapter.filter(newText);
        return true;
    }

    @Override
    public boolean isListening() {
        return true;
    }

    @Override
    public int getFabResourceId() {
        return R.drawable.ic_add_white_24dp;
    }

    @Override
    public void onFabClick(View v) {
        if (Settings.canUseInternet(getActivity())) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Add username");
            final EditText input = new EditText(getActivity());
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);
            builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    String name2Add = input.getText().toString();
                    (new AddUser(getActivity(), name2Add)).execute();
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.show();
        } else Toast.makeText(getActivity(), "Can't use internet", Toast.LENGTH_SHORT).show();
    }

    public class AddUser extends AsyncTask<Void, Void, String> {
        private Context mContext;
        private String mUrl;

        AddUser(Context context, String username) {
            mContext = context;
            mUrl = Accessor.getUserDetails(username).toString();
        }

        @Override
        protected String doInBackground(Void... params) {
           return Accessor.getJSON(mUrl);
        }

        @Override
        protected void onPostExecute(String strings) {
            super.onPostExecute(strings);

            if (strings != null) {
                try {
                    JSONObject json = new JSONObject(strings);

                    long id = json.getLong("id");
                    String username = json.getString("username");
                    String permalink = json.getString("permalink");

                    int i = 0;
                    while (i < mAdapter.getDataset().size() &&
                            mAdapter.getDataset().get(i).getUsername().toLowerCase().compareTo(username.toLowerCase()) < 0)
                        i++;
                    mAdapter.getDataset().add(i, new User(id, username, permalink));
                    mAdapter.notifyItemInserted(i);
                    updateEmptyDatasetMsg();

                    ContentValues cv = new ContentValues(3);
                    cv.put(DbContract.UserEntry.COLUMN_ID, id);
                    cv.put(DbContract.UserEntry.COLUMN_USERNAME, username);
                    cv.put(DbContract.UserEntry.COLUMN_PERMALINK_URL, json.getString("permalink_url"));
                    getActivity().getContentResolver().insert(DbContract.UserEntry.CONTENT_URI, cv);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else
                Toast.makeText(mContext, "User doesn't exist", Toast.LENGTH_SHORT).show();
        }
    }

    public void updateEmptyDatasetMsg() {
        emptyDatasetMsg.setVisibility((mAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE));
    }

    @Override
    public String toString() {
        return "Users";
    }
}
