/*#######################################################
 *
 *   Maintained by Gregor Santner, 2018-
 *   https://gsantner.net/
 *
 *   License: Apache 2.0
 *  https://github.com/gsantner/opoc/#licensing
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
#########################################################*/
/*
 * This class is not intended to be used directly.
 * Copy this file from net.gsantner.opoc to your app and modify
 * packageId, resources and arguments to needs and availability
 */
package com.huivip.gpsspeedwidget.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.Preference;
import android.text.TextUtils;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;

import com.huivip.gpsspeedwidget.Constant;
import com.huivip.gpsspeedwidget.R;
import com.huivip.gpsspeedwidget.activity.SettingsActivity;
import com.huivip.gpsspeedwidget.util.AppSettings;
import com.huivip.gpsspeedwidget.utils.HttpUtils;
import com.huivip.gpsspeedwidget.utils.PrefUtils;
import com.huivip.gpsspeedwidget.utils.Utils;

import net.gsantner.opoc.format.markdown.SimpleMarkdownParser;
import net.gsantner.opoc.preference.GsPreferenceFragmentCompat;
import net.gsantner.opoc.util.ActivityUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Locale;

public class SettingsAboutFragment extends GsPreferenceFragmentCompat<AppSettings> {
    public static final String TAG = "MoreInfoFragment";

    public static SettingsAboutFragment newInstance() {
        return new SettingsAboutFragment();
    }

    @Override
    public int getPreferenceResourceForInflation() {
        return R.xml.preferences_about;
    }

    @Override
    public String getFragmentTag() {
        return TAG;
    }

    @Override
    protected AppSettings getAppSettings(Context context) {
        return _appSettings != null ? _appSettings : new AppSettings(context);
    }

    @Override
    public Boolean onPreferenceClicked(Preference preference) {
        ActivityUtils au = new ActivityUtils(getActivity());
        if (isAdded() && preference.hasKey()) {
            switch (keyToStringResId(preference)) {
                case R.string.pref_key__more_info__app: {
                   /// _cu.openWebpageInExternalBrowser(getString(R.string.app_web_url));
                    return true;
                }
                case R.string.pref_key__more_info__settings: {
                    au.animateToActivity(SettingsActivity.class, false, 124);
                    return true;
                }
                case  R.string.pref_key__check_update:{
                    checkUpdate();
                    return true;
                }
                case R.string.pref_key__donate:{
                    LayoutInflater inflater = getLayoutInflater();
                    View layout = inflater.inflate(R.layout.dialog_pay,null);
                    new AlertDialog.Builder(new ContextThemeWrapper(getActivity(),R.style.Theme_AppCompat_DayNight)).setTitle("打赏随意，多少都是一种支持").setView(layout)
                            .setPositiveButton("关闭", null).show();
                    return true;
                }
           /*     case R.string.pref_key__more_info__rate_app: {
                    au.showGooglePlayEntryForThisApp();
                    return true;
                }
                case R.string.pref_key__more_info__join_community: {
                    _cu.openWebpageInExternalBrowser(getString(R.string.app_community_url));
                    return true;
                }
                case R.string.pref_key__more_info__donate: {
                    _cu.openWebpageInExternalBrowser(getString(R.string.app_donate_url));
                    return true;
                }
                case R.string.pref_key__more_info__bug_reports: {
                    _cu.openWebpageInExternalBrowser(getString(R.string.app_bugreport_url));
                    return true;
                }
                case R.string.pref_key__more_info__translate: {
                    _cu.openWebpageInExternalBrowser(getString(R.string.app_translate_url));
                    return true;
                }
                case R.string.pref_key__more_info__project_contribution_info: {
                    _cu.openWebpageInExternalBrowser(getString(R.string.app_contribution_info_url));
                    return true;
                }*/
              /*  case R.string.pref_key__more_info__android_contribution_guide: {
                    _cu.openWebpageInExternalBrowser(
                            String.format("https://gsantner.net/android-contribution-guide/?packageid=%s&name=%s&web=%s",
                                    _cu.context().getPackageName(), getString(R.string.app_name), getString(R.string.app_web_url).replace("=", "%3D")));
                    return true;
                }*/
                case R.string.pref_key__more_info__source_code: {
                    _cu.openWebpageInExternalBrowser(getString(R.string.app_source_code_url));
                    return true;
                }
                case R.string.pref_key__more_info__project_license: {
                    try {
                        au.showDialogWithHtmlTextView(R.string.licenses, new SimpleMarkdownParser().parse(
                                getResources().openRawResource(R.raw.license),
                                "", SimpleMarkdownParser.FILTER_ANDROID_TEXTVIEW).getHtml());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return true;
                }
                case R.string.pref_key__more_info__open_source_licenses: {
                    try {
                        au.showDialogWithHtmlTextView(R.string.licenses, new SimpleMarkdownParser().parse(
                                getResources().openRawResource(R.raw.licenses_3rd_party),
                                "", SimpleMarkdownParser.FILTER_ANDROID_TEXTVIEW).getHtml());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return true;
                }
               /* case R.string.pref_key__more_info__contributors_public_info: {
                    try {
                        au.showDialogWithHtmlTextView(R.string.contributors, new SimpleMarkdownParser().parse(
                                getResources().openRawResource(R.raw.contributors),
                                "", SimpleMarkdownParser.FILTER_ANDROID_TEXTVIEW).getHtml());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return true;
                }*/
               /* case R.string.pref_key__more_info__copy_build_information: {
                    new ShareUtil(getContext()).setClipboard(preference.getSummary());
                    SimpleMarkdownParser smp = new SimpleMarkdownParser();
                    try {
                        String html = smp.parse(getResources().openRawResource(R.raw.changelog), "", SimpleMarkdownParser.FILTER_ANDROID_TEXTVIEW, SimpleMarkdownParser.FILTER_CHANGELOG).getHtml();
                        au.showDialogWithHtmlTextView(R.string.changelog, html);
                    } catch (Exception ex) {

                    }
                    return true;
                }*/
            }
        }
        return null;
    }
    private void checkUpdate(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                String updateInfo= HttpUtils.getData(Constant.LBSURL+"/updateInfo?type=full");
                try {
                    if(!TextUtils.isEmpty(updateInfo) && !updateInfo.equalsIgnoreCase("-1")) {
                        String currentVersion= Utils.getLocalVersion(getContext());
                        int currentVersionCode=Utils.getLocalVersionCode(getContext());
                        JSONObject infoObj = new JSONObject(updateInfo);
                        JSONObject data= (JSONObject) infoObj.get("data");
                        String updateVersion=data.getString("serverVersion");
                        int updateVersionCode=data.getInt("serverVersionCode");
                        Message message = Message.obtain();
                        message.obj =updateInfo;
                        if(currentVersionCode!=0 && updateVersionCode!=0){
                            if(updateVersionCode>currentVersionCode){
                                message.arg1 = 1;
                                AlterHandler.handleMessage(message);
                            } else {
                                message.arg1 = 0;
                                AlterHandler.handleMessage(message);
                            }
                        } else {
                            if (currentVersion.equalsIgnoreCase(updateVersion)) {
                                message.arg1 = 0;
                                AlterHandler.handleMessage(message);
                            } else {
                                message.arg1 = 1;
                                AlterHandler.handleMessage(message);
                            }
                        }

                    }
                    else {
                        Message message = Message.obtain();
                        message.obj ="";
                        message.arg1 = 0;
                        AlterHandler.handleMessage(message);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Looper.loop();
            }
        }).start();
    }
    @SuppressLint("HandlerLeak")
    final Handler AlterHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.arg1==0) {
                AlertDialog.Builder  mDialog = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(),R.style.Theme_AppCompat_DayNight));
                mDialog.setTitle("版本检查");
                mDialog.setMessage("已是最新版本，无需更新！");
                mDialog.setPositiveButton("关闭",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        dialog.dismiss();
                    }
                }).setNegativeButton("重装", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        JSONObject updateInfo= null;
                        try {
                            updateInfo = new JSONObject((String)msg.obj);
                            JSONObject data= (JSONObject) updateInfo.get("data");
                            String updateUrl=data.getString("updateurl");
                            String appName=data.getString("appname");
                            HttpUtils.downLoadApk(getActivity(),updateUrl,appName);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
                mDialog.create().show();
            }
            else if (msg.arg1==1){
                AlertDialog.Builder  mDialog = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(),R.style.Theme_AppCompat_DayNight));
                try {
                    JSONObject updateInfo = new JSONObject((String)msg.obj);
                    JSONObject data= (JSONObject) updateInfo.get("data");
                    mDialog.setTitle("版本升级");
                    mDialog.setMessage(data.getString("upgradeinfo")).setCancelable(true);
                    String updateUrl=data.getString("updateurl");
                    String appName=data.getString("appname");
                    mDialog.setPositiveButton("更新", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            HttpUtils.downLoadApk(getActivity(),updateUrl,appName);
                        }
                    }).setNegativeButton("不用了", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                mDialog.create().show();
            }
        }
    };
    @Override
    protected boolean isAllowedToTint(Preference pref) {
        return !getString(R.string.pref_key__more_info__app).equals(pref.getKey());
    }

    @Override
    public synchronized void doUpdatePreferences() {
        super.doUpdatePreferences();
        Context context = getContext();
        if (context == null) {
            return;
        }
        Locale locale = Locale.getDefault();
        String tmp;
        Preference pref;
        updateSummary(R.string.pref_key__more_info__project_license, getString(R.string.app_license_name));

        // Basic app info
        if ((pref = findPreference(R.string.pref_key__more_info__app)) != null && pref.getSummary() == null) {
            pref.setIcon(R.drawable.ic_launcher);
            pref.setSummary(String.format(locale, "Version v%s (%d)\nDeviceId: %s",  _cu.getAppVersionName(), _cu.bcint("VERSION_CODE", 0), PrefUtils.getShortDeviceId(getContext())));
        }

        // Extract some build information and publish in summary
       /* if ((pref = findPreference(R.string.pref_key__more_info__copy_build_information)) != null && pref.getSummary() == null) {
            String summary = String.format(locale, "\n<b>Package:</b> %s\n<b>Version:</b> v%s (%d)", _cu.getPackageName(), _cu.getAppVersionName(), _cu.bcint("VERSION_CODE", 0));
            summary += (tmp = _cu.bcstr("FLAVOR", "")).isEmpty() ? "" : ("\n<b>Flavor:</b> " + tmp.replace("flavor", ""));
            summary += (tmp = _cu.bcstr("BUILD_TYPE", "")).isEmpty() ? "" : (" (" + tmp + ")");
            summary += (tmp = _cu.bcstr("BUILD_DATE", "")).isEmpty() ? "" : ("\n<b>Build date:</b> " + tmp);
            summary += (tmp = _cu.getAppInstallationSource()).isEmpty() ? "" : ("\n<b>ISource:</b> " + tmp);
            summary += (tmp = _cu.bcstr("GITHASH", "")).isEmpty() ? "" : ("\n<b>VCS Hash:</b> " + tmp);
            pref.setSummary(_cu.htmlToSpanned(summary.trim().replace("\n", "<br/>")));
        }*/

        // Extract project team from raw ressource, where 1 person = 4 lines
        // 1) Name/Title, 2) Description/Summary, 3) Link/View-Intent, 4) Empty line
       /* if ((pref = findPreference(R.string.pref_key__more_info__project_team)) != null && ((PreferenceGroup) pref).getPreferenceCount() == 0) {
            String[] data = (_cu.readTextfileFromRawRes(R.raw.project_team, "", "").trim() + "\n\n").split("\n");
            for (int i = 0; i + 2 < data.length; i += 4) {
                Preference person = new Preference(context);
                person.setTitle(data[i]);
                person.setSummary(data[i + 1]);
                person.setIcon(R.drawable.ic_person_black_24dp);
                try {
                    Uri uri = Uri.parse(data[i + 2]);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    person.setIntent(intent);
                } catch (Exception ignored) {
                }
                appendPreference(person, (PreferenceGroup) pref);
            }
        }*/
    }
}
