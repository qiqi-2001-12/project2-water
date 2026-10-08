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
    public static final int CONTROL_POWER_ADDRESS = 911;
    public static final int CONTROL_MODE_ADDRESS = 912;
    public static final int CONTROL_FORCE_DEFROST_ADDRESS = 940;
    public static final int CONTROL_PV_DIRECT_DRIVE_ADDRESS = 973;
    public static final int CONTROL_FAN_ALWAYS_ON_ADDRESS = 974;
    public static final int STATUS_FAULT_INFO_8_ADDRESS = 2100;
    public static final int STATUS_PARAMETER_CHECKSUM_ADDRESS = 2101;
    public static final int STATUS_DEVICE_ID_1_ADDRESS = 2102;
    public static final int STATUS_DEVICE_ID_2_ADDRESS = 2103;
    public static final int STATUS_DEVICE_ID_3_ADDRESS = 2104;
    public static final int STATUS_DEVICE_ID_4_ADDRESS = 2105;
    public static final int STATUS_DEVICE_ID_5_ADDRESS = 2106;
    public static final int STATUS_DEVICE_ID_6_ADDRESS = 2107;
    public static final int STATUS_DEVICE_ID_7_ADDRESS = 2108;
    public static final int STATUS_FAULT_INFO_6_ADDRESS = 2109;
    public static final int STATUS_FAULT_INFO_7_ADDRESS = 2110;
    public static final int STATUS_SOFTWARE_CODE_1_ADDRESS = 2111;
    public static final int STATUS_SOFTWARE_CODE_2_ADDRESS = 2112;
    public static final int STATUS_SOFTWARE_VERSION_ADDRESS = 2113;
    public static final int STATUS_SYSTEM_INFO_ADDRESS = 2114;
    public static final int STATUS_FAULT_INFO_1_ADDRESS = 2115;
    public static final int STATUS_FAULT_INFO_2_ADDRESS = 2116;
    public static final int STATUS_FAULT_INFO_3_ADDRESS = 2117;
    public static final int STATUS_UNIT_RUNNING_DAYS_ADDRESS = 2118;
    public static final int STATUS_FAULT_INFO_4_ADDRESS = 2119;
    public static final int STATUS_FAULT_INFO_5_ADDRESS = 2120;

    public static final RegisterBlock STATUS_SYSTEM_AND_FAULTS = new RegisterBlock(2100, 21);
    public static final RegisterBlock STATUS_B = new RegisterBlock(2121, 30);
    public static final RegisterBlock STATUS_D = new RegisterBlock(2151, 12);
    public static final RegisterBlock STATUS_Y = new RegisterBlock(2170, 24);

    private static final List<RegisterBlock> STATUS_POLL_BLOCKS = Collections.unmodifiableList(
            Arrays.asList(STATUS_SYSTEM_AND_FAULTS, STATUS_B, STATUS_D, STATUS_Y));

    /*
     * Do not read the complete settings range in one maximum-size request.
     * Some field controllers and ModbusSlave configurations cap a response below
     * the Modbus 125-register maximum. Smaller independent blocks also keep one
     * unmapped range from hiding all settings after it.
     */
    private static final List<RegisterBlock> SETTING_READ_BLOCKS = Collections.unmodifiableList(
            Arrays.asList(
                    new RegisterBlock(1001, 20),
                    new RegisterBlock(1021, 20),
                    new RegisterBlock(1041, 20),
                    new RegisterBlock(1061, 20),
                    new RegisterBlock(1081, 20),
                    new RegisterBlock(1101, 20),
                    new RegisterBlock(1121, 5),
                    new RegisterBlock(1126, 4),
                    new RegisterBlock(1400, 20),
                    new RegisterBlock(1420, 20),
                    new RegisterBlock(CONTROL_FORCE_DEFROST_ADDRESS, 1),
                    new RegisterBlock(CONTROL_PV_DIRECT_DRIVE_ADDRESS, 2)));

    private WaterUnitRegisterMap() {
    }

    public static List<RegisterBlock> getStatusPollBlocks() {
        return STATUS_POLL_BLOCKS;
    }

    public static List<RegisterBlock> getSettingReadBlocks() {
        return SETTING_READ_BLOCKS;
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
            if (code.startsWith("S")) {
                int number = Integer.parseInt(code.substring(1));
                if (number == 1) {
                    return new Register(code, CONTROL_POWER_ADDRESS,
                            ModbusFunction.READ_HOLDING_REGISTERS, true);
                }
                if (number == 2) {
                    return new Register(code, CONTROL_MODE_ADDRESS,
                            ModbusFunction.READ_HOLDING_REGISTERS, true);
                }
                if (number == 30) {
                    return new Register(code, CONTROL_FORCE_DEFROST_ADDRESS,
                            ModbusFunction.READ_HOLDING_REGISTERS, true);
                }
                if (number == 63) {
                    return new Register(code, CONTROL_PV_DIRECT_DRIVE_ADDRESS,
                            ModbusFunction.READ_HOLDING_REGISTERS, true);
                }
                if (number == 64) {
                    return new Register(code, CONTROL_FAN_ALWAYS_ON_ADDRESS,
                            ModbusFunction.READ_HOLDING_REGISTERS, true);
                }
            }
            if (code.startsWith("F")) {
                int number = Integer.parseInt(code.substring(1));
                int address = getFaultInformationAddress(number);
                if (address >= 0) {
                    return new Register(code, address, ModbusFunction.READ_INPUT_REGISTERS, false);
                }
            }
            if ("A01".equals(code)) {
                return new Register(code, STATUS_SYSTEM_INFO_ADDRESS,
                        ModbusFunction.READ_INPUT_REGISTERS, false);
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

    /** Addresses are non-contiguous in the controller's status table. */
    public static int getFaultInformationAddress(int number) {
        switch (number) {
            case 1:
                return STATUS_FAULT_INFO_1_ADDRESS;
            case 2:
                return STATUS_FAULT_INFO_2_ADDRESS;
            case 3:
                return STATUS_FAULT_INFO_3_ADDRESS;
            case 4:
                return STATUS_FAULT_INFO_4_ADDRESS;
            case 5:
                return STATUS_FAULT_INFO_5_ADDRESS;
            case 6:
                return STATUS_FAULT_INFO_6_ADDRESS;
            case 7:
                return STATUS_FAULT_INFO_7_ADDRESS;
            case 8:
                return STATUS_FAULT_INFO_8_ADDRESS;
            default:
                return -1;
        }
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
