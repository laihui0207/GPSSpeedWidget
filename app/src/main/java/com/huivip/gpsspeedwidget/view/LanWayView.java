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
    private int[] driveWayForegroundId = { 2130837664, 2130837666, 2130837643, 2130837671, 2130837645, 2130837676, 2130837647, 2130837648, 2130837685, 2130837650, 2130837651, 2130837652, 2130837653, 2130837699, 2130837655, 2130837656, 2130837657, 2130837658, 2130837659, 2130837660, 2130837661, 2130837662 };
    LayoutParams imgLp;
    LayoutParams lp;
    private Bitmap[] driveWayBitMaps = null;
    private Bitmap[] driveWayBitMapBgs = null;
    private int height;
    private int width;
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
    private Bitmap complexBitmap(int paramInt1, int paramInt2)
    {
        int i = 0;
        if (paramInt1 == 10)
        {
            if (paramInt2 == 0) {
                i = 2130837689;
            } else if (paramInt2 == 8) {
                i = 2130837690;
            }
        }
        else if (paramInt1 == 9)
        {
            if (paramInt2 == 0) {
                i = 2130837686;
            } else if (paramInt2 == 5) {
                i = 2130837687;
            }
        }
        else if (paramInt1 == 2)
        {
            if (paramInt2 == 0) {
                i = 2130837667;
            } else if (paramInt2 == 1) {
                i = 2130837668;
            }
        }
        else if (paramInt1 == 4)
        {
            if (paramInt2 == 0) {
                i = 2130837672;
            } else if (paramInt2 == 3) {
                i = 2130837673;
            }
        }
        else if (paramInt1 == 6)
        {
            if (paramInt2 == 1) {
                i = 2130837677;
            } else if (paramInt2 == 3) {
                i = 2130837678;
            }
        }
        else if (paramInt1 == 7)
        {
            if (paramInt2 == 0) {
                i = 2130837680;
            } else if (paramInt2 == 1) {
                i = 2130837681;
            } else if (paramInt2 == 3) {
                i = 2130837682;
            }
        }
        else if (paramInt1 == 11)
        {
            if (paramInt2 == 5) {
                i = 2130837693;
            } else if (paramInt2 == 1) {
                i = 2130837692;
            }
        }
        else if (paramInt1 == 12)
        {
            if (paramInt2 == 8) {
                i = 2130837696;
            } else if (paramInt2 == 3) {
                i = 2130837695;
            }
        }
        else if ((paramInt1 == 14) || (paramInt1 == 20))
        {
            if (paramInt2 == 1) {
                i = 2130837700;
            } else if (paramInt2 == 5) {
                i = 2130837701;
            }
        }
        else if (paramInt1 == 16)
        {
            if (paramInt2 == 0) {
                i = 2130837703;
            } else if (paramInt2 == 1) {
                i = 2130837704;
            } else if (paramInt2 == 5) {
                i = 2130837705;
            }
        }
        else if (paramInt1 == 17)
        {
            if (paramInt2 == 8) {
                i = 2130837696;
            } else if (paramInt2 == 3) {
                i = 2130837695;
            }
        }
        else if (paramInt1 == 18)
        {
            if (paramInt2 == 1) {
                i = 2130837718;
            } else if (paramInt2 == 5) {
                i = 2130837719;
            }
        }
        else if (paramInt1 == 19)
        {
            if (paramInt2 == 0) {
                i = 2130837680;
            } else if (paramInt2 == 3) {
                i = 2130837682;
            } else if (paramInt2 == 8) {
                i = 2130837681;
            }
        }
        else if (paramInt1 == 21) {
            i = 2130837721;
        } else if (paramInt1 == 23) {
            i = 2130837662;
        }
        Bitmap bitmap=BitmapFactory.decodeResource(getContext().getResources(), i);
        if (bitmap == null) {
            bitmap = BitmapFactory.decodeResource(getContext().getResources(), this.driveWayBackgroundId[paramInt1]);
        }
        return createBitmap(bitmap, this.driveWayWidth, this.driveWayHeight);
    }

    private boolean isComplexLane(int paramInt)
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
