package com.example.newsproject;

public class Weather {

    String icon;
    String maxTemp;
    String minTemp;
    String date;

    public Weather(String icon, String maxTemp, String minTemp, String date) {
        this.icon = icon;
        this.maxTemp = maxTemp;
        this.minTemp = minTemp;
        this.date = date;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getMaxTemp() {
        return maxTemp;
    }

    public void setMaxTemp(String maxTemp) {
        this.maxTemp = maxTemp;
    }

    public String getMinTemp() {
        return minTemp;
    }

    public void setMinTemp(String minTemp) {
        this.minTemp = minTemp;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
