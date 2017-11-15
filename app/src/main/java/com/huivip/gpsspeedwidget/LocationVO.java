package com.huivip.gpsspeedwidget;

import java.io.Serializable;

public class LocationVO implements Serializable {
    String lng;
    String lat;
    String speed;
    Double speedValue;
    Float bearingValue;
    Long createTime;
    Long lineId;

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public String getSpeed() {
        return speed;
    }

    public void setSpeed(String speed) {
        this.speed = speed;
    }

    public Double getSpeedValue() {
        return speedValue;
    }

    public void setSpeedValue(Double speedValue) {
        this.speedValue = speedValue;
    }

    public Float getBearingValue() {
        return bearingValue;
    }

    public void setBearingValue(Float bearingValue) {
        this.bearingValue = bearingValue;
    }

    public Long getLineId() {
        return lineId;
    }

    public void setLineId(Long lineId) {
        this.lineId = lineId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LocationVO that = (LocationVO) o;

        if (lng != null ? !lng.equals(that.lng) : that.lng != null) return false;
        if (lat != null ? !lat.equals(that.lat) : that.lat != null) return false;
        if (speed != null ? !speed.equals(that.speed) : that.speed != null) return false;
        if (speedValue != null ? !speedValue.equals(that.speedValue) : that.speedValue != null) return false;
        if (bearingValue != null ? !bearingValue.equals(that.bearingValue) : that.bearingValue != null) return false;
        if (createTime != null ? !createTime.equals(that.createTime) : that.createTime != null) return false;
        return lineId != null ? lineId.equals(that.lineId) : that.lineId == null;
    }

    @Override
    public int hashCode() {
        int result = lng != null ? lng.hashCode() : 0;
        result = 31 * result + (lat != null ? lat.hashCode() : 0);
        result = 31 * result + (speed != null ? speed.hashCode() : 0);
        result = 31 * result + (speedValue != null ? speedValue.hashCode() : 0);
        result = 31 * result + (bearingValue != null ? bearingValue.hashCode() : 0);
        result = 31 * result + (createTime != null ? createTime.hashCode() : 0);
        result = 31 * result + (lineId != null ? lineId.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "LocationVO{" +
                "lng='" + lng + '\'' +
                ", lat='" + lat + '\'' +
                ", speed='" + speed + '\'' +
                ", speedValue=" + speedValue +
                ", bearingValue=" + bearingValue +
                ", createTime=" + createTime +
                ", lineId=" + lineId +
                '}';
    }
}
