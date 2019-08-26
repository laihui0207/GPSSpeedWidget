package com.huivip.gpsspeedwidget.beans;

public class SearchTrafficEvent {
    public static String CIRCRL_QUERY="circle";
    public static String ROAD_QUERY="road";
    double latitude;
    double longitude;
    String roadName;
    int radius;
    int roadLevel;
    String adCode;
    String queryType;

    public SearchTrafficEvent(double latitude, double longitude, int radius, int roadLevel) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = radius;
        this.roadLevel = roadLevel;
        queryType=CIRCRL_QUERY;
    }

    public SearchTrafficEvent( String adCode,String roadName, int roadLevel) {
        this.roadName = roadName;
        this.roadLevel = roadLevel;
        this.adCode = adCode;
        queryType=ROAD_QUERY;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getRoadName() {
        return roadName;
    }

    public void setRoadName(String roadName) {
        this.roadName = roadName;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public int getRoadLevel() {
        return roadLevel;
    }

    public void setRoadLevel(int roadLevel) {
        this.roadLevel = roadLevel;
    }

    public String getAdCode() {
        return adCode;
    }

    public void setAdCode(String adCode) {
        this.adCode = adCode;
    }

    public String getQueryType() {
        return queryType;
    }

    public void setQueryType(String queryType) {
        this.queryType = queryType;
    }
}
