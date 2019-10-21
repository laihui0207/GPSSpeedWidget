package com.huivip.gpsspeedwidget.model;

public class SegmentModel {
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
