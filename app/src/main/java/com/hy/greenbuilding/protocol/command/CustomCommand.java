package com.hy.greenbuilding.protocol.command;

import com.hy.greenbuilding.protocol.FunctionObject;
import com.hy.greenbuilding.protocol.SpCommand;

public class CustomCommand extends SpCommand {
    public  CustomCommand(int type) {
        super((byte)0x0E);
        init(type);
    }
    private void init(int id){
        switch (id){
            case FunctionObject.GET_CUSTOM_DATA:
                command = new byte[]{(byte)0x00, (byte) 0x03};
                functionId = FunctionObject.GET_CUSTOM_DATA;
                functionLength = new byte[]{(byte)0x00, (byte) 0x00};
                dataLength = new byte[]{(byte)0x00, (byte) 0x03};
                priority = 20;
                break;
        }
    }
    public void setData(byte[] data){
        this.data = data;
    }
}
