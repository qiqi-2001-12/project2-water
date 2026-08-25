package com.hy.greenbuilding.config;

/**
 * 保存主控板数据
 */
public class SaveControlInfo {

    private String control_version;//主控机软件版本
    private int lowPower;//低功耗模式
    private int runMode;//运行模式
    private String humidity = "45";//湿度
    private int humidity1 = 35;//湿度回差值
    private String tempMax;//温度上限
    private String tempMin;//温度下限
    private int outTermType;//室外机类型
    private int manualMode; //手动模式

    public String getControl_version() {
        return control_version;
    }


    public void setControl_version(String control_version) {
        this.control_version = control_version;
    }

    public int getHumidity1() {
        return humidity1;
    }

    public void setHumidity1(int humidity1) {
        this.humidity1 = humidity1;
    }

    public int getLowPower() {
        return lowPower;
    }

    public void setLowPower(int lowPower) {
        this.lowPower = lowPower;
    }


    public int getRunMode() {
        return runMode;
    }

    public void setRunMode(int runMode) {
        this.runMode = runMode;
    }

    public String getHumidity() {
        return humidity;
    }

    public void setHumidity(String humidity) {
        this.humidity = humidity;
    }

    public String getTempMax() {
        return tempMax;
    }

    public void setTempMax(String tempMax) {
        this.tempMax = tempMax;
    }

    public String getTempMin() {
        return tempMin;
    }

    public void setTempMin(String tempMin) {
        this.tempMin = tempMin;
    }

    public int getOutTermType() {
        return outTermType;
    }

    public void setOutTermType(int outTermType) {
        this.outTermType = outTermType;
    }

    public int getManualMode() {
        return manualMode;
    }

    public void setManualMode(int manualMode) {
        this.manualMode = manualMode;
    }
}


