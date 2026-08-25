package com.hy.greenbuilding.mqtt;

import com.hy.greenbuilding.utils.ByteUtils;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * 空调监测数据（23bytes）
 * 转为小端模式发送
 */
public class HFTopic {
    private byte switchStatus;//开关状态 0x00关0x01开
    private byte hotSwitch = (byte)0x00;//电辅热开关
    private short frequency;//压机频率
    private short setTemp;//设定温度
    private short termTemp = (byte)0x00;//压机温度
    private short inTemp;//室内温度
    private short outTemp;//室外温度
    private short inHotTemp = 0;//室内热交温度
    private short outHotTemp;//室外热交温度
    private short inAuxTemp;//室内辅助热交温度
    private byte  airConditionMode;//空调模式
    private byte  windSpeed;//风速
    private byte  airConditionStatus;//强力/安静
    private byte  newWind;//换新风功能
    private byte errorCode = (byte)0x00 ;//故障代码

    public byte getSwitchStatus() {
        return switchStatus;
    }

    public void setSwitchStatus(byte switchStatus) {
        this.switchStatus = switchStatus;
    }

    public byte getHotSwitch() {
        return hotSwitch;
    }

    public void setHotSwitch(byte hotSwitch) {
        this.hotSwitch = hotSwitch;
    }

    public short getFrequency() {
        return frequency;
    }

    public void setFrequency(short frequency) {
        this.frequency = frequency;
    }

    public short getSetTemp() {
        return setTemp;
    }

    public void setSetTemp(short setTemp) {
        this.setTemp = setTemp;
    }

    public short getTermTemp() {
        return termTemp;
    }

    public void setTermTemp(short termTemp) {
        this.termTemp = termTemp;
    }

    public short getInTemp() {
        return inTemp;
    }

    public void setInTemp(short inTemp) {
        this.inTemp = inTemp;
    }

    public short getOutTemp() {
        return outTemp;
    }

    public void setOutTemp(short outTemp) {
        this.outTemp = outTemp;
    }

    public short getInHotTemp() {
        return inHotTemp;
    }

    public void setInHotTemp(short inHotTemp) {
        this.inHotTemp = inHotTemp;
    }

    public short getOutHotTemp() {
        return outHotTemp;
    }

    public void setOutHotTemp(short outHotTemp) {
        this.outHotTemp = outHotTemp;
    }

    public short getInAuxTemp() {
        return inAuxTemp;
    }

    public void setInAuxTemp(short inAuxTemp) {
        this.inAuxTemp = inAuxTemp;
    }

    public byte getAirConditionMode() {
        return airConditionMode;
    }

    public void setAirConditionMode(byte airConditionMode) {
        this.airConditionMode = airConditionMode;
    }

    public byte getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(byte windSpeed) {
        this.windSpeed = windSpeed;
    }

    public byte getAirConditionStatus() {
        return airConditionStatus;
    }

    public void setAirConditionStatus(byte airConditionStatus) {
        this.airConditionStatus = airConditionStatus;
    }

    public byte getNewWind() {
        return newWind;
    }

    public void setNewWind(byte newWind) {
        this.newWind = newWind;
    }

    public void setErrorCode(byte errorCode) {
        this.errorCode = errorCode;
    }

    public byte[] getBytes() {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(switchStatus);
            outputStream.write(hotSwitch);
            outputStream.write(ByteUtils.changeBytes(ByteUtils.int16ToByteArray(frequency)));
            outputStream.write(ByteUtils.changeBytes(ByteUtils.int16ToByteArray(setTemp)));
            outputStream.write(ByteUtils.changeBytes(ByteUtils.int16ToByteArray(termTemp)));
            outputStream.write(ByteUtils.changeBytes(ByteUtils.int16ToByteArray(inTemp)));
            outputStream.write(ByteUtils.changeBytes(ByteUtils.int16ToByteArray(outTemp)));
            outputStream.write(ByteUtils.changeBytes(ByteUtils.int16ToByteArray(inHotTemp)));
            outputStream.write(ByteUtils.changeBytes(ByteUtils.int16ToByteArray(outHotTemp)));
            outputStream.write(ByteUtils.changeBytes(ByteUtils.int16ToByteArray(inAuxTemp)));
            outputStream.write(airConditionMode);
            outputStream.write(windSpeed);
            outputStream.write(newWind);
            outputStream.write(airConditionStatus);
            outputStream.write((byte)0x00);
            return outputStream.toByteArray();
        }catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }
}
