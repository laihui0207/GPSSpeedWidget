package com.huivip.gpsspeedwidget.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.Preference;

import com.huivip.gpsspeedwidget.R;
import com.huivip.gpsspeedwidget.activity.HideAppsActivity;
import com.huivip.gpsspeedwidget.util.AppSettings;

import net.gsantner.opoc.util.ContextUtils;

public class SettingsAppDrawerFragment extends SettingsBaseFragment {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);
        addPreferencesFromResource(R.xml.preferences_app_drawer);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        super.onSharedPreferenceChanged(sharedPreferences, key);
        AppSettings.get().setAppRestartRequired(false);

    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        int key = new ContextUtils(getContext()).getResId(ContextUtils.ResType.STRING, preference.getKey());
        switch (key) {
            case R.string.pref_key__hidden_apps:
                Intent intent = new Intent(getActivity(), HideAppsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                return true;
        }
        return false;
    }
}