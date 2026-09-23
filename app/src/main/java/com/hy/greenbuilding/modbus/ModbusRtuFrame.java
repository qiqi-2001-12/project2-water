package com.hy.greenbuilding.modbus;

import java.util.Arrays;

/** 标准 Modbus RTU 请求帧与 CRC16（低字节先发送）。 */
public final class ModbusRtuFrame {
    private ModbusRtuFrame() {
    }

    public static byte[] readRegisters(int slaveId, int function, int startAddress, int quantity) {
        if (!ModbusFunction.isRead(function)) {
            throw new IllegalArgumentException("Unsupported read function: " + function);
        }
        if (quantity < 1 || quantity > 125) {
            throw new IllegalArgumentException("Read quantity must be in 1..125");
        }
        byte[] frame = new byte[]{
                (byte) slaveId, (byte) function,
                high(startAddress), low(startAddress),
                high(quantity), low(quantity)
        };
        return appendCrc(frame);
    }

    public static byte[] writeSingleRegister(int slaveId, int address, int value) {
        byte[] frame = new byte[]{
                (byte) slaveId, (byte) ModbusFunction.WRITE_SINGLE_REGISTER,
                high(address), low(address), high(value), low(value)
        };
        return appendCrc(frame);
    }

    public static byte[] writeMultipleRegisters(int slaveId, int startAddress, int[] values) {
        if (values == null || values.length == 0 || values.length > 123) {
            throw new IllegalArgumentException("Write quantity must be in 1..123");
        }
        byte[] frame = new byte[7 + values.length * 2];
        frame[0] = (byte) slaveId;
        frame[1] = (byte) ModbusFunction.WRITE_MULTIPLE_REGISTERS;
        frame[2] = high(startAddress);
        frame[3] = low(startAddress);
        frame[4] = high(values.length);
        frame[5] = low(values.length);
        frame[6] = (byte) (values.length * 2);
        for (int index = 0; index < values.length; index++) {
            frame[7 + index * 2] = high(values[index]);
            frame[8 + index * 2] = low(values[index]);
        }
        return appendCrc(frame);
    }

    public static byte[] appendCrc(byte[] payload) {
        int crc = crc16(payload, 0, payload.length);
        byte[] frame = Arrays.copyOf(payload, payload.length + 2);
        frame[frame.length - 2] = (byte) (crc & 0xFF);
        frame[frame.length - 1] = (byte) ((crc >> 8) & 0xFF);
        return frame;
    }

    public static boolean hasValidCrc(byte[] frame) {
        if (frame == null || frame.length < 4) {
            return false;
        }
        int crc = crc16(frame, 0, frame.length - 2);
        return (frame[frame.length - 2] & 0xFF) == (crc & 0xFF)
                && (frame[frame.length - 1] & 0xFF) == ((crc >> 8) & 0xFF);
    }

    public static int crc16(byte[] data, int offset, int length) {
        int crc = 0xFFFF;
        for (int index = offset; index < offset + length; index++) {
            crc ^= data[index] & 0xFF;
            for (int bit = 0; bit < 8; bit++) {
                if ((crc & 0x0001) != 0) {
                    crc = (crc >>> 1) ^ 0xA001;
                } else {
                    crc >>>= 1;
                }
            }
        }
        return crc & 0xFFFF;
    }

    private static byte high(int value) {
        return (byte) ((value >> 8) & 0xFF);
    }

    private static byte low(int value) {
        return (byte) (value & 0xFF);
    }
}
