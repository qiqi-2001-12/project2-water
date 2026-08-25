package com.hy.greenbuilding.protocol.command;

import com.hy.greenbuilding.HyApplication;
import com.hy.greenbuilding.protocol.SpCommand;
import com.hy.greenbuilding.utils.ByteUtils;
import com.hy.greenbuilding.utils.CrcUtils;
import com.hy.greenbuilding.utils.DigitalUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * OTA升级通知
 */
public class OTARequestCommand  extends SpCommand{
    protected static final byte[] HEADER = {(byte)0xAA, (byte) 0x55};//协议帧头
    protected static final byte[] TAIL = {0x6B, (byte) 0xBB};
//    protected static final byte[] serialNum = {(byte)0x00, (byte) 0x01};//序列号
    protected byte[] dataLength;//数据长度
    protected byte termType = 0x00;//设备类型
    protected byte[] command;//命令
    protected byte[] data = new byte[0];//功能指令
    protected byte[] functionLength;//功能长度
    protected byte crc[];//crc校验
    public OTARequestCommand(int mode) {
        super((byte)0x00);
        if(mode ==1){
            command = new byte[]{(byte)0x00, (byte) 0x04};
            functionLength = new byte[]{(byte)0x00, (byte) 0x00};
            dataLength = new byte[]{(byte)0x00, (byte) 0x03};
        }else if(mode == 2){
            command = new byte[]{(byte)0x80, (byte) 0x05};
            functionLength = new byte[]{(byte)0x00, (byte) 0x00};
            dataLength = new byte[]{(byte)0x00, (byte) 0x03};
        }else if(mode == 3){
            command = new byte[]{(byte)0x00, (byte) 0x07};
            functionLength = new byte[]{(byte)0x00, (byte) 0x00};
            dataLength = new byte[]{(byte)0x00, (byte) 0x03};
        }else{
            command = new byte[]{(byte)0x00, (byte) 0x01};
            functionLength = new byte[]{(byte)0x00, (byte) 0x00};
            dataLength = new byte[]{(byte)0x00, (byte) 0x03};
        }
        priority = 0;

    }
    public byte[] getBytes1(){
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
            byte[] dynamicSerialNum = new byte[2];
            dynamicSerialNum[0] = (byte) ((currentNum >> 8) & 0xFF);
            dynamicSerialNum[1] = (byte) (currentNum & 0xFF);
            outputStream.write(dynamicSerialNum);

            outputStream.write(command);
            outputStream.write(termType);
            outputStream.write(new byte[]{(byte)0x00, (byte) 0x0c});
            outputStream.write(new byte[]{(byte)0x00, (byte) 0x01});
            outputStream.write(new byte[]{(byte)0x00, (byte) 0x01});
            outputStream.write(DigitalUtil.int2bytesBy32(this.byteLength));
            outputStream.write(mVersion);
            outputStream.write(crc);
            byte[] crcSource = outputStream.toByteArray();
            String crcString = CrcUtils.getCRC(crcSource);
            outputStream.write(DigitalUtil.HexString2Bytes(crcString));
            HyApplication.putState(currentNum,outputStream);

            return outputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public byte[] getBytes2(){
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
            byte[] dynamicSerialNum = new byte[2];
            dynamicSerialNum[0] = (byte) ((currentNum >> 8) & 0xFF);
            dynamicSerialNum[1] = (byte) (currentNum & 0xFF);
            outputStream.write(dynamicSerialNum);

            outputStream.write(command);
            outputStream.write(termType);
            outputStream.write(ByteUtils.shortToByteArray((short)byteLength));
            outputStream.write(mSerial);
            outputStream.write(mSendByte);
            byte[] crcSource = outputStream.toByteArray();
            String crcString = CrcUtils.getCRC(crcSource);
            outputStream.write(DigitalUtil.HexString2Bytes(crcString));

            HyApplication.putState(currentNum,outputStream);

            return outputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public byte[] getBytes3(){
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
            byte[] dynamicSerialNum = new byte[2];
            dynamicSerialNum[0] = (byte) ((currentNum >> 8) & 0xFF);
            dynamicSerialNum[1] = (byte) (currentNum & 0xFF);
            outputStream.write(dynamicSerialNum);

            outputStream.write(command);
            outputStream.write(termType);
            outputStream.write(new byte[]{(byte)0x00, (byte) 0x00});
            byte[] crcSource = outputStream.toByteArray();
            String crcString = CrcUtils.getCRC(crcSource);
            outputStream.write(DigitalUtil.HexString2Bytes(crcString));

            HyApplication.putState(currentNum,outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private int byteLength;
    public void setByteLength(int length){
        this.byteLength = length;

    }

    public void setCrc(byte[] bytes){
        String crcString = CrcUtils.getCRC(bytes);
        crc = DigitalUtil.HexString2Bytes(crcString);
    }

    private byte[] mSerial;
    public void setSerial(byte[] serial){
        this.mSerial = serial;
    }
    private byte[] mSendByte;
    public void setSendData(byte[] bytes){
        this.mSendByte = bytes;
    }
    private byte[] mVersion;
    public void setVersion(byte[] version){
        this.mVersion = version;
    }
}
