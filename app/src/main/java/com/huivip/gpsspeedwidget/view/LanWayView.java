/*
package com.huivip.gpsspeedwidget.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
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
    public static final int IMG_WIDTH = 22;
    public static final int IMG_HEIGHT = 39;
    private int[] driveWayBackgroundId={};
    private int[] driveWayForegroundId = {};
    private int[] driveWayAutoForegroundId = {};
    LayoutParams imgLp;
    LayoutParams lp;
    private Bitmap[] driveWayBitMaps = null;
    private Bitmap[] driveWayBitMapBgs = null;
    private int height;
    private int width;
    public static final SparseIntArray driveWayIcon = new SparseIntArray();
    static {
        driveWayIcon.append(0,R.drawable.landback_0);
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
        driveWayBackgroundId=new int[]{R.drawable.landback_0,R.drawable.landback_1,
                R.drawable.landback_2,R.drawable.landback_3,R.drawable.landback_4,
                R.drawable.landback_5,R.drawable.landback_6,R.drawable.landback_7,
                R.drawable.landback_8,R.drawable.landback_9,R.drawable.landback_a,
                R.drawable.landback_b,R.drawable.landback_c,R.drawable.landback_d,
                R.drawable.landback_e,0,R.drawable.landback_10,R.drawable.landback_11,
                R.drawable.landback_12,R.drawable.landback_13,R.drawable.landback_14,
                R.drawable.landback_15,0,R.drawable.landback_17};
        driveWayAutoForegroundId=new int[]{R.drawable.auto_landback_0,R.drawable.auto_landback_1,
                R.drawable.auto_landback_2,R.drawable.auto_landback_3,R.drawable.auto_landback_4,
                R.drawable.auto_landback_5,R.drawable.auto_landback_6,R.drawable.auto_landback_7,
                R.drawable.auto_landback_8,R.drawable.auto_landback_9,R.drawable.auto_landback_a,
                R.drawable.auto_landback_b,R.drawable.auto_landback_c,R.drawable.auto_landback_d,
                R.drawable.auto_landback_e,0,R.drawable.auto_landback_10,R.drawable.auto_landback_11,
                R.drawable.auto_landback_12,R.drawable.auto_landback_13,R.drawable.auto_landback_14,
                R.drawable.auto_landback_15,0,R.drawable.auto_landback_17};
        driveWayForegroundId=new int[]{R.drawable.landfront_0,R.drawable.landfront_1,R.drawable.landback_2,
                R.drawable.landfront_3,R.drawable.landback_4,R.drawable.landfront_5,R.drawable.landback_6,R.drawable.landback_7,
                R.drawable.landfront_8,R.drawable.landback_9,R.drawable.landback_a,R.drawable.landback_b,R.drawable.landback_c,
                R.drawable.landfront_d,R.drawable.landback_e,0,R.drawable.landback_10,R.drawable.landback_11,R.drawable.landback_12,R.drawable.landback_13,
                R.drawable.landback_14,R.drawable.landfront_15,0,R.drawable.landfront_17};
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public LanWayView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public View createImageView(int front, int background) {
        ImageView imageView = new ImageView(this.getContext());
        Bitmap fontBitmap = BitmapFactory.decodeResource(getResources(), front);
        imageView.setImageBitmap(fontBitmap);
        imageView.setBackgroundDrawable(getResources().getDrawable(background));
        imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        return imageView;
    }
    public int buildDriveWay(int fronend, int backend)
    {
        if (isComplexLane(fronend)) {
            return complexBitmap(fronend, backend);
        }
        if (isThisLaneRecommended(backend)) {
            return driveWayAutoForegroundId[backend];
        }
        return -1;
    }
    public  int complexBitmap(int paramInt1, int paramInt2)
    {
        int i = 0;
        if (paramInt1 == 10)
        {
            if (paramInt2 == 0) {
                i =    R.drawable.landfront_a0;
            } else if (paramInt2 == 8) {
                i = R.drawable.landfront_a8;
            }
        }
        else if (paramInt1 == 9)
        {
            if (paramInt2 == 0) {
                i = R.drawable.landfront_90;
            } else if (paramInt2 == 5) {
                i = R.drawable.landfront_95;
            }
        }
        else if (paramInt1 == 2)
        {
            if (paramInt2 == 0) {
                i = R.drawable.landfront_20;
            } else if (paramInt2 == 1) {
                i = R.drawable.landfront_21;
            }
        }
        else if (paramInt1 == 4)
        {
            if (paramInt2 == 0) {
                i = R.drawable.landfront_40;
            } else if (paramInt2 == 3) {
                i = R.drawable.landfront_43;
            }
        }
        else if (paramInt1 == 6)
        {
            if (paramInt2 == 1) {
                i = R.drawable.landfront_61;
            } else if (paramInt2 == 3) {
                i = R.drawable.landfront_63;
            }
        }
        else if (paramInt1 == 7)
        {
            if (paramInt2 == 0) {
                i = R.drawable.landfront_70;
            } else if (paramInt2 == 1) {
                i = R.drawable.landfront_71;
            } else if (paramInt2 == 3) {
                i = R.drawable.landfront_73;
            }
        }
        else if (paramInt1 == 11)
        {
            if (paramInt2 == 5) {
                i = R.drawable.landfront_b5;
            } else if (paramInt2 == 1) {
                i = R.drawable.landfront_b1;
            }
        }
        else if (paramInt1 == 12)
        {
            if (paramInt2 == 8) {
                i = R.drawable.landfront_c8;
            } else if (paramInt2 == 3) {
                i = R.drawable.landfront_c3;
            }
        }
        else if (paramInt1 == 14)
        {
            if (paramInt2 == 1) {
                i = R.drawable.landfront_e1;
            } else if (paramInt2 == 5) {
                i = R.drawable.landfront_e5;
            }
        }
        else if (paramInt1 == 16)
        {
            if (paramInt2 == 0) {
                i = R.drawable.landfront_100;
            } else if (paramInt2 == 1) {
                i =  R.drawable.landfront_101;
            } else if (paramInt2 == 5) {
                i =  R.drawable.landfront_105;
            }
        }
        else if (paramInt1 == 17)
        {
            if (paramInt2 == 5) {
                i =  R.drawable.landfront_115;
            } else if (paramInt2 == 3) {
                i =  R.drawable.landfront_113;
            }
        }
        else if (paramInt1 == 18)
        {
            if (paramInt2 == 1) {
                i =  R.drawable.landfront_121;
            }
            else if (paramInt2 == 3) {
                i =  R.drawable.landfront_123;
            }else if (paramInt2 == 5) {
                i =  R.drawable.landfront_125;
            }
        }
        else if (paramInt1 == 19)
        {
            if (paramInt2 == 0) {
                i = R.drawable.landfront_130;
            } else if (paramInt2 == 3) {
                i = R.drawable.landfront_133;
            } else if (paramInt2 == 5) {
                i = R.drawable.landfront_135;
            }
        }
        else if (paramInt1 == 20)
        {
            if (paramInt2 == 1) {
                i = R.drawable.landfront_141;
            } else if (paramInt2 == 8) {
                i = R.drawable.landfront_148;
            }
        }
        if(i==0){
            return driveWayBackgroundId[0];
        }
        return i;
    }
    public static boolean isThisLaneRecommended(int paramInt)
    {
        return paramInt != 255;
    }
    public static boolean isComplexLane(int paramInt)
    {
        return (paramInt == 14) || (paramInt == 2) || (paramInt == 4) || (paramInt == 9) || (paramInt == 10) || (paramInt == 11) || (paramInt == 12) || (paramInt == 6) || (paramInt == 7) || (paramInt == 16) || (paramInt == 17) || (paramInt == 18) || (paramInt == 19) || (paramInt == 20) || (paramInt > 20);
    }
    public Bitmap createBitmap(Bitmap paramBitmap, int paramInt1, int paramInt2)
    {
        int i = paramBitmap.getWidth();
        int j = paramBitmap.getHeight();
        paramInt1 = paramInt1 / i;
        paramInt2 = paramInt2 / j;
        Matrix localMatrix;
        (localMatrix = new Matrix()).postScale(paramInt1, paramInt2);

        return Bitmap.createBitmap(paramBitmap, 0, 0, i, j, localMatrix, true);
    }
    Bitmap produceFinalBitmap()
    {
        try
        {
            Bitmap localBitmap = Bitmap.createBitmap(this.width, this.height, Bitmap.Config.ARGB_8888);
            Canvas localCanvas;
            (localCanvas = new Canvas(localBitmap)).drawColor(-16777216);
            int i = 0;
            while (i < this.driveWaySize)
            {
                if (this.driveWayBitMaps[i] != null) {
                    localCanvas.drawBitmap(this.driveWayBitMaps[i], i * this.driveWayWidth, 0.0F, null);
                }
                i++;
            }
            return localBitmap;
        }
        catch (Exception e)
        {
        }
        return null;
    }
}
*/
