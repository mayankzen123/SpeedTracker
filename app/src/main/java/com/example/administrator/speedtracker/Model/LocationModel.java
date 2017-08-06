package com.example.administrator.speedtracker.Model;

/**
 * Created by Administrator on 8/6/2017.
 */

public class LocationModel {
    String date, latitude, longitude, currentDuration, nextDuration;

    public LocationModel(String date, String latitude, String longitude, String currentDuration, String nextDuration) {
        this.date = date;
        this.latitude = latitude;
        this.longitude = longitude;
        this.currentDuration = currentDuration;
        this.nextDuration = nextDuration;
    }

    public String getDate() {
        return date;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getCurrentDuration() {
        return currentDuration;
    }

    public String getNextDuration() {
        return nextDuration;
    }
}
