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
        AirQualityInfo qualityInfo= new AirQualityInfo();
        if(PublicUse.mJniFunCB != null){
            String tempJsonString = PublicUse.mJniFunCB.onGetDeviceTypeInfo(0, (1 << HYJniService.SUB_DEVICE_TYPE_ENV_DETECTOR));
            qualityInfo = new Gson().fromJson(tempJsonString, AirQualityInfo.class);
        }
        for (int i = 0; i < list.size(); i++) {
            RoomInfo roomInfo = new RoomInfo();
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
            if(saveRoomList.size() < 9){
                roomInfo.setRoomId(i);
                roomInfo.setTemp(new BigDecimal((float) temp / 10).setScale(0, BigDecimal.ROUND_DOWN).intValue());
                roomInfo.setRoomName("房间" + (i + 1));
                roomInfo.setPm(pm);
                roomInfo.setCo2(co2);
                roomInfo.setHumidity(humidity);
                roomInfo.setAirValve("关");
                roomInfo.setFormaldehyde(formaldehyde);
                roomInfo.setTvoc(tvoc);

                saveRoomList.add(roomInfo);
            }else{
                if (getRoomErrorBit()[(getRoomErrorBit().length - 1) - i] == 1) {
                    roomBytes.setNotifyStatus((byte) 0x01);
                } else {
                    roomBytes.setNotifyStatus((byte) 0x00);
                }
                if(saveRoomList.get(i).getRoomId() == i){
                    saveRoomList.get(i).setTemp(new BigDecimal((float) temp / 10).setScale(0, BigDecimal.ROUND_DOWN).intValue());
                    saveRoomList.get(i).setPm(pm);
                    saveRoomList.get(i).setCo2(co2);
                    saveRoomList.get(i).setHumidity(humidity);
                    roomInfo.setFormaldehyde(formaldehyde);
                    roomInfo.setTvoc(tvoc);

                    if(qualityInfo.getDevlist() != null && saveRoomList.get(i).getAirQualityId() != 0){
                        for (int j =0;j< qualityInfo.getDevlist().size();j++){
                            if(qualityInfo.getDevlist().get(j).getId() == saveRoomList.get(i).getAirQualityId()){
                                saveRoomList.get(i).setTemp(qualityInfo.getDevlist().get(j).getTemp());
                                saveRoomList.get(i).setPm(qualityInfo.getDevlist().get(j).getPM25());
                                saveRoomList.get(i).setCo2(qualityInfo.getDevlist().get(j).getCO2());
                                saveRoomList.get(i).setHumidity(qualityInfo.getDevlist().get(j).getHumi());
                                roomBytes.setNotifyStatus((byte) 0x01);
                                saveRoomList.get(i).setFormaldehyde(qualityInfo.getDevlist().get(j).getFormaldehyde());
                                saveRoomList.get(i).setTvoc(qualityInfo.getDevlist().get(j).getTvoc());
                            }
                        }
                    }
                }
                roomBytes.setRoomTemp((byte) saveRoomList.get(i).getTemp());
                roomBytes.setRoomHumidity((byte) saveRoomList.get(i).getHumidity());
                roomBytes.setRoomCo2(ByteUtils.int16ToByteArray(saveRoomList.get(i).getCo2()));
                roomBytes.setRoomPm(ByteUtils.int16ToByteArray(saveRoomList.get(i).getPm()));

                roomBytes.setRoomFormaldehyde(ByteUtils.int16ToByteArray(saveRoomList.get(i).getFormaldehyde()));
                roomBytes.setRoomTvoc(ByteUtils.int16ToByteArray(saveRoomList.get(i).getTvoc()));
                roomBytes.setFanSwitch((byte) 0x00);
                roomBytesList.add(roomBytes);
            }

        }
        if (roomBytesList != null && roomBytesList.size() > 0) {
            HDTopic hdTopic = MqttUploadManager.getInstance().getmHDTopic();
            hdTopic.setRoomByte(roomBytesList);
        }
        MySpUtil.setParam(mContext, MySpUtil.ROOM_DATA, new Gson().toJson(saveRoomList));
        sendQuality(saveRoomList,qualityInfo);
        return saveRoomList;
    }

    /**
     * 空气质量数据下发主控板
     */
    private void sendQuality(List<RoomInfo> saveRoomList,AirQualityInfo qualityInfo){
        for(int i = 0;i<saveRoomList.size();i++){
            if(qualityInfo.getDevlist() != null && saveRoomList.get(i).getAirQualityId() != 0){
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
