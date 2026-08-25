package com.hy.greenbuilding.protocol.command;

import com.hy.greenbuilding.protocol.FunctionObject;
import com.hy.greenbuilding.protocol.SpCommand;

/**
 * PID
 */
public class PIDCommand extends SpCommand {
    public PIDCommand(int id) {
        super((byte)0x09);
        init(id);
    }
    private void init(int id){
        switch (id){
            case FunctionObject.GET_PID_STATUS:
                //获取PID状态
                command = new byte[]{(byte)0x00, (byte) 0x03};
                functionId = FunctionObject.GET_PID_STATUS;
                functionLength = new byte[]{(byte)0x00, (byte) 0x00};
                dataLength = new byte[]{(byte)0x00, (byte) 0x03};
                priority = 20;
                break;
            case FunctionObject.SET_PID_VALUE:
                //设置PID值
                command = new byte[]{(byte)0x00, (byte) 0x02};
                functionId = FunctionObject.SET_PID_VALUE;
                functionLength = new byte[]{(byte)0x00, (byte) 0x0a};
                dataLength = new byte[]{(byte)0x00, (byte) 0x0d};
                priority = 10;
                break;
            case FunctionObject.SET_PID_TEMP1:
                //设置PID温度1
                command = new byte[]{(byte)0x00, (byte) 0x02};
                functionId = FunctionObject.SET_PID_TEMP1;
                functionLength = new byte[]{(byte)0x00, (byte) 0x02};
                dataLength = new byte[]{(byte)0x00, (byte) 0x05};
                priority = 10;
                break;
            case FunctionObject.SET_PID_TEMP2:
                //设置PID温度2
                command = new byte[]{(byte)0x00, (byte) 0x02};
                functionId = FunctionObject.SET_PID_TEMP2;
                functionLength = new byte[]{(byte)0x00, (byte) 0x02};
                dataLength = new byte[]{(byte)0x00, (byte) 0x05};
                priority = 10;
                break;
            case FunctionObject.SET_OUT_TEMP:
                //设置室外温度
                command = new byte[]{(byte)0x00, (byte) 0x02};
                functionId = FunctionObject.SET_OUT_TEMP;
                functionLength = new byte[]{(byte)0x00, (byte) 0x02};
                dataLength = new byte[]{(byte)0x00, (byte) 0x05};
                priority = 10;
                break;
        }
    }
    public void setData(byte[] data){
        this.data = data;
    }

}
