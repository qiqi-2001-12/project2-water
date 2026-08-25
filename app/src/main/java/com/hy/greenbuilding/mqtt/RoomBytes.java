package com.hy.greenbuilding.mqtt;

public class RoomBytes{
    private byte notifyStatus;//房间通讯状态
    private byte roomTemp;//房间温度
    private byte roomHumidity;//房间湿度
    private byte[] roomCo2 = new byte[2];
    private byte[] roomPm = new byte[2];
    private byte[] roomFormaldehyde = new byte[2];
    private byte[] roomTvoc = new byte[2];
    private byte fanSwitch;//风阀开关

    public RoomBytes(){

    }

    public byte getNotifyStatus() {
        return notifyStatus;
    }

    public void setNotifyStatus(byte notifyStatus) {
        this.notifyStatus = notifyStatus;
    }

    public byte getRoomTemp() {
        return roomTemp;
    }

    public void setRoomTemp(byte roomTemp) {
        this.roomTemp = roomTemp;
    }

    public byte getRoomHumidity() {
        return roomHumidity;
    }

    public void setRoomHumidity(byte roomHumidity) {
        this.roomHumidity = roomHumidity;
    }

    public byte[] getRoomCo2() {
        return roomCo2;
    }

    public void setRoomCo2(byte[] roomCo2) {
        this.roomCo2 = roomCo2;
    }

    public byte[] getRoomPm() {
        return roomPm;
    }

    public void setRoomPm(byte[] roomPm) {
        this.roomPm = roomPm;
    }

    public byte getFanSwitch() {
        return fanSwitch;
    }

    public void setFanSwitch(byte fanSwitch) {
        this.fanSwitch = fanSwitch;
    }

    public byte[] getRoomFormaldehyde() {
        return roomFormaldehyde;
    }

    public void setRoomFormaldehyde(byte[] roomFormaldehyde) {
        this.roomFormaldehyde = roomFormaldehyde;
    }

    public byte[] getRoomTvoc() {
        return roomTvoc;
    }

    public void setRoomTvoc(byte[] roomTvoc) {
        this.roomTvoc = roomTvoc;
    }
}
