package com.hy.greenbuilding.event;


public class SettingUpdateEvent {
    private int type;
    private String humidity;
    private String humidity1;
    private String tempMin;
    private String tempMax;
    private boolean isHumidity;
    private boolean isTimingSwitch;
    public SettingUpdateEvent(int type){
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public String getHumidity1() {
        return humidity1;
    }

    public boolean isHumidity() {
        return isHumidity;
    }

    public void setHumidity(boolean humidity) {
        isHumidity = humidity;
    }

    public void setHumidity1(String humidity1) {
        this.humidity1 = humidity1;
    }

    public String getHumidity() {
        return humidity;
    }

    public void setHumidity(String humidity) {
        this.humidity = humidity;
    }

    public String getTempMin() {
        return tempMin;
    }

    public void setTempMin(String tempMin) {
        this.tempMin = tempMin;
    }

    public String getTempMax() {
        return tempMax;
    }

    public void setTempMax(String tempMax) {
        this.tempMax = tempMax;
    }

    public void setType(int type) {
        this.type = type;
    }

    public boolean isTimingSwitch() {
        return isTimingSwitch;
    }

    public void setTimingSwitch(boolean timingSwitch) {
        isTimingSwitch = timingSwitch;
    }
}
