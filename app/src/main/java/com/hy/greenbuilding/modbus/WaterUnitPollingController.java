package com.hy.greenbuilding.modbus;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

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
        scheduler.scheduleWithFixedDelay(this::readStatusCycle, 500L,
                STATUS_POLL_INTERVAL_MS, TimeUnit.MILLISECONDS);
    }

    /** 进入设置信息页时读取全部 P 参数；连续地址按 Modbus 单次最大 125 个寄存器拆分。 */
    public void readSettings() {
        if (!ModbusRtuManager.getInstance().isCommunicationEnabled()
                || !settingsReading.compareAndSet(false, true)) {
            return;
        }
        readSettingsBlock(0);
    }

    private void readStatusCycle() {
        if (!ModbusRtuManager.getInstance().isCommunicationEnabled()
                || !statusReading.compareAndSet(false, true)) {
            return;
        }
        readStatusBlock(0);
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
                        statusReading.set(false);
                    }
                });
    }

    private void readSettingsBlock(final int index) {
        final WaterUnitRegisterMap.RegisterBlock[] blocks = new WaterUnitRegisterMap.RegisterBlock[]{
                new WaterUnitRegisterMap.RegisterBlock(1001, 125),
                new WaterUnitRegisterMap.RegisterBlock(1126, 4),
                new WaterUnitRegisterMap.RegisterBlock(1400, 40)
        };
        if (index >= blocks.length) {
            settingsReading.set(false);
            return;
        }
        WaterUnitRegisterMap.RegisterBlock block = blocks[index];
        ModbusRtuManager.getInstance().submit(ModbusRequest.read(
                ModbusFunction.READ_HOLDING_REGISTERS, block.startAddress, block.quantity),
                new ModbusRtuManager.Callback() {
                    @Override
                    public void onSuccess(ModbusResponse response) {
                        readSettingsBlock(index + 1);
                    }

                    @Override
                    public void onFailure(String message) {
                        settingsReading.set(false);
                    }
                });
    }
}
