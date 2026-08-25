package com.hy.greenbuilding.protocol.ResPonseInfo;

import android.util.Log;

import com.hy.greenbuilding.model.AirValveItemInfo;
import com.hy.greenbuilding.model.FanDataInfo;
import com.hy.greenbuilding.utils.ByteUtils;
import com.hy.greenbuilding.utils.Hex;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AirValveStatusInfo {

    private byte[] airValveData;
    private List<byte[]> list = new ArrayList<>();
    private byte[][] splitData = new byte[9][];
    private List<AirValveItemInfo> airValveList = new ArrayList<>();
    private int byteLength = (byte) 0x003F;

    public AirValveStatusInfo(byte[] data) {
        if (data != null && data.length >= byteLength) {
            this.airValveData = data;
            byte[] bytes = Arrays.copyOfRange(airValveData, 0, 63);
            splitBytes(bytes, 7);
        } else {
            this.airValveData = new byte[byteLength];
            byte[] bytes = Arrays.copyOfRange(airValveData, 0, 63);
            splitBytes(bytes, 7);
        }
    }

    /**
     * 拆分
     */
    public void splitBytes(byte[] bytes, int size) {
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
    }

    /**
     * 获取风阀数据
     */
    public List<AirValveItemInfo> getAirValveData() {
        if (airValveList != null) {
            airValveList.clear();
        }
        for (int i = 0; i < list.size(); i++) {
            AirValveItemInfo airValveItemInfo = new AirValveItemInfo();
            byte[] fanData = list.get(i);
            if (fanData.length < 7) {
                break;
            }

            int address = ByteUtils.byteArrayToInt(Arrays.copyOfRange(fanData, 0, 1), 0, Arrays.copyOfRange(fanData, 0, 1).length);
            int pressDiff = ByteUtils.byteArrayToInt(Arrays.copyOfRange(fanData, 1, 3), 0, Arrays.copyOfRange(fanData, 1, 3).length);
            int openValue = ByteUtils.byteArrayToInt(Arrays.copyOfRange(fanData, 3, 5), 0, Arrays.copyOfRange(fanData, 3, 5).length);
            int maxOpenValue = ByteUtils.byteArrayToInt(Arrays.copyOfRange(fanData, 5, 7), 0, Arrays.copyOfRange(fanData, 5, 7).length);

            airValveItemInfo.setValveId(i + 1);
            airValveItemInfo.setAddress(address);
            airValveItemInfo.setPressureDiff(pressDiff);
            airValveItemInfo.setRealOpenValue(openValue);
            airValveItemInfo.setMaxNumber(maxOpenValue);

            airValveList.add(airValveItemInfo);
        }

        return airValveList;
    }


}
