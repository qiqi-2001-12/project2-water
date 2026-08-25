package com.hy.greenbuilding.protocol.command;

import com.hy.greenbuilding.protocol.FunctionObject;
import com.hy.greenbuilding.protocol.SpCommand;

/**
 * 主控板
 */
public class ControlCommand extends SpCommand {
    public  ControlCommand(int type) {
        super((byte)0x00);
        init(type);
    }
    private void init(int type){
        switch (type){
            case FunctionObject.GET_CONTROL_STATUS:
                command = new byte[]{(byte)0x00, (byte) 0x03};
                functionId = FunctionObject.GET_CONTROL_STATUS;
                functionLength = new byte[]{(byte)0x00, (byte) 0x00};
                dataLength = new byte[]{(byte)0x00, (byte) 0x03};
                priority = 20;
                break;
            case FunctionObject.SET_LOW_POWER:
                command = new byte[]{(byte)0x00, (byte) 0x02};
                functionId = FunctionObject.SET_LOW_POWER;
                functionLength = new byte[]{(byte)0x00, (byte) 0x01};
                dataLength = new byte[]{(byte)0x00, (byte) 0x04};
                priority = 10;
                break;
            case FunctionObject.SET_CONTROL_MODE:
                command = new byte[]{(byte)0x00, (byte) 0x02};
                functionId = FunctionObject.SET_CONTROL_MODE;
                functionLength = new byte[]{(byte)0x00, (byte) 0x03};
                dataLength = new byte[]{(byte)0x00, (byte) 0x06};
                priority = 10;
                break;
            case FunctionObject.SET_HUMIDITY:
                command = new byte[]{(byte)0x00, (byte) 0x02};
                functionId = FunctionObject.SET_HUMIDITY;
                functionLength = new byte[]{(byte)0x00, (byte) 0x03};
                dataLength = new byte[]{(byte)0x00, (byte) 0x06};
                break;
            case FunctionObject.SET_TEMP_SECTION:
                command = new byte[]{(byte)0x00, (byte) 0x02};
                functionId = FunctionObject.SET_TEMP_SECTION;
                functionLength = new byte[]{(byte)0x00, (byte) 0x04};
                dataLength = new byte[]{(byte)0x00, (byte) 0x07};
                priority = 10;
                break;
            case FunctionObject.SET_OUTDOOR_TYPE:
                command = new byte[]{(byte)0x00, (byte) 0x02};
                functionId = FunctionObject.SET_OUTDOOR_TYPE;
                functionLength = new byte[]{(byte)0x00, (byte) 0x02};
                dataLength = new byte[]{(byte)0x00, (byte) 0x05};
                priority = 10;
                break;
            case FunctionObject.GET_TEMP_SWITCH:
                command = new byte[]{(byte)0x00, (byte) 0x02};
                functionId = FunctionObject.GET_TEMP_SWITCH;
                functionLength = new byte[]{(byte)0x00, (byte) 0x01};
                dataLength = new byte[]{(byte)0x00, (byte) 0x04};
                break;
            case FunctionObject.SET_COLD_TEMP:
                command = new byte[]{(byte)0x00, (byte) 0x02};
                functionId = FunctionObject.SET_COLD_TEMP;
                functionLength = new byte[]{(byte)0x00, (byte) 0x01};
                dataLength = new byte[]{(byte)0x00, (byte) 0x04};
                priority = 10;
                break;
            case FunctionObject.SET_HUMI_TEMP:
                command = new byte[]{(byte)0x00, (byte) 0x02};
                functionId = FunctionObject.SET_HUMI_TEMP;
                functionLength = new byte[]{(byte)0x00, (byte) 0x01};
                dataLength = new byte[]{(byte)0x00, (byte) 0x04};
                priority = 10;
                break;
            case FunctionObject.SET_HUMI_SWITCH:
                command = new byte[]{(byte)0x00, (byte) 0x02};
                functionId = FunctionObject.SET_HUMI_SWITCH;
                functionLength = new byte[]{(byte)0x00, (byte) 0x01};
                dataLength = new byte[]{(byte)0x00, (byte) 0x04};
                priority = 10;
                break;
        }
    }
    public void setData(byte[] data){
        this.data = data;
    }
}
