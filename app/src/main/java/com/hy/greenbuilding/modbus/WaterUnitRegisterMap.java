package com.hy.greenbuilding.modbus;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 水机寄存器目录。
 *
 * 地址采用厂家表的零基准值，发送到 Modbus PDU 时不做 -1 偏移。
 */
public final class WaterUnitRegisterMap {
    public static final int SLAVE_ID = 1;

    public static final RegisterBlock STATUS_B = new RegisterBlock(2121, 30);
    public static final RegisterBlock STATUS_D = new RegisterBlock(2151, 12);
    public static final RegisterBlock STATUS_Y = new RegisterBlock(2170, 24);

    private static final List<RegisterBlock> STATUS_POLL_BLOCKS = Collections.unmodifiableList(
            Arrays.asList(STATUS_B, STATUS_D, STATUS_Y));

    private WaterUnitRegisterMap() {
    }

    public static List<RegisterBlock> getStatusPollBlocks() {
        return STATUS_POLL_BLOCKS;
    }

    public static Register resolve(String parameterCode) {
        if (parameterCode == null) {
            return null;
        }
        String code = parameterCode.trim().toUpperCase();
        try {
            if (code.startsWith("P")) {
                int number = Integer.parseInt(code.substring(1));
                if (number >= 1 && number <= 129) {
                    return new Register(code, 1000 + number, ModbusFunction.READ_HOLDING_REGISTERS, true);
                }
                if (number >= 130 && number <= 169) {
                    return new Register(code, 1270 + number, ModbusFunction.READ_HOLDING_REGISTERS, true);
                }
            }
            if (code.startsWith("B")) {
                int number = Integer.parseInt(code.substring(1));
                if (number >= 1 && number <= 30) {
                    return new Register(code, 2120 + number, ModbusFunction.READ_INPUT_REGISTERS, false);
                }
            }
            if (code.startsWith("D")) {
                int number = Integer.parseInt(code.substring(1));
                if (number >= 1 && number <= 12) {
                    return new Register(code, 2150 + number, ModbusFunction.READ_INPUT_REGISTERS, false);
                }
            }
            if (code.startsWith("Y")) {
                int number = Integer.parseInt(code.substring(1));
                if (number >= 0 && number <= 23) {
                    return new Register(code, 2170 + number, ModbusFunction.READ_INPUT_REGISTERS, false);
                }
            }
        } catch (NumberFormatException ignored) {
            // 不符合 P/B/D/Y 编号格式时返回空，由调用方按未绑定处理。
        }
        return null;
    }

    public static final class RegisterBlock {
        public final int startAddress;
        public final int quantity;

        RegisterBlock(int startAddress, int quantity) {
            this.startAddress = startAddress;
            this.quantity = quantity;
        }
    }

    public static final class Register {
        public final String code;
        public final int address;
        public final int readFunction;
        public final boolean writable;

        Register(String code, int address, int readFunction, boolean writable) {
            this.code = code;
            this.address = address;
            this.readFunction = readFunction;
            this.writable = writable;
        }
    }
}
