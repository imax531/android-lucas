package com.max.lucas.activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.max.lucas.Auxiliary;
import com.max.lucas.models.FabListenerFragment;
import com.max.lucas.R;
import com.max.lucas.frangments.Main;
import com.max.lucas.frangments.Settings;
import com.max.lucas.frangments.Users;
import com.max.lucas.sync.SyncTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Main2Activity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public FloatingActionButton fab = null;
    private DrawerLayout drawer;
    private TextView tvLastSyncTime;
    public TextView tvProgress;
    private ProgressBar pbProgress1, pbProgress2, pbProgress3, pbProgress4, pbProgress5;
    private FrameLayout flSyncProgress;
    private SyncTask syncTask;

    private Fragment currentFragment;

    public final SyncTask.SyncListerner SYNC_LISTENER = new SyncTask.SyncListerner() {

        boolean finishMsgDisplayed = false;

        @Override
        public void preSync() {
            tvLastSyncTime.setText("Starting sync");
            tvProgress.setText("Starting sync");
            flSyncProgress.setVisibility(View.VISIBLE);
            setProgressBar(0);
        }

        @Override
        public void userCompleted(long id) {

        }

        @Override
        public void trackCompleted(int current, int amount, int newTracks) {
            if (current < amount) {
                tvLastSyncTime.setText(getString(R.string.sync_track_progress, current, amount));
                tvProgress.setText(getString(R.string.sync_track_progress, current, amount));
                setProgressBar((current*100/amount));
            } else {
                updateLastSyncTimeLabel();
                if (!finishMsgDisplayed) {
                    Toast.makeText(Main2Activity.this, getString(R.string.sync_finished_toast, newTracks), Toast.LENGTH_SHORT).show();
                    finishMsgDisplayed = true;
                    flSyncProgress.setVisibility(View.GONE);
                    updateLastSyncTimeLabel();
                }
            }
        }
    };

    private void setProgressBar(int progress) {
        pbProgress1.setProgress(progress);
        pbProgress2.setProgress(progress);
        pbProgress3.setProgress(progress);
        pbProgress4.setProgress(progress);
        pbProgress5.setProgress(progress);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main2);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        final ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(this);
        }

        tvLastSyncTime = ((TextView) navigationView.getHeaderView(0).findViewById(R.id.tvDetails));
        flSyncProgress = ((FrameLayout) findViewById(R.id.flSyncProgress));
        tvProgress = ((TextView) findViewById(R.id.tvProgress));
        pbProgress1 = ((ProgressBar) findViewById(R.id.pbProgress1));
        pbProgress2 = ((ProgressBar) findViewById(R.id.pbProgress2));
        pbProgress3 = ((ProgressBar) findViewById(R.id.pbProgress3));
        pbProgress4 = ((ProgressBar) findViewById(R.id.pbProgress4));
        pbProgress5 = ((ProgressBar) findViewById(R.id.pbProgress5));

        // request writing permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int permissionCheck = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (permissionCheck == PackageManager.PERMISSION_DENIED)
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 52);
        }

        fab = (FloatingActionButton) findViewById(R.id.fab);
        if (savedInstanceState != null)
            drawerId = savedInstanceState.getInt("drawer_index", -1);
        onNavigationItemSelected(null, savedInstanceState);

        navigationView.getHeaderView(0).findViewById(R.id.btnSync).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.closeDrawers();
                if (Auxiliary.checkConnectivity(Main2Activity.this)) {
                    syncTask = new SyncTask(Main2Activity.this, SYNC_LISTENER);
                    syncTask.execute();
                    Snackbar.make(fab, "Sync started", Snackbar.LENGTH_LONG).setAction("Cancel", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            syncTask.cancelSyncing(Main2Activity.this);
                            Snackbar.make(fab, "Sync cancelled", Snackbar.LENGTH_SHORT).show();
                        }
                    }).show();
                    tvProgress.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            syncTask.cancelSyncing(Main2Activity.this);
                        }
                    });
                    setLastSynctime();
                } else
                    Toast.makeText(Main2Activity.this, "No internet", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

        if (syncTask != null && syncTask.isActive()) {
            flSyncProgress.setVisibility(View.VISIBLE);
            syncTask.updateListener();
        } else
            flSyncProgress.setVisibility(View.GONE);
    }

    private void updateLastSyncTimeLabel() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String lastSyncTime = preferences.getString(getString(R.string.last_sync_time), getString(R.string.no_sync_yet_label));
        tvLastSyncTime.setText("Last sync: " + lastSyncTime);
    }

    private void setLastSynctime() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(getString(R.string.last_sync_time), getDateString());
        editor.apply();
        updateLastSyncTimeLabel();
    }

    private String getDateString() {
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy 'at' HH:mm");
        return format.format(Calendar.getInstance().getTime());
    }

    @Override
    public void onBackPressed() {
        if (drawer != null && drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    // Default screen
    private int drawerId = R.id.nav_home;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("drawer_index", drawerId);

        if (currentFragment != null)
            currentFragment.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (currentFragment instanceof Main) {
            ((Main) currentFragment).onRestoreInstanceState(savedInstanceState);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (requestCode == 52 && permissionCheck == PackageManager.PERMISSION_DENIED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 52);
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        return onNavigationItemSelected(item, null);
    }

    public boolean onNavigationItemSelected(MenuItem item, Bundle savedState) {
        // Handle navigation view item clicks here.

        int selected;
        if (item != null)
            selected = item.getItemId();
        else
            selected = drawerId;

        if (selected == R.id.nav_home) {
            drawerId = selected;
            Fragment f = new Main();
            f.setArguments(savedState);
            setFragment(f);
        } else if (selected == R.id.nav_users) {
            drawerId = selected;
            setFragment(new Users());
        } else if (selected == R.id.nav_settings) {
            drawerId = selected;
            setFragment(new Settings());
        } else if (selected == R.id.nav_about) {
            Intent i = new Intent(getApplicationContext(), AboutActivity.class);
            startActivity(i);
            return true;
        } else if (selected == R.id.nav_help) {
            Intent i = new Intent(getApplicationContext(), HelpActivity.class);
            startActivity(i);
            return true;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer != null)
            drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void setFragment(Fragment fragment) {
        FragmentManager manager = getFragmentManager();
        FragmentTransaction ft = manager.beginTransaction();
        ft.replace(R.id.container, fragment);
        ft.commit();

        currentFragment = fragment;

        if (fragment instanceof FabListenerFragment) {
            final FabListenerFragment fabFrag = (FabListenerFragment) fragment;
            if (fabFrag.isListening()) {
                fab.setVisibility(View.VISIBLE);
                fab.setImageResource(fabFrag.getFabResourceId());
                fab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        fabFrag.onFabClick(v);
                    }
                });
                return;
            }
        }
        fab.setVisibility(View.GONE);
    }
}