package com.hy.greenbuilding.protocol.ResPonseInfo;

import android.util.Log;

import com.hy.greenbuilding.mqtt.HETopic;
import com.hy.greenbuilding.mqtt.MqttUploadManager;
import com.hy.greenbuilding.utils.ByteUtils;
import com.hy.greenbuilding.utils.Hex;

import java.math.BigDecimal;
import java.util.Arrays;

/**
 * 电表数据
 */
public class ElectricityMeterInfo {
    private byte[] electricityData;
    private int byteLength = (byte) 0x000E;
    HETopic heTopic;
    public ElectricityMeterInfo(byte[] data) {
//        Log.i("info","ElectricityMeterInfo---"+ Hex.bytesToHexString(data));
        if (data != null && data.length >= byteLength) {
            this.electricityData = data;
        } else {
            this.electricityData = new byte[byteLength];
        }
        heTopic = MqttUploadManager.getInstance().getmHETopic();
    }

    public String  getData(){
        return Hex.bytesToHexString(electricityData);
    }


    /**
     * 总电量 除10
     *
     * @return
     */
    public BigDecimal getTotalElectricity() {
        byte[] bytes = Arrays.copyOfRange(electricityData, 0, 4);
        int total = ByteUtils.byteArrayToInt(bytes, 0, bytes.length);
        heTopic.setTotalElectric(total);
        return new BigDecimal((float) total / 10).setScale(2, BigDecimal.ROUND_DOWN);
    }

    /**
     * 功率 除10000
     *
     * @return
     */
    public BigDecimal getPower() {
        byte[] bytes = Arrays.copyOfRange(electricityData, 4, 8);
        int power = ByteUtils.byteArrayToInt(bytes, 0, bytes.length);
        heTopic.setCurrentPower((short) (power / 10));
        return new BigDecimal((float) power / 10000).setScale(3, BigDecimal.ROUND_DOWN);
    }


    /**
     * 电流 除1000
     *
     * @return
     */
    public BigDecimal getElectric() {
        int electric = ByteUtils.byteArrayToInt(Arrays.copyOfRange(electricityData, 8, 12), 0, Arrays.copyOfRange(electricityData, 8, 12).length);
        heTopic.setElectric((short) electric);
        return new BigDecimal((float) electric / 1000).setScale(2, BigDecimal.ROUND_DOWN);
    }

    /**
     * 电压 除100
     *
     * @return
     */
    public BigDecimal getVoltage() {
        int voltage = ByteUtils.byteArrayToInt(Arrays.copyOfRange(electricityData, 12, 14), 0, Arrays.copyOfRange(electricityData, 12, 14).length);
        heTopic.setVoltage((short) voltage);
        return new BigDecimal((float) voltage / 100).setScale(2, BigDecimal.ROUND_DOWN);
    }

    public void sendData() {
        getTotalElectricity();
        getPower();
        getElectric();
        getVoltage();
    }
}
