package com.hy.greenbuilding.protocol.ResPonseInfo;

import android.util.Log;

import com.hy.greenbuilding.mqtt.HXTopic;
import com.hy.greenbuilding.mqtt.MqttUploadManager;
import com.hy.greenbuilding.utils.ByteUtils;
import com.hy.greenbuilding.utils.Hex;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * PID数据(23byte)
 */
public class PIDStatusInfo {
    private byte[] pidData;
    private boolean isSuccess;
    private int type;
    private HXTopic hxTopic;
    private int byteLength = (byte) 0x0017;
    public PIDStatusInfo(byte[] data,boolean isSuccess,int type){
        if(data != null && data.length >= byteLength){
            this.pidData = data;
        }else{
            this.pidData = new byte[byteLength];
        }
//        Log.e("TAG", "PIDStatusInfo: "+Hex.toHexString(pidData));

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
     * P值
     * @return
     */
    public int getPValue(){
        byte[] bytes = Arrays.copyOfRange(this.pidData, 0, 2);
        return ByteUtils.byteArrayToInt(bytes,0,bytes.length);
    }

    /**
     * I值
     * @return
     */
    public int getIValue(){
        byte[] bytes = Arrays.copyOfRange(this.pidData, 2, 4);
        return ByteUtils.byteArrayToInt(bytes,0,bytes.length);
    }

    /**
     * D值
     * @return
     */
    public int getDValue(){
        byte[] bytes = Arrays.copyOfRange(this.pidData, 4, 6);
        return ByteUtils.byteArrayToInt(bytes,0,bytes.length);
    }

    /**
     * PID周期
     * @return
     */
    public int getPIDTime(){
        byte[] bytes = Arrays.copyOfRange(this.pidData, 6, 8);
        return ByteUtils.byteArrayToInt(bytes,0,bytes.length);
    }

    /**
     * PID最小输出
     * @return
     */
    public int getPIDMin(){
        byte[] bytes = Arrays.copyOfRange(this.pidData, 8, 10);
        return ByteUtils.byteArrayToInt(bytes,0,bytes.length);
    }
    /**
     * PID故障代码
     * @return
     */
    public String getPIDError(){
        String errorCode = "其它";
        byte[] bytes = Arrays.copyOfRange(this.pidData, 10, 11);
        int value = ByteUtils.byteArrayToInt16(bytes);
        switch (value){
            case 0:
                errorCode = "正常";
                break;
            case 1:
                errorCode = "220V电源接入";
                break;
            case 2:
                errorCode = "温度超过上限";
                break;
            case 4:
                errorCode = "温度超过下限";
                break;
            case 8:
                errorCode = "驱动异常或接入PTC";
                break;
        }
        return errorCode;
    }

    /**
     * 室外机的室外温度
     * @return
     */
    public BigDecimal getOutDeviceTemp(){
        byte[] bytes = Arrays.copyOfRange(this.pidData, 11, 13);
        int value = ByteUtils.byteArrayToInt16(bytes);
        return new BigDecimal(value).divide(new BigDecimal(10)).setScale(1, BigDecimal.ROUND_DOWN);
    }
    /**
     * 设置温度1
     * @return
     */
        public BigDecimal getTempSet1(){
        byte[] bytes = Arrays.copyOfRange(this.pidData, 13, 15);
        int value = ByteUtils.byteArrayToInt16(bytes);
        return new BigDecimal(value).divide(new BigDecimal(10)).setScale(1, BigDecimal.ROUND_DOWN);
    }

    /**
     * 设置温度2
     * @return
     */
    public BigDecimal getTempSet2(){
        byte[] bytes = Arrays.copyOfRange(this.pidData, 15, 17);
        int value = ByteUtils.byteArrayToInt16(bytes);
        return new BigDecimal(value).divide(new BigDecimal(10)).setScale(1, BigDecimal.ROUND_DOWN);
    }

    /**
     * 设置室外机的室外温度
     * @return
     */
    public BigDecimal getDeviceTempSet(){
        byte[] bytes = Arrays.copyOfRange(this.pidData, 17, 19);
        int value = ByteUtils.byteArrayToInt16(bytes);
        return new BigDecimal(value).divide(new BigDecimal(10)).setScale(1, BigDecimal.ROUND_DOWN);
    }

    /**
     * PID开关状态
     * @return
     */
    public int getPIDStatus(){
        byte[] bytes = Arrays.copyOfRange(this.pidData, 19, 20);
        int value = ByteUtils.byteArrayToInt16(bytes);
        return value;
    }

    /**
     * 选择执行的目标温度
     * @return
     */
    public int getChoiceTemp(){
        byte[] bytes = Arrays.copyOfRange(this.pidData, 20, 21);
        int value = ByteUtils.byteArrayToInt16(bytes);
        return value;
    }
    /**
     * 防冻NTC温度值
     * @return
     */
    public BigDecimal getPidNTC(){
        byte[] bytes = Arrays.copyOfRange(this.pidData, 21, 23);
        int value = ByteUtils.byteArrayToInt16(bytes);
        return new BigDecimal(value).divide(new BigDecimal(10)).setScale(1, BigDecimal.ROUND_DOWN);
    }
    public void sendData(){
        ByteBuffer byteBuffer = ByteBuffer.allocate(21);
        byteBuffer.put(ByteUtils.changeBytes(Arrays.copyOfRange(this.pidData, 21, 23)));
        byteBuffer.put(ByteUtils.changeBytes(Arrays.copyOfRange(this.pidData, 17, 19)));
        byteBuffer.put(ByteUtils.changeBytes(Arrays.copyOfRange(this.pidData, 0, 2)));
        byteBuffer.put(ByteUtils.changeBytes(Arrays.copyOfRange(this.pidData, 2, 4)));
        byteBuffer.put(ByteUtils.changeBytes(Arrays.copyOfRange(this.pidData, 4, 6)));
        byteBuffer.put(ByteUtils.changeBytes(Arrays.copyOfRange(this.pidData, 6, 8)));
        byteBuffer.put(ByteUtils.changeBytes(Arrays.copyOfRange(this.pidData, 8, 10)));
        byteBuffer.put(ByteUtils.changeBytes(Arrays.copyOfRange(this.pidData, 13, 15)));
        byteBuffer.put(ByteUtils.changeBytes(Arrays.copyOfRange(this.pidData, 15, 17)));
        if(ByteUtils.byteArrayToInt16(Arrays.copyOfRange(this.pidData, 11, 13)) != 0){
            byteBuffer.put(ByteUtils.changeBytes(Arrays.copyOfRange(this.pidData, 11, 13)));
        }else{
            byteBuffer.put(ByteUtils.changeBytes(Arrays.copyOfRange(this.pidData, 17, 19)));
        }
        byteBuffer.put(Arrays.copyOfRange(this.pidData, 10, 11));
        hxTopic.setPidSet(byteBuffer.array());
    }
}
