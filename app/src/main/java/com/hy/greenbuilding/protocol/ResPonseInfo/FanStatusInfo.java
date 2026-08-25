package com.hy.greenbuilding.protocol.ResPonseInfo;

import android.util.Log;

import com.hy.greenbuilding.model.FanDataInfo;
import com.hy.greenbuilding.mqtt.HXTopic;
import com.hy.greenbuilding.mqtt.MqttUploadManager;
import com.hy.greenbuilding.utils.ByteUtils;
import com.hy.greenbuilding.utils.Hex;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 风机返回状态
 */
public class FanStatusInfo {
    private byte[] fanData;
    private List<byte[]> list = new ArrayList<>();
    private byte[][] splitData = new byte[4][];
    private List<FanDataInfo> fanList = new ArrayList<>();
    private int byteLength = (byte) 0x007F;
    private HXTopic hxTopic;

    public FanStatusInfo(byte[] data) {
        hxTopic = MqttUploadManager.getInstance().getmHxTopic();
        if (data != null && data.length >= byteLength) {
            this.fanData = Arrays.copyOfRange(data, 0, byteLength);
            byte[] bytes = Arrays.copyOfRange(fanData, 0, 29 * 4);
            splitBytes(bytes, 29);
        } else {
            this.fanData = new byte[byteLength];
            byte[] bytes = Arrays.copyOfRange(fanData, 0, 29 * 4);
            splitBytes(bytes, 29);
        }
        Log.i("info",  "fan data == " + Hex.bytesToHexString(data));
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
     * 获取风机数据
     */
    public List<FanDataInfo> getFanData() {
        if (fanList != null) {
            fanList.clear();
        }
        for (int i = 0; i < list.size(); i++) {
            FanDataInfo fanInfo = new FanDataInfo();
            byte[] fanData = list.get(i);
            if (fanData.length < 29) {
                break;
            }
            int address = ByteUtils.byteArrayToInt(Arrays.copyOfRange(fanData, 0, 1), 0, Arrays.copyOfRange(fanData, 0, 1).length);
            int fanStatus = ByteUtils.byteArrayToInt(Arrays.copyOfRange(fanData, 1, 2), 0, Arrays.copyOfRange(fanData, 1, 2).length);
            int type = ByteUtils.byteArrayToInt(Arrays.copyOfRange(fanData, 2, 3), 0, Arrays.copyOfRange(fanData, 2, 3).length);
            int vref = ByteUtils.byteArrayToInt(Arrays.copyOfRange(fanData, 3, 5), 0, Arrays.copyOfRange(fanData, 3, 5).length);
            int value = ByteUtils.byteArrayToInt(Arrays.copyOfRange(fanData, 5, 7), 0, Arrays.copyOfRange(fanData, 5, 7).length);
            int error = ByteUtils.byteArrayToInt(Arrays.copyOfRange(fanData, 7, 9), 0, Arrays.copyOfRange(fanData, 7, 9).length);
            int pwmType = ByteUtils.byteArrayToInt(Arrays.copyOfRange(fanData, 9, 10), 0, Arrays.copyOfRange(fanData, 9, 10).length);
            int pwmCloseGear = ByteUtils.byteArrayToInt16(Arrays.copyOfRange(fanData, 10, 11));
            int pwmSmallGear = ByteUtils.byteArrayToInt16(Arrays.copyOfRange(fanData, 11, 12));
            int pwmMiddleGear = ByteUtils.byteArrayToInt16(Arrays.copyOfRange(fanData, 12, 13));
            int pwmBigGear = ByteUtils.byteArrayToInt16(Arrays.copyOfRange(fanData, 13, 14));
            int closeGear = ByteUtils.byteArrayToInt16(Arrays.copyOfRange(fanData, 14, 16));
            int smallGear = ByteUtils.byteArrayToInt16(Arrays.copyOfRange(fanData, 16, 18));
            int middleGear = ByteUtils.byteArrayToInt16(Arrays.copyOfRange(fanData, 18, 20));
            int bigGear = ByteUtils.byteArrayToInt16(Arrays.copyOfRange(fanData, 20, 22));

            int staticPressure = ByteUtils.byteArrayToInt16(Arrays.copyOfRange(fanData, 22, 23));
            int setPressure = ByteUtils.byteArrayToInt16(Arrays.copyOfRange(fanData, 23, 25));
            int realPressure = ByteUtils.byteArrayToInt16(Arrays.copyOfRange(fanData, 25, 27));
            int screenPressure = ByteUtils.byteArrayToInt16(Arrays.copyOfRange(fanData, 27, 29));

            fanInfo.setFanAddress(address + "");
            fanInfo.setFanStatus(fanStatus);
            fanInfo.setInterfaceType(type);
            fanInfo.setvrefModel(vref);
            fanInfo.setWindValue(value + "");
            fanInfo.setFanError(error + "");
            fanInfo.setPwmFanStatus(pwmType);
            fanInfo.setPwmCloseGear(pwmCloseGear);
            fanInfo.setPwmSmallGear(pwmSmallGear);
            fanInfo.setPwmMiddleGear(pwmMiddleGear);
            fanInfo.setPwmBigGear(pwmBigGear);
            fanInfo.setCloseGear(closeGear);
            fanInfo.setSmallGear(smallGear);
            fanInfo.setMiddleGear(middleGear);
            fanInfo.setBigGear(bigGear);
            fanInfo.setStaticPressure(staticPressure);
            fanInfo.setSetPressure(setPressure);
            fanInfo.setRealPressure(realPressure);
            fanInfo.setScreenPressure(screenPressure);
            fanList.add(fanInfo);
        }
        sendOta(fanList);
        return fanList;
    }

    private void sendOta(List<FanDataInfo> fanData) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(48);
        ByteBuffer byteBuffer1 = ByteBuffer.allocate(15);
        if (fanData != null && fanData.size() == 4) {
            byteBuffer.put(ByteUtils.changeBytes(ByteUtils.int16ToByteArray(fanData.get(0).getPwmSmallGear())));
            byteBuffer.put(ByteUtils.changeBytes(ByteUtils.int16ToByteArray(fanData.get(0).getPwmMiddleGear())));
            byteBuffer.put(ByteUtils.changeBytes(ByteUtils.int16ToByteArray(fanData.get(0).getPwmBigGear())));
            byteBuffer.put(ByteUtils.changeBytes(ByteUtils.int16ToByteArray(fanData.get(0).getSmallGear())));
            byteBuffer.put(ByteUtils.changeBytes(ByteUtils.int16ToByteArray(fanData.get(0).getMiddleGear())));
            byteBuffer.put(ByteUtils.changeBytes(ByteUtils.int16ToByteArray(fanData.get(0).getBigGear())));

            byteBuffer.put(ByteUtils.changeBytes(ByteUtils.int16ToByteArray(fanData.get(1).getPwmSmallGear())));
            byteBuffer.put(ByteUtils.changeBytes(ByteUtils.int16ToByteArray(fanData.get(1).getPwmMiddleGear())));
            byteBuffer.put(ByteUtils.changeBytes(ByteUtils.int16ToByteArray(fanData.get(1).getPwmBigGear())));
            byteBuffer.put(ByteUtils.changeBytes(ByteUtils.int16ToByteArray(fanData.get(1).getSmallGear())));
            byteBuffer.put(ByteUtils.changeBytes(ByteUtils.int16ToByteArray(fanData.get(1).getMiddleGear())));
            byteBuffer.put(ByteUtils.changeBytes(ByteUtils.int16ToByteArray(fanData.get(1).getBigGear())));

            byteBuffer.put(ByteUtils.changeBytes(ByteUtils.int16ToByteArray(fanData.get(2).getPwmSmallGear())));
            byteBuffer.put(ByteUtils.changeBytes(ByteUtils.int16ToByteArray(fanData.get(2).getPwmMiddleGear())));
            byteBuffer.put(ByteUtils.changeBytes(ByteUtils.int16ToByteArray(fanData.get(2).getPwmBigGear())));
            byteBuffer.put(ByteUtils.changeBytes(ByteUtils.int16ToByteArray(fanData.get(2).getSmallGear())));
            byteBuffer.put(ByteUtils.changeBytes(ByteUtils.int16ToByteArray(fanData.get(2).getMiddleGear())));
            byteBuffer.put(ByteUtils.changeBytes(ByteUtils.int16ToByteArray(fanData.get(2).getBigGear())));

            byteBuffer.put(ByteUtils.changeBytes(ByteUtils.int16ToByteArray(fanData.get(3).getPwmSmallGear())));
            byteBuffer.put(ByteUtils.changeBytes(ByteUtils.int16ToByteArray(fanData.get(3).getPwmMiddleGear())));
            byteBuffer.put(ByteUtils.changeBytes(ByteUtils.int16ToByteArray(fanData.get(3).getPwmBigGear())));
            byteBuffer.put(ByteUtils.changeBytes(ByteUtils.int16ToByteArray(fanData.get(3).getSmallGear())));
            byteBuffer.put(ByteUtils.changeBytes(ByteUtils.int16ToByteArray(fanData.get(3).getMiddleGear())));
            byteBuffer.put(ByteUtils.changeBytes(ByteUtils.int16ToByteArray(fanData.get(3).getBigGear())));

            hxTopic.setFanSet(byteBuffer.array());

            byteBuffer1.put(ByteUtils.changeBytes(ByteUtils.int16ToByteArray(fanData.get(0).getStaticPressure()))[0]);
            byteBuffer1.put(ByteUtils.changeBytes(ByteUtils.int16ToByteArray(fanData.get(0).getSetPressure())));
            byteBuffer1.put(ByteUtils.changeBytes(ByteUtils.int16ToByteArray(fanData.get(0).getRealPressure())));

            byteBuffer1.put(ByteUtils.changeBytes(ByteUtils.int16ToByteArray(fanData.get(1).getStaticPressure()))[0]);
            byteBuffer1.put(ByteUtils.changeBytes(ByteUtils.int16ToByteArray(fanData.get(1).getSetPressure())));
            byteBuffer1.put(ByteUtils.changeBytes(ByteUtils.int16ToByteArray(fanData.get(1).getRealPressure())));

            byteBuffer1.put(ByteUtils.changeBytes(ByteUtils.int16ToByteArray(fanData.get(2).getStaticPressure()))[0]);
            byteBuffer1.put(ByteUtils.changeBytes(ByteUtils.int16ToByteArray(fanData.get(2).getSetPressure())));
            byteBuffer1.put(ByteUtils.changeBytes(ByteUtils.int16ToByteArray(fanData.get(2).getRealPressure())));

            hxTopic.setStaticPressureData(byteBuffer1.array());


            byte[] bytes = Arrays.copyOfRange(this.fanData, 116, 118);
            hxTopic.setTypeAndModel(bytes);
        }

    }

    /**
     * 风机地址
     *
     * @return
     */
    public int getFanAddress(byte[] bytes) {
        int type = ByteUtils.byteArrayToInt16(bytes);
        return type;
    }

    /**
     * 风机状态
     *
     * @return
     */
    public int getFanStatus(byte[] bytes) {
        int type = ByteUtils.byteArrayToInt16(bytes);
        return type;
    }

    /**
     * 风机类型
     */
    public int getType(byte[] bytes) {
        int type = ByteUtils.byteArrayToInt16(bytes);
        return type;
    }


    /**
     * 风机安装类型
     *
     * @return
     */
    public int getFanInstallType() {
        byte[] bytes = Arrays.copyOfRange(this.fanData, 116, 117);
        return ByteUtils.byteArrayToInt16(bytes);
    }

    /**
     * 风机安装型号
     *
     * @return
     */
    public int getFanInstallModel() {
        byte[] bytes = Arrays.copyOfRange(this.fanData, 117, 118);
        return ByteUtils.byteArrayToInt16(bytes);
    }

    /**
     * DC风机状态
     *
     * @return
     */
    public int getDCFanStatus() {
        byte[] bytes = Arrays.copyOfRange(this.fanData, 118, 119);
        return ByteUtils.byteArrayToInt16(bytes);
    }

    /**
     * DC风机
     *
     * @return
     */
    public int getDCFanClose() {
        byte[] bytes = Arrays.copyOfRange(this.fanData, 119, 121);
        return ByteUtils.byteArrayToInt16(bytes);
    }

    /**
     * DC风机
     *
     * @return
     */
    public int getDCFanSmall() {
        byte[] bytes = Arrays.copyOfRange(this.fanData, 121, 123);
        return ByteUtils.byteArrayToInt16(bytes);
    }

    /**
     * DC风机
     *
     * @return
     */
    public int getDCFanMiddle() {
        byte[] bytes = Arrays.copyOfRange(this.fanData, 123, 125);
        return ByteUtils.byteArrayToInt16(bytes);
    }

    /**
     * DC风机
     *
     * @return
     */
    public int getDCFanHigh() {
        byte[] bytes = Arrays.copyOfRange(this.fanData, 125, this.fanData.length);
        return ByteUtils.byteArrayToInt16(bytes);
    }
}
