package com.hy.greenbuilding.modbus;

/** 已通过 CRC 校验的 Modbus RTU 响应。 */
public final class ModbusResponse {
    public final int slaveId;
    public final int function;
    public final int exceptionCode;
    public final int[] registerValues;

    private ModbusResponse(int slaveId, int function, int exceptionCode, int[] registerValues) {
        this.slaveId = slaveId;
        this.function = function;
        this.exceptionCode = exceptionCode;
        this.registerValues = registerValues;
    }

    public boolean isException() {
        return exceptionCode >= 0;
    }

    static ModbusResponse parse(byte[] frame) {
        int slaveId = frame[0] & 0xFF;
        int function = frame[1] & 0xFF;
        if ((function & 0x80) != 0) {
            return new ModbusResponse(slaveId, function & 0x7F, frame[2] & 0xFF, new int[0]);
        }
        if (ModbusFunction.isRead(function)) {
            int byteCount = frame[2] & 0xFF;
            int[] values = new int[byteCount / 2];
            for (int index = 0; index < values.length; index++) {
                values[index] = ((frame[3 + index * 2] & 0xFF) << 8)
                        | (frame[4 + index * 2] & 0xFF);
            }
            return new ModbusResponse(slaveId, function, -1, values);
        }
        return new ModbusResponse(slaveId, function, -1, new int[0]);
    }
}
