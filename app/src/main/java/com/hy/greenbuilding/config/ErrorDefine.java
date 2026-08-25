package com.hy.greenbuilding.config;

/**
 * 故障类型定义
 */
public class ErrorDefine {
    //升温除湿类型定义
    public static final String OPEN_CLOSE = "开关机";
    public static final String OUT_CIRCLE_TEMP = "外环温";
    public static final String PRESS_FREQUENCY = "压机目标频率";
    public static final String RUN_MODE = "运行模式";
    public static final String OUT_PIPE_TEMP = "外管温";
    public static final String PRESS_RUN_FREQUENCY = "压机运行频率";
    public static final String COLD_SET_TEMP= "制冷设定温度";
    public static final String RETURN_AIR_TEMP = "回气温度";
    public static final String COPM_ELEC = "压缩机电流";
    public static final String HOT_SET_TEMP = "制热设定温度";
    public static final String EXHAUST_TEMP = "排气温度";
    public static final String DRIVE_RUN_STATUS = "驱动运行状态";
    public static final String DOOR_IN_TEMP = "室内温度";
    public static final String EVAPORATION_TEMP = "蒸发温度";
    public static final String IPM_TEMP = "IPM温度";
    public static final String WIND_MODE = "出风温度";
    public static final String CONDENSATION_CLOSE = "冷凝温度";
    public static final String MAIN_VOLTAGE = "母线电压";
    public static final String UP_PIPE_TEMP = "升温盘管温度";
    public static final String SLOW_PRESS= "低压压力";
    public static final String IN_ELEC = "输入电流";
    public static final String HUMI_PIPE_TEMP = "除湿盘管温度";
    public static final String HIGH_PRESS = "高压压力";
    public static final String TARGET_SPEED = "目标转速";
    public static final String MAIN_VALVE_OPEN = "主阀初开度";
    public static final String MAIN_VALVE = "主阀过热度";
    public static final String AUX_VALVE = "辅阀过热度";
    public static final String WIND_ROTATION_SPEED = "风机1转速";


    public static final String SOFTWARE_VERSION = "软件版本号";
    public static final String FOUR_STATUS = "四通阀状态";
    public static final String ERROR_CODE1 = "故障信息1";
    public static final String HEAT_STATUS = "电加热状态";
    public static final String ERROR_CODE2 = "故障信息2";
    public static final String HEAT = "加热带";
    public static final String ERROR_CODE3 = "故障信息3";
    public static final String HUMI_VALVE = "除湿旁通阀";
    public static final String ERROR_CODE4 = "故障信息4";
    public static final String ENTHALPY_VALVE = "增焓阀";
    public static final String ERROR_CODE5 = "故障信息5";
    public static final String MAIN_EEV = "主路EEV";
    public static final String ERROR_CODE6 = "故障信息6";
    public static final String AUX_EEV = "辅助EEV";
    public static final String MANUAL_FAN_ENABLE = "手动外风机使能";


    public static final String SYSTEM_STATUS = "系统状态";
    public static final String DEFROST_SIGNAL = "除霜信号";
    public static final String DEFROST_MODEL = "除霜模式";
    public static final String MANUAL_FREQUENCY = "手动频率";
    public static final String MAIN_EEV_MODEL = "主路EEV模式";
    public static final String MAIN_EEV_OPEN = "主路EEV初开度";
    public static final String AUX_EEV_MODEL = "辅助EEV模式";
    public static final String AUX_EEV_OPEN = "辅助EEV初开度";
    public static final String AUX_EEV_OPEN_MIN = "辅助EEV最小开度";
    public static final String FAN_NUM = "直流风机数量";
    public static final String FAN_SPEED_MAX = "直流风机最高转速";
    public static final String FAN_SPEED_MIN= "直流风机最低转速";
    public static final String FAN_SPEED = "风机手动转速";
    public static final String DRIVE_TYPE = "驱动型号";
    public static final String COMP_TYPE = "压缩机型号";

    //低温增焓
    public static String[] LowTempError1 = {"","","","PFC过电流故障 PF","压缩机驱动故障 P4","电压过高或过低故障 P1",
            "室外E方故障 E9","","室外风机故障 E7","","室外温度传感器故障E5","","","","室内外通信故障 E1","室内板E方故障 E0"};
    public static int[] LowTempError1Code = {0,0,0,248,247,246,245,0,244,0,243,0,0,0,243,241};
    public static String[] LowTempError0 = {"新风主板通讯故障 EC","排气传感器故障 E15","外管温传感器故障E13","外环温传感器故障E14"};

    //光伏
    public static String[] PVError1 = {"","","","室外直流风机故障","室外环温传感器故障","室外盘管温度传感器障","吸气温度传感器故障",
            "排气温度传感器故障","室外风机故障","压缩机壳顶故障 ","IPM模块故障","压缩机失步故障","压缩机启动异常","主板与驱动板通信故障","","室外EE方故障"};
    public static int[] PVError1Code = {0,0,0,212,211,210,209,208,207,206,205,204,203,202,0,201};

    public static String[] PVError0 = {"","","","Ia过流保护","Va输入欠压保护","制热外环境温度过高保护停机","制冷外环境温度过低保护停机","制热内盘管过热保护停机","" +
            "制冷外盘管过热保护停机","制冷内盘管防冻结保护停机","排气温度过热保护停机","IPM温度过高保护","直流母线电压过高、低保护",
            "Vb电压过高、过低保护","压缩机相电流保护停机","Ib电流保护停机"};
    public static int[] PVError0Code = {0,0,0,225,224,223,222,221,220,219,218,217,216,215,214,213};

    //升温除湿
    public static String[] UpTempError1 = {"进水感温故障","出水感温故障","水箱感温故障","环境感温故障","系统1盘管感温故障",
            "系统2盘管感温故障","系统1回气感温故障","系统2回气感温故障","系统1排气感温故障","系统2排气感温故障",
            "系统1熷焓进感温故障","系统2熷焓进感温故障","系统1熷焓出感温故障","系统2熷焓出感温故障","系统1防冻感温故障","系统2防冻感温故障"};
    public static String[] UpTempError2 = {"系统1高压故障","系统1高压故障3次","系统1低压故障","系统1低压故障3次","系统2高压故障",
            "系统2高压故障3次","系统2低压压故障","系统2低压故障3次","系统1排气过高保护","系统1排气过高保护3次",
            "系统2排气过高保护","系统2排气过高保护3次","系统1过流保护","系统1过流保护3次","系统2过流保护","系统2过流保护3次"};
    public static String[] UpTempError3 = {"热水机高低水位开关断开","扩展模块通信故障","相序保护","使用侧水流故障","冬季一级保护",
            "冬季二级保护","进出水温差过大保护","电加热过热保护","系统1防冻保护","系统2防冻保护",
            "驱动1通信故障","驱动2通信故障","系统1高压压力传感器","系统1低压压力传感器故障","系统2高压压力传感器故障","系统2低压压力传感器故障"};

    public static String[] UpTempError4 = {"驱动器IPM模块硬件过流","输出过压故障","驱动器IPM模块过热","驱动器PFC模块过热","驱动器PFC瞬间过流",
            "交流输入过流","压缩机失步保护","直流母线过压","直流母线欠压","压缩机输出缺相",
            "压缩机启动失败","压缩机型号错误","压缩机瞬间过流","输入电压过高","输入电压过低","压缩机有效值过流"};

    public static String[] UpTempError5 = {"输入缺相","","","输出三相电流不平衡保护","",
            "驱动器电流检测电路故障","驱动器PFC模块硬件过流（IGBT短路保护）","","","其他故障",
            "驱动器芯片复位故障","驱动器存储芯片故障","驱动器IPM模块温度检测电路故障","驱动器PFC模块温度检测电路故障","驱动器与上位机通讯故障","驱动器充电回路故障"};

    public static String[] UpTempError6 = {"回气1防冻保护","回气1防冻锁机","回气2防冻保护","回气2防冻锁机","热源侧进水感温故障",
            "热源侧出水感温故障","热源侧防冻保护","热源侧水流故障","回水感温故障","水位开关故障",
            "直流风机1故障","直流风机2故障","直流风机模块通信故障","直流风机1故障3次","直流风机2故障3次",""};

    public static String[] SystemStatus = {"除霜","关机","运行","待机","运行范围保护",
            "运行时间锁定","目标温度修正状态","错峰控温时段","","","","","","","",""};


    public static int[] UpTempError2Code = {0,0,0,0,0,0,0,217,216,215,214,213,212,211,210,209};

    public static String[] Up_temp_data = {OPEN_CLOSE,OUT_CIRCLE_TEMP,PRESS_FREQUENCY,RUN_MODE,OUT_PIPE_TEMP,PRESS_RUN_FREQUENCY,COLD_SET_TEMP
            ,RETURN_AIR_TEMP,COPM_ELEC,HOT_SET_TEMP,EXHAUST_TEMP,DRIVE_RUN_STATUS,DOOR_IN_TEMP,EVAPORATION_TEMP,IPM_TEMP,WIND_MODE,
            CONDENSATION_CLOSE,MAIN_VOLTAGE,UP_PIPE_TEMP,SLOW_PRESS,IN_ELEC,HUMI_PIPE_TEMP,HIGH_PRESS,TARGET_SPEED,MAIN_VALVE_OPEN,MAIN_VALVE,
            AUX_VALVE,WIND_ROTATION_SPEED,SOFTWARE_VERSION,FOUR_STATUS,HEAT_STATUS,HEAT,HUMI_VALVE,ENTHALPY_VALVE,MAIN_EEV,AUX_EEV,MANUAL_FAN_ENABLE};

    public static String[] Up_temp_data1= {SOFTWARE_VERSION,FOUR_STATUS,ERROR_CODE1,HEAT_STATUS,ERROR_CODE2,HEAT,ERROR_CODE3
            ,HUMI_VALVE,ERROR_CODE4,ENTHALPY_VALVE,ERROR_CODE5,MAIN_EEV,ERROR_CODE6,AUX_EEV,MANUAL_FAN_ENABLE};

    public static String[] Up_temp_Error = {ERROR_CODE1,ERROR_CODE2,ERROR_CODE3,ERROR_CODE4,ERROR_CODE5,ERROR_CODE6};

    public static String[] NTC_Error = {"","","升温NTC故障","除湿NTC故障","出风NTC故障","内循环NTC故障","室外NTC故障","防冻NTC故障"};
}
