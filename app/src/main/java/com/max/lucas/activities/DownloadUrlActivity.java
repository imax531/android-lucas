package com.max.lucas.activities;

import android.os.Bundle;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.max.lucas.R;
import com.max.lucas.frangments.DownloadUrl;

public class DownloadUrlActivity extends AppCompatActivity {

    private DownloadUrl fragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_container);
        android.app.FragmentManager manager = getFragmentManager();

        if (savedInstanceState == null) {
            fragment = new DownloadUrl();
            ShareCompat.IntentReader intentReader = ShareCompat.IntentReader.from(this);
            if (intentReader.isShareIntent()) {
                String[] text = intentReader.getText().toString().split(("\\n"));
                String url = text[text.length - 1];
                Bundle args = new Bundle();
                args.putString(getString(R.string.download_url) , url);
                if (savedInstanceState != null)
                    args.putAll(savedInstanceState);
                fragment.setArguments(args);
            }

            android.app.FragmentTransaction ft = manager.beginTransaction();
            ft.add(R.id.fragment_container, fragment);
            ft.commit();
        } else {
            // TODO recreate fragment from saved state
        }
    }

    @Override
    public void onBackPressed() {
        if (fragment == null || fragment.onBackPressed())
            super.onBackPressed();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (fragment != null)
            fragment.onSaveInstanceState(outState);
    }
}
