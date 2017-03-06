package com.max.lucas.frangments;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.max.lucas.models.FabListenerFragment;
import com.max.lucas.R;
import com.max.lucas.activities.DownloadUrlActivity;
import com.max.lucas.adapters.frg_home.MainHomeTrackRecyclerAdapter;
import com.max.lucas.data.DbContract;
import com.max.lucas.models.HomeTrack;

import java.util.ArrayList;

/**
 * Created by imax5 on 07-May-16.
 */
public class Main extends FabListenerFragment
        implements android.support.v7.widget.SearchView.OnQueryTextListener, LoaderManager.LoaderCallbacks<Cursor> {

    View v;
    RecyclerView mRecyclerView;
    MainHomeTrackRecyclerAdapter mAdapter = null;
    View noTrasksMsg;
    String searchFilter = "";

    private static final int TRACKS_LOADER_ID = 0;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_main_home, container, false);
        noTrasksMsg = v.findViewById(R.id.tvNoUsersMsg);
        setHasOptionsMenu(true);

        mRecyclerView = (RecyclerView) v.findViewById(R.id.rcHomeTracks);

        mRecyclerView.setHasFixedSize(true);

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);

        if (mAdapter != null)
            noTrasksMsg.setVisibility(mAdapter.getCompleteItemCount() == 0 ? View.VISIBLE : View.GONE);

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(TRACKS_LOADER_ID, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(getActivity().getString(R.string.scroll_offset), mRecyclerView.getLayoutManager().onSaveInstanceState());
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null)
            mRecyclerView.getLayoutManager().onRestoreInstanceState(savedInstanceState.getParcelable(getActivity().getString(R.string.scroll_offset)));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.home_fragment, menu);

        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_search) {
            getActivity().onSearchRequested();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        searchFilter = newText;
        mAdapter.filter(searchFilter);
        return true;
    }

    @Override
    public boolean isListening() {
        return true;
    }

    @Override
    public int getFabResourceId() {
        return R.drawable.ic_http_white_24dp;
    }

    @Override
    public void onFabClick(View v) {
        Intent i = new Intent(getActivity(), DownloadUrlActivity.class);
        startActivity(i);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String orderBy = DbContract.TrackEntry._ID + " DESC";

        return new CursorLoader(getActivity(),
                DbContract.TrackEntry.buildTracksWithUsers(),
                null,
                null,
                null,
                orderBy);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
        ArrayList<HomeTrack> tracks = new ArrayList<>();

        while (c != null && c.moveToNext()) {
            HomeTrack trackToAdd = new HomeTrack(
                    c.getInt(c.getColumnIndex(DbContract.TrackEntry.COLUMN_ID)),
                    c.getString(c.getColumnIndex(DbContract.TrackEntry.COLUMN_API_TITLE)),
                    c.getString(c.getColumnIndex(DbContract.TrackEntry.COLUMN_API_UPLOADER_USERNAME)),
                    c.getString(c.getColumnIndex(DbContract.TrackEntry.COLUMN_ARTWORK_URL)),
                    c.getInt(c.getColumnIndex(DbContract.TrackEntry.COLUMN_DOWNLOADED)) == 1);
            String users = c.getString(c.getColumnIndex(DbContract.UserEntry.COLUMN_ID));
            if (users != null && !users.equals("")) {
                String[] usersArr = users.split(",");
                for (int j = 0; j < usersArr.length; j++)
                    trackToAdd.addUser(Long.valueOf(usersArr[j]));
            }

            tracks.add(trackToAdd);
        }

        if (mAdapter == null)
            mAdapter = new MainHomeTrackRecyclerAdapter(tracks, getActivity());
        else
            mAdapter.update(tracks);
        mAdapter.filter(searchFilter);
        mRecyclerView.setAdapter(mAdapter);

        noTrasksMsg.setVisibility(mAdapter.getCompleteItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        /* mAdapter.clear(); */
    }
}