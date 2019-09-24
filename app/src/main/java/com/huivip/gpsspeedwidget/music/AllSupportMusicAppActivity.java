package com.huivip.gpsspeedwidget.music;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.huivip.gpsspeedwidget.R;
import com.huivip.gpsspeedwidget.utils.PrefUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by baina on 18-1-3.
 * 显示所有支持audio相关app
 */
public class AllSupportMusicAppActivity extends Activity {

    private static final String TAG = AllSupportMusicAppActivity.class.getSimpleName();

    private List<MusicAppInfo> mMusicAppInfoList;
    private SupportMusicPlayerAdapter mAdapter;
    private ListView mAppListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_allsupportaudioapp);
        mAppListView = findViewById(R.id.appListView);
        mMusicAppInfoList = new ArrayList<>();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //耳机控制播放器的intent action
        Intent intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        List<ResolveInfo> infoList = getPackageManager().queryBroadcastReceivers(intent, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT);
        if (infoList.size() > 0) {
            mMusicAppInfoList.clear();
            for (ResolveInfo resolveInfo : infoList) {
                MusicAppInfo MusicAppInfo = new MusicAppInfo();
                //set Icon
                MusicAppInfo.setAppIcon(resolveInfo.loadIcon(getPackageManager()));
                //set Application Name
                MusicAppInfo.setAppLabel(resolveInfo.loadLabel(getPackageManager()).toString());
                //set Package Name
                MusicAppInfo.setAppPkg(resolveInfo.activityInfo.packageName);
                mMusicAppInfoList.add(MusicAppInfo);
            }
            mAdapter = new SupportMusicPlayerAdapter(AllSupportMusicAppActivity.this, mMusicAppInfoList);
            mAppListView.setAdapter(mAdapter);
            mAppListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    mAdapter.setCheckedPosition(position);
                    mAdapter.notifyDataSetChanged();
                    MusicAppInfo MusicAppInfo = mMusicAppInfoList.get(position);
                    Log.d(TAG, "你选择了:" + MusicAppInfo.getAppLabel());
                    PrefUtils.setSelectMusicPlayer(getApplicationContext(),MusicAppInfo.getAppPkg());
                }
            });
        }
    }
}
