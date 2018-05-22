package com.huivip.gpsspeedwidget.appselection;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.crashlytics.android.Crashlytics;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.huivip.gpsspeedwidget.BuildConfig;
import com.huivip.gpsspeedwidget.R;
import com.huivip.gpsspeedwidget.detection.AppDetectionService;
import com.huivip.gpsspeedwidget.utils.PrefUtils;
import com.pluscubed.recyclerfastscroll.RecyclerFastScroller;
import rx.SingleSubscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

public class AppSelectionActivity extends AppCompatActivity {

    public static final String STATE_SELECTED_APPS = "state_selected_apps";
    public static final String STATE_SELECTED_APPS_NAME = "state_selected_apps_name";
    public static final String STATE_APPS = "state_apps";
    //public static final String STATE_MAP_APPS = "state_map_apps";
    //public static final String STATE_MAPS_ONLY = "state_maps_only";

    private AppAdapter mAdapter;
    private RecyclerFastScroller mScroller;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private Set<String> mSelectedApps;
    private Set<String> mSelectedAppsName;
    private List<AppInfo> mAppList;
    //private List<AppInfo> mMapApps;

   // private boolean mMapsOnly;

    private CompositeSubscription mLoadAppsSubscription;
    private boolean mLoadingAppList;
    //private boolean mLoadingMapApps;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appselection);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        mAdapter = new AppAdapter();
        recyclerView.setAdapter(mAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mScroller = (RecyclerFastScroller) findViewById(R.id.fastscroller);
        mScroller.attachRecyclerView(recyclerView);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                /*if (mMapsOnly) {
                    reloadMapApps();
                } else {*/
                    reloadInstalledApps();
                /*}*/
                mAdapter.setAppInfos(new ArrayList<AppInfo>());
            }
        });
        mSwipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(this, R.color.colorAccent));

        mLoadAppsSubscription = new CompositeSubscription();
        if (savedInstanceState != null) {
         /*   mMapsOnly = true;
        } else {*/
            mAppList = savedInstanceState.getParcelableArrayList(STATE_APPS);
           // mMapApps = savedInstanceState.getParcelableArrayList(STATE_MAP_APPS);
            //mMapsOnly = false;// savedInstanceState.getBoolean(STATE_MAPS_ONLY);
            mSelectedApps = new HashSet<>(savedInstanceState.getStringArrayList(STATE_SELECTED_APPS));
            mSelectedAppsName = new HashSet<>(savedInstanceState.getStringArrayList(STATE_SELECTED_APPS_NAME));

        }

        /*if (mMapApps == null) {
            reloadMapApps();
        } else if (mMapsOnly) {
            mAdapter.setAppInfos(mMapApps);
        }*/

        if (mAppList == null) {
            reloadInstalledApps();
        } else /*if (!mMapsOnly) */{
            mAdapter.setAppInfos(mAppList);
        }

        setTitle("选择自启动程序");
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (mLoadingAppList /*|| mLoadingMapApps*/) {
            mSwipeRefreshLayout.setRefreshing(true);
        }
    }

    private void reloadInstalledApps() {
        mLoadingAppList = true;
        mSwipeRefreshLayout.setRefreshing(true);
        mSelectedApps = new HashSet<>(PrefUtils.getAutoLaunchApps(this));
        mSelectedAppsName=new HashSet<>(PrefUtils.getAutoLaunchAppsName(this));
        Subscription subscription = SelectedAppDatabase.getInstalledApps(this)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleSubscriber<List<AppInfo>>() {
                    @Override
                    public void onSuccess(List<AppInfo> installedApps) {
                       /* if (!mMapsOnly) {*/
                            mAdapter.setAppInfos(installedApps);
                            mSwipeRefreshLayout.setRefreshing(false);
                      /*  }*/
                        mAppList = installedApps;

                        mLoadingAppList = false;
                    }

                    @Override
                    public void onError(Throwable error) {
                        error.printStackTrace();
                        if (!BuildConfig.DEBUG) {
                            Crashlytics.logException(error);
                        }
                    }
                });
        mLoadAppsSubscription.add(subscription);
    }

    /*private void reloadMapApps() {
        mLoadingMapApps = true;
        mSwipeRefreshLayout.setRefreshing(true);
        mSelectedApps = PrefUtils.getAutoLaunchApps(this);
        Subscription subscription = SelectedAppDatabase.getMapApps(this)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleSubscriber<List<AppInfo>>() {
                    @Override
                    public void onSuccess(List<AppInfo> mapApps) {
                        if (mMapsOnly) {
                            mAdapter.setAppInfos(mapApps);
                            mSwipeRefreshLayout.setRefreshing(false);
                        }
                        mMapApps = mapApps;

                        mLoadingMapApps = false;
                    }

                    @Override
                    public void onError(Throwable error) {

                    }
                });
        mLoadAppsSubscription.add(subscription);
    }*/

    @Override
    protected void onDestroy() {
        mLoadAppsSubscription.unsubscribe();
        mLoadAppsSubscription = null;
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_app_selection, menu);
       // MenuItem item = menu.findItem(R.id.menu_app_selection_maps);
      /*  Drawable drawable-v17 = AppCompatResources.getDrawable(this, R.drawable-v17.ic_map_white_24dp).mutate();
        drawable-v17 = DrawableCompat.wrap(drawable-v17);*/
       /* if (mMapsOnly) {
            DrawableCompat.setTint(drawable-v17, ContextCompat.getColor(this, R.color.colorAccent));
        }*/
       // item.setIcon(drawable-v17);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_app_selection_done:
                finish();
                return true;
            /*case R.id.menu_app_selection_maps:
                //mMapsOnly = !mMapsOnly;
                invalidateOptionsMenu();
                mAdapter.setAppInfos(mAppList);
                *//*mSwipeRefreshLayout.setRefreshing(
                        mMapsOnly && mLoadingMapApps ||
                                !mMapsOnly && mLoadingAppList);*//*
                return true;*/
        }
        return super.onOptionsItemSelected(item);
    }

    private void onItemClick(final AppInfo appInfo, final boolean checked) {
        if (appInfo.packageName != null && !appInfo.packageName.isEmpty()) {
            if (checked) {
                mSelectedApps.add(appInfo.packageName);
                mSelectedAppsName.add(appInfo.name);
            } else {
                mSelectedApps.remove(appInfo.packageName);
                mSelectedAppsName.remove(appInfo.name);
            }

            SingleSubscriber<Object> subscriber = new SingleSubscriber<Object>() {
                @Override
                public void onSuccess(Object value) {

                }

                @Override
                public void onError(Throwable error) {
                    error.printStackTrace();
                    if (!BuildConfig.DEBUG)
                        Crashlytics.logException(error);
                }
            };
            PrefUtils.setAutoLaunchApps(this, mSelectedApps);
            PrefUtils.setAutoLaunchAppsName(this,mSelectedAppsName);
            if (AppDetectionService.get() != null) {
                AppDetectionService.get().updateSelectedApps();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(STATE_APPS, (ArrayList<AppInfo>) mAppList);
        //outState.putParcelableArrayList(STATE_MAP_APPS, (ArrayList<AppInfo>) mMapApps);
        //outState.putBoolean(STATE_MAPS_ONLY, mMapsOnly);
        outState.putStringArrayList(STATE_SELECTED_APPS, new ArrayList<>(mSelectedApps));
        outState.putStringArrayList(STATE_SELECTED_APPS_NAME,new ArrayList<>(mSelectedAppsName));
        super.onSaveInstanceState(outState);
    }

    private class AppAdapter extends RecyclerView.Adapter<AppAdapter.ViewHolder> {
        private List<AppInfo> mAppInfos;

        public AppAdapter() {
            super();
            setHasStableIds(true);
            mAppInfos = new ArrayList<>();
        }

        public void setAppInfos(List<AppInfo> list) {
            if (list == null) {
                mAppInfos = new ArrayList<>();
            } else {
                mAppInfos = list;
            }
            notifyDataSetChanged();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.list_item_app, parent, false);
            return new ViewHolder(view);
        }


        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            final AppInfo app = mAppInfos.get(position);

            Glide.with(AppSelectionActivity.this)
                    .load(app)
                    .crossFade()
                    .into(holder.icon);

            holder.title.setText(app.name);
            holder.desc.setText(app.packageName);
            holder.checkbox.setChecked(mSelectedApps.contains(app.packageName));

        }

        @Override
        public int getItemCount() {
            return mAppInfos.size();
        }

        @Override
        public long getItemId(int position) {
            return mAppInfos.get(position).packageName.hashCode();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView icon;
            TextView title;
            TextView desc;
            CheckBox checkbox;

            public ViewHolder(View itemView) {
                super(itemView);

                icon = (ImageView) itemView.findViewById(R.id.image_app);
                title = (TextView) itemView.findViewById(R.id.text_name);
                desc = (TextView) itemView.findViewById(R.id.text_desc);
                checkbox = (CheckBox) itemView.findViewById(R.id.checkbox);

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        checkbox.toggle();

                        int adapterPosition = getAdapterPosition();
                        if (adapterPosition != RecyclerView.NO_POSITION) {
                            onItemClick(mAppInfos.get(adapterPosition), checkbox.isChecked());
                        }
                    }
                });
            }
        }
    }
}
