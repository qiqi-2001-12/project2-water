package com.hy.greenbuilding.protocol.ResPonseInfo;

import android.util.Log;

import com.google.gson.Gson;
import com.hy.greenbuilding.HyApplication;
import com.hy.greenbuilding.config.ErrorDefine;
import com.hy.greenbuilding.model.UpTempSystemStatusInfo;
import com.hy.greenbuilding.mqtt.HDTopic;
import com.hy.greenbuilding.mqtt.HFTopic;
import com.hy.greenbuilding.mqtt.HSTopic;
import com.hy.greenbuilding.mqtt.HXTopic;
import com.hy.greenbuilding.mqtt.MqttUploadManager;
import com.hy.greenbuilding.utils.ByteConvert;
import com.hy.greenbuilding.utils.ByteUtils;
import com.hy.greenbuilding.utils.Hex;

import org.apache.mina.core.buffer.IoBuffer;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class UpTempStatusInfo {

    private byte[] statusData;
    private HDTopic hdTopic;
    private HFTopic hfTopic;
    private HXTopic hxTopic;
    private HSTopic hsTopic;
    private HashMap<String, Object> upTempMap = new HashMap<>();
    private int byteLength = (byte) 0x0072;

    public UpTempStatusInfo(byte[] data) {
        this.statusData = data;

        hdTopic = MqttUploadManager.getInstance().getmHDTopic();
        hfTopic = MqttUploadManager.getInstance().getmHFTopic();
        hxTopic = MqttUploadManager.getInstance().getmHxTopic();
        hsTopic = MqttUploadManager.getInstance().getmHsTopic();
        if (data != null && data.length >= byteLength) {
            this.statusData = data;
        } else {
            this.statusData = new byte[byteLength];
        }
        Log.i("info", data.length+"----upTemp data---" + statusData.length);
        parseData(this.statusData);
        hsTopic.setHsData(this.statusData);
    }


    public static int getInt(IoBuffer buffer) {
        byte[] bytes = new byte[4];
        buffer.get(bytes);
        return ByteConvert.bytesToInt(bytes);
    }


    public static short getShort(IoBuffer buffer) {
        byte[] bytes = new byte[2];
        buffer.get(bytes);
        return ByteConvert.bytesToShort(bytes);
    }

    public static byte[] get2Bytes(IoBuffer buffer) {
        byte[] bytes = new byte[2];
        buffer.get(bytes);
        return bytes;
    }

    //解析升温除湿数据
    private void parseData(byte[] data) {
        upTempMap.clear();
        IoBuffer ioBuffer = IoBuffer.wrap(data);
        short mode = getShort(ioBuffer);
        String openMode;
        String runMode;
        if (mode == 0) {
            openMode = "关";
            runMode = "关机";
        } else {
            openMode = "开";
            runMode = airConditionerMode(mode);
        }
        upTempMap.put(ErrorDefine.OPEN_CLOSE, openMode);
        upTempMap.put(ErrorDefine.RUN_MODE, runMode);

        //除霜信号
        short single1 = getShort(ioBuffer);
        upTempMap.put(ErrorDefine.DEFROST_SIGNAL, single1);
        hxTopic.setDefrostSignal(single1);
        //除霜模式
        short single2 = getShort(ioBuffer);
        upTempMap.put(ErrorDefine.DEFROST_MODEL, single2);
        //制冷设定温度
        BigDecimal coldTemp = new BigDecimal((float) getShort(ioBuffer) / 10).setScale(1, BigDecimal.ROUND_DOWN);
        upTempMap.put(ErrorDefine.COLD_SET_TEMP, coldTemp);
        hfTopic.setSetTemp(coldTemp.shortValue());
        //制热设定温度
        BigDecimal hotTemp = new BigDecimal((float) getShort(ioBuffer) / 10).setScale(1, BigDecimal.ROUND_DOWN);
        upTempMap.put(ErrorDefine.HOT_SET_TEMP, hotTemp);

        //室内温度
        BigDecimal inDoorTemp = new BigDecimal((float) getShort(ioBuffer) / 10).setScale(1, BigDecimal.ROUND_DOWN);
        upTempMap.put(ErrorDefine.DOOR_IN_TEMP, inDoorTemp);
        hfTopic.setInTemp(inDoorTemp.shortValue());
        BigDecimal inDoorTemp1 = inDoorTemp.setScale(0, BigDecimal.ROUND_DOWN);
        hxTopic.setInCircleTemp(ByteUtils.shortToByteArray(inDoorTemp1.shortValue()));

        //出风温度
        BigDecimal windTemp = new BigDecimal((float) getShort(ioBuffer) / 10).setScale(1, BigDecimal.ROUND_DOWN);
        upTempMap.put(ErrorDefine.WIND_MODE, windTemp);
        //升温盘管温度
        BigDecimal upPipeTemp = new BigDecimal((float) getShort(ioBuffer) / 10).setScale(1, BigDecimal.ROUND_DOWN);
        upTempMap.put(ErrorDefine.UP_PIPE_TEMP, upPipeTemp);
        hxTopic.setUpPipeTemp(ByteUtils.shortToByteArray(upPipeTemp.shortValue()));

        //除湿盘管温度
        BigDecimal humiPipeTemp = new BigDecimal((float) getShort(ioBuffer) / 10).setScale(1, BigDecimal.ROUND_DOWN);
        upTempMap.put(ErrorDefine.HUMI_PIPE_TEMP, humiPipeTemp);
        hxTopic.setHumidityPipeTemp(ByteUtils.shortToByteArray(humiPipeTemp.shortValue()));
        //外环温度
        short aShort1 = getShort(ioBuffer);
        BigDecimal outCircleTemp = new BigDecimal((float)  aShort1 / 10).setScale(1, BigDecimal.ROUND_DOWN);
        upTempMap.put(ErrorDefine.OUT_CIRCLE_TEMP, outCircleTemp);
//        HyApplication.setOutTemp(outCircleTemp);
        Log.e("TAG", "parseData: "+outCircleTemp);
        HyApplication.setOutTemp(new BigDecimal(aShort1));

        //外盘管温度
        BigDecimal outPipeTemp = new BigDecimal((float) getShort(ioBuffer) / 10).setScale(1, BigDecimal.ROUND_DOWN);
        upTempMap.put(ErrorDefine.OUT_PIPE_TEMP, outPipeTemp);

        //回气温度
        BigDecimal returnTemp = new BigDecimal((float) getShort(ioBuffer) / 10).setScale(1, BigDecimal.ROUND_DOWN);
        upTempMap.put(ErrorDefine.RETURN_AIR_TEMP, returnTemp);

        //排气温度
        BigDecimal exhuastTemp = new BigDecimal((float) getShort(ioBuffer) / 10).setScale(1, BigDecimal.ROUND_DOWN);
        upTempMap.put(ErrorDefine.EXHAUST_TEMP, exhuastTemp);
        BigDecimal bigDecimal2 = exhuastTemp.setScale(0, BigDecimal.ROUND_DOWN);
        hfTopic.setInAuxTemp(bigDecimal2.shortValue());

        //蒸发温度
        BigDecimal evaporationTemp = new BigDecimal((float) getShort(ioBuffer) / 10).setScale(1, BigDecimal.ROUND_DOWN);
        upTempMap.put(ErrorDefine.EVAPORATION_TEMP, evaporationTemp);
        //冷凝温度
        BigDecimal condensingTemp = new BigDecimal((float) getShort(ioBuffer) / 10).setScale(1, BigDecimal.ROUND_DOWN);
        upTempMap.put(ErrorDefine.CONDENSATION_CLOSE, condensingTemp);
        //低压压力
        BigDecimal lowPressure = new BigDecimal((float) getShort(ioBuffer)).setScale(0, BigDecimal.ROUND_DOWN);
        upTempMap.put(ErrorDefine.SLOW_PRESS, lowPressure);
        //高压压力
        BigDecimal highPressure = new BigDecimal((float) getShort(ioBuffer)).setScale(0, BigDecimal.ROUND_DOWN);
        upTempMap.put(ErrorDefine.HIGH_PRESS, highPressure);
        //压机目标频率
        BigDecimal press1 = new BigDecimal((float) getShort(ioBuffer)).setScale(0, BigDecimal.ROUND_DOWN);
        upTempMap.put(ErrorDefine.PRESS_FREQUENCY, press1);
        //压机运行频率
        BigDecimal press2 = new BigDecimal((float) getShort(ioBuffer)).setScale(0, BigDecimal.ROUND_DOWN);
        upTempMap.put(ErrorDefine.PRESS_RUN_FREQUENCY, press2);
        //压缩机电流
        BigDecimal electric = new BigDecimal((float) getShort(ioBuffer)).setScale(0, BigDecimal.ROUND_DOWN);
        upTempMap.put(ErrorDefine.COPM_ELEC, electric);
        //驱动运行状态
        BigDecimal status = new BigDecimal((float) getShort(ioBuffer)).setScale(0, BigDecimal.ROUND_DOWN);
        upTempMap.put(ErrorDefine.DRIVE_RUN_STATUS, status);
        //IPM温度
        BigDecimal ipmTemp = new BigDecimal((float) getShort(ioBuffer) / 10).setScale(1, BigDecimal.ROUND_DOWN);
        upTempMap.put(ErrorDefine.IPM_TEMP, ipmTemp);
        //母线电压
        short voltage = getShort(ioBuffer);
        upTempMap.put(ErrorDefine.MAIN_VOLTAGE, voltage);
        //输入电流
        short inputElectric = getShort(ioBuffer);
        upTempMap.put(ErrorDefine.IN_ELEC, inputElectric);
        //目标转速
        short rotate = getShort(ioBuffer);
        upTempMap.put(ErrorDefine.TARGET_SPEED, rotate);
        //主膨胀阀初开度
        short mainValve = getShort(ioBuffer);
        upTempMap.put(ErrorDefine.MAIN_VALVE_OPEN, mainValve);
        //主膨胀阀过热度
        short mainValve1 = getShort(ioBuffer);
        upTempMap.put(ErrorDefine.MAIN_VALVE, mainValve1);
        //辅助膨胀阀过热度
        short auxValve = getShort(ioBuffer);
        upTempMap.put(ErrorDefine.AUX_VALVE, auxValve);
        //风机1转速
        short rotate1 = getShort(ioBuffer);
        upTempMap.put(ErrorDefine.WIND_ROTATION_SPEED, rotate1);
        hxTopic.setFanSpeed(rotate1);

        //软件版本号
        short version = getShort(ioBuffer);
        upTempMap.put(ErrorDefine.SOFTWARE_VERSION, version);
        //系统状态
        short status1 = getShort(ioBuffer);
        upTempMap.put(ErrorDefine.SYSTEM_STATUS, status1);
        //故障信息1
        byte[] errorBytes1 = ByteUtils.shortToByteArray(getShort(ioBuffer));
        List<UpTempSystemStatusInfo> list1 = new ArrayList<>();
        if (errorBytes1 != null) {
            byte[] bytes = ByteUtils.getBitArray(errorBytes1);
            for (int i = 0; i < bytes.length; i++) {
                if (bytes[i] == 1) {
                    UpTempSystemStatusInfo upTempSystemStatusInfo = new UpTempSystemStatusInfo();
                    upTempSystemStatusInfo.setName(ErrorDefine.UpTempError1[bytes.length - 1 - i]);
                    list1.add(upTempSystemStatusInfo);
                }
            }
        }
        upTempMap.put(ErrorDefine.ERROR_CODE1, new Gson().toJson(list1));
        //故障信息2
        byte[] errorBytes2 = ByteUtils.shortToByteArray(getShort(ioBuffer));
        List<UpTempSystemStatusInfo> list2 = new ArrayList<>();
        if (errorBytes2 != null) {
            byte[] bytes = ByteUtils.getBitArray(errorBytes2);
            for (int i = 0; i < bytes.length; i++) {
                if (bytes[i] == 1) {
                    UpTempSystemStatusInfo upTempSystemStatusInfo = new UpTempSystemStatusInfo();
                    upTempSystemStatusInfo.setName(ErrorDefine.UpTempError2[bytes.length - 1 - i]);
                    list2.add(upTempSystemStatusInfo);
                }
            }
        }
        upTempMap.put(ErrorDefine.ERROR_CODE2, new Gson().toJson(list2));
        //故障信息3

        byte[] errorBytes3 = ByteUtils.shortToByteArray(getShort(ioBuffer));
        List<UpTempSystemStatusInfo> list3 = new ArrayList<>();
        if (errorBytes3 != null) {
            byte[] bytes = ByteUtils.getBitArray(errorBytes3);
            for (int i = 0; i < bytes.length; i++) {
                if (bytes[i] == 1) {
                    UpTempSystemStatusInfo upTempSystemStatusInfo = new UpTempSystemStatusInfo();
                    upTempSystemStatusInfo.setName(ErrorDefine.UpTempError3[bytes.length - 1 - i]);
                    list3.add(upTempSystemStatusInfo);
                }
            }
        }
        upTempMap.put(ErrorDefine.ERROR_CODE3, new Gson().toJson(list3));

        //故障信息4
        byte[] errorBytes4 = ByteUtils.shortToByteArray(getShort(ioBuffer));
        List<UpTempSystemStatusInfo> list4 = new ArrayList<>();
        if (errorBytes4 != null) {
            byte[] bytes = ByteUtils.getBitArray(errorBytes4);
            for (int i = 0; i < bytes.length; i++) {
                if (bytes[i] == 1) {
                    UpTempSystemStatusInfo upTempSystemStatusInfo = new UpTempSystemStatusInfo();
                    upTempSystemStatusInfo.setName(ErrorDefine.UpTempError4[bytes.length - 1 - i]);
                    list4.add(upTempSystemStatusInfo);
                }
            }
        }
        upTempMap.put(ErrorDefine.ERROR_CODE4, new Gson().toJson(list4));

        //故障信息5
        byte[] errorBytes5 = ByteUtils.shortToByteArray(getShort(ioBuffer));
        List<UpTempSystemStatusInfo> list5 = new ArrayList<>();
        if (errorBytes5 != null) {
            byte[] bytes = ByteUtils.getBitArray(errorBytes5);
            for (int i = 0; i < bytes.length; i++) {
                if (bytes[i] == 1) {
                    UpTempSystemStatusInfo upTempSystemStatusInfo = new UpTempSystemStatusInfo();
                    upTempSystemStatusInfo.setName(ErrorDefine.UpTempError5[bytes.length - 1 - i]);
                    list5.add(upTempSystemStatusInfo);
                }
            }
        }
        upTempMap.put(ErrorDefine.ERROR_CODE5, new Gson().toJson(list5));

        //故障信息6
        byte[] errorBytes6 = ByteUtils.shortToByteArray(getShort(ioBuffer));
        List<UpTempSystemStatusInfo> list6 = new ArrayList<>();
        if (errorBytes6 != null) {
            byte[] bytes = ByteUtils.getBitArray(errorBytes6);
            for (int i = 0; i < bytes.length; i++) {
                if (bytes[i] == 1) {
                    UpTempSystemStatusInfo upTempSystemStatusInfo = new UpTempSystemStatusInfo();
                    upTempSystemStatusInfo.setName(ErrorDefine.UpTempError6[bytes.length - 1 - i]);
                    list6.add(upTempSystemStatusInfo);
                }
            }
        }
        upTempMap.put(ErrorDefine.ERROR_CODE6, new Gson().toJson(list6));


        //四通阀状态
        short status2 = getShort(ioBuffer);
        upTempMap.put(ErrorDefine.FOUR_STATUS, status2);
        //电加热状态
        short status3 = getShort(ioBuffer);
        upTempMap.put(ErrorDefine.HEAT_STATUS, status3);
        //加热带状态
        short status4 = getShort(ioBuffer);
        upTempMap.put(ErrorDefine.HEAT, status4);
        //除湿旁通阀状态
        short status5 = getShort(ioBuffer);
        upTempMap.put(ErrorDefine.HUMI_VALVE, status5);
        //增焓阀状态
        short status6 = getShort(ioBuffer);
        upTempMap.put(ErrorDefine.ENTHALPY_VALVE, status6);
        //主路EEV
        short mainEEV = getShort(ioBuffer);
        upTempMap.put(ErrorDefine.MAIN_EEV, mainEEV);
        //辅助EEV
        short auxEEV = getShort(ioBuffer);
        upTempMap.put(ErrorDefine.AUX_EEV, auxEEV);
        //手动外风机使能
        short enable = getShort(ioBuffer);
        upTempMap.put(ErrorDefine.MANUAL_FAN_ENABLE, enable);
        //手动频率
        short frequency = getShort(ioBuffer);
        upTempMap.put(ErrorDefine.MANUAL_FREQUENCY, frequency);
        //主路EEV模式
        short eevModel = getShort(ioBuffer);
        upTempMap.put(ErrorDefine.MAIN_EEV_MODEL, eevModel);
        //主路EEV初开度
        short eevOpen = getShort(ioBuffer);
        upTempMap.put(ErrorDefine.MAIN_EEV_OPEN, eevOpen);
        //辅助EEV模式
        short auxModel = getShort(ioBuffer);
        upTempMap.put(ErrorDefine.AUX_EEV_MODEL, auxModel);
        //辅助EEV初开度
        short auxOpen = getShort(ioBuffer);
        upTempMap.put(ErrorDefine.AUX_EEV_OPEN, auxOpen);
        //辅助EEV最小开度
        short auxOpenMin = getShort(ioBuffer);
        upTempMap.put(ErrorDefine.AUX_EEV_OPEN_MIN, auxOpenMin);
        //直流风机数量
        short fanNum = getShort(ioBuffer);
        upTempMap.put(ErrorDefine.FAN_NUM, fanNum);
        //直流风机最高转速
        short max = getShort(ioBuffer);
        upTempMap.put(ErrorDefine.FAN_SPEED_MAX, max);
        //直流风机最低转速
        short min = getShort(ioBuffer);
        upTempMap.put(ErrorDefine.FAN_SPEED_MIN, min);
        //风机手动转速
        short speed = getShort(ioBuffer);
        upTempMap.put(ErrorDefine.FAN_SPEED, speed);
        //驱动型号
        short type1 = getShort(ioBuffer);
        upTempMap.put(ErrorDefine.DRIVE_TYPE, type1);
        //压缩机型号
        byte[] type2 = get2Bytes(ioBuffer);
        int type = ByteUtils.byteArrayToInt(type2, 0, type2.length);
        upTempMap.put(ErrorDefine.COMP_TYPE, type);
    }


    public HashMap<String, Object> getDataMap() {
        return upTempMap;
    }

    public String airConditionerMode(int mode) {
        String airMode = "";
        if (mode == 0) {
            airMode = "关机";
        } else if (mode == 1) {
            airMode = "制冷";
            hfTopic.setAirConditionMode((byte) 0x01);
        } else if (mode == 2) {
            airMode = "制热";
            hfTopic.setAirConditionMode((byte) 0x02);
        } else if (mode == 3) {
            airMode = "送风";
            hfTopic.setAirConditionMode((byte) 0x03);
        } else if (mode == 4) {
            airMode = "除湿";
            hfTopic.setAirConditionMode((byte) 0x04);
        } else if (mode == 5) {
            airMode = "待机";
            hfTopic.setAirConditionMode((byte) 0x05);
        } else if (mode == 6) {
            airMode = "强制除氟";
            hfTopic.setAirConditionMode((byte) 0x06);
        }
        return airMode;
    }

}
