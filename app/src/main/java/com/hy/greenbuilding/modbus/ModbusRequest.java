package com.hy.greenbuilding.modbus;

/** 单个 Modbus RTU 事务的请求定义。 */
public final class ModbusRequest {
    public final int slaveId;
    public final int function;
    public final int startAddress;
    public final int quantity;
    public final int[] values;
    public final long timeoutMs;

    private ModbusRequest(int slaveId, int function, int startAddress, int quantity, int[] values,
                          long timeoutMs) {
        this.slaveId = slaveId;
        this.function = function;
        this.startAddress = startAddress;
        this.quantity = quantity;
        this.values = values;
        this.timeoutMs = timeoutMs;
    }

    public static ModbusRequest read(int function, int startAddress, int quantity) {
        return new ModbusRequest(WaterUnitRegisterMap.SLAVE_ID, function, startAddress, quantity,
                null, 1200L);
    }

    public static ModbusRequest writeSingle(int address, int value) {
        return new ModbusRequest(WaterUnitRegisterMap.SLAVE_ID,
                ModbusFunction.WRITE_SINGLE_REGISTER, address, 1, new int[]{value}, 1200L);
    }

    public static ModbusRequest writeMultiple(int startAddress, int[] values) {
        int[] copiedValues = values == null ? null : values.clone();
        return new ModbusRequest(WaterUnitRegisterMap.SLAVE_ID,
                ModbusFunction.WRITE_MULTIPLE_REGISTERS, startAddress,
                copiedValues == null ? 0 : copiedValues.length, copiedValues, 1500L);
    }

    public byte[] toFrame() {
        switch (function) {
            case ModbusFunction.READ_HOLDING_REGISTERS:
            case ModbusFunction.READ_INPUT_REGISTERS:
                return ModbusRtuFrame.readRegisters(slaveId, function, startAddress, quantity);
            case ModbusFunction.WRITE_SINGLE_REGISTER:
                return ModbusRtuFrame.writeSingleRegister(slaveId, startAddress, values[0]);
            case ModbusFunction.WRITE_MULTIPLE_REGISTERS:
                return ModbusRtuFrame.writeMultipleRegisters(slaveId, startAddress, values);
            default:
                throw new IllegalStateException("Unsupported function: " + function);
        }
    }
}
