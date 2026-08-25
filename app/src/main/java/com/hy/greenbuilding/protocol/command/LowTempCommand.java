package com.hy.greenbuilding.protocol.command;

import com.hy.greenbuilding.protocol.FunctionObject;
import com.hy.greenbuilding.protocol.SpCommand;


/**
 * 低温增焓
 */
public class LowTempCommand extends SpCommand {
    public LowTempCommand(int id) {
        super((byte)0x01);
        init(id);
    }
    private void init(int id){
        switch (id){
            case FunctionObject.GET_OUT_STATUS:
                //获取室外机状态
                command = new byte[]{(byte)0x00, (byte) 0x03};
                functionId = FunctionObject.GET_OUT_STATUS;
                functionLength = new byte[]{(byte)0x00, (byte) 0x00};
                dataLength = new byte[]{(byte)0x00, (byte) 0x03};
                priority = 20;
                break;
            case FunctionObject.SET_POWER:
                //设定压缩机频率
                command = new byte[]{(byte)0x00, (byte) 0x02};
                functionId = FunctionObject.SET_POWER;
                functionLength = new byte[]{(byte)0x00, (byte) 0x02};
                dataLength = new byte[]{(byte)0x00, (byte) 0x05};
                priority = 10;
                break;
            case FunctionObject.SET_MODE:
                //设定模式
                command = new byte[]{(byte)0x00, (byte) 0x02};
                functionId = FunctionObject.SET_MODE;
                functionLength = new byte[]{(byte)0x00, (byte) 0x02};
                dataLength = new byte[]{(byte)0x00, (byte) 0x05};
                priority = 10;
                break;
            case FunctionObject.ABILITY_TEST:
                //能力测试
                command = new byte[]{(byte)0x00, (byte) 0x02};
                functionId = FunctionObject.ABILITY_TEST;
                functionLength = new byte[]{(byte)0x00, (byte) 0x02};
                dataLength = new byte[]{(byte)0x00, (byte) 0x05};
                priority = 10;
                break;
            case FunctionObject.FORCE_DEFROST:
                //强制除霜
                command = new byte[]{(byte)0x00, (byte) 0x02};
                functionId = FunctionObject.FORCE_DEFROST;
                functionLength = new byte[]{(byte)0x00, (byte) 0x01};
                dataLength = new byte[]{(byte)0x00, (byte) 0x05};
                priority = 10;
                break;
            case FunctionObject.MAIN_EXPANSION:
                //设定主膨胀阀开度
                command = new byte[]{(byte)0x00, (byte) 0x02};
                functionId = FunctionObject.MAIN_EXPANSION;
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
