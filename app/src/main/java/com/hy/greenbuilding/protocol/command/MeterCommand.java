package com.hy.greenbuilding.protocol.command;

import com.hy.greenbuilding.protocol.FunctionObject;
import com.hy.greenbuilding.protocol.SpCommand;

/**
 * 电表
 */
public class MeterCommand extends SpCommand {
    private int MeterType;//1.获取电表数据，2. 重置电量
    public MeterCommand(int type) {
        super((byte)0x05);
        this.MeterType = type;
        if(MeterType == 1){
            command = new byte[]{(byte)0x00, (byte) 0x03};
            functionId = FunctionObject.GET_OUT_STATUS;
            functionLength = new byte[]{(byte)0x00, (byte) 0x00};
            dataLength = new byte[]{(byte)0x00, (byte) 0x03};
        }else{
            command = new byte[]{(byte)0x00, (byte) 0x02};
            functionId = FunctionObject.SET_TEMP;
            functionLength = new byte[]{(byte)0x00, (byte) 0x00};
            dataLength = new byte[]{(byte)0x00, (byte) 0x03};
        }
        priority = 20;
    }
}
