package com.hy.greenbuilding.model;

public class AirValveItemInfo {
    private int valveId;
    private int address;//地址
    private int pressureDiff;//压差值
    private int maxNumber;//最大步进数
    private int setOpenValue;//设置开度值
    private int realOpenValue;//当前开度值

    public int getValveId() {
        return valveId;
    }

    public void setValveId(int valveId) {
        this.valveId = valveId;
    }

    public int getMaxNumber() {
        return maxNumber;
    }

    public void setMaxNumber(int maxNumber) {
        this.maxNumber = maxNumber;
    }

    public int getSetOpenValue() {
        return setOpenValue;
    }

    public void setSetOpenValue(int setOpenValue) {
        this.setOpenValue = setOpenValue;
    }

    public int getRealOpenValue() {
        return realOpenValue;
    }

    public void setRealOpenValue(int realOpenValue) {
        this.realOpenValue = realOpenValue;
    }

    public int getAddress() {
        return address;
    }

    public void setAddress(int address) {
        this.address = address;
    }

    public int getPressureDiff() {
        return pressureDiff;
    }

    public void setPressureDiff(int pressureDiff) {
        this.pressureDiff = pressureDiff;
    }

}
