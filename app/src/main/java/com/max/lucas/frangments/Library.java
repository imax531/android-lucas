package com.max.lucas.frangments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.max.lucas.R;

/**
 * Created by imax5 on 04-Dec-15.
 */
public class Library extends Fragment {
    View v;
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_library, container, false);
        return v;
    }

    @Override
    public String toString() { return "Library"; }
}
