package com.hy.greenbuilding.protocol.ResPonseInfo;

import android.util.Log;

import com.hy.greenbuilding.mqtt.HDTopic;
import com.hy.greenbuilding.mqtt.HXTopic;
import com.hy.greenbuilding.mqtt.MqttUploadManager;
import com.hy.greenbuilding.utils.ByteUtils;

import java.math.BigDecimal;
import java.util.Arrays;

public class CustomDataInfo {

    private byte[] customData;
    private HXTopic hxTopic;
    private HDTopic hdTopic;
    private int byteLength = (byte) 0x0003;
    public CustomDataInfo(byte[] data){
        this.customData = data;
        hxTopic = MqttUploadManager.getInstance().getmHxTopic();
        hdTopic =  MqttUploadManager.getInstance().getmHDTopic();
        if(data != null && data.length >= byteLength){
            customData = data;
        }else{
            customData = new byte[byteLength];
        }
        sendData();
    }
    /**
     * 制冷温差限制值
     * @return
     */
    public int getColdTemp(){
        byte[] bytes = Arrays.copyOfRange(this.customData, 0, 1);
        int value = ByteUtils.byteArrayToInt16(bytes);
        BigDecimal bigDecimal = new BigDecimal((float)value / 10).setScale(0, BigDecimal.ROUND_DOWN);
        int value1 = bigDecimal.intValue();

        Log.e("TAG", "getColdTemp: "+ value1);
        hxTopic.setColdTemp((byte)value1);
        return value;
    }

    /**
     * 除湿温差限制值
     * @return
     */
    public int getHumidityTemp(){
        byte[] bytes = Arrays.copyOfRange(this.customData, 1, 2);
        int value = ByteUtils.byteArrayToInt16(bytes);
        BigDecimal bigDecimal = new BigDecimal((float)value / 10).setScale(0, BigDecimal.ROUND_DOWN);
        int value1 = bigDecimal.intValue();

        Log.e("TAG", "getHumidityTemp: "+value1);
        hxTopic.setHumidityTemp((byte)value1);
        return value;
    }

    /**
     * 除湿使能位开关
     * @return
     */
    public int getHumiditySwitch(){
        byte[] bytes = Arrays.copyOfRange(this.customData, 2, 3);
        int value = ByteUtils.byteArrayToInt16(bytes);
        hdTopic.setDeHumiditySwitch((byte)value);
        return value;
    }

    public void sendData(){
        getColdTemp();
        getHumidityTemp();
        getHumiditySwitch();
    }
}
