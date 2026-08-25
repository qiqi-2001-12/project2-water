package com.hy.greenbuilding.protocol.command;

import com.hy.greenbuilding.protocol.FunctionObject;
import com.hy.greenbuilding.protocol.SpCommand;

/**
 * 风阀
 */
public class AirValveCommand extends SpCommand {
    public AirValveCommand(int type) {
        super((byte) 0x07);
        init(type);
    }

    private void init(int id) {
        switch (id) {
            case FunctionObject.GET_AIR_VALVE_STATUS:
                command = new byte[]{(byte) 0x00, (byte) 0x03};
                functionId = FunctionObject.GET_AIR_VALVE_STATUS;
                functionLength = new byte[]{(byte) 0x00, (byte) 0x00};
                dataLength = new byte[]{(byte) 0x00, (byte) 0x03};
                priority = 20;
                break;
            case FunctionObject.SET_AIR_VALVE_MODE:
                command = new byte[]{(byte) 0x00, (byte) 0x02};
                functionId = FunctionObject.SET_AIR_VALVE_MODE;
                functionLength = new byte[]{(byte) 0x00, (byte) 0x02};
                dataLength = new byte[]{(byte) 0x00, (byte) 0x05};
                priority = 10;
                break;
            case FunctionObject.SET_AIR_VALVE_OPEN:
                command = new byte[]{(byte) 0x00, (byte) 0x02};
                functionId = FunctionObject.SET_AIR_VALVE_OPEN;
                functionLength = new byte[]{(byte) 0x00, (byte) 0x03};
                dataLength = new byte[]{(byte) 0x00, (byte) 0x06};
                priority = 10;
                break;
            case FunctionObject.SET_AIR_VALVE_OPEN_MAX:
                command = new byte[]{(byte) 0x00, (byte) 0x02};
                functionId = FunctionObject.SET_AIR_VALVE_OPEN_MAX;
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
