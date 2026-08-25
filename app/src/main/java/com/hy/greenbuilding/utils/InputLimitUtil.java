package com.hy.greenbuilding.utils;

import android.widget.RadioButton;

import java.math.BigDecimal;

public class InputLimitUtil {
    public static final int DEHUMIDIFY_MIN = 45;
    public static final int DEHUMIDIFY_MAX = 99;
    public static final int HUMIDIFY_MIN = 35;
    public static final int HUMIDIFY_MAX = 80;

    /**
     * 温度上下限判断
     * @param tempMin
     * @param tempMax
     * @return
     */
    public static boolean tempLimit(BigDecimal tempMin,BigDecimal tempMax){
        if(tempMin.compareTo(new BigDecimal(160)) < 0){
            return false;
        }
        if(tempMax.compareTo(new BigDecimal(300)) > 0){
            return false;
        }
        if(tempMax.compareTo(tempMin) <= 0){
            return false;
        }
        if(tempMax.subtract(tempMin).compareTo(new BigDecimal(30)) < 0){
            return false;
        }
        return true;
    }

    /**
     * 湿度输入验证 45-100
     * @param humidity
     * @return
     */
    public static boolean humidityLimit(BigDecimal humidity){
        return dehumidifyLimit(humidity);
    }

    public static boolean dehumidifyLimit(BigDecimal humidity){
        return humidityRangeLimit(humidity, DEHUMIDIFY_MIN, DEHUMIDIFY_MAX);
    }

    public static boolean humidifyLimit(BigDecimal humidity){
        return humidityRangeLimit(humidity, HUMIDIFY_MIN, HUMIDIFY_MAX);
    }

    private static boolean humidityRangeLimit(BigDecimal humidity, int min, int max){
        if(humidity == null){
            return false;
        }
        if(humidity.compareTo(new BigDecimal(min)) < 0){
            return false;
        }
        if(humidity.compareTo(new BigDecimal(max)) > 0){
            return false;
        }
        return true;
    }
    //定时限制
    public static boolean timingLimit(String before,String after){
        if (!StringUtils.isNullOrEmpty(before) && !StringUtils.isNullOrEmpty(after)) {
            if (Integer.parseInt(before) >= 24 || Integer.parseInt(after) > 24
                    || Integer.parseInt(before) >= Integer.parseInt(after)) {
                return false;
            }
        }
       return true;
    }

    /**
     * 风机调试输入限制
     * @param small
     * @param middle
     * @param high
     */
    public static boolean inputLimit(RadioButton rb_wind_pwm, String small, String middle, String high){

        boolean isInput = false;
        if(!StringUtils.isNullOrEmpty(small)){
            if(small.startsWith("0")){
                return false;
            }
            if(rb_wind_pwm.isChecked()){
                if(Integer.parseInt(small) > 100){
                    return false;
                }
            }else{
                if(Integer.parseInt(small) > 999){
                    return false;
                }
            }
            isInput = true;
        }
        if(!StringUtils.isNullOrEmpty(middle)){
            if(middle.startsWith("0")){
                return false;
            }
            if(rb_wind_pwm.isChecked()){
                if(Integer.parseInt(middle) > 100){
                    return false;
                }
            }else{
                if(Integer.parseInt(middle) > 999){
                    return false;
                }
            }
            isInput = true;
        }
        if(!StringUtils.isNullOrEmpty(high)){
            if(high.startsWith("0")){
                return false;
            }
            if(rb_wind_pwm.isChecked()){
                if(Integer.parseInt(high) > 100){
                    return false;
                }
            }else{
                if(Integer.parseInt(high) > 999){
                    return false;
                }
            }
            isInput = true;
        }
        if(!isInput){
            return false;
        }
        if(!StringUtils.isNullOrEmpty(small) && !StringUtils.isNullOrEmpty(middle)){
            if(Integer.parseInt(small)>= Integer.parseInt(middle)){
                return false;
            }
        }
        if(!StringUtils.isNullOrEmpty(middle) && !StringUtils.isNullOrEmpty(high)){
            if(Integer.parseInt(middle)>= Integer.parseInt(high)){
                return false;
            }
        }
        return true;
    }

   //主膨胀阀限制
    public static boolean mainExpansionLimit(String mainExpansion){
        if(!StringUtils.isNullOrEmpty(mainExpansion)){
            if(Integer.parseInt(mainExpansion) > 240 && Integer.parseInt(mainExpansion) != 254){
                return false;
            }

        }
        return true;
    }
    //辅膨胀阀限制
    public static boolean auxExpansionLimit(String auxExpansion){
        if(!StringUtils.isNullOrEmpty(auxExpansion)){
            if(Integer.parseInt(auxExpansion) > 240 && Integer.parseInt(auxExpansion) != 254){
                return false;
            }
        }
        return true;
    }
    //按钮重复点击判断
    private static long lastClickTime;
    public static boolean isFastDoubleClick() {
        long time = System.currentTimeMillis();
        if ( time - lastClickTime < 1000) {
            return true;
        }
        lastClickTime = time;
        return false;
    }

    /**
     * PID输入限制
     * @return
     */
    public static boolean pidInputLimit(String pValue,String iValue,String dValue,String time,String min){
        if(StringUtils.isNullOrEmpty(pValue) || StringUtils.isNullOrEmpty(iValue) ||StringUtils.isNullOrEmpty(dValue)
                ||StringUtils.isNullOrEmpty(time)||StringUtils.isNullOrEmpty(min)){
            return false;
        }
        if(Integer.parseInt(pValue)> 65535 || Integer.parseInt(iValue)> 65535
                ||Integer.parseInt(dValue)> 65535 || Integer.parseInt(min)> 100){
            return false;
        }
        return true;
    }

    //首字母为0判断
    public static boolean startWithZero(String input){
        if(!StringUtils.isNullOrEmpty(input) && input.startsWith("0")){
            return true;
        }
        return false;
    }
}
