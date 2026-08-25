package com.hy.greenbuilding.protocol;

import com.hy.greenbuilding.HyApplication;
import com.hy.greenbuilding.utils.CrcUtils;
import com.hy.greenbuilding.utils.DigitalUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class SpCommand implements Comparable<SpCommand>{
    protected int priority = 10;

    protected static final byte[] HEADER = {(byte)0xAA, (byte) 0x55};//协议帧头
    protected static final byte[] TAIL = {0x6B, (byte) 0xBB};
//    protected static final byte[] serialNum = {(byte)0x00, (byte) 0x01};//序列号
    protected byte[] dataLength;//数据长度
    protected byte termType = 0x00;//设备类型
    protected byte[] command;//命令
    protected byte[] data = new byte[0];//功能指令
    protected byte[] functionLength;//功能长度
    protected byte functionId;//功能Id
    protected byte crc[];//crc校验
    protected static int globalAtomicCounter = 0;//序列号

    public SpCommand(byte termType){
        this.termType = termType;
    }

    public SpCommand(byte termType, int priority){
        this.termType = termType;
        this.priority = priority;
    }

    @Override
    public int compareTo(SpCommand other) {
        // 数字越小，优先级越高（排在队列最前面）
        return Integer.compare(this.priority, other.priority);
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public byte[] getBytes(){
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
            // 将 int 转为 2 字节 (大端序)
            byte[] dynamicSerialNum = new byte[2];
            dynamicSerialNum[0] = (byte) ((currentNum >> 8) & 0xFF);
            dynamicSerialNum[1] = (byte) (currentNum & 0xFF);
            outputStream.write(dynamicSerialNum);
            // --- 序列号自增逻辑结束 ---
            outputStream.write(command);
            outputStream.write(termType);
            outputStream.write(dataLength);
            outputStream.write(functionId);
            outputStream.write(functionLength);
            outputStream.write(data);
            byte[] crcSource = outputStream.toByteArray();
            String crcString = CrcUtils.getCRC(crcSource);
            crc = DigitalUtil.HexString2Bytes(crcString);
            outputStream.write(crc);

            HyApplication.putState(currentNum,outputStream);

            return outputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int getTermType(){
        return termType;
    }

    public int getFunctionId(){
        return functionId;
    }

    public byte[] getDataLength(){
        return dataLength;
    }

    public void setData(byte[] data){
        this.data = data;
    }
}
