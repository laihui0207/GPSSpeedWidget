package com.huivip.gpsspeedwidget.beans;

public class WeatherEvent {
    String city;
    String altitude;
    String weather;
    String temperature;

    public WeatherEvent(String city, String altitude, String weather, String temperature) {
        this.city = city;
        this.altitude = altitude;
        this.weather = weather;
        this.temperature = temperature;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getAltitude() {
        return altitude;
    }

    public void setAltitude(String altitude) {
        this.altitude = altitude;
    }

    public String getWeather() {
        return weather;
    }

    public void setWeather(String weather) {
        this.weather = weather;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }
}
