package com.huivip.gpsspeedwidget.viewutil;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.huivip.gpsspeedwidget.R;
import com.huivip.gpsspeedwidget.activity.HomeActivity;
import com.huivip.gpsspeedwidget.manager.Setup;
import com.huivip.gpsspeedwidget.model.App;
import com.huivip.gpsspeedwidget.model.Item;
import com.huivip.gpsspeedwidget.util.DragAction;
import com.huivip.gpsspeedwidget.util.DragHandler;
import com.huivip.gpsspeedwidget.util.Tool;
import com.huivip.gpsspeedwidget.widget.AppItemView;
import com.huivip.gpsspeedwidget.widget.CellContainer;
import com.huivip.gpsspeedwidget.widget.WidgetView;

public class ItemViewFactory {
    public static View getItemView(final Context context, final DesktopCallback callback, final DragAction.Action type, final Item item) {
        View view = null;
        if (item.getType().equals(Item.Type.WIDGET)) {
            view = getWidgetView(context, callback, type, item);
        } else {
            AppItemView.Builder builder = new AppItemView.Builder(context);
            builder.setIconSize(Setup.appSettings().getIconSize());
            builder.vibrateWhenLongPress(Setup.appSettings().getGestureFeedback());
            builder.withOnLongClick(item, type, callback);
            switch(type) {
                case DRAWER:
                    builder.setLabelVisibility(Setup.appSettings().getDrawerShowLabel());
                    builder.setTextColor(Setup.appSettings().getDrawerLabelColor());
                    break;
                case DESKTOP:
                default:
                    builder.setLabelVisibility(Setup.appSettings().getDesktopShowLabel());
                    builder.setTextColor(Color.WHITE);
                    break;
            }
            switch (item.getType()) {
                case APP:
                    final App app = Setup.appLoader().findItemApp(item);
                    if (app == null) break;
                    view = builder.setAppItem(item).getView();
                    break;
                case SHORTCUT:
                    view = builder.setShortcutItem(item).getView();
                    break;
                case GROUP:
                    view = builder.setGroupItem(context, callback, item).getView();
                    view.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                    break;
                case ACTION:
                    view = builder.setActionItem(item).getView();
                    break;
            }
        }

        // TODO find out why tag is set here
        if (view != null) {
            view.setTag(item);
        }
        return view;
    }

    public static View getWidgetView(final Context context, final DesktopCallback callback, final DragAction.Action type, final Item item) {
        if (HomeActivity._appWidgetHost == null) return null;
        final AppWidgetProviderInfo appWidgetInfo = HomeActivity._appWidgetManager.getAppWidgetInfo(item.getWidgetValue());
        final WidgetView widgetView = (WidgetView) HomeActivity._appWidgetHost.createView(context, item.getWidgetValue(), appWidgetInfo);

        widgetView.setAppWidget(item.getWidgetValue(), appWidgetInfo);
        widgetView.post(new Runnable() {
            @Override
            public void run() {
                updateWidgetOption(item);
            }
        });

        final FrameLayout widgetContainer = (FrameLayout) LayoutInflater.from(context).inflate(R.layout.view_widget_container, null);
        widgetContainer.addView(widgetView);

        final View ve = widgetContainer.findViewById(R.id.vertexpand);
        ve.bringToFront();
        final View he = widgetContainer.findViewById(R.id.horiexpand);
        he.bringToFront();
        final View vl = widgetContainer.findViewById(R.id.vertless);
        vl.bringToFront();
        final View hl = widgetContainer.findViewById(R.id.horiless);
        hl.bringToFront();

        ve.animate().scaleY(1).scaleX(1);
        he.animate().scaleY(1).scaleX(1);
        vl.animate().scaleY(1).scaleX(1);
        hl.animate().scaleY(1).scaleX(1);

        final Runnable action = new Runnable() {
            @Override
            public void run() {
                ve.animate().scaleY(0).scaleX(0);
                he.animate().scaleY(0).scaleX(0);
                vl.animate().scaleY(0).scaleX(0);
                hl.animate().scaleY(0).scaleX(0);
            }
        };

        widgetContainer.postDelayed(action, 5000);
        // TODO move this to standard DragHandler.getLongClick() method
        // needs to be set on widgetView but use widgetContainer inside
        widgetView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (Setup.appSettings().getDesktopLock()) {
                    return false;
                }
                if (Setup.appSettings().getGestureFeedback()) {
                    Tool.vibrate(view);
                }
                DragHandler.startDrag(widgetContainer, item, DragAction.Action.DESKTOP, callback);
                return true;
            }
        });
        /*ve.setOnTouchListener(new OnTouchListener() {
            private float mInitialTouchX;
            private float mInitialTouchY;
            private int mInitialX;
            private int mInitialY;
            private int movedX;
            private int movedY;
            private long mStartClickTime;
            private boolean mIsClick;
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //final ViewGroup.LayoutParams params = v.getLayoutParams();

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mInitialTouchX = event.getRawX();
                        mInitialTouchY = event.getRawY();
                        mStartClickTime = System.currentTimeMillis();
                        //mIsClick = true;
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        float dX = event.getRawX() - mInitialTouchX;
                        float dY = event.getRawY() - mInitialTouchY;
                        if ((mIsClick && (Math.abs(dX) > 10 || Math.abs(dY) > 10))
                                || System.currentTimeMillis() - mStartClickTime > ViewConfiguration.getLongPressTimeout()) {
                            mIsClick = false;
                        }

                        //if (!mIsClick) {
                            movedX= (int) (dX + mInitialX);
                            movedY = (int) (dY + mInitialY);
                       // }
                        item.setSpanY(item.getSpanY()+movedY);
                        CellContainer.LayoutParams newWidgetLayoutParams = new CellContainer.LayoutParams(CellContainer.LayoutParams.WRAP_CONTENT, CellContainer.LayoutParams.WRAP_CONTENT, item.getX(), item.getY(), item.getSpanX(), item.getSpanY());

                        // update occupied array
                        HomeActivity.Companion.getLauncher().getDesktop().getCurrentPage().setOccupied(true, newWidgetLayoutParams);

                        // update the view
                        widgetContainer.setLayoutParams(newWidgetLayoutParams);
                        updateWidgetOption(item);
                        return true;
                    case MotionEvent.ACTION_UP:
                       //item.setSpanX(item.getSpanX()+movedX);
                       //item.setSpanY(item.getSpanY()+movedY);
                        Log.d("huivip","move x:"+movedX+",y:"+movedY);
                       //scaleWidget(widgetContainer,item);
                        item.setSpanY(item.getSpanY()+movedY);
                        CellContainer.LayoutParams newWidgetLayoutParams2 = new CellContainer.LayoutParams(CellContainer.LayoutParams.WRAP_CONTENT, CellContainer.LayoutParams.WRAP_CONTENT, item.getX(), item.getY(), item.getSpanX(), item.getSpanY());

                        // update occupied array
                        HomeActivity.Companion.getLauncher().getDesktop().getCurrentPage().setOccupied(true, newWidgetLayoutParams2);

                        // update the view
                        widgetContainer.setLayoutParams(newWidgetLayoutParams2);
                        updateWidgetOption(item);
                        HomeActivity._db.saveItem(item);

                        return true;
                }
                return true;
            }
        });*/
        ve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view.getScaleX() < 1) return;
                item.setSpanY(item.getSpanY() + 1);
                scaleWidget(widgetContainer, item);
                widgetContainer.removeCallbacks(action);
                widgetContainer.postDelayed(action, 2000);
            }
        });
        /*he.setOnTouchListener(new View.OnTouchListener() {
            private float mInitialTouchX;
            private float mInitialTouchY;
            private float movedX;
            private float movedY;
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mInitialTouchX = event.getRawX();
                        mInitialTouchY = event.getRawY();
                        Log.d("huivip","donw, x:"+mInitialTouchX+",y:"+mInitialTouchY);
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        movedX = event.getRawX() - mInitialTouchX;
                        movedY = event.getRawY() - mInitialTouchY;
                        Log.d("huivip","move,x;"+event.getRawX()+",y:"+event.getRawY());
                        //item.setSpanX((int) (item.getSpanX()+movedX));
                        //item.setSpanY((int) (item.getSpanY()+movedY));
                        if(movedX>0) {
                            item.setSpanX((int) (item.getSpanX() + (movedX / HomeActivity.Companion.getLauncher().getDesktop().getCurrentPage().getCellWidth()) + 1));
                        }
                        if(movedY>0) {
                            item.setSpanY((int) (item.getSpanY() + movedY / HomeActivity.Companion.getLauncher().getDesktop().getCurrentPage().getCellHeight() + 1));
                        }
                        Log.d("huivip","up x:"+movedX+",y:"+movedY);
                        scaleWidget(widgetContainer,item);
                        return true;
                    case MotionEvent.ACTION_UP:
                        item.setSpanX((int) (item.getSpanX()+(movedX/HomeActivity.Companion.getLauncher().getDesktop().getCurrentPage().getCellWidth())+1));
                        item.setSpanY((int) (item.getSpanY()+movedY/HomeActivity.Companion.getLauncher().getDesktop().getCurrentPage().getCellHeight()+1));
                        Log.d("huivip","up x:"+movedX+",y:"+movedY);
                        scaleWidget(widgetContainer,item);
                        return true;
                }
                return false;
            }
        });*/
        he.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view.getScaleX() < 1) return;
                item.setSpanX(item.getSpanX() + 1);
                scaleWidget(widgetContainer, item);
                widgetContainer.removeCallbacks(action);
                widgetContainer.postDelayed(action, 2000);
            }
        });
      /* vl.setOnTouchListener(new View.OnTouchListener() {
           private float mInitialTouchX;
           private float mInitialTouchY;
           private int mInitialX;
           private int mInitialY;
           private int movedX;
           private int movedY;
           @SuppressLint("ClickableViewAccessibility")
           @Override
           public boolean onTouch(View v, MotionEvent event) {
               switch (event.getAction()) {
                   case MotionEvent.ACTION_DOWN:
                       mInitialTouchX = event.getRawX();
                       mInitialTouchY = event.getRawY();
                       //mIsClick = true;
                       return true;
                   case MotionEvent.ACTION_MOVE:
                       float dX = event.getRawX() - mInitialTouchX;
                       float dY = event.getRawY() - mInitialTouchY;
                       *//* if ((mIsClick && (Math.abs(dX) > 10 || Math.abs(dY) > 10))
                                || System.currentTimeMillis() - mStartClickTime > ViewConfiguration.getLongPressTimeout()) {
                            mIsClick = false;
                        }*//*

                       //if (!mIsClick) {
                       movedX= (int) (dX + mInitialX);
                       movedY = (int) (dY + mInitialY);
                       // }
                       return true;
                   case MotionEvent.ACTION_UP:
                       //item.setSpanX(item.getSpanX()+movedX);
                       item.setSpanY(item.getSpanY()+movedY);
                       Log.d("huivip","move x:"+movedX+",y:"+movedY);
                       scaleWidget(widgetContainer,item);
                       return true;
               }
               return false;
           }
       });*/
        vl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view.getScaleX() < 1) return;
                item.setSpanY(item.getSpanY() - 1);
                scaleWidget(widgetContainer, item);
                widgetContainer.removeCallbacks(action);
                widgetContainer.postDelayed(action, 2000);
            }
        });
       /* hl.setOnTouchListener(new View.OnTouchListener() {
            private float mInitialTouchX;
            private float mInitialTouchY;
            private int mInitialX;
            private int mInitialY;
            private int movedX;
            private int movedY;
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mInitialTouchX = event.getRawX();
                        mInitialTouchY = event.getRawY();
                        //mIsClick = true;
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        float dX = event.getRawX() - mInitialTouchX;
                        float dY = event.getRawY() - mInitialTouchY;
                       *//* if ((mIsClick && (Math.abs(dX) > 10 || Math.abs(dY) > 10))
                                || System.currentTimeMillis() - mStartClickTime > ViewConfiguration.getLongPressTimeout()) {
                            mIsClick = false;
                        }*//*

                        //if (!mIsClick) {
                        movedX= (int) (dX + mInitialX);
                        movedY = (int) (dY + mInitialY);
                        // }
                        item.setSpanY(item.getSpanY()+movedY);
                        scaleWidget(widgetContainer,item);
                        return true;
                    case MotionEvent.ACTION_UP:
                        item.setSpanX(item.getSpanX()+movedX);
                        //item.setSpanY(item.getSpanY()+movedY);
                        Log.d("huivip","move x:"+movedX+",y:"+movedY);
                        scaleWidget(widgetContainer,item);
                        return true;
                }
                return false;
            }
        });*/
        hl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view.getScaleX() < 1) return;
                item.setSpanX(item.getSpanX() - 1);
                scaleWidget(widgetContainer, item);
                widgetContainer.removeCallbacks(action);
                widgetContainer.postDelayed(action, 2000);
            }
        });
        return widgetContainer;
    }

    private static void scaleWidget(View view, Item item) {
        item.setSpanX(Math.min(item.getSpanX(), HomeActivity.Companion.getLauncher().getDesktop().getCurrentPage().getCellSpanH()));
        item.setSpanX(Math.max(item.getSpanX(), 1));
        item.setSpanY(Math.min(item.getSpanY(), HomeActivity.Companion.getLauncher().getDesktop().getCurrentPage().getCellSpanV()));
        item.setSpanY(Math.max(item.getSpanY(), 1));

        HomeActivity.Companion.getLauncher().getDesktop().getCurrentPage().setOccupied(false, (CellContainer.LayoutParams) view.getLayoutParams());
        if (!HomeActivity.Companion.getLauncher().getDesktop().getCurrentPage().checkOccupied(new Point(item.getX(), item.getY()), item.getSpanX(), item.getSpanY())) {
            CellContainer.LayoutParams newWidgetLayoutParams = new CellContainer.LayoutParams(CellContainer.LayoutParams.WRAP_CONTENT, CellContainer.LayoutParams.WRAP_CONTENT, item.getX(), item.getY(), item.getSpanX(), item.getSpanY());

            // update occupied array
            HomeActivity.Companion.getLauncher().getDesktop().getCurrentPage().setOccupied(true, newWidgetLayoutParams);

            // update the view
            view.setLayoutParams(newWidgetLayoutParams);
            updateWidgetOption(item);

            // update the widget size in the database
            HomeActivity._db.saveItem(item);
        } else {
            Toast.makeText(HomeActivity.Companion.getLauncher().getDesktop().getContext(), R.string.toast_not_enough_space, Toast.LENGTH_SHORT).show();

            // add the old layout params to the occupied array
            HomeActivity.Companion.getLauncher().getDesktop().getCurrentPage().setOccupied(true, (CellContainer.LayoutParams) view.getLayoutParams());
        }
    }

    private static void updateWidgetOption(Item item) {
        Bundle newOps = new Bundle();
        if(HomeActivity.Companion.getLauncher().getDesktop().getCurrentPage()!=null) {
            newOps.putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, item.getSpanX() * HomeActivity.Companion.getLauncher().getDesktop().getCurrentPage().getCellWidth());
            newOps.putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, item.getSpanX() * HomeActivity.Companion.getLauncher().getDesktop().getCurrentPage().getCellWidth());
            newOps.putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, item.getSpanY() * HomeActivity.Companion.getLauncher().getDesktop().getCurrentPage().getCellHeight());
            newOps.putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, item.getSpanY() * HomeActivity.Companion.getLauncher().getDesktop().getCurrentPage().getCellHeight());
            HomeActivity._appWidgetManager.updateAppWidgetOptions(item.getWidgetValue(), newOps);
        }
    }
}
