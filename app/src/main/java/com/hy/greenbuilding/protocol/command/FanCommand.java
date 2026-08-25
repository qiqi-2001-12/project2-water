package com.hy.greenbuilding.protocol.command;

import com.hy.greenbuilding.protocol.FunctionObject;
import com.hy.greenbuilding.protocol.SpCommand;

/**
 * 风机
 */
public class FanCommand extends SpCommand {
    public FanCommand(int type) {
        super((byte) 0x04);
        init(type);
    }

    private void init(int type) {
        switch (type) {
            case FunctionObject.GET_FAN_STATUS:
                command = new byte[]{(byte) 0x00, (byte) 0x03};
                functionId = FunctionObject.GET_FAN_STATUS;
                functionLength = new byte[]{(byte) 0x00, (byte) 0x00};
                dataLength = new byte[]{(byte) 0x00, (byte) 0x03};
                priority = 20;

                break;
            case FunctionObject.SET_SPEED:
                command = new byte[]{(byte) 0x00, (byte) 0x02};
                functionId = FunctionObject.SET_SPEED;
                functionLength = new byte[]{(byte) 0x00, (byte) 0x02};
                dataLength = new byte[]{(byte) 0x00, (byte) 0x05};
                priority = 10;
                break;
            case FunctionObject.SET_SPEED_VALUE:
                command = new byte[]{(byte) 0x00, (byte) 0x02};
                functionId = FunctionObject.SET_SPEED_VALUE;
                functionLength = new byte[]{(byte) 0x00, (byte) 0x08};
                dataLength = new byte[]{(byte) 0x00, (byte) 0x0B};
                priority = 10;
                break;
            case FunctionObject.SET_FAN_TYPE:
                command = new byte[]{(byte) 0x00, (byte) 0x02};
                functionId = FunctionObject.SET_FAN_TYPE;
                functionLength = new byte[]{(byte) 0x00, (byte) 0x02};
                dataLength = new byte[]{(byte) 0x00, (byte) 0x05};
                priority = 10;
                break;
            case FunctionObject.SET_FAN_ADDRESS:
                command = new byte[]{(byte) 0x00, (byte) 0x02};
                functionId = FunctionObject.SET_FAN_ADDRESS;
                functionLength = new byte[]{(byte) 0x00, (byte) 0x02};
                dataLength = new byte[]{(byte) 0x00, (byte) 0x05};
                priority = 10;
                break;
            case FunctionObject.TEST_FAN_VALUE:
                command = new byte[]{(byte) 0x00, (byte) 0x02};
                functionId = FunctionObject.TEST_FAN_VALUE;
                functionLength = new byte[]{(byte) 0x00, (byte) 0x03};
                dataLength = new byte[]{(byte) 0x00, (byte) 0x06};
                priority = 10;
                break;
            case FunctionObject.SEARCH_FAN_ADDRESS:
                command = new byte[]{(byte) 0x00, (byte) 0x02};
                functionId = FunctionObject.SEARCH_FAN_ADDRESS;
                functionLength = new byte[]{(byte) 0x00, (byte) 0x01};
                dataLength = new byte[]{(byte) 0x00, (byte) 0x04};
                priority = 10;
                break;
            case FunctionObject.SEARCH_FAN_TYPE_MODEL:
                command = new byte[]{(byte) 0x00, (byte) 0x02};
                functionId = FunctionObject.SEARCH_FAN_TYPE_MODEL;
                functionLength = new byte[]{(byte) 0x00, (byte) 0x02};
                dataLength = new byte[]{(byte) 0x00, (byte) 0x05};
                priority = 10;
                break;
            case FunctionObject.SET_STATIC_PRESSURE_MODE:
                command = new byte[]{(byte) 0x00, (byte) 0x02};
                functionId = FunctionObject.SET_STATIC_PRESSURE_MODE;
                functionLength = new byte[]{(byte) 0x00, (byte) 0x02};
                dataLength = new byte[]{(byte) 0x00, (byte) 0x05};
                priority = 10;
                break;
            case FunctionObject.SET_FAN_PRESSURE_VALUE:
                command = new byte[]{(byte) 0x00, (byte) 0x02};
                functionId = FunctionObject.SET_FAN_PRESSURE_VALUE;
                functionLength = new byte[]{(byte) 0x00, (byte) 0x03};
                dataLength = new byte[]{(byte) 0x00, (byte) 0x06};
                priority = 10;
                break;
        }
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
