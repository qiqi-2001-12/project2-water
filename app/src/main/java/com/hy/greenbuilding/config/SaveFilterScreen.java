package com.hy.greenbuilding.config;

/**
 * 保存滤网数据
 */
public class SaveFilterScreen {
    private String freshAirUse;//滤网使用
    private String exhaustUse;
    private String circle1Use;
    private String circle2Use;
    private String freshAirChange;//滤网更换
    private String exhaustChange;
    private String circle1Change;
    private String circle2Change;
    private String freshAirPressure;//滤网压差
    private String exhaustPressure;
    private String circle1Pressure;
    private String circle2Pressure;

    private boolean isFreshAirUseTime = false ;
    private boolean isExhaustUseTime;
    private boolean isCircle1UseTime;
    private boolean isCircle2UseTime;
    private boolean isFreshAirUsePressure;
    private boolean isExhaustUsePressure;
    private boolean isCircle1UsePressure;
    private boolean isCircle2UsePressure;

    private String freshAirPressureValue;//滤网压差
    private String exhaustPressureValue;
    private String circle1PressureValue;
    private String circle2PressureValue;

    public String getFreshAirUse() {
        return freshAirUse;
    }

    public void setFreshAirUse(String freshAirUse) {
        this.freshAirUse = freshAirUse;
    }

    public String getExhaustUse() {
        return exhaustUse;
    }

    public void setExhaustUse(String exhaustUse) {
        this.exhaustUse = exhaustUse;
    }

    public String getCircle1Use() {
        return circle1Use;
    }

    public void setCircle1Use(String circle1Use) {
        this.circle1Use = circle1Use;
    }

    public String getCircle2Use() {
        return circle2Use;
    }

    public void setCircle2Use(String circle2Use) {
        this.circle2Use = circle2Use;
    }

    public String getFreshAirChange() {
        return freshAirChange;
    }

    public void setFreshAirChange(String freshAirChange) {
        this.freshAirChange = freshAirChange;
    }

    public String getExhaustChange() {
        return exhaustChange;
    }

    public void setExhaustChange(String exhaustChange) {
        this.exhaustChange = exhaustChange;
    }

    public String getCircle1Change() {
        return circle1Change;
    }

    public void setCircle1Change(String circle1Change) {
        this.circle1Change = circle1Change;
    }

    public String getCircle2Change() {
        return circle2Change;
    }

    public void setCircle2Change(String circle2Change) {
        this.circle2Change = circle2Change;
    }

    public String getFreshAirPressure() {
        return freshAirPressure;
    }

    public void setFreshAirPressure(String freshAirPressure) {
        this.freshAirPressure = freshAirPressure;
    }

    public String getExhaustPressure() {
        return exhaustPressure;
    }

    public void setExhaustPressure(String exhaustPressure) {
        this.exhaustPressure = exhaustPressure;
    }

    public String getCircle1Pressure() {
        return circle1Pressure;
    }

    public void setCircle1Pressure(String circle1Pressure) {
        this.circle1Pressure = circle1Pressure;
    }

    public String getCircle2Pressure() {
        return circle2Pressure;
    }

    public void setCircle2Pressure(String circle2Pressure) {
        this.circle2Pressure = circle2Pressure;
    }

    public boolean isFreshAirUseTime() {
        return isFreshAirUseTime;
    }

    public void setFreshAirUseTime(boolean freshAirUseTime) {
        isFreshAirUseTime = freshAirUseTime;
    }

    public boolean isExhaustUseTime() {
        return isExhaustUseTime;
    }

    public void setExhaustUseTime(boolean exhaustUseTime) {
        isExhaustUseTime = exhaustUseTime;
    }

    public boolean isCircle1UseTime() {
        return isCircle1UseTime;
    }

    public void setCircle1UseTime(boolean circle1UseTime) {
        isCircle1UseTime = circle1UseTime;
    }

    public boolean isCircle2UseTime() {
        return isCircle2UseTime;
    }

    public void setCircle2UseTime(boolean circle2UseTime) {
        isCircle2UseTime = circle2UseTime;
    }

    public boolean isFreshAirUsePressure() {
        return isFreshAirUsePressure;
    }

    public void setFreshAirUsePressure(boolean freshAirUsePressure) {
        isFreshAirUsePressure = freshAirUsePressure;
    }

    public boolean isExhaustUsePressure() {
        return isExhaustUsePressure;
    }

    public void setExhaustUsePressure(boolean exhaustUsePressure) {
        isExhaustUsePressure = exhaustUsePressure;
    }

    public boolean isCircle1UsePressure() {
        return isCircle1UsePressure;
    }

    public void setCircle1UsePressure(boolean circle1UsePressure) {
        isCircle1UsePressure = circle1UsePressure;
    }

    public boolean isCircle2UsePressure() {
        return isCircle2UsePressure;
    }

    public void setCircle2UsePressure(boolean circle2UsePressure) {
        isCircle2UsePressure = circle2UsePressure;
    }

    public String getFreshAirPressureValue() {
        return freshAirPressureValue;
    }

    public void setFreshAirPressureValue(String freshAirPressureValue) {
        this.freshAirPressureValue = freshAirPressureValue;
    }

    public String getExhaustPressureValue() {
        return exhaustPressureValue;
    }

    public void setExhaustPressureValue(String exhaustPressureValue) {
        this.exhaustPressureValue = exhaustPressureValue;
    }

    public String getCircle1PressureValue() {
        return circle1PressureValue;
    }

    public void setCircle1PressureValue(String circle1PressureValue) {
        this.circle1PressureValue = circle1PressureValue;
    }

    public String getCircle2PressureValue() {
        return circle2PressureValue;
    }

    public void setCircle2PressureValue(String circle2PressureValue) {
        this.circle2PressureValue = circle2PressureValue;
    }
}
