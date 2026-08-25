package com.hy.greenbuilding.protocol.ResPonseInfo;

import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.hy.greenbuilding.mqtt.HDTopic;
import com.hy.greenbuilding.mqtt.MqttUploadManager;
import com.hy.greenbuilding.utils.ByteUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *  主控板返回状态
 *  长度41字节
 */
public class MainControlInfo {
    private byte[] controlData;
    private int byteLength = (byte) 0x002D;
    public MainControlInfo(byte[] data){
        if(data != null && data.length >= byteLength){
            this.controlData = data;
        }else{
            this.controlData = new byte[byteLength];
        }
    }
    /**
     *  固件包类型。2字节
     */

    public int mainControlType(){
        byte[] bytes = Arrays.copyOfRange(controlData, 0, 2);
        int type = ByteUtils.byteArrayToInt16(bytes);
        return type;
    }

    /**
     * 厂商ID 2字节
     */
    public int factoryId(){
        byte[] bytes = Arrays.copyOfRange(controlData, 2, 4);
        int type = ByteUtils.byteArrayToInt16(bytes);
        return type;
    }

    /**
     * 软件版本号 2 字节
     */
    public String softwareVersion(){
        byte[] bytes = Arrays.copyOfRange(controlData, 4, 6);
        String version = ByteUtils.byteArrayToBcdString(bytes);
        version = TextUtils.isEmpty(version) ? "0057" : ("0000".equals(version) ? "0057" : version);
        return (float)Integer.parseInt(version)/10+"";
    }

    /**
     * 硬件版本号 2字节
     */
    public int hardwareVersion(){
        byte[] bytes = Arrays.copyOfRange(controlData, 6, 8);
        //  int type = ByteUtils.byteArrayToInt16(bytes);
        String version = new String(bytes);
        return 0;
    }

    /**
     * 生产日期 4字节
     */
    public String productDate(){
        byte[] byte1 = Arrays.copyOfRange(controlData, 8, 12);
        return ByteUtils.byteArrayToBcdString(byte1);
    }

    /**
     *低功耗模式 1字节
     */

    public int lowPower(){
        int type = ByteUtils.byteArrayToInt(Arrays.copyOfRange(controlData, 12, 13),
                0, Arrays.copyOfRange(controlData, 12, 13).length);
        return type;
    }
    /**
     * 运行模式 1字节
     */
    public int runMode(){
        byte[] bytes = Arrays.copyOfRange(controlData, 13, 14);
        int type = ByteUtils.byteArrayToInt(bytes, 0, bytes.length);
        HDTopic hdTopic = MqttUploadManager.getInstance().getmHDTopic();
        hdTopic.setRunMode(bytes[0]);
        return type;
    }

    /**
     * 温控模式 1字节
     */
    public int tempControlMode(){
        int type = ByteUtils.byteArrayToInt(Arrays.copyOfRange(controlData, 14, 15),
                0, Arrays.copyOfRange(controlData, 14, 15).length);
        return type;
    }

    /**
     * 设定湿度 2字节
     */
    public BigDecimal getHumidity(){
        byte[] bytes = Arrays.copyOfRange(controlData, 15, 17);
        int type = ByteUtils.byteArrayToInt16(bytes);
        return new BigDecimal((float)type).setScale(0, BigDecimal.ROUND_DOWN);

    }
    /**
     * 设定湿度回差 1字节
     */
    public BigDecimal getHumidity1(){
        byte[] bytes = Arrays.copyOfRange(controlData, 17, 18);
        int type = ByteUtils.byteArrayToInt16(bytes);
        return new BigDecimal((float)type).setScale(0, BigDecimal.ROUND_DOWN);

    }
    /**
     * 设定温度上限 2字节
     */
    public BigDecimal setTempMax(){
        byte[] bytes = Arrays.copyOfRange(controlData, 18, 20);
        int type = ByteUtils.byteArrayToInt16(bytes);
        return new BigDecimal((float)type / 10).setScale(0, BigDecimal.ROUND_DOWN);
    }
    /**
     * 设定温度下限  2字节
     */
    public BigDecimal setTempMin(){
        byte[] bytes = Arrays.copyOfRange(controlData, 20, 22);
        int type = ByteUtils.byteArrayToInt16(bytes);
        return new BigDecimal((float)type / 10).setScale(0, BigDecimal.ROUND_DOWN);
    }
    /**
     * 室外机类型 2字节
     */
    public int getOutTermType(){
        byte[] bytes = Arrays.copyOfRange(controlData, 22, 24);
        int type = ByteUtils.byteArrayToInt16(bytes);
        return type;
    }
    /**
     * 滤网状态 4字节
     */
    public int getGreenType(){
        int type = ByteUtils.byteArrayToInt(Arrays.copyOfRange(controlData, 24, 28),
                0, Arrays.copyOfRange(controlData, 24, 28).length);
        return type;
    }
    /**
     * 化霜状态
     * @return
     */
    public int getDefrostStatus(){
        int type = ByteUtils.byteArrayToInt(Arrays.copyOfRange(controlData, 28, 29), 0, Arrays.copyOfRange(controlData, 28, 29).length);
        return type;
    }
    /**
     * 温控延时保护状态
     * @return
     */
    public int delayProtectStatus(){
        byte[] bytes = Arrays.copyOfRange(controlData, 29, 30);
        int type = ByteUtils.byteArrayToInt(bytes, 0, bytes.length);
        return type;
    }
    /**
     * NTC检测故障
     * @return
     */
    public int ntcError(){
        byte[] bytes = Arrays.copyOfRange(controlData, 30, 31);
        int type = ByteUtils.byteArrayToInt(bytes, 0, bytes.length);
        return type;
    }

    /**
     * NTC 1-6个顺序发送，每个值为2B的温度值
     */
    public String getNtc(){
        // *** 核心修改：将结束索引改为 controlData.length - 1 ***
        // 这样可以排除数组末尾新增的 1 字节字段
        byte[] bytes = Arrays.copyOfRange(controlData, 31, controlData.length - 2);

        splitBytes(bytes,2);
        return getNTCData();
    }

    /**
     * 手动模式（1 字节）位于倒数第二个字节
     */
    public int newControlField(){
        // 起始索引：数组的倒数第二个位置（即新字段的起始位置）
        int startIndex = controlData.length - 2;
        // 结束索引：数组的最后一个位置 (不包含)
        int endIndex = controlData.length - 1;
        byte[] bytes = Arrays.copyOfRange(controlData, startIndex, endIndex);
        int type = ByteUtils.byteArrayToInt(bytes, 0, bytes.length);
        return type;
    }


    /**
     * 温控使能状态（最后 1 个字节）
     */
    public int getTempControlEnable() {
        // 索引位置为最后一个字节
        byte[] bytes = Arrays.copyOfRange(controlData, controlData.length - 1, controlData.length);
        return ByteUtils.byteArrayToInt(bytes, 0, bytes.length);
    }

    private List<byte[]> list = new ArrayList<>();
    private byte[][] splitData = new byte[6][];
    private List<String> ntcList = new ArrayList<>();

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

    public String getNTCData() {
        if (ntcList != null) {
            ntcList.clear();
        }
        for (int i = 0; i < list.size(); i++) {
            byte[] data = list.get(i);
            int ntc = ByteUtils.byteArrayToInt16(data);
            ntcList.add(new BigDecimal((float)ntc /10).setScale(1, BigDecimal.ROUND_DOWN).toString());
        }
        String ntc = new Gson().toJson(ntcList);
        return ntc;
    }

}
