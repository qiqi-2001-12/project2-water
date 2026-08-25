package com.hy.greenbuilding.protocol;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.serialport.SerialHelper;
import android.util.Log;

import com.google.gson.Gson;
import com.hy.greenbuilding.HyApplication;
import com.hy.greenbuilding.event.FanErrorEvent;
import com.hy.greenbuilding.event.FanResetEvent;
import com.hy.greenbuilding.event.FunctionTestEvent;
import com.hy.greenbuilding.event.OTAErrorEvent;
import com.hy.greenbuilding.event.OTAStatusEvent;
import com.hy.greenbuilding.event.ResetSystemEvent;
import com.hy.greenbuilding.event.RunModeEvent;
import com.hy.greenbuilding.event.SetStatusEvent;
import com.hy.greenbuilding.event.UptempStatusChangeEvent;
import com.hy.greenbuilding.protocol.ResPonseInfo.AirValveStatusInfo;
import com.hy.greenbuilding.protocol.ResPonseInfo.CO2StatusInfo;
import com.hy.greenbuilding.protocol.ResPonseInfo.CustomDataInfo;
import com.hy.greenbuilding.protocol.ResPonseInfo.DCFanStatusInfo;
import com.hy.greenbuilding.protocol.ResPonseInfo.ElectricityMeterInfo;
import com.hy.greenbuilding.protocol.ResPonseInfo.EnvironmentDataInfo;
import com.hy.greenbuilding.protocol.ResPonseInfo.FanStatusInfo;
import com.hy.greenbuilding.protocol.ResPonseInfo.MainControlInfo;
import com.hy.greenbuilding.protocol.ResPonseInfo.OutDoorStatusInfo;
import com.hy.greenbuilding.protocol.ResPonseInfo.PIDStatusInfo;
import com.hy.greenbuilding.protocol.ResPonseInfo.PVStatusInfo;
import com.hy.greenbuilding.protocol.ResPonseInfo.UpTempStatusInfo;
import com.hy.greenbuilding.protocol.command.OTARequestCommand;
import com.hy.greenbuilding.protocol.command.UpTempCommand;
import com.hy.greenbuilding.utils.ByteUtils;
import com.hy.greenbuilding.utils.Hex;

import org.greenrobot.eventbus.EventBus;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * 接收串口数据
 */
public class SpDataProcessor {
    private static final SpDataProcessor ourInstance = new SpDataProcessor();
    private SerialHelper mSerialHelper;
    private byte[] mResponseBuffer = new byte[0];
    private static final int MAX_FRAME_LENGTH = 256;

    // 定义单线程
    private final HandlerThread mSerialThread = new HandlerThread("SerialRead");
    private Handler mSerialHandler;

    public static SpDataProcessor getInstance() {
        return ourInstance;
    }

    private SpDataProcessor() {
        mSerialThread.start();
        mSerialHandler = new Handler(mSerialThread.getLooper());

        mSerialHelper = new SerialHelper() {
            private static final int BUFFER_INIT_CAPACITY = 1024;
            private byte[] buffer = new byte[BUFFER_INIT_CAPACITY];
            private int bufferLen = 0; // 当前有效长度

            @Override
            protected void onDataReceived(byte[] paramComBean) {
                // 全部抛到单工作线程，不阻塞主线程
                mSerialHandler.post(() -> {
                    try {
                        ensureCapacity(bufferLen + paramComBean.length);
                        System.arraycopy(paramComBean, 0, buffer, bufferLen, paramComBean.length);
                        bufferLen += paramComBean.length;
                        int offset = 0;
                        while (bufferLen - offset >= 10) {
                            int headerIndex = findHeader(buffer, offset, bufferLen);
                            if (headerIndex < 0) {
                                bufferLen = 0;
                                return;
                            }
                            offset = headerIndex;
                            if (bufferLen - offset < 10) break;
                            int dataLen = ((buffer[offset + 8] & 0xFF) << 8) | (buffer[offset + 9] & 0xFF);
                            int frameTotalLen = 10 + dataLen + 2;
                            if (frameTotalLen > MAX_FRAME_LENGTH || frameTotalLen <= 0) {
                                offset++;
                                continue;
                            }
                            if (bufferLen - offset < frameTotalLen) break;
                            byte[] oneFrame = Arrays.copyOfRange(buffer, offset, offset + frameTotalLen);
                            int nextHeaderInFrame = findHeader(buffer, offset + 2, offset + frameTotalLen);
                            if (nextHeaderInFrame != -1) {
//                                Log.e("TAG", "发现帧内嵌套帧头，丢弃当前，跳转到: " + nextHeaderInFrame);
                                offset = nextHeaderInFrame;
                                continue;
                            }
                            int currentNum = ((oneFrame[3] & 0xFF) << 8) | (oneFrame[4] & 0xFF);
                            String serialNumber = Hex.bytesToHexString(new byte[]{oneFrame[5], oneFrame[6]});
                            if (HyApplication.getAndRemoveState(currentNum) != null || serialNumber.equals("0005")) {
//                                Log.e("TAG", "检查到数据，开始 CRC 校验");
                                short crcCalc = crcVerify(oneFrame, oneFrame.length - 2);
                                short crcStored = (short) (((oneFrame[frameTotalLen - 2] & 0xFF) << 8) | (oneFrame[frameTotalLen - 1] & 0xFF));

                                if (crcCalc == crcStored) {
                                    // --- 校验通过：解析并跳过整帧 ---
                                    SpResponse spResponse = new SpResponse(oneFrame);
                                    processSpResponse(spResponse);
                                    offset += frameTotalLen;
//                                    Log.e("TAG", "校验通过，处理完成"+Hex.bytesToHexString(oneFrame));
                                } else {
                                    // --- 校验失败：说明这包数据坏了，去找包内的下一个 AA 55 ---
//                                    Log.e("TAG", "CRC 校验失败！");
                                    int nextPossibleHeader = findHeader(buffer, offset + 1, offset + frameTotalLen);
                                    if (nextPossibleHeader != -1) {
                                        offset = nextPossibleHeader;
                                    } else {
                                        offset += frameTotalLen;
                                    }
                                }
                            } else {
                                // 4. 【如果没有数据】直接跳过这一帧，不浪费 CPU 算校验
//                                Log.d("TAG", "标识 " + String.format("%04X", currentNum) + " 不在处理队列，跳过"+Hex.bytesToHexString(oneFrame));
                                offset += frameTotalLen;
                            }
                        }
                        if (offset > 0) {
                            System.arraycopy(buffer, offset, buffer, 0, bufferLen - offset);
                            bufferLen -= offset;
                        }

                        if (bufferLen > 2048) {
                            bufferLen = 0;
                            buffer = new byte[BUFFER_INIT_CAPACITY];
                        }
                    } catch (Exception e) {
                        bufferLen = 0;
                        buffer = new byte[BUFFER_INIT_CAPACITY];
                    }
                });
            }

            /** 确保缓冲区容量够用 */
            private void ensureCapacity(int size) {
                if (size <= buffer.length) return;
                int newSize = Math.max(size, buffer.length * 2);
                buffer = Arrays.copyOf(buffer, newSize);
            }

            /** 查找帧头 */
            private int findHeader(byte[] data, int from, int to) {
                for (int i = from; i < to - 1; i++) {
                    if (data[i] == (byte) 0xAA && data[i + 1] == (byte) 0x55)
                        return i;
                }
                return -1;
            }
        };
    }

    /**
     * CRC校验函数，与提供的C语言版本功能一致
     *
     * @param data   要进行CRC校验的数据字节数组
     * @param length 要校验的数据长度
     * @return 计算得到的CRC校验值
     */
    public static short crcVerify(byte[] data, int length) {
        int temp0;
        int temp1;
        int crcData = 0xFFFF;
        int state;

        for (temp0 = 0; temp0 < length; temp0++) {
            crcData ^= (data[temp0] & 0xFF);
            for (temp1 = 0; temp1 < 8; temp1++) {
                state = crcData & 0x01; // 取最低位
                crcData = (crcData >> 1) & 0x7FFF; // 右移一位并清除最高位

                if (state == 0x01) {
                    crcData ^= 0xA001; // 多项式异或
                }
            }
        }
        // 交换高低字节
        crcData = ((crcData >> 8) & 0xFF) | ((crcData << 8) & 0xFF00);
        return (short) crcData;
    }

    private void processSpResponse(SpResponse spResponse) {
        if (spResponse.getTermType() == FunctionObject.LOW_TEMP) {
            //低温增焓
            if (spResponse.getFunctionId() == FunctionObject.GET_OUT_STATUS) {
                //获取室外机状态
                OutDoorStatusInfo outDoorStatusInfo = new OutDoorStatusInfo(spResponse.data);
                EventBus.getDefault().post(outDoorStatusInfo);
            }

            switch (spResponse.getFunctionId()) {
                case FunctionObject.SET_MODE:
                    //设定模式
                    outTermStatus(Hex.bytesToHexString(spResponse.data), 1);
                    break;
                case FunctionObject.SET_POWER:
                    //设定压缩机频率
                    outTermStatus(Hex.bytesToHexString(spResponse.data), 2);
                    break;
                case FunctionObject.FORCE_DEFROST:
                    //强制除霜
                    outTermStatus(Hex.bytesToHexString(spResponse.data), 3);
                    break;
                case FunctionObject.ABILITY_TEST:
                    //能力测试
                    Log.e("TAG", "ABILITY_TEST"+Hex.bytesToHexString(spResponse.data));

                    outTermStatus(Hex.bytesToHexString(spResponse.data), 4);
                    break;
                case FunctionObject.MAIN_EXPANSION:
                    //设定主膨胀阀
                    outTermStatus(Hex.bytesToHexString(spResponse.data), 5);
                    break;
            }

        } else if (spResponse.getTermType() == FunctionObject.PV) {
            //光伏
            if (spResponse.getFunctionId() == FunctionObject.GET_OUT_STATUS) {
                //获取室外机状态
                PVStatusInfo pvStatusInfo = new PVStatusInfo(spResponse.data);
                EventBus.getDefault().post(pvStatusInfo);
            }
            switch (spResponse.getFunctionId()) {
                case FunctionObject.SET_MODE:
                    //设定模式
                    outTermStatus(Hex.bytesToHexString(spResponse.data), 1);
                    break;
                case FunctionObject.SET_POWER:
                    //设定压缩机频率
                    outTermStatus(Hex.bytesToHexString(spResponse.data), 2);
                    break;
                case FunctionObject.FORCE_DEFROST:
                    //强制除霜
                    outTermStatus(Hex.bytesToHexString(spResponse.data), 3);
                    break;
                case FunctionObject.ABILITY_TEST:
                    //能力测试
                    outTermStatus(Hex.bytesToHexString(spResponse.data), 4);
                    break;
                case FunctionObject.MAIN_EXPANSION:
                    //设定主膨胀阀
                    outTermStatus(Hex.bytesToHexString(spResponse.data), 5);
                    break;
            }
        } else if (spResponse.getTermType() == FunctionObject.TEMP_RISE) {
            //升温除湿
            switch (spResponse.getFunctionId()) {
                case FunctionObject.UP_GET_OUT_STATUS:
//                    Log.e("TAG", "processSpResponse: UP_GET_OUT_STATUS = "+Hex.toHexString(spResponse.data));
                    //获取室外机状态
                    UpTempStatusInfo statusInfo = new UpTempStatusInfo(spResponse.data);
                    EventBus.getDefault().post(statusInfo);
                    break;
                case FunctionObject.UP_SET_MODE:

                case FunctionObject.UP_FREQUNCY:

                case FunctionObject.UP_MAIN_EEV_MODE:

                case FunctionObject.UP_MAIN_EEV_OPEN:

                case FunctionObject.UP_AUX_EEV_MODE:

                case FunctionObject.UP_AUX_EEV_OPEN:

                case FunctionObject.UP_AUX_EEV_OPEN_MIN:

                case FunctionObject.UP_FAN_NUM:

                case FunctionObject.UP_FAN_SPEED_MAX:

                case FunctionObject.UP_FAN_SPEED_MIN:

                case FunctionObject.UP_SPEED_STATUS:

                case FunctionObject.UP_SET_SPEED:

                case FunctionObject.UP_PRESS_TYPE:
                case FunctionObject.UP_SET_TYPE:
                    // EventBus.getDefault().post(new UptempStatusChangeEvent(Hex.bytesToHexString(spResponse.data), FunctionObject.UP_SET_MODE));
                    break;
                case FunctionObject.UP_DEFROST_MODE:
                case FunctionObject.UP_DEFROST_STATUS:
                case FunctionObject.UP_SET_COMMON_DATA:
                    if (Hex.bytesToHexString(spResponse.data).contains("00")) {
                        EventBus.getDefault().post(new UptempStatusChangeEvent(Hex.bytesToHexString(spResponse.data), true));
                    } else {
                        EventBus.getDefault().post(new UptempStatusChangeEvent(Hex.bytesToHexString(spResponse.data), false));
                    }
                    break;
            }

        } else if (spResponse.getTermType() == FunctionObject.ENVIRONMENT_CHECK) {
            //环境检测
            if (spResponse.getFunctionId() == FunctionObject.GET_ENVIRONMENT_STATUS) {
                if (spResponse.data.length >= 0x006E) {
                    EnvironmentDataInfo environmentDataInfo = new EnvironmentDataInfo(spResponse.data);
                    EventBus.getDefault().post(environmentDataInfo);
                }
            } else if (spResponse.getFunctionId() == FunctionObject.SET_CO2_VALUE) {
                CO2StatusInfo co2StatusInfo;
                if (Hex.bytesToHexString(spResponse.data).contains("00")) {
                    co2StatusInfo = new CO2StatusInfo(spResponse.data, true, 1);
                } else {
                    co2StatusInfo = new CO2StatusInfo(spResponse.data, false, 1);
                }
                EventBus.getDefault().post(co2StatusInfo);
            } else if (spResponse.getFunctionId() == FunctionObject.SET_PM_VALUE) {
                CO2StatusInfo co2StatusInfo;
                if (Hex.bytesToHexString(spResponse.data).contains("00")) {
                    co2StatusInfo = new CO2StatusInfo(spResponse.data, true, 2);
                } else {
                    co2StatusInfo = new CO2StatusInfo(spResponse.data, false, 2);
                }
                EventBus.getDefault().post(co2StatusInfo);
            } else if (spResponse.getFunctionId() == FunctionObject.GET_PM_CO2) {
                CO2StatusInfo co2StatusInfo = new CO2StatusInfo(spResponse.data, true, 3);
                EventBus.getDefault().post(co2StatusInfo);
            }
        } else if (spResponse.getTermType() == FunctionObject.PID_SET) {
            //PID
            if (spResponse.getFunctionId() == FunctionObject.GET_PID_STATUS) {
                //获取PID信息
                PIDStatusInfo pidStatusInfo = new PIDStatusInfo(spResponse.data, true, 1);
                EventBus.getDefault().post(pidStatusInfo);
            } else if (spResponse.getFunctionId() == FunctionObject.SET_PID_VALUE) {
                //设置PID值
                PIDStatusInfo pidStatusInfo;
                if (Hex.bytesToHexString(spResponse.data).equals("00")) {
                    pidStatusInfo = new PIDStatusInfo(spResponse.data, true, 2);
                } else {
                    pidStatusInfo = new PIDStatusInfo(spResponse.data, false, 2);
                }
                EventBus.getDefault().post(pidStatusInfo);
            } else if (spResponse.getFunctionId() == FunctionObject.SET_PID_TEMP1 || spResponse.getFunctionId() == FunctionObject.SET_PID_TEMP2 || spResponse.getFunctionId() == FunctionObject.SET_OUT_TEMP) {
                //设置PID温度
                PIDStatusInfo pidStatusInfo;
                if (Hex.bytesToHexString(spResponse.data).equals("00")) {
                    pidStatusInfo = new PIDStatusInfo(spResponse.data, true, 3);
                } else {
                    pidStatusInfo = new PIDStatusInfo(spResponse.data, false, 3);
                }
                EventBus.getDefault().post(pidStatusInfo);
            }

        } else if (spResponse.getTermType() == FunctionObject.FILTER_SCREEN) {
            //风阀
            AirValveStatusInfo statusInfo;
            if (spResponse.getFunctionId() == FunctionObject.GET_AIR_VALVE_STATUS) {
                statusInfo = new AirValveStatusInfo(spResponse.data);
                EventBus.getDefault().post(statusInfo);
            } else if (spResponse.getFunctionId() == FunctionObject.SET_AIR_VALVE_OPEN || spResponse.getFunctionId() == FunctionObject.SET_AIR_VALVE_OPEN_MAX) {
                FanErrorEvent fanErrorEvent;
                if (Hex.bytesToHexString(spResponse.data).contains("00")) {
                    fanErrorEvent = new FanErrorEvent(5, true);
                } else {
                    fanErrorEvent = new FanErrorEvent(5, false);
                }
                EventBus.getDefault().post(fanErrorEvent);
            }
        } else if (spResponse.getTermType() == FunctionObject.DC_FAN) {
            //DC风机
            DCFanStatusInfo dcFanStatusInfo = null;
            if (spResponse.getFunctionId() == FunctionObject.GET_DC_FAN_STATUS) {
                dcFanStatusInfo = new DCFanStatusInfo(spResponse.data, true, FunctionObject.GET_DC_FAN_STATUS);
            } else {
                if (Hex.bytesToHexString(spResponse.data).equals("00")) {
                    dcFanStatusInfo = new DCFanStatusInfo(spResponse.data, true, FunctionObject.SET_DC_FAN_SPEED);
                } else {
                    dcFanStatusInfo = new DCFanStatusInfo(spResponse.data, false, FunctionObject.SET_DC_FAN_SPEED);
                }
            }
            EventBus.getDefault().post(dcFanStatusInfo);

        } else if (spResponse.getTermType() == FunctionObject.HOUR_METER) {
            //电表
            if (spResponse.getFunctionId() == 0x01) {
                int byteLength = (byte) 0x000E;
                if (spResponse.data.length < byteLength) {
//                    Log.i("info", "electricityMeterInfo: error");
                } else {
//                    Log.i("info", "electricityMeterInfo: success");
                    ElectricityMeterInfo electricityMeterInfo = new ElectricityMeterInfo(spResponse.data);
                    EventBus.getDefault().post(electricityMeterInfo);
                }
            } else {
                //电能清零
                SetStatusEvent setStatusEvent;
                if (Hex.bytesToHexString(spResponse.data).equals("00")) {
                    setStatusEvent = new SetStatusEvent(4, true);
                } else {
                    setStatusEvent = new SetStatusEvent(4, false);
                }
                EventBus.getDefault().post(setStatusEvent);
            }

        } else if (spResponse.getTermType() == FunctionObject.FAN) {
            //风机
            if (spResponse.getFunctionId() == FunctionObject.GET_FAN_STATUS) {
                //获取风机状态
                FanStatusInfo fanStatusInfo = new FanStatusInfo(spResponse.data);
                EventBus.getDefault().post(fanStatusInfo);
            } else if (spResponse.getFunctionId() == FunctionObject.SET_SPEED) {
                //设定风速
            } else if (spResponse.getFunctionId() == FunctionObject.SET_FAN_TYPE) {
                //设定风机类型
                FanErrorEvent fanErrorEvent;
                if (Hex.bytesToHexString(spResponse.data).equals("00")) {
                    fanErrorEvent = new FanErrorEvent(2, true);
                } else {
                    fanErrorEvent = new FanErrorEvent(2, false);
                }
                EventBus.getDefault().post(fanErrorEvent);
            } else if (spResponse.getFunctionId() == FunctionObject.SET_SPEED_VALUE) {
                //设定风量
                FanErrorEvent fanErrorEvent;
                if (Hex.bytesToHexString(spResponse.data).equals("00")) {
                    fanErrorEvent = new FanErrorEvent(3, true);
                } else if (Hex.bytesToHexString(spResponse.data).equals("01")) {
                    fanErrorEvent = new FanErrorEvent(3, false);
                } else {
                    fanErrorEvent = new FanErrorEvent(3, true);
                }
                EventBus.getDefault().post(fanErrorEvent);
            } else if (spResponse.getFunctionId() == FunctionObject.TEST_FAN_VALUE) {
                //测试风量
                FanResetEvent fanResetEvent = new FanResetEvent(1, spResponse.data);
                EventBus.getDefault().post(fanResetEvent);
            } else if (spResponse.getFunctionId() == FunctionObject.SET_FAN_ADDRESS) {
                //设置风机地址
                FanResetEvent fanResetEvent = new FanResetEvent(2, spResponse.data);
                EventBus.getDefault().post(fanResetEvent);
            } else if (spResponse.getFunctionId() == FunctionObject.SET_FAN_PRESSURE_VALUE || spResponse.getFunctionId() == FunctionObject.SET_STATIC_PRESSURE_MODE) {
                //设置风机压力值
                FanErrorEvent fanErrorEvent;
                if (Hex.bytesToHexString(spResponse.data).equals("00")) {
                    fanErrorEvent = new FanErrorEvent(5, true);
                } else {
                    fanErrorEvent = new FanErrorEvent(5, false);
                }
                EventBus.getDefault().post(fanErrorEvent);
            }
        } else if (spResponse.getTermType() == FunctionObject.CUSTOM_GET) {
            if (spResponse.getFunctionId() == FunctionObject.GET_CUSTOM_DATA) {
                CustomDataInfo customDataInfo = new CustomDataInfo(spResponse.data);
                EventBus.getDefault().post(customDataInfo);
            }
        } else if (spResponse.getTermType() == FunctionObject.MAIN_CONTROL_BOARD) {


            //主控板
            if (spResponse.getFunctionId() == FunctionObject.GET_CONTROL_STATUS && Hex.bytesToHexString(spResponse.command).equals("8003")) {
                //获取主控板运行状态
                MainControlInfo mainControlInfo = new MainControlInfo(spResponse.data);
//                Log.e("TAG", "processSpResponse: "+new Gson().toJson(spResponse.data));
//                Log.e("TAG", "settingUpdateEvent11: "+new Gson().toJson(mainControlInfo));

                EventBus.getDefault().post(mainControlInfo);
            } else if (spResponse.getFunctionId() == FunctionObject.SET_LOW_POWER && Hex.bytesToHexString(spResponse.command).equals("8002")) {
                //低功耗模式控制
                Log.i("info", "低功耗返回--" + Hex.bytesToHexString(mResponseBuffer));

                SetStatusEvent setStatusEvent;
                if (Hex.bytesToHexString(spResponse.data).equals("00")) {
                    setStatusEvent = new SetStatusEvent(1, true);
                } else {
                    setStatusEvent = new SetStatusEvent(1, false);
                }
                EventBus.getDefault().post(setStatusEvent);
            } else if (spResponse.getFunctionId() == FunctionObject.SET_CONTROL_MODE && Hex.bytesToHexString(spResponse.command).equals("8002")) {
                //设置主控板运行模式
                byte[] modeBytes = spResponse.data;

                //定时器模式
                byte[] timerByte = Arrays.copyOfRange(modeBytes, 1, 2);
                int timerMode = ByteUtils.byteArrayToInt16(timerByte);

                //运行模式
                byte[] runModeByte = Arrays.copyOfRange(modeBytes, 2, 3);
                int runMode = ByteUtils.byteArrayToInt16(runModeByte);

                // 手动模式
                byte[] manualModeByte = Arrays.copyOfRange(modeBytes, 3, 4);
                int manualMode = ByteUtils.byteArrayToInt16(manualModeByte);
                EventBus.getDefault().post(new RunModeEvent(timerMode == 0 ? true : false, runMode,manualMode));

            } else if (spResponse.getFunctionId() == FunctionObject.SET_HUMIDITY && Hex.bytesToHexString(spResponse.command).equals("8002")) {
                //设置湿度
                SetStatusEvent setStatusEvent;
                if (Hex.bytesToHexString(spResponse.data).equals("00")) {
                    setStatusEvent = new SetStatusEvent(2, true);
                } else {
                    setStatusEvent = new SetStatusEvent(2, false);
                }
                EventBus.getDefault().post(setStatusEvent);
            } else if (spResponse.getFunctionId() == FunctionObject.SET_TEMP_SECTION
                    && Hex.bytesToHexString(spResponse.command).equals("8002")) {
                //设置温度上下限
                SetStatusEvent setStatusEvent;
                if (Hex.bytesToHexString(spResponse.data).contains("00")) {
                    setStatusEvent = new SetStatusEvent(3, true);
                } else {
                    setStatusEvent = new SetStatusEvent(3, false);
                }
                EventBus.getDefault().post(setStatusEvent);
            } else if (spResponse.getFunctionId() == FunctionObject.SET_OUTDOOR_TYPE && Hex.bytesToHexString(spResponse.command).equals("8002")) {
                //设置室外机类型
                SetStatusEvent setStatusEvent;
                if (Hex.bytesToHexString(spResponse.data).equals("00")) {
                    setStatusEvent = new SetStatusEvent(5, true);
                } else {
                    setStatusEvent = new SetStatusEvent(5, false);
                }
                EventBus.getDefault().post(setStatusEvent);
            } else if (spResponse.getFunctionId() == FunctionObject.GET_TEMP_SWITCH && Hex.bytesToHexString(spResponse.command).equals("8002")) {
                //温控状态
//                        TempControlEvent tempControlEvent = new TempControlEvent(Integer.parseInt(Hex.bytesToHexString(spResponse.data)));
//                        EventBus.getDefault().post(tempControlEvent);
            } else if (Hex.bytesToHexString(spResponse.command).equals("0006")) {
                //ota状态上报
                Log.i("info", "ota状态上报.." + Hex.bytesToHexString(spResponse.getOtaData()) );
                OTAErrorEvent otaErrorEvent;
                try {
                    if (Hex.bytesToHexString(spResponse.getOtaData()).equals("00")) {
                        otaErrorEvent = new OTAErrorEvent(0);
                        // isUpdate = false;
                    } else if (Hex.bytesToHexString(spResponse.getOtaData()).equals("01")) {
                        isUpdate = false;
                        otaErrorEvent = new OTAErrorEvent(1);
                    } else if (Hex.bytesToHexString(spResponse.getOtaData()).equals("04")) {
                        isUpdate = false;
                        otaErrorEvent = new OTAErrorEvent(4);
                    } else {
                        isUpdate = false;
                        otaErrorEvent = new OTAErrorEvent(3);
                    }
                    EventBus.getDefault().post(otaErrorEvent);
                } catch (Exception e) {
                    e.printStackTrace();
                    isUpdate = false;
                }

            } else if (Hex.bytesToHexString(spResponse.command).equals("0005")) {
                isUpdate = false;
                //ota升级请求
                Log.i("info", "ota升级请求.."+Hex.bytesToHexString(spResponse.getOtaData()));
                OTAStatusEvent otaStatusEvent = new OTAStatusEvent(spResponse.getOtaData());
                EventBus.getDefault().post(otaStatusEvent);
            } else if (Hex.bytesToHexString(spResponse.command).equals("8007")) {
                //恢复出厂设置
                //发送AA550100010007000000549F
                //返回AA5501000180070000040F000100B29C
                ResetSystemEvent resetSystemEvent;
                if (Hex.bytesToHexString(spResponse.getOtaData()).equals("00")) {
                    resetSystemEvent = new ResetSystemEvent(true);
                } else {
                    resetSystemEvent = new ResetSystemEvent(false);
                }

                EventBus.getDefault().post(resetSystemEvent);
            }
        }
    }

    private void outTermStatus(String data, int type) {
        FunctionTestEvent functionTestEvent;
        if (data.equals("00")) {
            functionTestEvent = new FunctionTestEvent(type, true, data);
        } else {
            functionTestEvent = new FunctionTestEvent(type, false, data);
        }
        EventBus.getDefault().post(functionTestEvent);
    }

    // SpDataProcessor.java 的 init() 方法
    public void init() {
        try {
            mSerialHelper.open();
        } catch (Exception e) {
            e.printStackTrace();
        }

        initCommandQueue();
    }

    public void close() {
        mSerialHelper.close();
    }

    // 添加同步锁对象
    private final Object sendLock = new Object();

    /**
     * 发送串口命令
     *
     * @param spCommand
     */
//    public void send(SpCommand spCommand) {
//        // 使用同步锁确保同一时间只有一个发送操作
//        synchronized (sendLock) {
//            // --- 强制检查点 ---
//            Log.i("info", "Serial Open Status: " + mSerialHelper.isOpen());
//            Log.i("info", "isUpdate Status: " + isUpdate);
//            // --- 强制检查点 ---
//
//            if (mSerialHelper.isOpen() && spCommand != null) {
//                if (!isUpdate) { // 只有 isUpdate 为 false 才会走到这里
//                    Log.i("info", "send data--- " + Hex.bytesToHexString(spCommand.getBytes())); // 这是你想要看到的打印
//                    SystemClock.sleep(300);
//                    mSerialHelper.send(spCommand.getBytes());
//                } else {
//                    Log.i("info", "Command blocked: isUpdate is TRUE."); // 新增：确认是否被锁定
//                }
//            }
//        }
//    }

    private final BlockingQueue<SpCommand> commandQueue = new PriorityBlockingQueue<>();

    private void initCommandQueue() {
        new Thread("SerialCommandQueue") {
            @Override
            public void run() {
                while (!isInterrupted()) {
                    try {
                        processCommand(commandQueue.take());
                    } catch (InterruptedException e) {
                        Log.e("SpDataProcessor", "Queue thread interrupted", e);
                        break;
                    }
                }
            }
        }.start();
    }

    // 处理单个命令（原 send 方法的核心逻辑）
    private void processCommand(SpCommand spCommand) {
        synchronized (sendLock) {
            if (mSerialHelper.isOpen() && spCommand != null) {
                if (!isUpdate) {
                    // --- 添加这行日志 ---
//                    Log.i("QueueTest", ">>> 实际执行发送，优先级为: " + spCommand.priority);
//                    Log.d("SpDataProcessor", "正在发送指令: " + Hex.bytesToHexString(spCommand.getBytes()) + " | 优先级: " + spCommand.priority+ " | 功能id: " + spCommand.functionId);

                    mSerialHelper.send(spCommand.getBytes());
                    if (spCommand.priority < 5) {
                        // 高优先级指令（如恢复出厂、OTA）通常需要更多处理时间
                        SystemClock.sleep(120);
                    } else {
                        // 普通指令间隔
                        SystemClock.sleep(50);
                    }
//                    SystemClock.sleep(50);
//                    SystemClock.sleep(100);
                }
            }
        }
    }

    // 修改 send 方法为入队操作
    public void send(SpCommand spCommand) {
        if (spCommand == null) {
//            Log.i("info", "Null command, skip enqueue");
            return;
        }
//        Log.d("QueueTest", "入队: " + spCommand.priority);
        try {
            commandQueue.put(spCommand);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * OTA升级通知
     *
     * @param
     */
    public void send1(OTARequestCommand command) {
        if (mSerialHelper.isOpen() && command != null) {
//            Log.i("info", "send ota = " + Hex.bytesToHexString(command.getBytes1()));
            mSerialHelper.send(command.getBytes1());
        }
    }

    /**
     * OTA升级数据下发
     *
     * @param
     */
    public void send2(OTARequestCommand command) {
        if (mSerialHelper.isOpen() && command != null) {
            isUpdate = true;
            byte[] bytes = command.getBytes2();
            mSerialHelper.send(bytes);
        }
    }

    /**
     * 恢复出厂设置
     *
     * @param
     */
    public void send3(OTARequestCommand command) {
        if (mSerialHelper.isOpen() && command != null) {
            if (!isUpdate) {
                byte[] bytes = command.getBytes3();
                mSerialHelper.send(bytes);
                SystemClock.sleep(120);
            }
        }
    }

    public void send4(UpTempCommand command) {
        if (mSerialHelper.isOpen() && command != null) {
            if (!isUpdate) {
                byte[] bytes = command.getBytes4();
//                Log.i("info", "通用数据 = " + Hex.bytesToHexString(bytes));
                mSerialHelper.send(bytes);
                SystemClock.sleep(120);
            }
        }
    }

    private boolean isUpdate = false;
}
