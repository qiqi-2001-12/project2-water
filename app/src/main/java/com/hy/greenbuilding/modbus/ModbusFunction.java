package com.hy.greenbuilding.modbus;

/** 本水机已确认使用的 Modbus RTU 功能码。 */
public final class ModbusFunction {
    public static final int READ_HOLDING_REGISTERS = 0x03;
    public static final int READ_INPUT_REGISTERS = 0x04;
    public static final int WRITE_SINGLE_REGISTER = 0x06;
    public static final int WRITE_MULTIPLE_REGISTERS = 0x10;

    private ModbusFunction() {
    }

    public static boolean isRead(int function) {
        return function == READ_HOLDING_REGISTERS || function == READ_INPUT_REGISTERS;
    }
}
