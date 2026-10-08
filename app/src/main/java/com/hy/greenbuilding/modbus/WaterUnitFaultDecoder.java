package com.hy.greenbuilding.modbus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Decodes the water-unit fault bitmaps defined in the supplied fault information table. */
public final class WaterUnitFaultDecoder {
    private static final Definition[] DEFINITIONS = new Definition[]{
            new Definition(WaterUnitRegisterMap.STATUS_SYSTEM_INFO_ADDRESS, new String[]{
                    null, null, null, null, null, null, null, null,
                    "回水感温故障", "水位开关故障", "运行时间锁定", "直流风机1故障",
                    "直流风机2故障", "直流风机模块通信故障", "直流风机1故障3次", "直流风机2故障3次"
            }),
            new Definition(WaterUnitRegisterMap.STATUS_FAULT_INFO_1_ADDRESS, new String[]{
                    "进水感温故障", "出水感温故障", "水箱感温故障", "环境感温故障",
                    "系统1盘管感温故障", "系统2盘管感温故障", "系统1回气感温故障", "系统2回气感温故障",
                    "系统1排气感温故障", "系统2排气感温故障", "系统1增焓进感温故障", "系统2增焓进感温故障",
                    "系统1增焓出感温故障", "系统2增焓出感温故障", "系统1防冻感温故障", "系统2防冻感温故障"
            }),
            new Definition(WaterUnitRegisterMap.STATUS_FAULT_INFO_2_ADDRESS, new String[]{
                    "系统1高压故障", "系统1高压故障3次", "系统1低压故障", "系统1低压故障3次",
                    "系统2高压故障", "系统2高压故障3次", "系统2低压故障", "系统2低压故障3次",
                    "系统1排气过高保护", "系统1排气过高保护3次", "系统2排气过高保护", "系统2排气过高保护3次",
                    "系统1过流保护", "系统1过流保护3次", "系统2过流保护", "系统2过流保护3次"
            }),
            new Definition(WaterUnitRegisterMap.STATUS_FAULT_INFO_3_ADDRESS, new String[]{
                    "热水机高低水位开关断开", "扩展模块通信故障", "相序保护", "使用侧水流故障",
                    "冬季一级保护", "冬季二级保护", "进出水温差过大保护", "电加热过热保护",
                    "系统1防冻保护", "系统2防冻保护", "驱动1通信故障", "驱动2通信故障",
                    "系统1高压压力传感器故障", "系统1低压压力传感器故障", "系统2高压压力传感器故障", "系统2低压压力传感器故障"
            }),
            new Definition(WaterUnitRegisterMap.STATUS_FAULT_INFO_4_ADDRESS, new String[]{
                    "驱动器IPM模块硬件过流", "输出过压故障", "驱动器IPM模块过热", "驱动器PFC模块过热",
                    "驱动器PFC瞬间过流", "交流输入过流", "压缩机失步保护", "直流母线过压",
                    "直流母线欠压", "压缩机输出缺相", "压缩机启动失败", "压缩机型号错误",
                    "压缩机瞬间过流", "输入电压过高", "输入电压过低", "压缩机有效值过流"
            }),
            new Definition(WaterUnitRegisterMap.STATUS_FAULT_INFO_5_ADDRESS, new String[]{
                    "输入缺相", null, null, "输出三相电流不平衡保护",
                    null, "驱动器电流检测电路故障", "驱动器PFC模块硬件过流（IGBT短路保护）", "对地短路故障",
                    "电机相间短路故障", "其他故障", "驱动器芯片复位故障", "驱动器存储芯片故障",
                    "驱动器IPM模块温度检测电路故障", "驱动器PFC模块温度检测电路故障", "驱动器与上位机通讯故障", "驱动器充电回路故障"
            }),
            new Definition(WaterUnitRegisterMap.STATUS_FAULT_INFO_7_ADDRESS, new String[]{
                    null, "变频水泵通信故障", null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null
            }),
            new Definition(WaterUnitRegisterMap.STATUS_FAULT_INFO_8_ADDRESS, new String[]{
                    "回气1防冻保护", "回气1防冻锁机", "回气2防冻保护", "回气2防冻锁机",
                    "热源侧进水感温故障", "热源侧出水感温故障", "热源侧防冻保护", "热源侧水流故障",
                    "风机过载保护", "风机过载保护3次", "热源侧水泵过载保护", "热源侧水泵过载保护3次",
                    "使用侧水泵过载保护", "使用侧水泵过载保护3次", "电能表通信故障", "驱动多次故障锁机"
            })
    };

    private WaterUnitFaultDecoder() {
    }

    /** Returns active faults in high-bit-first order for every fault bitmap. */
    public static List<Fault> decode(Map<Integer, Integer> registerValues) {
        List<Fault> faults = new ArrayList<>();
        if (registerValues == null) {
            return faults;
        }
        for (Definition definition : DEFINITIONS) {
            Integer value = registerValues.get(definition.address);
            if (value == null) {
                continue;
            }
            int bitmap = value & 0xFFFF;
            for (int bit = 15; bit >= 0; bit--) {
                String name = definition.names[bit];
                if (name != null && (bitmap & (1 << bit)) != 0) {
                    faults.add(new Fault(definition.address, bit, name));
                }
            }
        }
        return faults;
    }

    private static final class Definition {
        final int address;
        final String[] names;

        Definition(int address, String[] names) {
            this.address = address;
            this.names = names;
        }
    }

    public static final class Fault {
        public final int address;
        public final int bit;
        public final String name;

        Fault(int address, int bit, String name) {
            this.address = address;
            this.bit = bit;
            this.name = name;
        }

        public String key() {
            return address + ":" + bit;
        }
    }
}
