package com.hy.greenbuilding.protocol;

import java.math.BigDecimal;

public interface IUpTempStatusInfo {
    /**
     * 空调模式十六进制
     * @return
     */
    String airModeHex();
    /**
     * 空调模式
     * @return
     */
    String airConditionerMode();


    /**
     * 除霜信号
     * @return
     */
    String defrostSignal();

    /**
     * 除霜模式
     * @return
     */
    String defrostModel();


    /**
     * 制冷设定温度
     * @return
     */
    BigDecimal settingTempCold();
    /**
     * 设定温度
     * @return
     */
    BigDecimal settingTemp();

    /**
     *室内温度
     */
    BigDecimal inDoorTemp();

    /**
     * 室外温度
     */
    BigDecimal outDoorTemp();

    /**
     * 室外冷凝温度
     */
    BigDecimal outDoorTemp1();

    /**
     * 内盘管1温度
     */
    BigDecimal inPipeTemp1();

    /**
     * 内盘管2温度
     */
    BigDecimal inPipeTemp2();

    /**
     * 出风温度
     */
    BigDecimal windTemp();

    /**
     * 排气温度
     */
    BigDecimal exHaustTemp();

    /**
     * 回气温度
     */
    BigDecimal exReturnTemp();

    /**
     * 室外风机转速,单位RPM +
     */
    BigDecimal outFunSpeed();

    /**
     * 外机电流，单位A +
     */
    BigDecimal outElectric();

    /**
     * 母线电压，单位V +
     */
    BigDecimal voltage();

    /**
     * 模块温度，单位℃ +
     */
    BigDecimal moduleTemp();
    /**
     * 压缩机频率，单位HZ +
     */
    BigDecimal frequency();

    /**
     * 主膨胀阀开度 +
     */
    String mainExpansion();

    /**
     * 辅膨胀阀开度 +
     */
    String auxExpansion();

    /**
     * 故障代码1
     */
    byte[] faultMessage1();

    /**
     * 故障代码0
     */
    byte[] faultMessage2();

    /**
     * 内机故障代码1
     */
    byte[] faultMessage3();

    /**
     * 售后代码Er
     */
    byte[] faultMessage4();

    /**
     * 内机运行风速
     * @return
     */
    String getInTermSpeed();

    /**
     * 化霜状态
     * @return
     */
    int defrostStatus();

    /**
     * 能力测试数值
     */
    String functionTestValue();

    /**
     * 测试压缩机定频
     * @return
     */
    int frequencyTestValue();

    /**
     * 测试主膨胀阀开度
     * @return
     */
    int mainExpansionTest();

    /**
     * 测试副膨胀阀开度
     * @return
     */
    int auxExpansionTest();
}
