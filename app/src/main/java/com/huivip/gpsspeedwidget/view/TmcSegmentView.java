package com.huivip.gpsspeedwidget.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class TmcSegmentView extends View {
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

    public void setSegments(List<SegmentModel> segments) {
        if (segments == null) {
            return;
        }
       this.segments = segments;
        Collections.sort(this.segments, new Comparator<SegmentModel>() {
            @Override
            public int compare(SegmentModel o1, SegmentModel o2) {
                return o1.number - o2.number;
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
        mPaint.setColor(color[5]);
        canvas.drawRect(0, 0, getWidth(), getHeight(), mPaint);
        int left = 0;
        for (SegmentModel segment : segments) {
            if (segment.status < 0 || segment.status > 4) {
                segment.status = 5;
            }
            mPaint.setColor(color[segment.status]);
            int w = (int) (getWidth() * (segment.percent* 1f / 100));
            int right = left + w;
            canvas.drawRect(left, 0, right, getHeight(), mPaint);
            left = right;
        }
    }

    public static class SegmentModel {
        private int status;//每段柱状图信息 -1 无效, 0 无交通流(蓝色), 1 畅通（绿色）, 2 缓行（黄色）, 3 拥堵（红色）, 4 严重拥堵（深红色）, 10 行驶过的路段（灰色）
        private int number;//路况柱状图每段的编号，编号越小越靠近起点
        private int distance;// 路况柱状图每段的路程距离，单位米，所有段加起来的距离等于剩余总路程距离（每段柱状图的百分比为tmc_segment_distance除以residual_distance的值）
        private float percent;

        public int getStatus() {
            return status;
        }

        public SegmentModel setStatus(int status) {
            this.status = status;
            return this;
        }

        public int getNumber() {
            return number;
        }

        public SegmentModel setNumber(int number) {
            this.number = number;
            return this;
        }

        public int getDistance() {
            return distance;
        }

        public SegmentModel setDistance(int distance) {
            this.distance = distance;
            return this;
        }

        public float getPercent() {
            return percent;
        }

        public SegmentModel setPercent(float percent) {
            this.percent = percent;
            return this;
        }

        @Override
        public String toString() {
            return "SegmentModel{" +
                    "status=" + status +
                    ", number=" + number +
                    ", distance=" + distance +
                    '}';
        }
    }
}
