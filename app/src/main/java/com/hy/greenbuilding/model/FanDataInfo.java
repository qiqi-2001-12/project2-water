package com.hy.greenbuilding.model;

public class FanDataInfo {
    private String fanAddress;//风机地址
    private int fanStatus;//风机状态
    private int interfaceType;//接口类型
    private int vrefModel;//Vref(体积流量参考值)
    private String windValue;//当前风量
    private String fanError;//风机错误码
    private int PwmFanStatus;//PWM风机状态
    private int pwmCloseGear;//PWM风机档位
    private int pwmSmallGear;
    private int pwmMiddleGear;
    private int pwmBigGear;
    private int closeGear;
    private int smallGear;
    private int middleGear;
    private int bigGear;

    private int staticPressure;
    private int setPressure;
    private int realPressure;
    private int screenPressure;

    public int getVrefModel() {
        return vrefModel;
    }

    public void setVrefModel(int vrefModel) {
        this.vrefModel = vrefModel;
    }

    public int getPwmCloseGear() {
        return pwmCloseGear;
    }

    public void setPwmCloseGear(int pwmCloseGear) {
        this.pwmCloseGear = pwmCloseGear;
    }

    public int getPwmSmallGear() {
        return pwmSmallGear;
    }

    public void setPwmSmallGear(int pwmSmallGear) {
        this.pwmSmallGear = pwmSmallGear;
    }

    public int getPwmMiddleGear() {
        return pwmMiddleGear;
    }

    public void setPwmMiddleGear(int pwmMiddleGear) {
        this.pwmMiddleGear = pwmMiddleGear;
    }

    public int getPwmBigGear() {
        return pwmBigGear;
    }

    public void setPwmBigGear(int pwmBigGear) {
        this.pwmBigGear = pwmBigGear;
    }

    public int getCloseGear() {
        return closeGear;
    }

    public void setCloseGear(int closeGear) {
        this.closeGear = closeGear;
    }

    public int getSmallGear() {
        return smallGear;
    }

    public void setSmallGear(int smallGear) {
        this.smallGear = smallGear;
    }

    public int getMiddleGear() {
        return middleGear;
    }

    public void setMiddleGear(int middleGear) {
        this.middleGear = middleGear;
    }

    public int getBigGear() {
        return bigGear;
    }

    public void setBigGear(int bigGear) {
        this.bigGear = bigGear;
    }

    public String getFanAddress() {
        return fanAddress;
    }

    public void setFanAddress(String fanAddress) {
        this.fanAddress = fanAddress;
    }

    public int getFanStatus() {
        return fanStatus;
    }

    public void setFanStatus(int fanStatus) {
        this.fanStatus = fanStatus;
    }

    public int getInterfaceType() {
        return interfaceType;
    }

    public void setInterfaceType(int interfaceType) {
        this.interfaceType = interfaceType;
    }

    public int getvrefModel() {
        return vrefModel;
    }

    public void setvrefModel(int vrefModel) {
        this.vrefModel = vrefModel;
    }

    public String getWindValue() {
        return windValue;
    }

    public void setWindValue(String windValue) {
        this.windValue = windValue;
    }

    public String getFanError() {
        return fanError;
    }

    public void setFanError(String fanError) {
        this.fanError = fanError;
    }

    public int getPwmFanStatus() {
        return PwmFanStatus;
    }

    public void setPwmFanStatus(int pwmFanStatus) {
        PwmFanStatus = pwmFanStatus;
    }

    public int getStaticPressure() {
        return staticPressure;
    }

    public void setStaticPressure(int staticPressure) {
        this.staticPressure = staticPressure;
    }

    public int getSetPressure() {
        return setPressure;
    }

    public void setSetPressure(int setPressure) {
        this.setPressure = setPressure;
    }

    public int getRealPressure() {
        return realPressure;
    }

    public void setRealPressure(int realPressure) {
        this.realPressure = realPressure;
    }

    public int getScreenPressure() {
        return screenPressure;
    }

    public void setScreenPressure(int screenPressure) {
        this.screenPressure = screenPressure;
    }
}
