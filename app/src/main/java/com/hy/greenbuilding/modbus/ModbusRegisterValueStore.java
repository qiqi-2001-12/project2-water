package com.hy.greenbuilding.modbus;

import org.greenrobot.eventbus.EventBus;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/** 已读到的原始 16 位寄存器值。倍率、单位和字序确认后再在此处统一转换。 */
public final class ModbusRegisterValueStore {
    private static final ModbusRegisterValueStore INSTANCE = new ModbusRegisterValueStore();
    private final Map<Integer, Integer> values = new HashMap<>();

    private ModbusRegisterValueStore() {
    }

    public static ModbusRegisterValueStore getInstance() {
        return INSTANCE;
    }

    public synchronized Integer get(int address) {
        return values.get(address);
    }

    synchronized void putRange(int startAddress, int[] registerValues) {
        for (int index = 0; index < registerValues.length; index++) {
            values.put(startAddress + index, registerValues[index]);
        }
        int endAddress = startAddress + registerValues.length - 1;
        if (startAddress <= WaterUnitRegisterMap.STATUS_FAULT_INFO_5_ADDRESS
                && endAddress >= WaterUnitRegisterMap.STATUS_FAULT_INFO_8_ADDRESS) {
            WaterUnitFaultHistoryStore.getInstance().update(new HashMap<>(values));
        }
        EventBus.getDefault().post(new ModbusRegistersUpdatedEvent(startAddress, registerValues.length));
    }

    public synchronized Map<Integer, Integer> snapshot() {
        return Collections.unmodifiableMap(new HashMap<>(values));
    }

    public static final class ModbusRegistersUpdatedEvent {
        public final int startAddress;
        public final int quantity;

        ModbusRegistersUpdatedEvent(int startAddress, int quantity) {
            this.startAddress = startAddress;
            this.quantity = quantity;
        }
    }
}
