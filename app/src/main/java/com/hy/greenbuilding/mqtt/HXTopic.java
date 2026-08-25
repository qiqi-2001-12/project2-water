package com.hy.greenbuilding.mqtt;

import android.util.Log;

import com.hy.greenbuilding.HyApplication;
import com.hy.greenbuilding.utils.ByteUtils;
import com.hy.greenbuilding.utils.Hex;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * 设置数据上报
 */
public class HXTopic {
    //风机设定 48bytes
    private byte[] fanSet = new byte[48];
    //滤网设定 32bytes
    private byte[] screenFilterSet = new byte[24];
    private byte[] screenPressureGet = new byte[8];

    //风阀小板设定 36bytes
    private byte[] airSmallSet = new byte[36];


    //温度反馈 8bytes
    //private byte[] tempResponse = new byte[8];
    private byte[] upPipeTemp = new byte[2];//升温盘管温度
    private byte[] humidityPipeTemp = new byte[2];//除湿盘管温度
    private byte[] windTemp = new byte[2];//送风口温度
    private byte[] inCircleTemp = new byte[2];//内环温

    //CO2和PM2.5设置 12bytes
    private byte[] CO2AndPmSet = new byte[12];
    //防冻设置 21bytes
    private byte[] pidSet = new byte[21];

    //室外机数据 8bytes
    private int suctionTemp;//吸气温度
    private int defrostSignal;//除霜信号
    private int fanSpeed;//室外机风机转速
    private int electricExpansion;//室外电子膨胀阀开度
    //配置文件 19bytes
    private byte fanChoice1;//新风风机选择
    private byte fanChoice2;//排风风机选择
    private byte fanChoice3;//内循环1风机选择
    private byte fanChoice4;//内循环2风机选择
    private byte outTermChoice;//外机选择
    private byte screenChange = (byte) 0x00;//过滤器更换
    private byte[] softwareVersion = new byte[2];//软件版本
    private byte[] hardwareVersion = new byte[2];//硬件版本
    private byte[] errorCode1 = new byte[2];//故障代码1
    private byte[] errorCode0 = new byte[2];//故障代码0
    private byte[] errorCode = new byte[2];//故障代码
    private byte humidity1;//湿度回差值                ================================================================================================================
    private byte coldTemp; //制冷温差限制值            ================================================================================================================
    private byte humidityTemp;//除湿温差限制值         ================================================================================================================

    private byte[] typeAndModel = new byte[2];//风机安装类型和型号
    private byte[] dcSpeed = new byte[2];//DC风机转速  ========================
    private byte electricValveSwitch;//电磁阀开关
    private byte expansionValveSwitch;//电子膨胀阀开关
    private byte[] valveValue = new byte[2];//膨胀阀开度
    private byte valveChangeType;
    private byte[] fixedValue = new byte[2];
    private byte[] targetValue = new byte[2];
    private byte[] staticPressureData = new byte[15];//定静压控制


    private byte heatingDehumControlType; // 1B 升温除湿控制类型
    private byte controlBoardType;        // 1B 控制小板类型
    private byte path1Enable;             // 1B 第一路选通使能
    private byte path1ControlType;        // 1B 第一路控制方式
    private byte path1EevEnable;          // 1B 第一路 EEV 使能
    private byte[] path1EevOpen = new byte[2]; // 2B 第一路 EEV 开度
    private byte[] path1EevManualOpen = new byte[2]; // 2B 第一路 EEV 手动开度    /////////////////////////
    private byte path1SolenoidValveEnable;       // 1B 第一路电磁阀使能
    private byte path1SolenoidValveStatus;       // 1B 第二路电磁阀状态
    private byte path1SolenoidValveManualStatus; // 1B 第一路电磁阀手动状态  //////////////////////
    private byte[] coolingTargetValue = new byte[2]; // 2B 制冷目标值
    private byte[] heatingTargetValue = new byte[2]; // 2B 制热目标值
    private byte path2Enable;             // 1B 第二路选通使能
    private byte path2ControlType;        // 1B 第二路控制方式
    private byte path2EevEnable;          // 1B 第二路 EEV 使能
    private byte[] path2EevOpen = new byte[2]; // 2B 第二路 EEV 开度
    private byte[] path2EevManualOpen = new byte[2]; // 2B 第二路 EEV 手动开度    /////////////////////////
    private byte path2SolenoidValveEnable;       // 1B 第二路电磁阀使能
    private byte path2SolenoidValveStatus;       // 1B 第二路电磁阀状态
    private byte path2SolenoidValveManualStatus; // 1B 第二路电磁阀手动状态  //////////////////////
    private byte[] dehumTargetValue = new byte[2]; // 2B 除湿目标值
    //
    private byte[] eev2PidFixedTarget = new byte[2];   // EEV2_PID 开度调节固定开度调节 (Short)
    private byte systemSwitch;                         // 系统开关机 (byte) ====
    private byte additionalManualMode;                 // 附加手动模式 (byte) ====
    private byte childLockStatus;                      // 童锁状态 (byte) ====
    private byte systemInterfaceMode;                  // 系统界面模式 (byte) ====


    public void setFanSet(byte[] fanSet) {
        this.fanSet = fanSet;
    }

    public void setScreenFilterSet(byte[] screenFilterSet) {
        this.screenFilterSet = screenFilterSet;
    }

    public void setScreenPressureGet(byte[] screenPressureGet) {
        this.screenPressureGet = screenPressureGet;
    }

    public byte getHumidity1() {
        return humidity1;
    }

    public void setHumidity1(byte humidity1) {
        this.humidity1 = humidity1;
    }

    public void setAirSmallSet(byte[] airSmallSet) {
        this.airSmallSet = airSmallSet;
    }

    public byte[] getUpPipeTemp() {
        return upPipeTemp;
    }

    public void setUpPipeTemp(byte[] upPipeTemp) {
        this.upPipeTemp = upPipeTemp;
    }

    public byte[] getHumidityPipeTemp() {
        return humidityPipeTemp;
    }

    public void setHumidityPipeTemp(byte[] humidityPipeTemp) {
        this.humidityPipeTemp = humidityPipeTemp;
    }

    public byte[] getWindTemp() {
        return windTemp;
    }

    public void setWindTemp(byte[] windTemp) {
        this.windTemp = windTemp;
    }

    public byte[] getInCircleTemp() {
        return inCircleTemp;
    }

    public void setInCircleTemp(byte[] inCircleTemp) {
        this.inCircleTemp = inCircleTemp;
    }

    public byte[] getFanSet() {
        return fanSet;
    }

    public byte[] getScreenFilterSet() {
        return screenFilterSet;
    }

    public byte[] getAirSmallSet() {
        return airSmallSet;
    }

    public byte[] getCO2AndPmSet() {
        return CO2AndPmSet;
    }

    public void setCO2AndPmSet(byte[] CO2AndPmSet) {
        this.CO2AndPmSet = CO2AndPmSet;
    }

    public byte[] getPidSet() {
        return pidSet;
    }

    public void setPidSet(byte[] pidSet) {
        this.pidSet = pidSet;
    }

    public void setSuctionTemp(int suctionTemp) {
        this.suctionTemp = suctionTemp;
    }

    public void setDefrostSignal(int defrostSignal) {
        this.defrostSignal = defrostSignal;
    }

    public void setFanSpeed(int fanSpeed) {
        this.fanSpeed = fanSpeed;
    }

    public void setElectricExpansion(int electricExpansion) {
        this.electricExpansion = electricExpansion;
    }

    public void setFanChoice1(byte fanChoice1) {
        this.fanChoice1 = fanChoice1;
    }

    public void setFanChoice2(byte fanChoice2) {
        this.fanChoice2 = fanChoice2;
    }

    public void setFanChoice3(byte fanChoice3) {
        this.fanChoice3 = fanChoice3;
    }

    public void setFanChoice4(byte fanChoice4) {
        this.fanChoice4 = fanChoice4;
    }

    public void setOutTermChoice(byte outTermChoice) {
        this.outTermChoice = outTermChoice;
    }

    public void setScreenChange(byte screenChange) {
        this.screenChange = screenChange;
    }

    public void setSoftwareVersion(byte[] softwareVersion) {
        this.softwareVersion = softwareVersion;
    }

    public void setHardwareVersion(byte[] hardwareVersion) {
        this.hardwareVersion = hardwareVersion;
    }

    public void setErrorCode1(byte[] errorCode1) {
        this.errorCode1 = errorCode1.clone();
    }

    public void setErrorCode0(byte[] errorCode0) {
        this.errorCode0 = errorCode0.clone();
    }

    public void setErrorCode(byte[] errorCode) {
        this.errorCode = errorCode.clone();
    }

    public byte getColdTemp() {
        return coldTemp;
    }

    public void setColdTemp(byte coldTemp) {
        this.coldTemp = coldTemp;
    }

    public byte getHumidityTemp() {
        return humidityTemp;
    }

    public void setHumidityTemp(byte humidityTemp) {
        this.humidityTemp = humidityTemp;
    }

    public byte[] getTypeAndModel() {
        return typeAndModel;
    }

    public void setTypeAndModel(byte[] typeAndModel) {
        this.typeAndModel = typeAndModel;
    }

    public byte[] getDcSpeed() {
        return dcSpeed;
    }

    public void setDcSpeed(byte[] dcSpeed) {
        this.dcSpeed = dcSpeed;
    }

    public byte getElectricValveSwitch() {
        return electricValveSwitch;
    }

    public void setElectricValveSwitch(byte electricValveSwitch) {
        this.electricValveSwitch = electricValveSwitch;
    }

    public byte getExpansionValveSwitch() {
        return expansionValveSwitch;
    }

    public void setExpansionValveSwitch(byte expansionValveSwitch) {
        this.expansionValveSwitch = expansionValveSwitch;
    }

    public byte[] getValveValue() {
        return valveValue;
    }

    public void setValveValue(byte[] valveValue) {
        this.valveValue = valveValue;
    }

    public byte getValveChangeType() {
        return valveChangeType;
    }

    public void setValveChangeType(byte valveChangeType) {
        this.valveChangeType = valveChangeType;
    }

    public byte[] getFixedValue() {
        return fixedValue;
    }

    public void setFixedValue(byte[] fixedValue) {
        this.fixedValue = fixedValue;
    }

    public byte[] getTargetValue() {
        return targetValue;
    }

    public void setTargetValue(byte[] targetValue) {
        this.targetValue = targetValue;
    }

    public byte[] getScreenPressureGet() {
        return screenPressureGet;
    }

    public byte[] getStaticPressureData() {
        return staticPressureData;
    }

    public byte getPath1SolenoidValveStatus() {
        return path1SolenoidValveStatus;
    }

    public void setPath1SolenoidValveStatus(byte path1SolenoidValveStatus) {
        this.path1SolenoidValveStatus = path1SolenoidValveStatus;
    }

    public void setStaticPressureData(byte[] staticPressureData) {
        this.staticPressureData = staticPressureData;
    }

    public int getSuctionTemp() {
        return suctionTemp;
    }

    public int getDefrostSignal() {
        return defrostSignal;
    }

    public int getFanSpeed() {
        return fanSpeed;
    }

    public int getElectricExpansion() {
        return electricExpansion;
    }

    public byte getFanChoice1() {
        return fanChoice1;
    }

    public byte getFanChoice2() {
        return fanChoice2;
    }

    public byte getFanChoice3() {
        return fanChoice3;
    }

    public byte getFanChoice4() {
        return fanChoice4;
    }

    public byte getOutTermChoice() {
        return outTermChoice;
    }

    public byte getScreenChange() {
        return screenChange;
    }

    public byte[] getSoftwareVersion() {
        return softwareVersion;
    }

    public byte[] getHardwareVersion() {
        return hardwareVersion;
    }

    public byte[] getErrorCode1() {
        return errorCode1;
    }

    public byte[] getErrorCode0() {
        return errorCode0;
    }

    public byte[] getErrorCode() {
        return errorCode;
    }

    public byte getHeatingDehumControlType() {
        return heatingDehumControlType;
    }

    public void setHeatingDehumControlType(byte heatingDehumControlType) {
        this.heatingDehumControlType = (byte) (heatingDehumControlType - 1);
    }

    public byte getControlBoardType() {
        return controlBoardType;
    }

    public void setControlBoardType(byte controlBoardType) {
        this.controlBoardType = (byte) (controlBoardType - 1);
    }

    public byte getPath1Enable() {
        return path1Enable;
    }

    public void setPath1Enable(byte path1Enable) {
        this.path1Enable = path1Enable;
    }

    public byte getPath1ControlType() {
        return path1ControlType;
    }

    public void setPath1ControlType(byte path1ControlType) {
        this.path1ControlType = (byte) (path1ControlType - 1);
    }

    public byte getPath1EevEnable() {
        return path1EevEnable;
    }

    public void setPath1EevEnable(byte path1EevEnable) {
        this.path1EevEnable = path1EevEnable;
    }

    public byte[] getPath1EevOpen() {
        return path1EevOpen;
    }

    public void setPath1EevOpen(byte[] path1EevOpen) {
        this.path1EevOpen = path1EevOpen;
    }

    public byte[] getPath1EevManualOpen() {
        return path1EevManualOpen;
    }

    public void setPath1EevManualOpen(byte[] path1EevManualOpen) {
        this.path1EevManualOpen = path1EevManualOpen;
    }

    public byte getPath1SolenoidValveEnable() {
        return path1SolenoidValveEnable;
    }

    public void setPath1SolenoidValveEnable(byte path1SolenoidValveEnable) {
        this.path1SolenoidValveEnable = path1SolenoidValveEnable;
    }

    public byte getPath1SolenoidValveManualStatus() {
        return path1SolenoidValveManualStatus;
    }

    public void setPath1SolenoidValveManualStatus(byte path1SolenoidValveManualStatus) {
        this.path1SolenoidValveManualStatus = path1SolenoidValveManualStatus;
    }

    public byte[] getCoolingTargetValue() {
        return coolingTargetValue;
    }

    public void setCoolingTargetValue(byte[] coolingTargetValue) {
        this.coolingTargetValue = coolingTargetValue;
    }

    public byte[] getHeatingTargetValue() {
        return heatingTargetValue;
    }

    public void setHeatingTargetValue(byte[] heatingTargetValue) {
        this.heatingTargetValue = heatingTargetValue;
    }

    public byte getPath2Enable() {
        return path2Enable;
    }

    public void setPath2Enable(byte path2Enable) {
        this.path2Enable = path2Enable;
    }

    public byte getPath2ControlType() {
        return path2ControlType;
    }

    public void setPath2ControlType(byte path2ControlType) {
        this.path2ControlType = (byte) (path2ControlType - 1);
    }

    public byte getPath2EevEnable() {
        return path2EevEnable;
    }

    public void setPath2EevEnable(byte path2EevEnable) {
        this.path2EevEnable = path2EevEnable;
    }

    public byte[] getPath2EevOpen() {
        return path2EevOpen;
    }

    public void setPath2EevOpen(byte[] path2EevOpen) {
        this.path2EevOpen = path2EevOpen;
    }

    public byte[] getPath2EevManualOpen() {
        return path2EevManualOpen;
    }

    public void setPath2EevManualOpen(byte[] path2EevManualOpen) {
        this.path2EevManualOpen = path2EevManualOpen;
    }

    public byte getPath2SolenoidValveEnable() {
        return path2SolenoidValveEnable;
    }

    public void setPath2SolenoidValveEnable(byte path2SolenoidValveEnable) {
        this.path2SolenoidValveEnable = path2SolenoidValveEnable;
    }

    public byte getPath2SolenoidValveStatus() {
        return path2SolenoidValveStatus;
    }

    public void setPath2SolenoidValveStatus(byte path2SolenoidValveStatus) {
        this.path2SolenoidValveStatus = path2SolenoidValveStatus;
    }

    public byte getPath2SolenoidValveManualStatus() {
        return path2SolenoidValveManualStatus;
    }

    public void setPath2SolenoidValveManualStatus(byte path2SolenoidValveManualStatus) {
        this.path2SolenoidValveManualStatus = path2SolenoidValveManualStatus;
    }

    public byte[] getDehumTargetValue() {
        return dehumTargetValue;
    }

    public void setDehumTargetValue(byte[] dehumTargetValue) {
        this.dehumTargetValue = dehumTargetValue;
    }

    public byte[] getEev2PidFixedTarget() {
        return eev2PidFixedTarget;
    }

    public void setEev2PidFixedTarget(byte[] eev2PidFixedTarget) {
        this.eev2PidFixedTarget = eev2PidFixedTarget;
    }

    public byte getSystemSwitch() {
        return systemSwitch;
    }

    public void setSystemSwitch(byte systemSwitch) {
        this.systemSwitch = systemSwitch;
    }

    public byte getAdditionalManualMode() {
        return additionalManualMode;
    }

    public void setAdditionalManualMode(byte additionalManualMode) {
        this.additionalManualMode = additionalManualMode;
    }

    public byte getChildLockStatus() {
        return childLockStatus;
    }

    public void setChildLockStatus(byte childLockStatus) {
        this.childLockStatus = childLockStatus;
    }

    public byte getSystemInterfaceMode() {
        return systemInterfaceMode;
    }

    public void setSystemInterfaceMode(byte systemInterfaceMode) {
        this.systemInterfaceMode = systemInterfaceMode;
    }

    public byte[] getBytes() {

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(fanSet);
            outputStream.write(screenFilterSet);
            outputStream.write(airSmallSet);
            outputStream.write(screenPressureGet);
            // outputStream.write(tempResponse);
            outputStream.write(ByteUtils.changeBytes(upPipeTemp));
            outputStream.write(ByteUtils.changeBytes(humidityPipeTemp));
            outputStream.write(ByteUtils.changeBytes(windTemp));
            outputStream.write(ByteUtils.changeBytes(inCircleTemp));
            outputStream.write(CO2AndPmSet);
            outputStream.write(pidSet);
            outputStream.write(ByteUtils.changeBytes(ByteUtils.int16ToByteArray(suctionTemp)));
            outputStream.write(ByteUtils.changeBytes(ByteUtils.int16ToByteArray(defrostSignal)));
            outputStream.write(ByteUtils.changeBytes(ByteUtils.int16ToByteArray(fanSpeed)));
            outputStream.write(ByteUtils.changeBytes(ByteUtils.int16ToByteArray(electricExpansion)));
            outputStream.write(fanChoice1);
            outputStream.write(fanChoice2);
            outputStream.write(fanChoice3);
            outputStream.write(fanChoice4);
            outputStream.write(outTermChoice);
            outputStream.write(screenChange);
            outputStream.write(ByteUtils.changeBytes(softwareVersion));
            outputStream.write(ByteUtils.changeBytes(hardwareVersion));
            outputStream.write(ByteUtils.changeBytes(errorCode1));
            outputStream.write(ByteUtils.changeBytes(errorCode0));
            outputStream.write(ByteUtils.changeBytes(errorCode));
            outputStream.write(humidity1);
            outputStream.write(coldTemp);
            outputStream.write(humidityTemp);
            outputStream.write(typeAndModel);
            outputStream.write(ByteUtils.changeBytes(dcSpeed));
            outputStream.write((electricValveSwitch));
            outputStream.write((expansionValveSwitch));
            outputStream.write(ByteUtils.changeBytes(valveValue));
            outputStream.write(valveChangeType);
            outputStream.write(ByteUtils.changeBytes(fixedValue));
            outputStream.write(ByteUtils.changeBytes(targetValue));
            outputStream.write(staticPressureData);

            // 1B 升温除湿控制类型
            outputStream.write(heatingDehumControlType);
            // 1B 控制小板类型
            outputStream.write(controlBoardType);
            // 1B 第一路选通使能
            outputStream.write(path1Enable);
            // 1B 第一路控制方式
            outputStream.write(path1ControlType);
            // 1B 第一路 EEV 使能
            outputStream.write(path1EevEnable);
            // 2B 第一路 EEV 开度
            outputStream.write(ByteUtils.changeBytes(path1EevOpen));
            // 2B 第一路 EEV 手动开度
            outputStream.write(ByteUtils.changeBytes(path1EevManualOpen));
            // 1B 第一路电磁阀使能
            outputStream.write(path1SolenoidValveEnable);
            // 1B 第一路电磁阀状态
            outputStream.write(path1SolenoidValveStatus);
            // 1B 第一路电磁阀手动状态
            outputStream.write(path1SolenoidValveManualStatus);
            // 2B 制冷目标值
            outputStream.write(ByteUtils.changeBytes(coolingTargetValue));
            // 2B 制热目标值
            outputStream.write(ByteUtils.changeBytes(heatingTargetValue));
            // 1B 第二路选通使能
            outputStream.write(path2Enable);
            // 1B 第二路控制方式
            outputStream.write(path2ControlType);
            // 1B 第二路 EEV 使能
            outputStream.write(path2EevEnable);
            // 2B 第二路 EEV 开度
            outputStream.write(ByteUtils.changeBytes(path2EevOpen));
            // 2B 第二路 EEV 手动开度
            outputStream.write(ByteUtils.changeBytes(path2EevManualOpen));
            // 1B 第二路电磁阀使能
            outputStream.write(path2SolenoidValveEnable);
            // 1B 第二路电磁阀状态
            outputStream.write(path2SolenoidValveStatus);
            // 1B 第二路电磁阀手动状态
            outputStream.write(path2SolenoidValveManualStatus);
            // 2B 除湿目标值
            outputStream.write(ByteUtils.changeBytes(dehumTargetValue));


            outputStream.write(ByteUtils.changeBytes(eev2PidFixedTarget));   // 2B EEV2_PID开度调节固定开度调节
            outputStream.write(systemSwitch);                                 // 1B 系统开关机 ===
            outputStream.write(additionalManualMode);                         // 1B 附加手动模式 ====
            outputStream.write(HyApplication.isLocking ? 1 : 0);              // 1B 童锁状态 ====
            outputStream.write(systemInterfaceMode);                          // 1B 系统界面模式


            Log.i("TAG", "send length -HX-- data "+ Hex.bytesToHexString(outputStream.toByteArray()) );

            return outputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
