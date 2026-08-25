package com.hy.greenbuilding.event;
/**
 * 墨迹天气数据推送 EventBus 事件
 * 对应功能号 0x0D 的数据结构
 */
public class WeatherDataEvent {

    // 1. 室外温度
    private final int outdoorTemp;

    // 2. 室外湿度
    private final int outdoorHumidity;

    // 3. 室外 PM2.5
    private final double outdoorPM25;

    // 4. 天气情况代码
    private final int weatherCode;

    // 5. 风向代码
    private final int windDirectionCode;

    // 6. 风力
    private final int windForce;

    // 7. 污染程度代码
    private final int pollutionLevelCode;

    /**
     * 构造函数
     */
    public WeatherDataEvent(int outdoorTemp, int outdoorHumidity, double outdoorPM25, int weatherCode, int windDirectionCode, int windForce, int pollutionLevelCode) {
        this.outdoorTemp = outdoorTemp;
        this.outdoorHumidity = outdoorHumidity;
        this.outdoorPM25 = outdoorPM25;
        this.weatherCode = weatherCode;
        this.windDirectionCode = windDirectionCode;
        this.windForce = windForce;
        this.pollutionLevelCode = pollutionLevelCode;
    }

    // --- Getter 方法 ---

    public int getOutdoorTemp() {
        return outdoorTemp;
    }

    public int getOutdoorHumidity() {
        return outdoorHumidity;
    }

    /**
     * 获取室外 PM2.5 值 (已处理精度 0.1)
     */
    public double getOutdoorPM25() {
        return outdoorPM25;
    }

    /**
     * 获取天气情况代码 (需要自行调用工具类转为文本)
     */
    public int getWeatherCode() {
        return weatherCode;
    }

    /**
     * 获取风向代码 (需要自行调用工具类转为文本)
     */
    public int getWindDirectionCode() {
        return windDirectionCode;
    }

    public int getWindForce() {
        return windForce;
    }

    /**
     * 获取污染程度代码 (需要自行调用工具类转为文本)
     */
    public int getPollutionLevelCode() {
        return pollutionLevelCode;
    }



}
