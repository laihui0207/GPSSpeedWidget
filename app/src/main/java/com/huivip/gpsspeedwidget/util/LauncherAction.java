package com.huivip.gpsspeedwidget.util;

import android.app.NotificationManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.huivip.gpsspeedwidget.R;
import com.huivip.gpsspeedwidget.activity.HomeActivity;
import com.huivip.gpsspeedwidget.activity.MainActivity;
import com.huivip.gpsspeedwidget.activity.MinibarEditActivity;
import com.huivip.gpsspeedwidget.activity.SettingsActivity;
import com.huivip.gpsspeedwidget.appselection.AppInfo;
import com.huivip.gpsspeedwidget.appselection.AppInfoIconLoader;
import com.huivip.gpsspeedwidget.appselection.AppSelectionActivity;
import com.huivip.gpsspeedwidget.manager.Setup;
import com.huivip.gpsspeedwidget.viewutil.DialogHelper;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

public class LauncherAction {

    public enum Action {
        EditMinibar, SelectApps,SearchTracker, LockScreen, LauncherSettings, VolumeDialog, DeviceSettings, SetWallpaper, AppDrawer, SearchBar, MobileNetworkSettings, ShowNotifications, TurnOffScreen
    }

    public static ActionDisplayItem[] actionDisplayItems = new ActionDisplayItem[]{
            new ActionDisplayItem(Action.EditMinibar, Setup.appContext().getResources().getString(R.string.minibar_title__edit_minibar), Setup.appContext().getResources().getString(R.string.minibar_summary__edit_minibar), R.drawable.ic_mode_edit_black_24dp, 98),
            new ActionDisplayItem(Action.SelectApps, "选择第三方应用", "选择开机要拉起的应用", R.drawable.ic_autolauncher, 46),
            new ActionDisplayItem(Action.SearchTracker, "查询轨迹", "查询车辆历史轨迹", R.drawable.ic_searchtracker, 47),
            new ActionDisplayItem(Action.LockScreen, Setup.appContext().getResources().getString(R.string.minibar_title__lock_screen), Setup.appContext().getResources().getString(R.string.minibar_summary__lock_screen), R.drawable.ic_lock_black_24dp, 24),
            new ActionDisplayItem(Action.LauncherSettings, Setup.appContext().getResources().getString(R.string.minibar_title__launcher_settings), Setup.appContext().getResources().getString(R.string.minibar_summary__launcher_settings), R.drawable.ic_launchersetting, 50),
            new ActionDisplayItem(Action.VolumeDialog, Setup.appContext().getResources().getString(R.string.minibar_title__volume_dialog), Setup.appContext().getResources().getString(R.string.minibar_summary__volume_dialog), R.drawable.ic_volume_up_black_24dp, 71),
            new ActionDisplayItem(Action.DeviceSettings, Setup.appContext().getResources().getString(R.string.minibar_title__device_settings), Setup.appContext().getResources().getString(R.string.minibar_summary__device_settings), R.drawable.ic_settings_launcher_black_24dp, 25),
            new ActionDisplayItem(Action.SetWallpaper, Setup.appContext().getResources().getString(R.string.minibar_title__set_wallpaper), Setup.appContext().getResources().getString(R.string.minibar_summary__set_wallpaper), R.drawable.ic_photo_black_24dp, 36),
            new ActionDisplayItem(Action.AppDrawer, Setup.appContext().getResources().getString(R.string.minibar_title__app_drawer), Setup.appContext().getResources().getString(R.string.minibar_summary__app_drawer), R.drawable.ic_apps_dark_24dp, 73),
            new ActionDisplayItem(Action.SearchBar, Setup.appContext().getResources().getString(R.string.minibar_title__search_bar), Setup.appContext().getResources().getString(R.string.minibar_summary__search_bar), R.drawable.ic_search_light_24dp, 89),
            new ActionDisplayItem(Action.MobileNetworkSettings, Setup.appContext().getResources().getString(R.string.minibar_title__mobile_network), Setup.appContext().getResources().getString(R.string.minibar_summary__mobile_network), R.drawable.ic_network_24dp, 46),
            new ActionDisplayItem(Action.ShowNotifications, Setup.appContext().getResources().getString(R.string.minibar_title__show_notifications), Setup.appContext().getResources().getString(R.string.minibar_summary__show_notifications), R.drawable.ic_notifications, 46),
    };

    public static List<Action> defaultArrangement = Arrays.asList(
            Action.EditMinibar,Action.SelectApps, Action.SearchTracker,
            Action.LockScreen, Action.LauncherSettings,Action.VolumeDialog,
             Action.DeviceSettings,Action.SetWallpaper
    );

    public static void RunAction(Action action, final Context context) {
        LauncherAction.RunAction(getActionItem(action), context);
    }

    @SuppressWarnings("WrongConstant")
    public static void RunAction(ActionDisplayItem action, final Context context) {
        switch (action._action) {
            case EditMinibar:
                context.startActivity(new Intent(context, MinibarEditActivity.class));
                break;
            case SelectApps:
                Glide.get(Setup.appContext())
                        .register(AppInfo.class, InputStream.class, new AppInfoIconLoader.Factory());
                context.startActivity(new Intent(context, AppSelectionActivity.class));
                break;
            case SearchTracker:
                context.startActivity(new Intent(context, MainActivity.class));
                break;
            case SetWallpaper:
                context.startActivity(Intent.createChooser(new Intent(Intent.ACTION_SET_WALLPAPER), context.getString(R.string.select_wallpaper)));
                break;
            case LockScreen:
                try {
                    ((DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE)).lockNow();
                } catch (Exception e) {
                    DialogHelper.alertDialog(context, context.getString(R.string.device_admin_title), context.getString(R.string.device_admin_summary), context.getString(R.string.enable), new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            Tool.toast(context, context.getString(R.string.toast_device_admin_required));
                            Intent intent = new Intent();
                            intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.DeviceAdminSettings"));
                            context.startActivity(intent);
                        }
                    });
                }
                break;
            case DeviceSettings:
                context.startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));
                break;
            case LauncherSettings:
                context.startActivity(new Intent(context, SettingsActivity.class));
                break;
            case VolumeDialog:
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    try {
                        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                        audioManager.setStreamVolume(AudioManager.STREAM_RING, audioManager.getStreamVolume(AudioManager.STREAM_RING), AudioManager.FLAG_SHOW_UI);
                    } catch (Exception e) {
                        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                        if (!mNotificationManager.isNotificationPolicyAccessGranted()) {
                            Intent intent = new Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                            context.startActivity(intent);
                        }
                    }
                } else {
                    AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                    audioManager.setStreamVolume(AudioManager.STREAM_RING, audioManager.getStreamVolume(AudioManager.STREAM_RING), AudioManager.FLAG_SHOW_UI);
                }
                break;
            case AppDrawer:
                if(HomeActivity._launcher!=null) {
                    HomeActivity._launcher.openAppDrawer();
                }
                break;
            case SearchBar:
                if(HomeActivity._launcher!=null) {
                    HomeActivity._launcher.getSearchBar().getSearchButton().performClick();
                }
                break;
            case MobileNetworkSettings:
                context.startActivity(new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS));
                break;
            case ShowNotifications:
                try {
                    Object statusBarService = context.getSystemService("statusbar");
                    Class<?> statusBarManager = Class.forName("android.app.StatusBarManager");
                    Method statusBarExpand = statusBarManager.getMethod("expandNotificationsPanel");
                    statusBarExpand.invoke(statusBarService);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case TurnOffScreen:
                try {
                    // still needs to reset screen timeout back to default on activity destroy
                    int defaultTurnOffTime =  Settings.System.getInt(context.getContentResolver(),Settings.System.SCREEN_OFF_TIMEOUT, 60000);
                    Settings.System.putInt(context.getContentResolver(),Settings.System.SCREEN_OFF_TIMEOUT, 1000);
                    Settings.System.putInt(context.getContentResolver(),Settings.System.SCREEN_OFF_TIMEOUT, defaultTurnOffTime);
                } catch (Exception e) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                    intent.setData(Uri.parse("package:" + context.getPackageName()));
                    context.startActivity(intent);
                }
                break;
        }
    }

    public static ActionDisplayItem getActionItem(int position) {
        // used for pick action dialog
        return getActionItem(Action.values()[position]);
    }

    public static ActionDisplayItem getActionItem(Action action) {
        return getActionItem(action.toString());
    }

    public static ActionDisplayItem getActionItem(String action) {
        for (ActionDisplayItem item : actionDisplayItems) {
            if (item._action.toString().equals(action)) {
                return item;
            }
        }
        return null;
    }

    public static class ActionDisplayItem {
        public Action _action;
        public String _label;
        public String _description;
        public int _icon;
        public int _id;

        public ActionDisplayItem(Action action, String label, String description, int icon, int id) {
            _action = action;
            _label = label;
            _description = description;
            _icon = icon;
            _id = id;
        }
    }
}
