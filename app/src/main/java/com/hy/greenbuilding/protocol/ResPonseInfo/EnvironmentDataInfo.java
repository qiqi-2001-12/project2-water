package com.hy.greenbuilding.protocol.ResPonseInfo;


import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hwellyi.smarthome.HYJniService;
import com.hwellyi.smarthome.PublicUse;
import com.hy.greenbuilding.model.AirQualityInfo;
import com.hy.greenbuilding.model.RoomInfo;
import com.hy.greenbuilding.mqtt.HDTopic;
import com.hy.greenbuilding.mqtt.MqttUploadManager;
import com.hy.greenbuilding.mqtt.RoomBytes;
import com.hy.greenbuilding.protocol.FunctionObject;
import com.hy.greenbuilding.protocol.SpDataProcessor;
import com.hy.greenbuilding.protocol.command.EnvironmentCommand;
import com.hy.greenbuilding.utils.ByteUtils;
import com.hy.greenbuilding.utils.Hex;
import com.hy.greenbuilding.utils.MySpUtil;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 环境检测数据
 */
public class EnvironmentDataInfo {
    private byte[] environmentData;
    private List<byte[]> list;
    private byte[][] splitData = new byte[9][];
    private List<RoomInfo> roomList;
    private List<RoomBytes> roomBytesList = new ArrayList<>();
    //private List<RoomInfo> saveRoomList = new ArrayList<>();
    AirQualityInfo qualityInfo;
    public EnvironmentDataInfo(byte[] data) {
        this.environmentData = data;
        list = new ArrayList<>();
        roomList = new ArrayList<>();
        Log.i("info", "..environment data.." + Hex.bytesToHexString(this.environmentData));
        if (environmentData != null && environmentData.length == 0x006E) {
            byte[] bytes = Arrays.copyOfRange(environmentData, 2, environmentData.length);
            if (bytes.length == 0x006E - 2) {
                splitBytes(bytes, 12);
            }
        }
    }

    //环境检测故障码
    public byte[] getRoomError() {
        return Arrays.copyOfRange(environmentData, 0, 2);
    }

    public byte[] getRoomErrorBit() {
        return ByteUtils.getBitArray(Arrays.copyOfRange(environmentData, 0, 2));
    }

    /**
     * 拆分
     */
    public List<byte[]> splitBytes(byte[] bytes, int size) {
        if (list != null) {
            list.clear();
        }
        double splitLength = Double.parseDouble(size + "");
        int arrayLength = (int) Math.ceil(bytes.length / splitLength);
        splitData = new byte[arrayLength][];
        int from, to;
        for (int i = 0; i < arrayLength; i++) {
            from = (int) (i * splitLength);
            to = (int) (from + splitLength);
            if (to > bytes.length)
                to = bytes.length;
            splitData[i] = Arrays.copyOfRange(bytes, from, to);
            list.add(splitData[i]);
        }
        return list;
    }

    /**
     * 获取房间数据
     */
    public List<RoomInfo> getRoomData(Context mContext) {
        if (roomBytesList != null) {
            roomBytesList.clear();
        }
        String roomJson = MySpUtil.getParam(mContext, MySpUtil.ROOM_DATA, new Gson().toJson(roomList)).toString();
        List<RoomInfo> saveRoomList = new Gson().fromJson(roomJson, new TypeToken<List<RoomInfo>>() {
        }.getType());
        if (saveRoomList == null) {
            saveRoomList = new ArrayList<>();
        }
        AirQualityInfo qualityInfo= new AirQualityInfo();
        if(PublicUse.mJniFunCB != null){
            String tempJsonString = PublicUse.mJniFunCB.onGetDeviceTypeInfo(0, (1 << HYJniService.SUB_DEVICE_TYPE_ENV_DETECTOR));
            AirQualityInfo gatewayQualityInfo = new Gson().fromJson(tempJsonString, AirQualityInfo.class);
            if (gatewayQualityInfo != null) {
                qualityInfo = gatewayQualityInfo;
            }
        }
        for (int i = 0; i < list.size(); i++) {
            RoomBytes roomBytes = new RoomBytes();
            byte[] roomData = list.get(i);
            if (roomData.length < 12) {
                break;
            }
            int pm = ByteUtils.byteArrayToInt16(Arrays.copyOfRange(roomData, 0, 2));
            int temp = ByteUtils.byteArrayToInt16(Arrays.copyOfRange(roomData, 2, 4));
            int humidity = ByteUtils.byteArrayToInt16(Arrays.copyOfRange(roomData, 4, 6));
            int co2 = ByteUtils.byteArrayToInt16(Arrays.copyOfRange(roomData, 6, 8));
            int formaldehyde = ByteUtils.byteArrayToInt16(Arrays.copyOfRange(roomData, 8, 10));
            int tvoc = ByteUtils.byteArrayToInt16(Arrays.copyOfRange(roomData, 10, 12));

            // The saved list can contain custom rooms and is not guaranteed to be ordered by ID.
            RoomInfo roomInfo = findRoomById(saveRoomList, i);
            if (roomInfo == null) {
                roomInfo = new RoomInfo();
                roomInfo.setRoomId(i);
                roomInfo.setRoomName("房间" + (i + 1));
                roomInfo.setAirValve("关");
                saveRoomList.add(roomInfo);
            }

            roomInfo.setTemp(new BigDecimal((float) temp / 10)
                    .setScale(0, BigDecimal.ROUND_DOWN).intValue());
            roomInfo.setPm(pm);
            roomInfo.setCo2(co2);
            roomInfo.setHumidity(humidity);
            roomInfo.setFormaldehyde(formaldehyde);
            roomInfo.setTvoc(tvoc);

            byte[] errorBits = getRoomErrorBit();
            roomBytes.setNotifyStatus(errorBits[(errorBits.length - 1) - i] == 1
                    ? (byte) 0x01 : (byte) 0x00);
            if(qualityInfo.getDevlist() != null && roomInfo.getAirQualityId() != 0){
                for (int j =0;j< qualityInfo.getDevlist().size();j++){
                    if(qualityInfo.getDevlist().get(j).getId() == roomInfo.getAirQualityId()){
                        roomInfo.setTemp(qualityInfo.getDevlist().get(j).getTemp());
                        roomInfo.setPm(qualityInfo.getDevlist().get(j).getPM25());
                        roomInfo.setCo2(qualityInfo.getDevlist().get(j).getCO2());
                        roomInfo.setHumidity(qualityInfo.getDevlist().get(j).getHumi());
                        roomBytes.setNotifyStatus((byte) 0x01);
                        roomInfo.setFormaldehyde(qualityInfo.getDevlist().get(j).getFormaldehyde());
                        roomInfo.setTvoc(qualityInfo.getDevlist().get(j).getTvoc());
                        break;
                    }
                }
            }
            roomBytes.setRoomTemp((byte) roomInfo.getTemp());
            roomBytes.setRoomHumidity((byte) roomInfo.getHumidity());
            roomBytes.setRoomCo2(ByteUtils.int16ToByteArray(roomInfo.getCo2()));
            roomBytes.setRoomPm(ByteUtils.int16ToByteArray(roomInfo.getPm()));
            roomBytes.setRoomFormaldehyde(ByteUtils.int16ToByteArray(roomInfo.getFormaldehyde()));
            roomBytes.setRoomTvoc(ByteUtils.int16ToByteArray(roomInfo.getTvoc()));
            roomBytes.setFanSwitch((byte) 0x00);
            roomBytesList.add(roomBytes);
        }
        if (roomBytesList != null && roomBytesList.size() > 0) {
            HDTopic hdTopic = MqttUploadManager.getInstance().getmHDTopic();
            hdTopic.setRoomByte(roomBytesList);
        }
        MySpUtil.setParam(mContext, MySpUtil.ROOM_DATA, new Gson().toJson(saveRoomList));
        sendQuality(saveRoomList,qualityInfo);
        return saveRoomList;
    }

    private RoomInfo findRoomById(List<RoomInfo> rooms, int roomId) {
        for (RoomInfo room : rooms) {
            if (room.getRoomId() == roomId) {
                return room;
            }
        }
        return null;
    }

    /**
     * 空气质量数据下发主控板
     */
    private void sendQuality(List<RoomInfo> saveRoomList,AirQualityInfo qualityInfo){
        if (qualityInfo == null || qualityInfo.getDevlist() == null) {
            return;
        }
        for(int i = 0;i<saveRoomList.size();i++){
            if(saveRoomList.get(i).getAirQualityId() != 0){
                for (int j =0;j< qualityInfo.getDevlist().size();j++){
                    if(qualityInfo.getDevlist().get(j).getId() == saveRoomList.get(i).getAirQualityId()){
                        EnvironmentCommand command = new EnvironmentCommand(FunctionObject.SET_AIR_QUALITY);
                        ByteBuffer byteBuffer = ByteBuffer.allocate(9);
                        byteBuffer.put((byte)j);
                        byteBuffer.put(ByteUtils.int16ToByteArray(qualityInfo.getDevlist().get(j).getPM25()));
                        byteBuffer.put(ByteUtils.int16ToByteArray(qualityInfo.getDevlist().get(j).getTemp()));
                        byteBuffer.put(ByteUtils.int16ToByteArray(qualityInfo.getDevlist().get(j).getHumi()));
                        byteBuffer.put(ByteUtils.int16ToByteArray(qualityInfo.getDevlist().get(j).getCO2()));
                        command.setData(byteBuffer.array());
                        SpDataProcessor.getInstance().send(command);
                    }
                }
            }
        }
    }
}
