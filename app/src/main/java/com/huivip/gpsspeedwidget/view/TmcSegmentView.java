package com.huivip.gpsspeedwidget.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

import com.huivip.gpsspeedwidget.model.SegmentModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class TmcSegmentView extends AppCompatImageView {
    public static final String TAG = "TMcSegmentView";

    public TmcSegmentView(Context context) {
        super(context);
        init();
    }

    public TmcSegmentView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        segments = new ArrayList<>();
    }

    private List<SegmentModel> segments;
    private int total=0;
    public void setSegments(List<SegmentModel> segments) {
        this.segments.clear();
        if (segments == null) {
            return;
        }
       /* this.total=0;
        for(SegmentModel model:segments){
            if(model.status>=0 && model.status<=4){
                this.segments.add(model);
                this.total+=model.distance;
            }
        }*/
       this.segments = segments;
        Collections.sort(this.segments, new Comparator<SegmentModel>() {
            @Override
            public int compare(SegmentModel o1, SegmentModel o2) {
                return o1.getNumber()- o2.getNumber();
            }
        });
        invalidate();
    }

    private Paint mPaint;

    private int[] color = {
            Color.parseColor("#4cb6f6"),//蓝色
            Color.parseColor("#6ae128"), //绿色
            Color.parseColor("#fcfd12"), //黄色
            Color.parseColor("#ff7b69"), //浅红色
            Color.parseColor("#B22222"), //深红色
            Color.GRAY};

    @Override
    protected void onDraw(Canvas canvas) {
        if (segments == null || segments.size()==0) {
            return;
        }
        mPaint.setColor(color[1]);
        canvas.drawRect(0, 0, getWidth(), getHeight(), mPaint);
        int left = 0;
        for (SegmentModel segment : segments) {
            if (segment.getStatus()< 0 || segment.getStatus()> 4) {
                segment.setStatus(5);
            }
            mPaint.setColor(color[segment.getStatus()]);
            int w = (int) (getWidth() * segment.getPercent()*1.0f/100);
            int right = left + w;
            canvas.drawRect(left, 0, right, getHeight(), mPaint);
            left = right;
        }
    }


}
