package com.hy.greenbuilding.modbus;

/** 供 Modbus 队列使用的独占串口事务接口。 */
public interface ModbusTransport {
    boolean beginTransaction(long waitTimeoutMs);

    boolean sendFrame(byte[] frame);

    void endTransaction();
}
