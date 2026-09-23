package com.hy.greenbuilding.modbus;

import com.hy.greenbuilding.protocol.SpDataProcessor;

/** 使用现有 App 串口的 Modbus 传输适配器。 */
public final class AppSerialModbusTransport implements ModbusTransport {
    @Override
    public boolean beginTransaction(long waitTimeoutMs) {
        return SpDataProcessor.getInstance().beginModbusTransaction(waitTimeoutMs);
    }

    @Override
    public boolean sendFrame(byte[] frame) {
        return SpDataProcessor.getInstance().sendModbusFrame(frame);
    }

    @Override
    public void endTransaction() {
        SpDataProcessor.getInstance().endModbusTransaction();
    }
}
