package com.hy.greenbuilding.model;

public class RoomInfo {
    private int roomId;//房间ID
    private String roomName;//房间名称
    private int temp;//温度
    private int humidity;//湿度
    private int pm;
    private int co2;
    private String airValve;
    private int airQualityId;
    private String airQualityName;
    private int formaldehyde;
    private int tvoc;


    public int getAirQualityId() {
        return airQualityId;
    }

    public void setAirQualityId(int airQualityId) {
        this.airQualityId = airQualityId;
    }

    public String getAirQualityName() {
        return airQualityName;
    }

    public void setAirQualityName(String airQualityName) {
        this.airQualityName = airQualityName;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public int getTemp() {
        if (temp <= -32768) {
            return 0;
        }
        return temp;
    }

    public void setTemp(int temp) {
        this.temp = temp;
    }

    public int getHumidity() {
        return humidity;
    }

    public void setHumidity(int humidity) {
        this.humidity = humidity;
    }

    public int getPm() {
        return pm;
    }

    public void setPm(int pm) {
        this.pm = pm;
    }

    public int getCo2() {
        return co2;
    }

    public void setCo2(int co2) {
        this.co2 = co2;
    }

    public String getAirValve() {
        return airValve;
    }

    public void setAirValve(String airValve) {
        this.airValve = airValve;
    }

    public int getFormaldehyde() {
        return formaldehyde;
    }

    public void setFormaldehyde(int formaldehyde) {
        this.formaldehyde = formaldehyde;
    }

    public int getTvoc() {
        return tvoc;
    }

    public void setTvoc(int tvoc) {
        this.tvoc = tvoc;
    }
}
