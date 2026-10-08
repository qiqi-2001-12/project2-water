package com.hy.greenbuilding.modbus;

import com.orhanobut.logger.Logger;

/** Water unit controls exposed by the existing home and settings screens. */
public final class WaterUnitControl {
    public static final int POWER_REGISTER = WaterUnitRegisterMap.CONTROL_POWER_ADDRESS; // S01: 0 off, 1 on
    public static final int MODE_REGISTER = WaterUnitRegisterMap.CONTROL_MODE_ADDRESS; // S02
    public static final int MODE_COOLING = 0;
    public static final int MODE_HEATING = 1;

    private static final Object STATE_LOCK = new Object();
    private static Integer desiredPower;
    private static Integer desiredMode;

    private WaterUnitControl() {
    }

    public static void setPower(boolean on) {
        setDesiredPower(on ? 1 : 0);
    }

    public static void setMode(int mode) {
        if (mode != MODE_COOLING && mode != MODE_HEATING) {
            throw new IllegalArgumentException("Unsupported water unit mode: " + mode);
        }
        setDesiredMode(mode);
    }

    /** Re-applies the App's target only when the water unit reports a different value. */
    static void reconcile(int[] values) {
        if (values == null || values.length < 2) {
            return;
        }
        Integer powerToWrite = null;
        Integer modeToWrite = null;
        synchronized (STATE_LOCK) {
            if (desiredPower != null && desiredPower != values[0]) {
                powerToWrite = desiredPower;
            }
            if (desiredMode != null && desiredMode != values[1]) {
                modeToWrite = desiredMode;
            }
        }
        if (powerToWrite != null) {
            writeSingle(POWER_REGISTER, powerToWrite, "S01");
        }
        if (modeToWrite != null) {
            writeSingle(MODE_REGISTER, modeToWrite, "S02");
        }
    }

    private static void setDesiredPower(int value) {
        boolean changed;
        synchronized (STATE_LOCK) {
            changed = desiredPower == null || desiredPower != value;
            desiredPower = value;
        }
        if (changed) {
            writeSingle(POWER_REGISTER, value, "S01");
        }
    }

    private static void setDesiredMode(int value) {
        boolean changed;
        synchronized (STATE_LOCK) {
            changed = desiredMode == null || desiredMode != value;
            desiredMode = value;
        }
        if (changed) {
            writeSingle(MODE_REGISTER, value, "S02");
        }
    }

    private static void writeSingle(final int address, final int value, final String name) {
        ModbusRtuManager.getInstance().submit(ModbusRequest.writeSingle(address, value),
                new ModbusRtuManager.Callback() {
                    @Override
                    public void onSuccess(ModbusResponse response) {
                        Logger.d("Water unit " + name + " written: " + value);
                    }

                    @Override
                    public void onFailure(String message) {
                        Logger.d("Water unit " + name + " write failed: " + message);
                    }
                });
    }
}
