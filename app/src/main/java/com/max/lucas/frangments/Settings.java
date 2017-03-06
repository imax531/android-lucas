package com.max.lucas.frangments;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v4.provider.DocumentFile;
import android.util.Log;
import android.widget.Toast;

import com.max.lucas.ActiveDownloads;
import com.max.lucas.data.Handler;
import com.max.lucas.R;

public class Settings extends PreferenceFragment {

    public static final String TAG = "com.max.lucas.settings";

    StringFilter storageLocationStringFilter = new StringFilter() {
        @Override
        public String filter(String str) {
            if (str.equals("")) return ActiveDownloads.DEFAULT_DOWNLOAD_LOCATION;
            String[] loc = str.split("/document/");
            str = loc[loc.length - 1];
            try {
                String path = java.net.URLDecoder.decode(str, "UTF-8");
                path = path.replace(':', '/');
                return path;
            } catch (java.io.UnsupportedEncodingException ex) { }
            return "";
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Add 'general' preferences, defined in the XML file
        addPreferencesFromResource(R.xml.pref_general);

        // For all preferences, attach an OnPreferenceChangeListener so the UI summary can be
        // updated when the preference changes.
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_download_limit_key)), null);
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_storage_location_key)), storageLocationStringFilter);
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_track_sync_limit_key)), null);


        Preference pref = findPreference(getString(R.string.pref_storage_location_key));
        pref.setDefaultValue(ActiveDownloads.DEFAULT_DOWNLOAD_LOCATION);
        pref.setEnabled(Build.VERSION.SDK_INT >= 21);
        pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                startActivityForResult(intent, 42);
                return true;
            }
        });
        findPreference(getString(R.string.pref_reset_database_key)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Are you sure?");
                builder.setPositiveButton("Reset database", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Handler db = new Handler(getActivity(), null, null, 1);
                        //TODO: delete everything
                        db.deleteALLHistory();
                        db.close();

                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
                return true;
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (requestCode == 42 && resultCode == Activity.RESULT_OK) {
            Uri treeUri = resultData.getData();
            DocumentFile pickedDir = DocumentFile.fromTreeUri(getActivity(), treeUri);

            String path = pickedDir.getUri().toString();
            if (path.contains("com.max.lucas") && (path.contains("files"))) Toast.makeText(getActivity(), "You can't save there!!!", Toast.LENGTH_LONG).show();
            else setStorageLocation(path);
        }
    }

    private void setStorageLocation(String path) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(getString(R.string.pref_storage_location_key), path);
        editor.apply();

        setPreferenceText(findPreference(getString(R.string.pref_storage_location_key)), path, storageLocationStringFilter);
    }

    public static void setStorageLocationToDefault(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(context.getString(R.string.pref_storage_location_key), ActiveDownloads.DEFAULT_DOWNLOAD_LOCATION);
        editor.apply();
    }

    public boolean setPreferenceText(Preference preference, Object value, StringFilter filter) {
        String stringValue = value.toString();

        if (filter != null) {
            stringValue = filter.filter(stringValue);
        }
        if (preference instanceof ListPreference) {
            // For list preferences, look up the correct display value in
            // the preference's 'entries' list (since they have separate labels/values).
            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(stringValue);
            if (prefIndex >= 0) {
                preference.setSummary(listPreference.getEntries()[prefIndex]);
            }
        } else {
            // For other preferences, set the summary to the value's simple string representation.
            preference.setSummary(stringValue);
        }
        return true;
    }

    /**
     * Attaches a listener so the summary is always updated with the preference value.
     * Also fires the listener once, to initialize the summary (so it shows up before the value
     * is changed.)
     */
    private void bindPreferenceSummaryToValue(Preference preference, StringFilter filter) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(new MyOnPreferenceChangeListener(filter));

        // Trigger the listener immediately with the preference's
        // current value.
        String newValue = PreferenceManager
                .getDefaultSharedPreferences(preference.getContext())
                .getString(preference.getKey(), "");

        setPreferenceText(preference, newValue, filter);
    }

    public static boolean getBooleanPreference (Context context, int pref) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(context.getString(pref), false);
    }

    public static String getStringPreference (Context context, int pref) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString(context.getString(pref), "");
    }

    public static boolean isConnectedToWIFI(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return mWifi.isConnected();
    }

    private interface StringFilter {
        String filter(String str);
    }

    private class MyOnPreferenceChangeListener implements Preference.OnPreferenceChangeListener {
        StringFilter filter;

        public MyOnPreferenceChangeListener(StringFilter sf) {
            filter = sf;
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            return setPreferenceText(preference, value, filter);
        }
    }

    public static boolean canUseInternet (Context c) {
        return !getBooleanPreference(c, R.string.pref_only_wifi_key) ||
                isConnectedToWIFI(c);
    }

    public static UserPreferences getUserPreferences(Context c, long userId) {
        return new UserPreferences(c, userId);
    }

    public static class UserPreferences {
        private Context context;
        private long userId;

        public UserPreferences(Context context, long userId) {
            this.context = context;
            this.userId = userId;
        }

        public void removeAllPreferences() {
            SharedPreferences.Editor sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context).edit();
            sharedPreferences.remove(context.getString(R.string.pref_sync_user_playlists_key, userId)).apply();
            // TODO should we remove likes?
        }

        // Playlist download
        public Boolean getPlaylistDownload() {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            return sharedPreferences.getBoolean(context.getString(R.string.pref_sync_user_playlists_key, userId), Boolean.valueOf(context.getString(R.string.pref_sync_user_playlists_default)));
        }

        public void removePlaylistDownload() {
            SharedPreferences.Editor sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context).edit();
            sharedPreferences.remove(context.getString(R.string.pref_sync_user_playlists_key, userId)).apply();
        }

        public void setPlaylistDownload(Boolean value) {
            SharedPreferences.Editor sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context).edit();
            sharedPreferences.putBoolean(context.getString(R.string.pref_sync_user_playlists_key, userId), value).apply();
        }
    }
}
