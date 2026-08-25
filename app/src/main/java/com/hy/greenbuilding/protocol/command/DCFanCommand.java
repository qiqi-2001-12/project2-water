package com.hy.greenbuilding.protocol.command;

import com.hy.greenbuilding.protocol.FunctionObject;
import com.hy.greenbuilding.protocol.SpCommand;

public class DCFanCommand extends SpCommand {
    public DCFanCommand(int id) {
        super((byte) 0x08);
        init(id);
    }
    private void init(int id) {
        switch (id) {
            case FunctionObject.GET_DC_FAN_STATUS:
                //获取DC风机状态
                command = new byte[]{(byte) 0x00, (byte) 0x03};
                functionId = FunctionObject.GET_DC_FAN_STATUS;
                functionLength = new byte[]{(byte) 0x00, (byte) 0x00};
                dataLength = new byte[]{(byte) 0x00, (byte) 0x03};
                priority = 20;

                break;
            case FunctionObject.SET_DC_FAN_SPEED:
                //设置DC转速
                command = new byte[]{(byte) 0x00, (byte) 0x02};
                functionId = FunctionObject.SET_DC_FAN_SPEED;
                functionLength = new byte[]{(byte) 0x00, (byte) 0x02};
                dataLength = new byte[]{(byte) 0x00, (byte) 0x05};
                priority = 10;
                break;
            case FunctionObject.SET_DC_FAN_SWITCH:
                //电磁阀开关
                command = new byte[]{(byte) 0x00, (byte) 0x02};
                functionId = FunctionObject.SET_DC_FAN_SWITCH;
                functionLength = new byte[]{(byte) 0x00, (byte) 0x02};
                dataLength = new byte[]{(byte) 0x00, (byte) 0x05};
                priority = 10;
                break;
            case FunctionObject.SET_EXPANSION_SWITCH:
                //电子膨胀阀开关
                command = new byte[]{(byte) 0x00, (byte) 0x02};
                functionId = FunctionObject.SET_EXPANSION_SWITCH;
                functionLength = new byte[]{(byte) 0x00, (byte) 0x02};
                dataLength = new byte[]{(byte) 0x00, (byte) 0x05};
                priority = 10;
                break;
            case FunctionObject.SET_EXPANSION_OPEN:
                command = new byte[]{(byte) 0x00, (byte) 0x02};
                functionId = FunctionObject.SET_EXPANSION_OPEN;
                functionLength = new byte[]{(byte) 0x00, (byte) 0x03};
                dataLength = new byte[]{(byte) 0x00, (byte) 0x06};
                priority = 10;
                break;
            case FunctionObject.SET_EXPANSION_TYPE:
                command = new byte[]{(byte) 0x00, (byte) 0x02};
                functionId = FunctionObject.SET_EXPANSION_TYPE;
                functionLength = new byte[]{(byte) 0x00, (byte) 0x01};
                dataLength = new byte[]{(byte) 0x00, (byte) 0x04};
                priority = 10;
                break;
            case FunctionObject.SET_EXPANSION_PID_VALUE:
                command = new byte[]{(byte) 0x00, (byte) 0x02};
                functionId = FunctionObject.SET_EXPANSION_PID_VALUE;
                functionLength = new byte[]{(byte) 0x00, (byte) 0x03};
                dataLength = new byte[]{(byte) 0x00, (byte) 0x06};
                priority = 10;
                break;
            case FunctionObject.SET_EXPANSION_REGULAR_VALUE:
                command = new byte[]{(byte) 0x00, (byte) 0x02};
                functionId = FunctionObject.SET_EXPANSION_REGULAR_VALUE;
                functionLength = new byte[]{(byte) 0x00, (byte) 0x02};
                dataLength = new byte[]{(byte) 0x00, (byte) 0x05};
                priority = 10;
                break;
            case FunctionObject.SET_HEATING_DEHUMIDIFICATION_TYPE:
                command = new byte[]{(byte) 0x00, (byte) 0x02};
                functionId = FunctionObject.SET_HEATING_DEHUMIDIFICATION_TYPE;
                // 功能长度：协议表中的 0x0002
                functionLength = new byte[]{(byte) 0x00, (byte) 0x02};
                // 数据长度：协议表中的 0x0005
                dataLength = new byte[]{(byte) 0x00, (byte) 0x05};
                priority = 10;
                break;


            case FunctionObject.SET_FAN_BOARD_TYPE:
                // 设置风机小板类型 (1B 类型, Function ID 0x0A)
                command = new byte[]{(byte) 0x00, (byte) 0x02};
                functionId = FunctionObject.SET_FAN_BOARD_TYPE;
                functionLength = new byte[]{(byte) 0x00, (byte) 0x01}; // 1B Data
                dataLength = new byte[]{(byte) 0x00, (byte) 0x04};      // 4B
                priority = 10;
                break;
            case FunctionObject.SET_MAINBOARD_CONFIG:
                command = new byte[]{(byte) 0x00, (byte) 0x02};
                functionId = FunctionObject.SET_MAINBOARD_CONFIG;
                functionLength = new byte[]{(byte) 0x00, (byte) 0x05};
                dataLength = new byte[]{(byte) 0x00, (byte) 0x08};
                priority = 10;
                break;
        }
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
