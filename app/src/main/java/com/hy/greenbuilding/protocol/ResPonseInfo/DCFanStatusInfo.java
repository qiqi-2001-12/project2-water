package com.hy.greenbuilding.protocol.ResPonseInfo;

import android.util.Log;

import com.hy.greenbuilding.R;
import com.hy.greenbuilding.mqtt.HXTopic;
import com.hy.greenbuilding.mqtt.MqttUploadManager;
import com.hy.greenbuilding.utils.ByteUtils;
import com.hy.greenbuilding.utils.Hex;
import com.hy.greenbuilding.utils.MySpUtil;

import java.util.Arrays;

public class DCFanStatusInfo {
    private boolean isSuccess;
    private int type;
    private byte[] dcData;
    private int byteLength = (byte) 0x001A;
    private HXTopic hxTopic;

    public DCFanStatusInfo(byte[] data, boolean isSuccess, int type) {
        hxTopic = MqttUploadManager.getInstance().getmHxTopic();
        this.isSuccess = isSuccess;
        this.type = type;
        if (data != null && data.length >= byteLength) {
            dcData = data;
        } else {
            dcData = new byte[byteLength];
        }
        Log.i("info", "dc data---" + Hex.bytesToHexString(dcData));
    }

    public boolean getSuccess() {
        return this.isSuccess;
    }

    public int getType() {
        return this.type;
    }

    /**
     * 1B 除湿电磁阀开关 (索引 0)
     *
     * @return
     */
    public int getDehumSolenoidValveSwitch() {
        // 索引 0 (1B)
        byte[] bytes = Arrays.copyOfRange(this.dcData, 0, 1);
        // [更新] 调用新方法
        hxTopic.setPath2SolenoidValveStatus(bytes[0]);
        return ByteUtils.byteArrayToInt16(bytes);
    }

    /**
     * 2B 转速值 (索引 1-2)
     *
     * @return
     */
    public int getDCSpeed() {
        // 索引 1, 2 (2B)
        byte[] bytes = Arrays.copyOfRange(this.dcData, 1, 3);
        hxTopic.setDcSpeed(bytes);
        return ByteUtils.byteArrayToInt16(bytes);
    }

    /**
     * 2B 除湿电子膨胀阀开度 (原索引 4-5，现索引 3-4)
     *
     * @return
     */
    public int getDehumExpansionOpen() {
        // 🌟 更新索引：原 4, 6 -> 现 3, 5 (2B)
        byte[] bytes = Arrays.copyOfRange(this.dcData, 3, 5);
        hxTopic.setPath2EevOpen(bytes);
        return ByteUtils.byteArrayToInt16(bytes);
    }

    /**
     * 2B 除湿电子膨胀阀PID目标值 (原索引 6-7，现索引 5-6)
     *
     * @return
     */
    public int getDehumPidValue() {
        // 🌟 更新索引：原 6, 8 -> 现 5, 7 (2B)
        byte[] bytes = Arrays.copyOfRange(this.dcData, 5, 7);
        hxTopic.setDehumTargetValue(bytes);
        return ByteUtils.byteArrayToInt16(bytes);
    }

    /**
     * 1B 升温除湿类型 (原索引 8，现索引 7)
     *
     * @return
     */
    public int getHeatingDehumType() {
        // 🌟 更新索引：原 8, 9 -> 现 7, 8 (1B)
        byte[] bytes = Arrays.copyOfRange(this.dcData, 7, 8);
        hxTopic.setHeatingDehumControlType(bytes[0]);
        return ByteUtils.byteArrayToInt16(bytes);
    }

    /**
     * 1B 冷热电磁阀开关 (原索引 9，现索引 8)
     *
     * @return
     */
    public int getColdHotSolenoidValveSwitch() {
        // 🌟 更新索引：原 9, 10 -> 现 8, 9 (1B)
        byte[] bytes = Arrays.copyOfRange(this.dcData, 8, 9);
        hxTopic.setPath1SolenoidValveStatus(bytes[0]);
        return ByteUtils.byteArrayToInt16(bytes);
    }

    /**
     * 2B 冷热电子膨胀阀开度 (原索引 10-11，现索引 9-10)
     *
     * @return
     */
    public int getColdHotEevOpen() {
        // 🌟 更新索引：原 10, 12 -> 现 9, 11 (2B)
        byte[] bytes = Arrays.copyOfRange(this.dcData, 9, 11);
        // [更新] 调用新方法
        hxTopic.setPath1EevOpen(bytes);
        return ByteUtils.byteArrayToInt16(bytes);
    }

    /**
     * 1B 风机小板类型 (原索引 12，现索引 11)
     *
     * @return
     */
    public int getFanBoardMode() {
        // 🌟 更新索引：原 12, 13 -> 现 11, 12 (1B)
        byte[] bytes = Arrays.copyOfRange(this.dcData, 11, 12);
        hxTopic.setControlBoardType(bytes[0]);
        return ByteUtils.byteArrayToInt16(bytes);
    }

    /**
     * 1B 一路使能状态 (原索引 13，现索引 12)
     *
     * @return
     */
    public int getPath1EnableStatus() {
        // 🌟 更新索引：原 13, 14 -> 现 12, 13 (1B)
        byte[] bytes = Arrays.copyOfRange(this.dcData, 12, 13);
        hxTopic.setPath1Enable(bytes[0]);
        return ByteUtils.byteArrayToInt16(bytes);
    }

    /**
     * 1B 一路控制方式 (原索引 14，现索引 13)
     *
     * @return
     */
    public int getPath1ControlType() {
        // 🌟 更新索引：原 14, 15 -> 现 13, 14 (1B)
        byte[] bytes = Arrays.copyOfRange(this.dcData, 13, 14);
        hxTopic.setPath1ControlType(bytes[0]);
        return ByteUtils.byteArrayToInt16(bytes);
    }

    /**
     * 1B 一路冷热电子膨胀阀使能状态 (原索引 15，现索引 14)
     *
     * @return
     */
    public int getPath1ColdHotEevEnableStatus() {
        // 🌟 更新索引：原 15, 16 -> 现 14, 15 (1B)
        byte[] bytes = Arrays.copyOfRange(this.dcData, 14, 15);
        hxTopic.setPath1EevEnable(bytes[0]);
        return ByteUtils.byteArrayToInt16(bytes);
    }

    /**
     * 1B 一路冷热电磁阀使能状态 (原索引 16，现索引 15)
     *
     * @return
     */
    public int getPath1ColdHotSolenoidValveEnableStatus() {
        // 🌟 更新索引：原 16, 17 -> 现 15, 16 (1B)
        byte[] bytes = Arrays.copyOfRange(this.dcData, 15, 16);
        hxTopic.setPath1SolenoidValveEnable(bytes[0]);
        return ByteUtils.byteArrayToInt16(bytes);
    }

    /**
     * 1B 二路使能状态 (原索引 17，现索引 16)
     *
     * @return
     */
    public int getPath2EnableStatus() {
        // 🌟 更新索引：原 17, 18 -> 现 16, 17 (1B)
        byte[] bytes = Arrays.copyOfRange(this.dcData, 16, 17);
        hxTopic.setPath2Enable(bytes[0]);
        return ByteUtils.byteArrayToInt16(bytes);
    }

    /**
     * 1B 二路控制方式 (原索引 18，现索引 17)
     *
     * @return
     */
    public int getPath2ControlType() {
        // 🌟 更新索引：原 18, 19 -> 现 17, 18 (1B)
        byte[] bytes = Arrays.copyOfRange(this.dcData, 17, 18);
        hxTopic.setPath2ControlType(bytes[0]);
        return ByteUtils.byteArrayToInt16(bytes);
    }

    /**
     * 1B 二路除湿电子膨胀阀使能状态 (原索引 19，现索引 18)
     *
     * @return
     */
    public int getPath2DehumEevEnableStatus() {
        // 🌟 更新索引：原 19, 20 -> 现 18, 19 (1B)
        byte[] bytes = Arrays.copyOfRange(this.dcData, 18, 19);
        // [更新] 调用新方法
        hxTopic.setPath2EevEnable(bytes[0]);
        return ByteUtils.byteArrayToInt16(bytes);
    }

    /**
     * 1B 二路除湿电磁阀使能状态 (原索引 20，现索引 19)
     *
     * @return
     */
    public int getPath2DehumSolenoidValveEnableStatus() {
        // 🌟 更新索引：原 20, 21 -> 现 19, 20 (1B)
        byte[] bytes = Arrays.copyOfRange(this.dcData, 19, 20);
        hxTopic.setPath2SolenoidValveEnable(bytes[0]);
        return ByteUtils.byteArrayToInt16(bytes);
    }

    /**
     * 2B 冷热电磁阀制冷PID目标值 (原索引 21-22，现索引 20-21)
     *
     * @return
     */
    public int getColdHotSolenoidValveCoolingPidValue() {
        // 🌟 更新索引：原 21, 23 -> 现 20, 22 (2B)
        byte[] bytes = Arrays.copyOfRange(this.dcData, 20, 22);
        // [更新] 调用新方法
        hxTopic.setCoolingTargetValue(bytes);
        return ByteUtils.byteArrayToInt16(bytes);
    }

    /**
     * 2B 冷热电磁阀制热PID目标值 (原索引 23-24，现索引 22-23)
     *
     * @return
     */
    public int getColdHotSolenoidValveHeatingPidValue() {
        // 🌟 更新索引：原 23, 25 -> 现 22, 24 (2B)
        byte[] bytes = Arrays.copyOfRange(this.dcData, 22, 24);
        // [更新] 调用新方法
        hxTopic.setHeatingTargetValue(bytes);
        return ByteUtils.byteArrayToInt16(bytes);
    }

    /**
     * 2B 除湿电子膨胀阀固定开度值 (新索引 24-25)
     *
     * @return
     */
    public int getDehumEevFixedOpenValue() {
        byte[] bytes = Arrays.copyOfRange(this.dcData, 24, 26);
        hxTopic.setEev2PidFixedTarget(bytes);
        return ByteUtils.byteArrayToInt16(bytes);
    }


    // 新增：获取字段值的方法（可选）
    public void setPath1EevManualOpen(byte[] path1EevManualOpen) {
        hxTopic.setPath1EevManualOpen(path1EevManualOpen);
    }

    public void setPath1SolenoidValveManualStatus(byte path1SolenoidValveManualStatus) {
        hxTopic.setPath1SolenoidValveManualStatus(path1SolenoidValveManualStatus); // 可选

    }

    public void setPath2EevManualOpen(byte[] path2EevManualOpen) {
        hxTopic.setPath2EevManualOpen(path2EevManualOpen); // 可选
    }

    public void setPath2SolenoidValveManualStatus(byte path2SolenoidValveManualStatus) {
        hxTopic.setPath2SolenoidValveManualStatus(path2SolenoidValveManualStatus); // 可选
    }

    public void sendData() {
        getDehumSolenoidValveSwitch();
        getDCSpeed();
        getDehumExpansionOpen();
        getDehumPidValue();
        getHeatingDehumType();
        getColdHotSolenoidValveSwitch();
        getColdHotEevOpen();
        getFanBoardMode();
        getPath1EnableStatus();
        getPath1ControlType();
        getPath1ColdHotEevEnableStatus();
        getPath1ColdHotSolenoidValveEnableStatus();
        getPath2EnableStatus();
        getPath2ControlType();
        getPath2DehumEevEnableStatus();
        getPath2DehumSolenoidValveEnableStatus();
        getColdHotSolenoidValveCoolingPidValue();
        getColdHotSolenoidValveHeatingPidValue();
        getDehumEevFixedOpenValue();
    }

}