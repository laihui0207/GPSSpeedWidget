package com.huivip.gpsspeedwidget.fragment;

import android.os.Bundle;
import android.support.v7.preference.Preference;

import com.huivip.gpsspeedwidget.R;
import com.huivip.gpsspeedwidget.viewutil.DialogHelper;

import net.gsantner.opoc.util.ContextUtils;

public class SettingsAppearanceFragment extends SettingsBaseFragment {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);
        addPreferencesFromResource(R.xml.preferences_appearance);
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        int key = new ContextUtils(getContext()).getResId(ContextUtils.ResType.STRING, preference.getKey());
        switch (key) {
            case R.string.pref_key__icon_pack:
                DialogHelper.startPickIconPackIntent(getActivity());
                return true;
        }
        return false;
    }
}
