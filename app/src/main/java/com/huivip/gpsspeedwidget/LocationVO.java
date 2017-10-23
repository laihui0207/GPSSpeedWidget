package com.huivip.gpsspeedwidget;

import java.io.Serializable;
import java.util.Date;

public class LocationVO implements Serializable {
    String lng;
    String lat;
    Long createTime;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LocationVO that = (LocationVO) o;

        if (lng != null ? !lng.equals(that.lng) : that.lng != null) return false;
        if (lat != null ? !lat.equals(that.lat) : that.lat != null) return false;
        return createTime != null ? createTime.equals(that.createTime) : that.createTime == null;
    }

    @Override
    public int hashCode() {
        int result = lng != null ? lng.hashCode() : 0;
        result = 31 * result + (lat != null ? lat.hashCode() : 0);
        result = 31 * result + (createTime != null ? createTime.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "LocationVO{" +
                "lng='" + lng + '\'' +
                ", lat='" + lat + '\'' +
                ", createTime=" + createTime +
                '}';
    }
}
