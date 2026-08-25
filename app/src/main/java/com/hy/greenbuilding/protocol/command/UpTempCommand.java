package com.hy.greenbuilding.protocol.command;

import com.hy.greenbuilding.HyApplication;
import com.hy.greenbuilding.protocol.FunctionObject;
import com.hy.greenbuilding.protocol.SpCommand;
import com.hy.greenbuilding.utils.ByteUtils;
import com.hy.greenbuilding.utils.CrcUtils;
import com.hy.greenbuilding.utils.DigitalUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * 升温除湿
 */
public class UpTempCommand extends SpCommand {

    public UpTempCommand(int id) {
        super((byte) 0x03);
        init(id);
    }

    private void init(int id) {
        switch (id) {
            case FunctionObject.UP_GET_OUT_STATUS:
                //获取室外机状态
                command = new byte[]{(byte) 0x00, (byte) 0x03};
                functionId = FunctionObject.UP_GET_OUT_STATUS;
                functionLength = new byte[]{(byte) 0x00, (byte) 0x00};
                dataLength = new byte[]{(byte) 0x00, (byte) 0x03};
                priority = 20;
                break;
            case FunctionObject.UP_SET_MODE:
                command = new byte[]{(byte) 0x00, (byte) 0x02};
                functionId = FunctionObject.UP_SET_MODE;
                functionLength = new byte[]{(byte) 0x00, (byte) 0x02};
                dataLength = new byte[]{(byte) 0x00, (byte) 0x05};
                priority = 10;
                break;
            case FunctionObject.UP_DEFROST_MODE:
                command = new byte[]{(byte) 0x00, (byte) 0x02};
                functionId = FunctionObject.UP_DEFROST_MODE;
                functionLength = new byte[]{(byte) 0x00, (byte) 0x02};
                dataLength = new byte[]{(byte) 0x00, (byte) 0x05};
                priority = 10;
                break;
            case FunctionObject.UP_DEFROST_STATUS:
                command = new byte[]{(byte) 0x00, (byte) 0x02};
                functionId = FunctionObject.UP_DEFROST_STATUS;
                functionLength = new byte[]{(byte) 0x00, (byte) 0x02};
                dataLength = new byte[]{(byte) 0x00, (byte) 0x05};
                priority = 10;
                break;
            case FunctionObject.UP_FREQUNCY:
                command = new byte[]{(byte) 0x00, (byte) 0x02};
                functionId = FunctionObject.UP_FREQUNCY;
                functionLength = new byte[]{(byte) 0x00, (byte) 0x01};
                dataLength = new byte[]{(byte) 0x00, (byte) 0x05};
                priority = 10;
                break;
            case FunctionObject.UP_MAIN_EEV_MODE:
                command = new byte[]{(byte) 0x00, (byte) 0x02};
                functionId = FunctionObject.UP_MAIN_EEV_MODE;
                functionLength = new byte[]{(byte) 0x00, (byte) 0x02};
                dataLength = new byte[]{(byte) 0x00, (byte) 0x05};
                priority = 10;
                break;
            case FunctionObject.UP_MAIN_EEV_OPEN:
                command = new byte[]{(byte) 0x00, (byte) 0x02};
                functionId = FunctionObject.UP_MAIN_EEV_OPEN;
                functionLength = new byte[]{(byte) 0x00, (byte) 0x02};
                dataLength = new byte[]{(byte) 0x00, (byte) 0x05};
                priority = 10;
                break;
            case FunctionObject.UP_AUX_EEV_MODE:
                command = new byte[]{(byte) 0x00, (byte) 0x02};
                functionId = FunctionObject.UP_AUX_EEV_MODE;
                functionLength = new byte[]{(byte) 0x00, (byte) 0x02};
                dataLength = new byte[]{(byte) 0x00, (byte) 0x05};
                priority = 10;
                break;
            case FunctionObject.UP_AUX_EEV_OPEN:
                command = new byte[]{(byte) 0x00, (byte) 0x02};
                functionId = FunctionObject.UP_AUX_EEV_OPEN;
                functionLength = new byte[]{(byte) 0x00, (byte) 0x01};
                dataLength = new byte[]{(byte) 0x00, (byte) 0x05};
                priority = 10;
                break;
            case FunctionObject.UP_AUX_EEV_OPEN_MIN:
                command = new byte[]{(byte) 0x00, (byte) 0x02};
                functionId = FunctionObject.UP_AUX_EEV_OPEN_MIN;
                functionLength = new byte[]{(byte) 0x00, (byte) 0x02};
                dataLength = new byte[]{(byte) 0x00, (byte) 0x05};
                priority = 10;
                break;
            case FunctionObject.UP_FAN_NUM:
                command = new byte[]{(byte) 0x00, (byte) 0x02};
                functionId = FunctionObject.UP_FAN_NUM;
                functionLength = new byte[]{(byte) 0x00, (byte) 0x02};
                dataLength = new byte[]{(byte) 0x00, (byte) 0x05};
                priority = 10;
                break;
            case FunctionObject.UP_FAN_SPEED_MAX:
                command = new byte[]{(byte) 0x00, (byte) 0x02};
                functionId = FunctionObject.UP_FAN_SPEED_MAX;
                functionLength = new byte[]{(byte) 0x00, (byte) 0x02};
                dataLength = new byte[]{(byte) 0x00, (byte) 0x05};
                priority = 10;
                break;
            case FunctionObject.UP_FAN_SPEED_MIN:
                command = new byte[]{(byte) 0x00, (byte) 0x02};
                functionId = FunctionObject.UP_FAN_SPEED_MIN;
                functionLength = new byte[]{(byte) 0x00, (byte) 0x01};
                dataLength = new byte[]{(byte) 0x00, (byte) 0x05};
                priority = 10;
                break;
            case FunctionObject.UP_SPEED_STATUS:
                command = new byte[]{(byte) 0x00, (byte) 0x02};
                functionId = FunctionObject.UP_SPEED_STATUS;
                functionLength = new byte[]{(byte) 0x00, (byte) 0x02};
                dataLength = new byte[]{(byte) 0x00, (byte) 0x05};
                priority = 10;
                break;
            case FunctionObject.UP_SET_SPEED:
                command = new byte[]{(byte) 0x00, (byte) 0x02};
                functionId = FunctionObject.UP_SET_SPEED;
                functionLength = new byte[]{(byte) 0x00, (byte) 0x02};
                dataLength = new byte[]{(byte) 0x00, (byte) 0x05};
                priority = 10;
                break;
            case FunctionObject.UP_PRESS_TYPE:
                command = new byte[]{(byte) 0x00, (byte) 0x02};
                functionId = FunctionObject.UP_PRESS_TYPE;
                functionLength = new byte[]{(byte) 0x00, (byte) 0x02};
                dataLength = new byte[]{(byte) 0x00, (byte) 0x05};
                priority = 10;
                break;
            case FunctionObject.UP_SET_TYPE:
                command = new byte[]{(byte) 0x00, (byte) 0x02};
                functionId = FunctionObject.UP_SET_TYPE;
                functionLength = new byte[]{(byte) 0x00, (byte) 0x02};
                dataLength = new byte[]{(byte) 0x00, (byte) 0x05};
                priority = 10;
                break;
            case FunctionObject.UP_SET_COMMON_DATA:
                command = new byte[]{(byte) 0x00, (byte) 0x02};
                functionId = FunctionObject.UP_SET_COMMON_DATA;
                functionLength = new byte[]{(byte) 0x00, (byte) 0x02};
                dataLength = new byte[]{(byte) 0x00, (byte) 0x05};
                priority = 10;
                break;
        }
    }

    public void setData(byte[] data) {
        this.data = data;
    }


    public byte[] getBytes4(){
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            outputStream.write(HEADER);
            outputStream.write((byte)0x01);

            // --- 序列号自增逻辑开始 ---
            int currentNum;
            synchronized (SpCommand.class) {
                globalAtomicCounter++;
                if (globalAtomicCounter > 0xFFFF) {
                    globalAtomicCounter = 1;
                }
                currentNum = globalAtomicCounter;
            }
            byte[] dynamicSerialNum = new byte[2];
            dynamicSerialNum[0] = (byte) ((currentNum >> 8) & 0xFF);
            dynamicSerialNum[1] = (byte) (currentNum & 0xFF);
            outputStream.write(dynamicSerialNum);

            outputStream.write(command);
            outputStream.write(termType);
            int dataLength = this.data.length+3;
            outputStream.write(ByteUtils.int16ToByteArray(dataLength));
            outputStream.write(functionId);
            outputStream.write(ByteUtils.int16ToByteArray(this.data.length));
            outputStream.write(data);
            byte[] crcSource = outputStream.toByteArray();
            String crcString = CrcUtils.getCRC(crcSource);
            outputStream.write(DigitalUtil.HexString2Bytes(crcString));

            HyApplication.putState(currentNum,outputStream);

            return outputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
