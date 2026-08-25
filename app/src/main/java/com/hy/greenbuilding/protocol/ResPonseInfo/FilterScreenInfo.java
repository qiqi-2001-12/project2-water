//package com.hy.greenbuilding.protocol.ResPonseInfo;
//
//import com.hy.greenbuilding.mqtt.HXTopic;
//import com.hy.greenbuilding.mqtt.MqttUploadManager;
//import com.hy.greenbuilding.utils.ByteUtils;
//
//import java.util.Arrays;
//
//public class FilterScreenInfo {
//    private byte[] filterScreenData;
//    private HXTopic hxTopic;
//    private int byteLength = (byte) 0x0008;
//    public FilterScreenInfo(byte[] data){
//        if(data != null && data.length >= byteLength){
//            this.filterScreenData = data;
//        }else{
//            this.filterScreenData = new byte[byteLength];
//        }
//        hxTopic = MqttUploadManager.getInstance().getmHxTopic();
//        hxTopic.setScreenPressureGet(this.filterScreenData);
//        //AA55010001800307000B0300080000000000000000CC47
//    }
//
//    /**
//     * 新风压差
//     * @return
//     */
//    public int getFreshAirValue(){
//        byte[] bytes = Arrays.copyOfRange(this.filterScreenData, 0, 2);
//        int value = ByteUtils.byteArrayToInt16(bytes);
//        return value;
//    }
//
//    /**
//     * 排风压差
//     * @return
//     */
//    public int getExhaustValue(){
//        byte[] bytes = Arrays.copyOfRange(this.filterScreenData, 2, 4);
//        int value = ByteUtils.byteArrayToInt16(bytes);
//        return value;
//    }
//
//    /**
//     * 内循环1压差
//     * @return
//     */
//    public int getCircleWind1Value(){
//        byte[] bytes = Arrays.copyOfRange(this.filterScreenData, 4, 6);
//        int value = ByteUtils.byteArrayToInt16(bytes);
//        return value;
//    }
//
//    /**
//     * 内循环2压差
//     * @return
//     */
//    public int getCircleWind2Value(){
//        byte[] bytes = Arrays.copyOfRange(this.filterScreenData, 6, 8);
//        int value = ByteUtils.byteArrayToInt16(bytes);
//        return value;
//    }
//
//}
