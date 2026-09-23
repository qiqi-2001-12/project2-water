package com.hy.greenbuilding.modbus;

import android.os.SystemClock;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 水机 Modbus RTU 单请求队列。
 *
 * 默认禁用：只有后续经管理员配置显式开启后，才允许向转换器发送报文。
 */
public final class ModbusRtuManager {
    private static final ModbusRtuManager INSTANCE = new ModbusRtuManager();
    private static final long BUS_WAIT_TIMEOUT_MS = 1500L;

    private final ExecutorService transactionExecutor = Executors.newSingleThreadExecutor();
    private final Object responseLock = new Object();
    private byte[] responseBuffer = new byte[0];
    private PendingRequest pendingRequest;
    private volatile boolean communicationEnabled;
    private volatile ModbusTransport transport;

    private ModbusRtuManager() {
    }

    public static ModbusRtuManager getInstance() {
        return INSTANCE;
    }

    public void setTransport(ModbusTransport transport) {
        this.transport = transport;
    }

    public boolean isCommunicationEnabled() {
        return communicationEnabled;
    }

    /** 仅由后续管理员通信配置页调用；应用启动时始终保持 false。 */
    public void setCommunicationEnabled(boolean communicationEnabled) {
        this.communicationEnabled = communicationEnabled;
    }

    public void submit(ModbusRequest request, Callback callback) {
        if (request == null) {
            notifyFailure(callback, "Modbus request is null");
            return;
        }
        if (!communicationEnabled) {
            notifyFailure(callback, "Modbus communication is disabled");
            return;
        }
        if (transport == null) {
            notifyFailure(callback, "Modbus transport is not configured");
            return;
        }
        transactionExecutor.execute(() -> execute(request, callback));
    }

    /** 供现有串口接收回调调用；仅在有待处理的 Modbus 请求时解析，其他数据完全忽略。 */
    public void onSerialBytes(byte[] incoming) {
        if (incoming == null || incoming.length == 0) {
            return;
        }
        synchronized (responseLock) {
            if (pendingRequest == null) {
                return;
            }
            responseBuffer = append(responseBuffer, incoming);
            extractResponseLocked();
        }
    }

    private void execute(ModbusRequest request, Callback callback) {
        ModbusTransport currentTransport = transport;
        if (currentTransport == null || !currentTransport.beginTransaction(BUS_WAIT_TIMEOUT_MS)) {
            notifyFailure(callback, "Serial bus is busy");
            return;
        }

        PendingRequest pending = new PendingRequest(request);
        try {
            synchronized (responseLock) {
                responseBuffer = new byte[0];
                pendingRequest = pending;
            }
            if (!currentTransport.sendFrame(request.toFrame())) {
                notifyFailure(callback, "Unable to send Modbus frame");
                return;
            }

            long deadline = SystemClock.elapsedRealtime() + request.timeoutMs;
            synchronized (responseLock) {
                while (pending.response == null && pending.error == null) {
                    long remaining = deadline - SystemClock.elapsedRealtime();
                    if (remaining <= 0) {
                        pending.error = "Modbus response timeout";
                        break;
                    }
                    try {
                        responseLock.wait(remaining);
                    } catch (InterruptedException interruptedException) {
                        Thread.currentThread().interrupt();
                        pending.error = "Modbus request interrupted";
                        break;
                    }
                }
            }
            if (pending.error != null) {
                notifyFailure(callback, pending.error);
            } else if (pending.response.isException()) {
                notifyFailure(callback, "Modbus exception " + pending.response.exceptionCode);
            } else {
                if (ModbusFunction.isRead(request.function)) {
                    ModbusRegisterValueStore.getInstance().putRange(request.startAddress,
                            pending.response.registerValues);
                }
                if (callback != null) {
                    callback.onSuccess(pending.response);
                }
            }
        } finally {
            synchronized (responseLock) {
                if (pendingRequest == pending) {
                    pendingRequest = null;
                    responseBuffer = new byte[0];
                }
            }
            currentTransport.endTransaction();
        }
    }

    private void extractResponseLocked() {
        PendingRequest pending = pendingRequest;
        if (pending == null) {
            return;
        }
        ModbusRequest request = pending.request;
        int offset = 0;
        while (offset + 5 <= responseBuffer.length) {
            if ((responseBuffer[offset] & 0xFF) != request.slaveId) {
                offset++;
                continue;
            }
            int responseFunction = responseBuffer[offset + 1] & 0xFF;
            if (responseFunction != request.function && responseFunction != (request.function | 0x80)) {
                offset++;
                continue;
            }
            int frameLength = responseLength(responseBuffer, offset, responseFunction);
            if (frameLength == 0) {
                break;
            }
            if (offset + frameLength > responseBuffer.length) {
                break;
            }
            byte[] candidate = Arrays.copyOfRange(responseBuffer, offset, offset + frameLength);
            if (!ModbusRtuFrame.hasValidCrc(candidate)) {
                offset++;
                continue;
            }
            pending.response = ModbusResponse.parse(candidate);
            responseBuffer = Arrays.copyOfRange(responseBuffer, offset + frameLength,
                    responseBuffer.length);
            responseLock.notifyAll();
            return;
        }
        if (offset > 0) {
            responseBuffer = Arrays.copyOfRange(responseBuffer, offset, responseBuffer.length);
        }
        if (responseBuffer.length > 512) {
            responseBuffer = new byte[0];
        }
    }

    private int responseLength(byte[] data, int offset, int responseFunction) {
        if ((responseFunction & 0x80) != 0) {
            return 5;
        }
        if (ModbusFunction.isRead(responseFunction)) {
            if (offset + 3 > data.length) {
                return 0;
            }
            return 5 + (data[offset + 2] & 0xFF);
        }
        if (responseFunction == ModbusFunction.WRITE_SINGLE_REGISTER
                || responseFunction == ModbusFunction.WRITE_MULTIPLE_REGISTERS) {
            return 8;
        }
        return 0;
    }

    private static byte[] append(byte[] source, byte[] add) {
        byte[] result = Arrays.copyOf(source, source.length + add.length);
        System.arraycopy(add, 0, result, source.length, add.length);
        return result;
    }

    private static void notifyFailure(Callback callback, String message) {
        if (callback != null) {
            callback.onFailure(message);
        }
    }

    private static final class PendingRequest {
        final ModbusRequest request;
        ModbusResponse response;
        String error;

        PendingRequest(ModbusRequest request) {
            this.request = request;
        }
    }

    public interface Callback {
        void onSuccess(ModbusResponse response);

        void onFailure(String message);
    }
}
