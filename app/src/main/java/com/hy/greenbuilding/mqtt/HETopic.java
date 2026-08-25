package com.hy.greenbuilding.mqtt;

import com.hy.greenbuilding.utils.ByteUtils;
import com.hy.greenbuilding.utils.DigitalUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * 用电及室外环境(15bytes)
 * 转为小端模式发送
 */
public class HETopic {
    private int totalElectric;//总电量
    private short currentPower;//当前功率
    private short electric;//电流
    private short voltage;//电压
    private short pm ;//pm2.5
    private byte outHumidity;//室外湿度
    private byte outTemp;//室外温度
    private byte signal = (byte)0x00;//强起信号

    public int getTotalElectric() {
        return totalElectric;
    }

    public void setTotalElectric(int totalElectric) {
        this.totalElectric = totalElectric;
    }

    public short getCurrentPower() {
        return currentPower;
    }

    public void setCurrentPower(short currentPower) {
        this.currentPower = currentPower;
    }

    public short getElectric() {
        return electric;
    }

    public void setElectric(short electric) {
        this.electric = electric;
    }

    public short getVoltage() {
        return voltage;
    }

    public void setVoltage(short voltage) {
        this.voltage = voltage;
    }

    public short getPm() {
        return pm;
    }

    public void setPm(short pm) {
        this.pm = pm;
    }

    public byte getOutHumidity() {
        return outHumidity;
    }

    public void setOutHumidity(byte outHumidity) {
        this.outHumidity = outHumidity;
    }

    public byte getOutTemp() {
        return outTemp;
    }

    public void setOutTemp(byte outTemp) {
        this.outTemp = outTemp;
    }

    public byte getSignal() {
        return signal;
    }

    public void setSignal(byte signal) {
        this.signal = signal;
    }

    public byte[] getBytes() {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(ByteUtils.changeBytes(DigitalUtil.int2bytesBy32(totalElectric)));
            outputStream.write(ByteUtils.changeBytes(ByteUtils.int16ToByteArray(currentPower)));
            outputStream.write(ByteUtils.changeBytes(ByteUtils.int16ToByteArray(electric)));
            outputStream.write(ByteUtils.changeBytes(ByteUtils.int16ToByteArray(voltage)));
            outputStream.write(ByteUtils.changeBytes(ByteUtils.int16ToByteArray(pm)));
            outputStream.write(outHumidity);
            outputStream.write(outTemp);
            outputStream.write(signal);
            return outputStream.toByteArray();
        }catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }
}
