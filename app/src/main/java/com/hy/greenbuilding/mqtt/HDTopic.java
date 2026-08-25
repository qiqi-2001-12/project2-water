package com.hy.greenbuilding.mqtt;

import com.hy.greenbuilding.utils.ByteUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 推送数据 (98bytes)
 * 小端模式发送
 */
public class HDTopic {
    private byte inTemp;//室内温度
    private byte inHumidity;//室内湿度
    private byte[] inCo2 = new byte[2];//室内Co2   2字节
    private byte[] inPM = new byte[2];//室内PM2.5  2字节
    private byte airMode;//除湿模式 0x00制冷 0x01制热  0x02除湿 0x03待机
    private byte airSwitch;//除湿开关
    private byte windStatus;//新风状态
    private byte circleStatus;//内循环状态
    private byte runMode;//运行模式
    private byte screenStatus;//滤网状态
    private byte deHumiditySwitch ;//除湿开关
    private byte setHumidity;//除湿湿度设定
    private byte tempMin;//控温温度下限
    private byte tempMax;//控温温度上限
    private byte bypassSwitch;//旁通开关
    private byte timingSwitch;//定时开关
    private byte[] timingDay = new byte[2];//定时天数
    private byte timeBefore1;
    private byte timeAfter1;
    private byte timeBefore2;
    private byte timeAfter2;
    private byte timeBefore3;
    private byte timeAfter3;

    private List<RoomBytes> roomByte = new ArrayList<>(9);

    public byte getInTemp() {
        return inTemp;
    }

    public void setInTemp(byte inTemp) {
        this.inTemp = inTemp;
    }

    public byte getInHumidity() {
        return inHumidity;
    }

    public void setInHumidity(byte inHumidity) {
        this.inHumidity = inHumidity;
    }

    public byte[] getInCo2() {
        return inCo2;
    }

    public void setInCo2(byte[] inCo2) {
        this.inCo2 = inCo2;
    }

    public byte[] getInPM() {
        return inPM;
    }

    public void setInPM(byte[] inPM) {
        this.inPM = inPM;
    }

    public byte getAirMode() {
        return airMode;
    }

    public void setAirMode(byte airMode) {
        this.airMode = airMode;
    }

    public byte getAirSwitch() {
        return airSwitch;
    }

    public void setAirSwitch(byte airSwitch) {
        this.airSwitch = airSwitch;
    }

    public byte getWindStatus() {
        return windStatus;
    }

    public void setWindStatus(byte windStatus) {
        this.windStatus = windStatus;
    }

    public byte getCircleStatus() {
        return circleStatus;
    }

    public void setCircleStatus(byte circleStatus) {
        this.circleStatus = circleStatus;
    }

    public byte getRunMode() {
        return runMode;
    }

    public void setRunMode(byte runMode) {
        this.runMode = runMode;
    }

    public byte getScreenStatus() {
        return screenStatus;
    }

    public void setScreenStatus(byte screenStatus) {
        this.screenStatus = screenStatus;
    }

    public byte getDeHumiditySwitch() {
        return deHumiditySwitch;
    }

    public void setDeHumiditySwitch(byte deHumiditySwitch) {
        this.deHumiditySwitch = deHumiditySwitch;
    }

    public byte getSetHumidity() {
        return setHumidity;
    }

    public void setSetHumidity(byte setHumidity) {
        this.setHumidity = setHumidity;
    }

    public byte getTempMin() {
        return tempMin;
    }

    public void setTempMin(byte tempMin) {
        this.tempMin = tempMin;
    }

    public byte getTempMax() {
        return tempMax;
    }

    public void setTempMax(byte tempMax) {
        this.tempMax = tempMax;
    }

    public byte getBypassSwitch() {
        return bypassSwitch;
    }

    public void setBypassSwitch(byte bypassSwitch) {
        this.bypassSwitch = bypassSwitch;
    }

    public byte getTimingSwitch() {
        return timingSwitch;
    }

    public void setTimingSwitch(byte timingSwitch) {
        this.timingSwitch = timingSwitch;
    }

    public byte[] getTimingDay() {
        return timingDay;
    }

    public void setTimingDay(byte[] timingDay) {
        this.timingDay = timingDay;
    }

    public byte getTimeBefore1() {
        return timeBefore1;
    }

    public void setTimeBefore1(byte timeBefore1) {
        this.timeBefore1 = timeBefore1;
    }

    public byte getTimeAfter1() {
        return timeAfter1;
    }

    public void setTimeAfter1(byte timeAfter1) {
        this.timeAfter1 = timeAfter1;
    }

    public byte getTimeBefore2() {
        return timeBefore2;
    }

    public void setTimeBefore2(byte timeBefore2) {
        this.timeBefore2 = timeBefore2;
    }

    public byte getTimeAfter2() {
        return timeAfter2;
    }

    public void setTimeAfter2(byte timeAfter2) {
        this.timeAfter2 = timeAfter2;
    }

    public byte getTimeBefore3() {
        return timeBefore3;
    }

    public void setTimeBefore3(byte timeBefore3) {
        this.timeBefore3 = timeBefore3;
    }

    public byte getTimeAfter3() {
        return timeAfter3;
    }

    public void setTimeAfter3(byte timeAfter3) {
        this.timeAfter3 = timeAfter3;
    }

    public List<RoomBytes> getRoomByte() {
        return roomByte;
    }

    public void setRoomByte(List<RoomBytes> roomByte) {
        this.roomByte = roomByte;
    }
    public byte[] getBytes() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            outputStream.write(inTemp);//室内温度 1
            outputStream.write(inHumidity);//室内湿度
            outputStream.write(ByteUtils.changeBytes(inCo2)); //室内Co2   2字节
            outputStream.write(ByteUtils.changeBytes(inPM)); //室内PM2.5  2字节
            outputStream.write(airMode);//除湿模式 0x00制冷 0x01制热  0x02除湿 0x03待机
            outputStream.write(airSwitch);//除湿开关
            outputStream.write(windStatus);//新风状态
            outputStream.write(circleStatus);//内循环状态
            outputStream.write(runMode);//运行模式
            outputStream.write(screenStatus);//滤网状态
            outputStream.write(deHumiditySwitch);//除湿开关
            outputStream.write(setHumidity);
            outputStream.write(tempMin);
            outputStream.write(tempMax);
            outputStream.write(bypassSwitch);
            outputStream.write(timingSwitch);
            outputStream.write(ByteUtils.changeBytes(timingDay));
            outputStream.write(timeBefore1);
            outputStream.write(timeAfter1);
            outputStream.write(timeBefore2);
            outputStream.write(timeAfter2);
            outputStream.write(timeBefore3);
            outputStream.write(timeAfter3);
            if (roomByte != null && roomByte.size() == 9) {
                for (int i = 0; i < roomByte.size(); i++) {
                    outputStream.write(roomByte.get(i).getNotifyStatus());
                    outputStream.write(roomByte.get(i).getRoomTemp());
                    outputStream.write(roomByte.get(i).getRoomHumidity());
                    outputStream.write(ByteUtils.changeBytes(roomByte.get(i).getRoomCo2()));
                    outputStream.write(ByteUtils.changeBytes(roomByte.get(i).getRoomPm()));
                    outputStream.write(roomByte.get(i).getFanSwitch());
                }
            }else{
                outputStream.write(new byte[72]);
            }
            return outputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
