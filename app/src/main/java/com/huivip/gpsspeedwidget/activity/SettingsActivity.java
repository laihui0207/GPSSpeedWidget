package com.huivip.gpsspeedwidget.activity;

import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.widget.Toolbar;

import com.huivip.gpsspeedwidget.Constant;
import com.huivip.gpsspeedwidget.R;
import com.huivip.gpsspeedwidget.fragment.SettingsBaseFragment;
import com.huivip.gpsspeedwidget.fragment.SettingsMasterFragment;
import com.huivip.gpsspeedwidget.manager.Setup;
import com.huivip.gpsspeedwidget.util.AppSettings;
import com.huivip.gpsspeedwidget.util.BackupHelper;
import com.huivip.gpsspeedwidget.util.Definitions;
import com.nononsenseapps.filepicker.Utils;

import net.gsantner.opoc.util.ContextUtils;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SettingsActivity extends ThemeActivity implements SettingsBaseFragment.OnPreferenceStartFragmentCallback {
    @BindView(R.id.toolbar)
    protected Toolbar toolbar;

    public void onCreate(Bundle b) {
        // must be applied before setContentView
        super.onCreate(b);
        ContextUtils contextUtils = new ContextUtils(this);
        contextUtils.setAppLanguage(_appSettings.getLanguage());

        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);

        toolbar.setTitle(R.string.pref_title__settings);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_arrow_back_white_24px));
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        toolbar.setBackgroundColor(_appSettings.getPrimaryColor());

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_holder, new SettingsMasterFragment()).commit();

        // if system exit is called the app will open settings activity again
        // this pushes the user back out to the home activity
        if (_appSettings.getAppRestartRequired()) {
            startActivity(new Intent(this, HomeActivity.class));
        }
    }

    @Override
    public boolean onPreferenceStartFragment(PreferenceFragmentCompat caller, Preference preference) {
        Fragment fragment = Fragment.instantiate(this, preference.getFragment(), preference.getExtras());
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_holder, fragment)
                .addToBackStack(fragment.getTag())
                .commit();
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            List<Uri> files = Utils.getSelectedFilesFromResult(data);
            switch (requestCode) {
                case Definitions.INTENT_BACKUP:
                    BackupHelper.backupConfig(this, new File(Utils.getFileForUri(files.get(0)).getAbsolutePath() + "/gpsWidget.zip").toString());
                    break;
                case Definitions.INTENT_RESTORE:
                    BackupHelper.restoreConfig(this, Utils.getFileForUri(files.get(0)).toString());
                    System.exit(0);
                    break;
                case Definitions.INTENT_LYRIC_PATH:
                    Setup.appSettings().setLyricPath(files.get(0).getPath());
                    break;
                case Constant.SELECT_AMAP_PLUGIN_REQUEST_CODE:
                    AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
                    AppWidgetHost appWidgetHost = new AppWidgetHost(this, Constant.APP_WIDGET_HOST_ID);
                    int id = data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
                    boolean check = false;
                    if (id > 0) {
                       /* AppWidgetProviderInfo popupWidgetInfo = appWidgetManager.getAppWidgetInfo(id);
                        final View amapView = appWidgetHost.createView(this, id, popupWidgetInfo);
                        View vv = com.huivip.gpsspeedwidget.utils.Utils.getViewByIds(amapView, new Object[]{"widget_container", "daohang_container", 0, "gongban_daohang_right_blank_container", "daohang_widget_image"});
                        if (vv instanceof ImageView) {
                            check = true;
                        }
                    }
                    if (check) {*/
                        AppSettings.get().setAmapPluginId(id);
                    } else {
                        AppSettings.get().setAmapPluginId(-1);
                    }
                    break;

            }
        }
    }
}
