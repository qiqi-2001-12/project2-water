package com.hy.greenbuilding.utils;

import android.content.Context;

import com.google.gson.Gson;
import com.hy.greenbuilding.config.SaveFilterScreen;
import com.hy.greenbuilding.config.SaveTimingInfo;
import com.hy.greenbuilding.mqtt.HDTopic;
import com.hy.greenbuilding.mqtt.MqttUploadManager;

import java.math.BigDecimal;

public class TimingUtils {
    /**
     * 判断3个时间段都没有设置
     * @param context
     * @return
     */
    public static boolean timeValueIsNull(Context context) {
        SaveTimingInfo timingInfo = MySpUtil.getTimingData(context);
        if (timingInfo == null) {
            return true;
        }
        if (timingInfo.getOpenDay() == 0) {
            return true;
        }
        String before1 = timingInfo.getBeforeTime1();
        String after1 = timingInfo.getAfterTime1();
        String before2 = timingInfo.getBeforeTime2();
        String after2 = timingInfo.getAfterTime2();
        String before3 = timingInfo.getBeforeTime3();
        String after3 = timingInfo.getAfterTime3();
        if (!StringUtils.isNullOrEmpty(before1) && !StringUtils.isNullOrEmpty(after1)) {
            return false;
        }
        if (!StringUtils.isNullOrEmpty(before2) && !StringUtils.isNullOrEmpty(after2)) {
            return false;
        }
        if (!StringUtils.isNullOrEmpty(before3) && !StringUtils.isNullOrEmpty(after3)) {
            return false;
        }
        return true;
    }

    /**
     * 当前时间是否在某个时间段
     * @param context
     * @param hour
     * @return
     */
    public static boolean timeSlot(Context context,int hour) {
        SaveTimingInfo timingInfo = MySpUtil.getTimingData(context);
        if (timingInfo == null) {
            return false;
        }
        String before1 = timingInfo.getBeforeTime1();
        String after1 = timingInfo.getAfterTime1();
        String before2 = timingInfo.getBeforeTime2();
        String after2 = timingInfo.getAfterTime2();
        String before3 = timingInfo.getBeforeTime3();
        String after3 = timingInfo.getAfterTime3();
        if (!StringUtils.isNullOrEmpty(before1) && !StringUtils.isNullOrEmpty(after1)) {
            if (hour >= Integer.parseInt(before1) && hour < Integer.parseInt(after1))
                return true;
        }
        if (!StringUtils.isNullOrEmpty(before2) && !StringUtils.isNullOrEmpty(after2)) {
            if (hour >= Integer.parseInt(before2) && hour < Integer.parseInt(after2))
                return true;
        }
        if (!StringUtils.isNullOrEmpty(before3) && !StringUtils.isNullOrEmpty(after3)) {
            if (hour >= Integer.parseInt(before3) && hour < Integer.parseInt(after3))
                return true;
        }
        return false;
    }

    //当前时间是否在时间段
    public static boolean isSection(int hour, int before, int after) {
        if (hour >= before && hour <= after) {
            return true;
        } else {
            return false;
        }
    }

    //压差异常判断
    public static boolean isPressure(Context context, int freshValue,int exhaustValue,int circle1Value,int circle2Value){

        boolean isPressureError = false;
        SaveFilterScreen saveFilterScreen = MySpUtil.getFilterScreen(context);
        String freshAir = saveFilterScreen.getFreshAirPressure();
        String exhaust = saveFilterScreen.getExhaustPressure();
        String circle1 = saveFilterScreen.getCircle1Pressure();
        String circle2 = saveFilterScreen.getCircle2Pressure();

        boolean b1 = saveFilterScreen.isFreshAirUsePressure();
        boolean b2 = saveFilterScreen.isExhaustUsePressure();
        boolean b3 = saveFilterScreen.isCircle1UsePressure();
        boolean b4 = saveFilterScreen.isCircle2UsePressure();
        if (!StringUtils.isNullOrEmpty(freshAir) && Integer.parseInt(freshAir) < freshValue && b1) {
            isPressureError = true;
        }
        if (!StringUtils.isNullOrEmpty(exhaust) && Integer.parseInt(exhaust) < exhaustValue && b2) {
            isPressureError = true;
        }
        if (!StringUtils.isNullOrEmpty(circle1) && Integer.parseInt(circle1) < circle1Value && b3) {
            isPressureError = true;
        }
        if (!StringUtils.isNullOrEmpty(circle2) && Integer.parseInt(circle2) < circle2Value && b4) {
            isPressureError = true;
        }
        saveFilterScreen.setFreshAirPressureValue(freshValue+"");
        saveFilterScreen.setExhaustPressureValue(exhaustValue+"");
        saveFilterScreen.setCircle1PressureValue(circle1Value+"");
        saveFilterScreen.setCircle2PressureValue(circle2Value+"");
        MySpUtil.setParam(context, MySpUtil.FILTER_SCREEN_DATA, new Gson().toJson(saveFilterScreen));
        return isPressureError;
    }

    //压差更换判断
    public static boolean initScreenError(Context context) {
        boolean isError = false;
        //滤网数据显示
        SaveFilterScreen saveFilterScreen = MySpUtil.getFilterScreen(context);
        if (saveFilterScreen != null) {
            //滤网使用
            String freshAirUse = saveFilterScreen.getFreshAirUse();
            String exhaustUse = saveFilterScreen.getExhaustUse();
            String circle1Use = saveFilterScreen.getCircle1Use();
            String circle2Use = saveFilterScreen.getCircle2Use();

            //滤网更换
            String freshAirChange = saveFilterScreen.getFreshAirChange();
            String exhaustChange = saveFilterScreen.getExhaustChange();
            String circle1Change = saveFilterScreen.getCircle1Change();
            String circle2Change = saveFilterScreen.getCircle2Change();

            boolean b1 = saveFilterScreen.isFreshAirUseTime();
            boolean b2 = saveFilterScreen.isExhaustUseTime();
            boolean b3 = saveFilterScreen.isCircle1UseTime();
            boolean b4 = saveFilterScreen.isCircle2UseTime();

            if (!StringUtils.isNullOrEmpty(freshAirUse) && !StringUtils.isNullOrEmpty(freshAirChange)) {
                //更换新风滤网
                if (Long.valueOf(freshAirUse) / 3600 > Long.valueOf(freshAirChange) && b1) {
                    isError = true;
                }
                BigDecimal a = new BigDecimal(Long.valueOf(freshAirUse) / 3600).divide(new BigDecimal(Long.valueOf(freshAirChange)),2, BigDecimal.ROUND_HALF_UP).setScale(2, BigDecimal.ROUND_DOWN);
                int b = a.multiply(new BigDecimal(100)).intValue();
                if(b >=100){
                    b = 100;
                }
                HDTopic hdTopic = MqttUploadManager.getInstance().getmHDTopic();
                hdTopic.setScreenStatus((byte)b);
            }
            if (!StringUtils.isNullOrEmpty(exhaustUse) && !StringUtils.isNullOrEmpty(exhaustChange)) {
                //更换排风滤网
                if (Long.valueOf(exhaustUse) / 3600 > Long.valueOf(exhaustChange) && b2) {
                    isError = true;
                }
            }
            if (!StringUtils.isNullOrEmpty(circle1Use) && !StringUtils.isNullOrEmpty(circle1Change)) {
                //更换内循环1滤网
                if (Long.valueOf(circle1Use) / 3600 > Long.valueOf(circle1Change)&& b3) {
                    isError = true;
                }
            }
            if (!StringUtils.isNullOrEmpty(circle2Use) && !StringUtils.isNullOrEmpty(circle2Change)) {
                //更换内循环2滤网
                if (Long.valueOf(circle2Use) / 3600 > Long.valueOf(circle2Change) && b4) {
                    isError = true;
                }
            }
        }
        return  isError;
    }
}
