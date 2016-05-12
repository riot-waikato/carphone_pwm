package com.example.android.bluetoothlegatt;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

/**
 * Created by nbk4 on 10/05/16.
 */
public class MyPreferenceActivity extends PreferenceActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.headers, target);
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        return MyPreferenceActivity.class.getName().equals(fragmentName);
    }
}
