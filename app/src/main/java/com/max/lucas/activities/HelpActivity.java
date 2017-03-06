package com.max.lucas.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.max.lucas.R;

public class HelpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_container);
        android.app.FragmentManager manager = getFragmentManager();

        com.max.lucas.frangments.Help fragment =
                new com.max.lucas.frangments.Help();
        android.app.FragmentTransaction ft = manager.beginTransaction();
        ft.replace(R.id.fragment_container, fragment);
        ft.commit();

    }
}
