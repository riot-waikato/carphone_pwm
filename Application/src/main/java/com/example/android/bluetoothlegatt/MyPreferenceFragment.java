package com.example.android.bluetoothlegatt;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created by nbk4 on 10/05/16.
 */
public class MyPreferenceFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
    }
}
