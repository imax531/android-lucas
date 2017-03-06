package com.max.lucas.models;

import android.app.Fragment;
import android.preference.PreferenceFragment;
import android.view.View;

/**
 * Created by imax5 on 12-Jun-16.
 */
public abstract class FabListenerFragment extends PreferenceFragment {

    public abstract boolean isListening();

    public abstract int getFabResourceId();

    public abstract void onFabClick(View v);

}
