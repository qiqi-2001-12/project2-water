package com.hy.greenbuilding.modbus;

/** Converts the 2114 system-status bitmap into readable active states. */
public final class WaterUnitSystemStatusDecoder {
    private static final String[] STATUS_NAMES = new String[]{
            "除霜", "关机", "运行", "待机", "运行范围保护", "杀菌状态", "目标温度修正状态", "错峰控温时段",
            "回水感温故障", "水位开关故障", "运行时间锁定", "直流风机1故障",
            "直流风机2故障", "直流风机模块通信故障", "直流风机1故障3次", "直流风机2故障3次"
    };

    private WaterUnitSystemStatusDecoder() {
    }

    /** Returns all active system states in high-bit-first order. */
    public static String decode(int rawValue) {
        StringBuilder result = new StringBuilder();
        int bitmap = rawValue & 0xFFFF;
        for (int bit = 15; bit >= 0; bit--) {
            if ((bitmap & (1 << bit)) == 0) {
                continue;
            }
            if (result.length() > 0) {
                result.append(" / ");
            }
            result.append(STATUS_NAMES[bit]);
        }
        return result.length() == 0 ? "无活动状态" : result.toString();
    }
}
