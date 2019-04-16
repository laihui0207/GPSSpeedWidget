package com.huivip.gpsspeedwidget.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.SparseIntArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.huivip.gpsspeedwidget.R;

public class LanWayView extends LinearLayout {
    private int driveWayWidth;
    private int driveWayHeight;
    private int driveWaySize;
    private int mItemLineWidth;
    private int[] driveWayBackgroundId;
    LayoutParams imgLp;
    LayoutParams lp;
    public static final SparseIntArray driveWayIcon = new SparseIntArray();
    static {
        driveWayIcon.append(0,R.drawable.auto_landback_0);
    }
    public LanWayView(Context context) {
        super(context);
    }

    public LanWayView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public LanWayView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        driveWayHeight=0;
        driveWayWidth=0;
        driveWaySize =0;
        mItemLineWidth =0;
        driveWayBackgroundId=new int[]{R.drawable.auto_landback_0,R.drawable.auto_landback_1,
                R.drawable.auto_landback_2,R.drawable.auto_landback_3,R.drawable.auto_landback_4,
                R.drawable.auto_landback_5,R.drawable.auto_landback_6,R.drawable.auto_landback_7,
                R.drawable.auto_landback_8,R.drawable.auto_landback_9,R.drawable.auto_landback_a,
                R.drawable.auto_landback_b,R.drawable.auto_landback_c,R.drawable.auto_landback_d,
                R.drawable.auto_landback_e,0,R.drawable.auto_landback_10,R.drawable.auto_landback_11,
                R.drawable.auto_landback_12,R.drawable.auto_landback_13,R.drawable.auto_landback_14,
                R.drawable.auto_landback_15,0,R.drawable.auto_landback_17};
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public LanWayView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    private View createImageView(int front, int background) {
        ImageView imageView = new ImageView(this.getContext());
        Bitmap fontBitmap = BitmapFactory.decodeResource(getResources(), front);
        imageView.setImageBitmap(fontBitmap);
        imageView.setBackgroundDrawable(getResources().getDrawable(background));
        imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        return imageView;
    }
}
