package com.hy.greenbuilding.protocol.ResPonseInfo;

import com.hy.greenbuilding.mqtt.HXTopic;
import com.hy.greenbuilding.mqtt.MqttUploadManager;
import com.hy.greenbuilding.utils.ByteUtils;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class CO2StatusInfo {
    private byte[] co2Data;
    private boolean isSuccess;
    private int type;
    private HXTopic hxTopic;
    public CO2StatusInfo(byte[] data,boolean isSuccess,int type){
        this.co2Data = data;
        this.isSuccess = isSuccess;
        this.type = type;
        hxTopic = MqttUploadManager.getInstance().getmHxTopic();
    }

    public boolean getSuccess(){
        return this.isSuccess;
    }

    public int getType(){
        return this.type;
    }

    /**
     * CO2低速
     * @return
     */
    public int getCO2Min(){
        byte[] bytes = Arrays.copyOfRange(this.co2Data, 0, 2);
        return ByteUtils.byteArrayToInt(bytes,0,bytes.length);
    }
    /**
     * CO2中速
     * @return
     */
    public int getCO2Middle(){
        byte[] bytes = Arrays.copyOfRange(this.co2Data, 2, 4);
        return ByteUtils.byteArrayToInt(bytes,0,bytes.length);
    }
    /**
     * CO2高速
     * @return
     */
    public int getCO2High(){
        byte[] bytes = Arrays.copyOfRange(this.co2Data, 4, 6);
        return ByteUtils.byteArrayToInt(bytes,0,bytes.length);
    }
    /**
     * PM2.5低速
     * @return
     */
    public int getPmMin(){
        byte[] bytes = Arrays.copyOfRange(this.co2Data, 6, 8);
        return ByteUtils.byteArrayToInt(bytes,0,bytes.length);
    }
    /**
     * PM2.5中速
     * @return
     */
    public int getPmMiddle(){
        byte[] bytes = Arrays.copyOfRange(this.co2Data, 8, 10);
        return ByteUtils.byteArrayToInt(bytes,0,bytes.length);
    }
    /**
     * PM2.5高速
     * @return
     */
    public int getPmHigh(){
        byte[] bytes = Arrays.copyOfRange(this.co2Data, 10, 12);
        return ByteUtils.byteArrayToInt(bytes,0,bytes.length);
    }

    public void sendData(){
        ByteBuffer byteBuffer = ByteBuffer.allocate(12);
        byteBuffer.put(ByteUtils.changeBytes(Arrays.copyOfRange(this.co2Data, 0, 2)));
        byteBuffer.put(ByteUtils.changeBytes(Arrays.copyOfRange(this.co2Data, 2, 4)));
        byteBuffer.put(ByteUtils.changeBytes(Arrays.copyOfRange(this.co2Data, 4, 6)));
        byteBuffer.put(ByteUtils.changeBytes(Arrays.copyOfRange(this.co2Data, 6, 8)));
        byteBuffer.put(ByteUtils.changeBytes(Arrays.copyOfRange(this.co2Data, 8, 10)));
        byteBuffer.put(ByteUtils.changeBytes(Arrays.copyOfRange(this.co2Data, 10, 12)));
        hxTopic.setCO2AndPmSet(byteBuffer.array());
    }
}
