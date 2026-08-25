package com.hy.greenbuilding.protocol;

public class FunctionObject {

    /**
     * 设备类型定义
     */
    public static final int MAIN_CONTROL_BOARD = 0x00;//主控板
    public static final int LOW_TEMP = 0X01;//低温增焓
    public static final int PV = 0x02;//光伏
    public static final int TEMP_RISE = 0X03;//升温除湿
    public static final int FAN = 0x04;//风机
    public static final int HOUR_METER = 0X05;//电表
    public static final int ENVIRONMENT_CHECK = 0x06;//环境检测
    public static final int FILTER_SCREEN = 0x07;//风阀
    public static final int DC_FAN = 0x08;//DC风机
    public static final int PID_SET = 0x09;//PID
    public static final int CUSTOM_GET = 0x0E;//杂项数据
    /**
     * 功能ID定义
     */

    //低温增焓 光伏
    public static final int GET_OUT_STATUS = 0x01;//获取室外机状态
    public static final int SET_MODE = 0x02;//设定模式
    public static final int ABILITY_TEST = 0x03;//能力测试
    public static final int FORCE_DEFROST = 0x04;//强制除霜
    public static final int SET_POWER = 0x05;//设定压缩机频率
    public static final int MAIN_EXPANSION = 0x06;//设定主膨胀阀开度
    //public static final int FORCE_FLUORINE_COLLECTION = 0x04;//强制收氟

    public static final int AUX_EXPANSION = 0x07;//设定辅膨胀阀开度

    public static final int SET_TEMP = 0x02;//设定温度


    //升温除湿
    public static final int UP_GET_OUT_STATUS = 0x01;//获取室外机状态
    public static final int UP_SET_MODE = 0x02;//设定模式
    public static final int UP_DEFROST_MODE = 0x03;//除霜模式
    public static final int UP_DEFROST_STATUS = 0x04;//强制除霜
    public static final int UP_FREQUNCY = 0x05;//设定手动频率
    public static final int UP_MAIN_EEV_MODE = 0x06;//设定主路EEV模式
    public static final int UP_MAIN_EEV_OPEN = 0x07;//设定主路EEV初开度
    public static final int UP_AUX_EEV_MODE = 0x08;//设定辅助EEV模式
    public static final int UP_AUX_EEV_OPEN = 0x09;//设定辅助EEV初开度
    public static final int UP_AUX_EEV_OPEN_MIN = 0x0A;//设定辅助EEV最小开度
    public static final int UP_FAN_NUM = 0x0B;//设置直流风机数量
    public static final int UP_FAN_SPEED_MAX = 0x0C;//设置直流风机最高转速
    public static final int UP_FAN_SPEED_MIN = 0x0D;//设置直流风机最低转速
    public static final int UP_SPEED_STATUS = 0x0E;//手动风速使能
    public static final int UP_SET_SPEED = 0x0F;//设置风机手动转速
    public static final int UP_PRESS_TYPE = 0x10;//设置压缩机型号
    public static final int UP_SET_TYPE = 0x11;//设置室外机驱动类型
    public static final int UP_SET_COMMON_DATA = 0x12;//通用数据发送

    //风机
    public static final int GET_FAN_STATUS = 0x01;//获取风机状态
    public static final int SET_SPEED = 0x02;//设定风速
    public static final int SET_SPEED_VALUE = 0x03;//设定风速对应风量
    public static final int SET_FAN_TYPE = 0x04;//设定风机接入 类型
    public static final int SET_FAN_ADDRESS = 0x05;//设定风机地址
    public static final int TEST_FAN_VALUE = 0x06;//测试风量
    public static final int SEARCH_FAN_ADDRESS = 0x07;//搜索地址
    public static final int SEARCH_FAN_TYPE_MODEL = 0x08;//安装类型和安装型号
    public static final int SET_STATIC_PRESSURE_MODE = 0x09;//设置定静压控制模式
    public static final int SET_FAN_PRESSURE_VALUE = 0x0a;//设置风机压力值



    //主控板
    public static final int GET_CONTROL_STATUS = 0x01;//获取主控板运行状态
    public static final int SET_LOW_POWER   = 0x02;//低功耗模式控制
    public static final int SET_CONTROL_MODE = 0x03;//设定主控板运行模式
    public static final int SET_HUMIDITY= 0x04;//设定湿度
    public static final int SET_TEMP_SECTION= 0x05;//设定温度上下限
    public static final int SET_OUTDOOR_TYPE= 0x06;//设定室外机类型
    public static final int GET_TEMP_SWITCH= 0x07;//温控开关
    public static final int SET_COLD_TEMP= 0x08;//设定制冷温差限制值
    public static final int SET_HUMI_TEMP= 0x09;//设定除湿温差限制值
    public static final int SET_HUMI_SWITCH= 0x0a;//除湿开关


    //PID
    public static final int GET_PID_STATUS = 0x01;//获取PID信息
    public static final int SET_PID_VALUE   = 0x02;//设置PID值
    public static final int SET_PID_TEMP1 = 0x03;//设置PID温度1
    public static final int SET_PID_TEMP2 = 0x04;//设置PID温度2
    public static final int SET_OUT_TEMP = 0x05;//设置室外机的室外温度

    //DC风机
    public static final int GET_DC_FAN_STATUS = 0x01;//获取DC风机信息
    public static final int SET_DC_FAN_SPEED = 0x02;//设置DC转速
    public static final int SET_DC_FAN_SWITCH = 0x03;//电磁阀开关
    public static final int SET_EXPANSION_SWITCH = 0x04;//电子膨胀阀开关
    public static final int SET_EXPANSION_OPEN = 0x05;//电子膨胀阀开度
    public static final int SET_EXPANSION_TYPE = 0x06;//电子膨胀阀调节类型
    public static final int SET_EXPANSION_PID_VALUE = 0x07;//电子膨胀阀PID目标值
    public static final int SET_EXPANSION_REGULAR_VALUE = 0x08;//电子膨胀阀固定开度值

    //环境检测
    public static final int GET_ENVIRONMENT_STATUS = 0x01;//获取DC风机信息
    public static final int SET_CO2_VALUE = 0x02;//设置CO2阈值
    public static final int SET_PM_VALUE = 0x03;//设置PM2.5阈值
    public static final int GET_PM_CO2 = 0x04;//获取PM2.5和CO2
    public static final int SET_AIR_QUALITY = 0X05;//设置空气质量


    //风阀
    public static final int GET_AIR_VALVE_STATUS = 0x01;//获取风阀信息
    public static final int SET_AIR_VALVE_MODE = 0x02;//设置风阀运行模式
    public static final int SET_AIR_VALVE_OPEN = 0x03;//设置风阀开度
    public static final int SET_AIR_VALVE_OPEN_MAX = 0x04;//设置风阀最大开度


    public static final int GET_CUSTOM_DATA = 0x01;//获取杂项数据

    public static final int SET_HEATING_DEHUMIDIFICATION_TYPE = 0x09;//设置升温除湿类型
    public static final int SET_FAN_BOARD_TYPE = 0x0A;//设置风机小板类型
    public static final int SET_MAINBOARD_CONFIG = 0x0B;//设置风机小板功能选择

}
