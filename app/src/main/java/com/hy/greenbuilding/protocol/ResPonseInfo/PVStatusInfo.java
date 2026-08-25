package com.hy.greenbuilding.protocol.ResPonseInfo;

import android.util.Log;

import com.hy.greenbuilding.HyApplication;
import com.hy.greenbuilding.mqtt.HDTopic;
import com.hy.greenbuilding.mqtt.HFTopic;
import com.hy.greenbuilding.mqtt.HXTopic;
import com.hy.greenbuilding.mqtt.MqttUploadManager;
import com.hy.greenbuilding.protocol.IPVStatusInfo;
import com.hy.greenbuilding.utils.ByteUtils;
import com.hy.greenbuilding.utils.Hex;

import java.math.BigDecimal;
import java.util.Arrays;

public class PVStatusInfo implements IPVStatusInfo {
    private byte[] statusData;
    private HDTopic hdTopic;
    private HFTopic hfTopic;
    private HXTopic hxTopic;

    public PVStatusInfo(byte[] data) {
        this.statusData = data;
        hdTopic = MqttUploadManager.getInstance().getmHDTopic();
        hfTopic = MqttUploadManager.getInstance().getmHFTopic();
        hxTopic = MqttUploadManager.getInstance().getmHxTopic();

    }

    @Override
    public String airModeHex() {
        return Hex.bytesToHexString(Arrays.copyOfRange(statusData, 0, 2));
    }

    @Override
    public String airConditionerMode() {
        String airMode = "";
        int total = ByteUtils.byteArrayToInt(Arrays.copyOfRange(statusData, 0, 2), 0, Arrays.copyOfRange(statusData, 0, 2).length);
        if (total == 0) {
            airMode = "关机";
        } else if (total == 1) {
            airMode = "制冷";
            hfTopic.setAirConditionMode((byte) 0x01);
        } else if (total == 2) {
            airMode = "制热";
            hfTopic.setAirConditionMode((byte) 0x02);
        } else if (total == 3) {
            airMode = "送风";
            hfTopic.setAirConditionMode((byte) 0x03);
        } else if (total == 4) {
            airMode = "除湿";
            hfTopic.setAirConditionMode((byte) 0x04);
        } else if (total == 5) {
            airMode = "待机";
            hfTopic.setAirConditionMode((byte) 0x05);
        } else if (total == 6) {
            airMode = "强制除氟";
            hfTopic.setAirConditionMode((byte) 0x06);
        }
        return airMode;
    }

    @Override
    public String switchMode() {
        int total = ByteUtils.byteArrayToInt(Arrays.copyOfRange(statusData, 0, 2),
                0, Arrays.copyOfRange(statusData, 0, 2).length);
        if (total == 0) {
            hfTopic.setSwitchStatus((byte) 0x00);
            return "关";
        } else {
            hfTopic.setSwitchStatus((byte) 0x01);
            return "开";
        }
    }

    @Override
    public String defrostSignal() {
        int signal = ByteUtils.byteArrayToInt(Arrays.copyOfRange(statusData, 2, 4),
                0, Arrays.copyOfRange(statusData, 2, 4).length);
        hxTopic.setDefrostSignal(signal);
        if (signal == 0) {
            return "除霜完成";
        } else {
            return "正在除霜";
        }
    }

    @Override
    public BigDecimal settingTemp() {
        byte[] b1 = Arrays.copyOfRange(statusData, 4, 6);
        short temp = ByteUtils.byteArrayToShort(b1);
        BigDecimal bigDecimal1 = new BigDecimal((float) temp / 10).setScale(1, BigDecimal.ROUND_DOWN);
        BigDecimal bigDecimal2 = bigDecimal1.setScale(0, BigDecimal.ROUND_DOWN);
        hfTopic.setSetTemp(bigDecimal2.shortValue());
        return bigDecimal1;
    }

    @Override
    public BigDecimal inDoorTemp() {
        byte[] b1 = Arrays.copyOfRange(statusData, 6, 8);
        short temp = ByteUtils.byteArrayToShort(b1);
        BigDecimal bigDecimal1 = new BigDecimal((float) temp / 10).setScale(1, BigDecimal.ROUND_DOWN);
        BigDecimal bigDecimal2 = bigDecimal1.setScale(0, BigDecimal.ROUND_DOWN);
        hfTopic.setInTemp(bigDecimal2.shortValue());
        hxTopic.setInCircleTemp(b1);
        return bigDecimal1;
    }

    @Override
    public BigDecimal outDoorTemp() {
        byte[] b1 = Arrays.copyOfRange(statusData, 8, 10);
        short temp = ByteUtils.byteArrayToShort(b1);
        BigDecimal bigDecimal1 = new BigDecimal((float) temp / 10).setScale(1, BigDecimal.ROUND_DOWN);
        BigDecimal bigDecimal2 = bigDecimal1.setScale(0, BigDecimal.ROUND_DOWN);
        hfTopic.setOutTemp(bigDecimal2.shortValue());
        HyApplication.setOutTemp(new BigDecimal(temp));
        return bigDecimal1;
    }

    @Override
    public BigDecimal outDoorTemp1() {
        byte[] b1 = Arrays.copyOfRange(statusData, 10, 12);
        short temp = ByteUtils.byteArrayToShort(b1);
        BigDecimal bigDecimal1 = new BigDecimal((float) temp / 10).setScale(1, BigDecimal.ROUND_DOWN);
        BigDecimal bigDecimal2 = bigDecimal1.setScale(0, BigDecimal.ROUND_DOWN);
        hfTopic.setOutHotTemp(bigDecimal2.shortValue());
        return bigDecimal1;
    }

    @Override
    public BigDecimal exHaustTemp() {
        byte[] b1 = Arrays.copyOfRange(statusData, 12, 14);
        short temp = ByteUtils.byteArrayToShort(b1);
        BigDecimal bigDecimal1 = new BigDecimal((float) temp / 10).setScale(1, BigDecimal.ROUND_DOWN);
        BigDecimal bigDecimal2 = bigDecimal1.setScale(0, BigDecimal.ROUND_DOWN);
        hfTopic.setInAuxTemp(bigDecimal2.shortValue());
        return bigDecimal1;
    }

    @Override
    public BigDecimal exReturnTemp() {
        byte[] b1 = Arrays.copyOfRange(statusData, 14, 16);
        short temp = ByteUtils.byteArrayToShort(b1);
        BigDecimal bigDecimal1 = new BigDecimal((float) temp / 10).setScale(1, BigDecimal.ROUND_DOWN);
        BigDecimal bigDecimal2 = bigDecimal1.setScale(0, BigDecimal.ROUND_DOWN);
        hxTopic.setSuctionTemp(bigDecimal2.shortValue());
        return bigDecimal1;
    }

    @Override
    public BigDecimal outFunSpeed() {
        int temp = ByteUtils.byteArrayToInt(Arrays.copyOfRange(statusData, 16, 18),
                0, Arrays.copyOfRange(statusData, 16, 18).length);
        hxTopic.setFanSpeed(temp);
        return new BigDecimal((float) temp).setScale(0, BigDecimal.ROUND_DOWN);
    }

    @Override
    public BigDecimal outElectric() {
        int temp = ByteUtils.byteArrayToInt(Arrays.copyOfRange(statusData, 18, 20),
                0, Arrays.copyOfRange(statusData, 18, 20).length);
        BigDecimal bigDecimal = new BigDecimal((float) temp / 10).setScale(1, BigDecimal.ROUND_DOWN);
        return bigDecimal;
    }

    @Override
    public BigDecimal voltage() {
        int temp = ByteUtils.byteArrayToInt(Arrays.copyOfRange(statusData, 20, 22),
                0, Arrays.copyOfRange(statusData, 20, 22).length);
        return new BigDecimal((float) temp).setScale(0, BigDecimal.ROUND_DOWN);
    }

    @Override
    public BigDecimal moduleTemp() {
        byte[] b1 = Arrays.copyOfRange(statusData, 22, 24);
        short temp = ByteUtils.byteArrayToShort(b1);
        return new BigDecimal((float) temp / 10).setScale(1, BigDecimal.ROUND_DOWN);
    }

    @Override
    public BigDecimal frequency() {
        int temp = ByteUtils.byteArrayToInt(Arrays.copyOfRange(statusData, 24, 26),
                0, Arrays.copyOfRange(statusData, 24, 26).length);
        hfTopic.setFrequency((short) temp);
        return new BigDecimal((float) temp).setScale(0, BigDecimal.ROUND_DOWN);
    }

    @Override
    public BigDecimal pvPower() {
        int temp = ByteUtils.byteArrayToInt(Arrays.copyOfRange(statusData, 26, 28),
                0, Arrays.copyOfRange(statusData, 26, 28).length);
        return new BigDecimal((float) temp / 1000).setScale(2, BigDecimal.ROUND_DOWN);
    }

    @Override
    public BigDecimal pvTotalPower() {
        int temp = ByteUtils.byteArrayToInt(Arrays.copyOfRange(statusData, 28, 30),
                0, Arrays.copyOfRange(statusData, 28, 30).length);
        return new BigDecimal((float) temp / 1000).setScale(2, BigDecimal.ROUND_DOWN);
    }

    @Override
    public String mainExpansion() {
        int temp = ByteUtils.byteArrayToInt(Arrays.copyOfRange(statusData, 30, 32),
                0, Arrays.copyOfRange(statusData, 30, 32).length);
        return new BigDecimal((float) temp).setScale(0, BigDecimal.ROUND_DOWN).toString();
    }

    @Override
    public byte[] faultMessage1() {
        byte[] bytes = Arrays.copyOfRange(statusData, 32, 34);
        hxTopic.setErrorCode1(bytes);
        return bytes;
    }

    @Override
    public byte[] faultMessage2() {
        byte[] bytes = Arrays.copyOfRange(statusData, 34, 36);
        hxTopic.setErrorCode0(bytes);
        return bytes;
    }

    @Override
    public String getInTermSpeed() {
        byte[] bytes = Arrays.copyOfRange(statusData, 36, 38);
        int speed = ByteUtils.byteArrayToInt(bytes,
                0, bytes.length);
        String speedStr;
        if (speed == 1) {
            speedStr = "高速";
            hfTopic.setWindSpeed((byte) 0x00);
        } else if (speed == 4) {
            speedStr = "低速";
            hfTopic.setWindSpeed((byte) 0x01);
        } else {
            speedStr = "";
        }
        return speedStr;
    }

    @Override
    public int defrostStatus() {
        byte[] bytes = Arrays.copyOfRange(statusData, 38, 39);
        int status = ByteUtils.byteArrayToInt(bytes, 0, bytes.length);
        return status;
    }

    @Override
    public String functionTestValue() {
        byte[] bytes = Arrays.copyOfRange(statusData, 39, 41);
        return Hex.bytesToHexString(bytes);
    }

    @Override
    public int frequencyTestValue() {
        byte[] bytes = Arrays.copyOfRange(statusData, 41, 43);
        int value = ByteUtils.byteArrayToInt(bytes, 0, bytes.length);
        return value;
    }

    @Override
    public int mainExpansionTest() {
        byte[] bytes = Arrays.copyOfRange(statusData, 43, 45);
        int value = ByteUtils.byteArrayToInt(bytes, 0, bytes.length);
        return value;
    }

    public void uploadData() {
        airConditionerMode();
        switchMode();
        settingTemp();
        inDoorTemp();
        outDoorTemp();
        exHaustTemp();
        frequency();
        outFunSpeed();
        defrostSignal();
        faultMessage1();
        faultMessage2();
        hxTopic.setWindTemp(new byte[2]);
        hxTopic.setHumidityPipeTemp(new byte[2]);
        hxTopic.setUpPipeTemp(new byte[2]);
        hxTopic.setSuctionTemp((short) 0);
        hfTopic.setInHotTemp((short) 0);
    }
}
