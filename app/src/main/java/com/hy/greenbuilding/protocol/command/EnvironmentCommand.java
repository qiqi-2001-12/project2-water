package com.hy.greenbuilding.protocol.command;

import com.hy.greenbuilding.protocol.FunctionObject;
import com.hy.greenbuilding.protocol.SpCommand;

/**
 * 环境检测
 */
public class EnvironmentCommand extends SpCommand {
    public EnvironmentCommand(int id) {
        super((byte)0x06);
        init(id);
    }
    private void init(int id){
        switch (id){
            case FunctionObject.GET_ENVIRONMENT_STATUS:
                //获取环境状态
                command = new byte[]{(byte)0x00, (byte) 0x03};
                functionId = FunctionObject.GET_ENVIRONMENT_STATUS;
                functionLength = new byte[]{(byte)0x00, (byte) 0x00};
                dataLength = new byte[]{(byte)0x00, (byte) 0x03};

                priority = 20;

                break;
            case FunctionObject.SET_CO2_VALUE:
                //设置CO2阈值
                command = new byte[]{(byte)0x00, (byte) 0x02};
                functionId = FunctionObject.SET_CO2_VALUE;
                functionLength = new byte[]{(byte)0x00, (byte) 0x06};
                dataLength = new byte[]{(byte)0x00, (byte) 0x09};
                priority = 10;
                break;
            case FunctionObject.SET_PM_VALUE:
                //设置PM2.5阈值
                command = new byte[]{(byte)0x00, (byte) 0x02};
                functionId = FunctionObject.SET_PM_VALUE;
                functionLength = new byte[]{(byte)0x00, (byte) 0x06};
                dataLength = new byte[]{(byte)0x00, (byte) 0x09};
                priority = 10;
                break;
            case FunctionObject.GET_PM_CO2:
                //获取PM2.5和CO2
                command = new byte[]{(byte)0x00, (byte) 0x03};
                functionId = FunctionObject.GET_PM_CO2;
                functionLength = new byte[]{(byte)0x00, (byte) 0x00};
                dataLength = new byte[]{(byte)0x00, (byte) 0x03};
                priority = 20;
                break;
            case FunctionObject.SET_AIR_QUALITY:
                //设置空气质量
                command = new byte[]{(byte)0x00, (byte) 0x02};
                functionId = FunctionObject.SET_AIR_QUALITY;
                functionLength = new byte[]{(byte)0x00, (byte) 0x09};
                dataLength = new byte[]{(byte)0x00, (byte) 0x0C};
                priority = 10;
                break;
        }
    }
    public void setData(byte[] data){
        this.data = data;
    }
}
