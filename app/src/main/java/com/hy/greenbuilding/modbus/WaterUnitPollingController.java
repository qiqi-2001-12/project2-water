package com.hy.greenbuilding.modbus;

import com.orhanobut.logger.Logger;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 水机自动读取调度器。
 * 实体通讯开关关闭时不发送任何报文；开启后，状态寄存器按低频顺序轮询。
 */
public final class WaterUnitPollingController {
    private static final WaterUnitPollingController INSTANCE = new WaterUnitPollingController();
    private static final long STATUS_POLL_INTERVAL_MS = 3000L;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final AtomicBoolean statusReading = new AtomicBoolean(false);
    private final AtomicBoolean settingsReading = new AtomicBoolean(false);
    private volatile boolean started;

    private WaterUnitPollingController() {
    }

    public static WaterUnitPollingController getInstance() {
        return INSTANCE;
    }

    /** 应用启动时调用一次；实际是否发报文受 ModbusRtuManager 开关控制。 */
    public synchronized void start() {
        if (started) {
            return;
        }
        started = true;
        // Queue the complete initial settings snapshot before periodic status traffic begins.
        readSettings();
        scheduler.scheduleWithFixedDelay(this::readStatusCycle, 500L,
                STATUS_POLL_INTERVAL_MS, TimeUnit.MILLISECONDS);
    }

    /**
     * Reads every independent settings range in one queueing pass. This gives a newly opened
     * application a complete snapshot quickly while retaining the serial transaction guarantee.
     */
    public void readSettings() {
        if (!ModbusRtuManager.getInstance().isCommunicationEnabled()
                || !settingsReading.compareAndSet(false, true)) {
            return;
        }
        List<WaterUnitRegisterMap.RegisterBlock> blocks =
                WaterUnitRegisterMap.getSettingReadBlocks();
        SettingsReadSession session = new SettingsReadSession(blocks.size());
        for (WaterUnitRegisterMap.RegisterBlock block : blocks) {
            readSettingsBlock(block, session);
        }
    }

    private void readStatusCycle() {
        if (!ModbusRtuManager.getInstance().isCommunicationEnabled()
                || !statusReading.compareAndSet(false, true)) {
            return;
        }
        readControlState();
    }

    private void readStatusBlock(final int index) {
        List<WaterUnitRegisterMap.RegisterBlock> blocks = WaterUnitRegisterMap.getStatusPollBlocks();
        if (index >= blocks.size()) {
            statusReading.set(false);
            return;
        }
        WaterUnitRegisterMap.RegisterBlock block = blocks.get(index);
        ModbusRtuManager.getInstance().submit(ModbusRequest.read(
                ModbusFunction.READ_INPUT_REGISTERS, block.startAddress, block.quantity),
                new ModbusRtuManager.Callback() {
                    @Override
                    public void onSuccess(ModbusResponse response) {
                        readStatusBlock(index + 1);
                    }

                    @Override
                    public void onFailure(String message) {
                        Logger.d("Water unit status block " + block.startAddress + "-"
                                + (block.startAddress + block.quantity - 1) + " failed: " + message);
                        readStatusBlock(index + 1);
                    }
        });
    }

    private void readControlState() {
        ModbusRtuManager.getInstance().submit(ModbusRequest.read(
                ModbusFunction.READ_HOLDING_REGISTERS,
                WaterUnitRegisterMap.CONTROL_POWER_ADDRESS, 2),
                new ModbusRtuManager.Callback() {
                    @Override
                    public void onSuccess(ModbusResponse response) {
                        WaterUnitControl.reconcile(response.registerValues);
                        readStatusBlock(0);
                    }

                    @Override
                    public void onFailure(String message) {
                        readStatusBlock(0);
                    }
                });
    }

    private void readSettingsBlock(final WaterUnitRegisterMap.RegisterBlock block,
                                   final SettingsReadSession session) {
        ModbusRtuManager.getInstance().submit(ModbusRequest.read(
                ModbusFunction.READ_HOLDING_REGISTERS, block.startAddress, block.quantity),
                new ModbusRtuManager.Callback() {
                    @Override
                    public void onSuccess(ModbusResponse response) {
                        session.completeBlock();
                    }

                    @Override
                    public void onFailure(String message) {
                        /*
                         * A few controller firmware variants reject a range when it contains an
                         * unsupported/reserved address, or cap the number of registers per read.
                         * Fall back to individual reads so one bad register does not leave the
                         * rest of this UI section at its placeholder value.
                         */
                        Logger.d("Water unit settings block " + block.startAddress + "-"
                                + (block.startAddress + block.quantity - 1) + " failed: " + message
                                + "; retrying each register");
                        readSettingsRegister(block, 0, session);
                    }
                });
    }

    private void readSettingsRegister(final WaterUnitRegisterMap.RegisterBlock block,
                                      final int offset, final SettingsReadSession session) {
        if (offset >= block.quantity) {
            session.completeBlock();
            return;
        }

        final int address = block.startAddress + offset;
        ModbusRtuManager.getInstance().submit(ModbusRequest.read(
                ModbusFunction.READ_HOLDING_REGISTERS, address, 1),
                new ModbusRtuManager.Callback() {
                    @Override
                    public void onSuccess(ModbusResponse response) {
                        readSettingsRegister(block, offset + 1, session);
                    }

                    @Override
                    public void onFailure(String message) {
                        Logger.d("Water unit setting register " + address + " failed: " + message);
                        readSettingsRegister(block, offset + 1, session);
                    }
                });
    }

    private final class SettingsReadSession {
        private final AtomicInteger remainingBlocks;

        SettingsReadSession(int blockCount) {
            remainingBlocks = new AtomicInteger(blockCount);
        }

        void completeBlock() {
            if (remainingBlocks.decrementAndGet() == 0) {
                settingsReading.set(false);
            }
        }
    }
}
