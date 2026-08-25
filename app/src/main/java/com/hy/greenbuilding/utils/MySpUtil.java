package com.hy.greenbuilding.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.hy.greenbuilding.config.SaveAddress;
import com.hy.greenbuilding.config.SaveControlInfo;
import com.hy.greenbuilding.config.SaveFanInfo;
import com.hy.greenbuilding.config.SaveFilterScreen;
import com.hy.greenbuilding.config.SaveTimingInfo;

/**
 * 保存数据工具类
 */
public class MySpUtil {

    public static String ADDRESS_NAME = "address";//设备安装地址
    public static String MAIN_CONTROL_STATUS = "control_status";//主控板状态
    public static String FAN_INIT = "fan_init";//风机参考值
    public static String FAN_DATA = "fan_data";//风机数据
    public static String TIMING_SET = "timing";//定时数据
    public static String TIMING_STATUS = "timing_status";//定时开启状态,断电保存
    public static String NTC_DATA = "ntc";//NTC数据
    public static String FAN_RESET = "fan_reset";//风机出厂配置
    public static String FAN_COUNT = "count";//风机数量
    public static String ROOM_DATA = "room";//房间数据
    public static String SERIAI_ID = "roomId";//新增房间ID
    public static String TEST_DATA  = "test_data";//测试工况
    public static String FILTER_SCREEN_DATA  = "filter_screen";//滤网
    public static String DC_FAN_DATA  = "dc_fan";//DC风机
    public static String PID_TEMP = "pid_temp";//PID设置温度
    public static String CARE_MODE  = "care_mode";//关怀模式
    public static String CO2_PM_DATA  = "co2_pm";//co2和pm2.5设置
    public static String HUMIDITY_SWITCH = "humidity_status";//除湿开关
    public static String TEMP_SWITCH = "temp_switch";//温控
    public static String OTA_STATUS = "ota_status";//启用 false/禁用 true
    public static String MANUAL_Mode_STATUS = "manualMode";//启用 false/禁用 true
    public static String RUN_Mode_STATUS = "RunMode";//启用 false/禁用 true
    public static String CLOSE_STATUS = "close";//启用 false/禁用 true

    public static final String KEY_EXPANSION_OPEN = "expansion_open_value"; // expansionOpenEt的值
    public static final String KEY_RADIO_GROUP1_CHECKED = "radio_group1_checked_id"; // radioGroup1选中ID
    public static final String KEY_PID_DEHUMIDIFY = "pid_dehumidify_value"; // pidValueDehumidify的值
    public static final String KEY_RADIO_GROUP2_CHECKED = "radio_group2_checked_id"; // radioGroup2选中ID
    public static final String WIND_STATUS = "windStatus"; // windStatus
    public static final String CIRCLE_STATUS = "circleStatus"; // circleStatus
    /**
     *
     * 保存在手机里面的文件名
     */
    private static final String FILE_NAME = "share_date";

    /**
     * 保存数据的方法，我们需要拿到保存数据的具体类型，然后根据类型调用不同的保存方法
     * @param context
     * @param key
     * @param object
     */
    public static void setParam(Context context, String key, Object object){

        String type = object.getClass().getSimpleName();
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        if("String".equals(type)){
            editor.putString(key, (String)object);
        }
        else if("Integer".equals(type)){
            editor.putInt(key, (Integer)object);
        }
        else if("Boolean".equals(type)){
            editor.putBoolean(key, (Boolean)object);
        }
        else if("Float".equals(type)){
            editor.putFloat(key, (Float)object);
        }
        else if("Long".equals(type)){
            editor.putLong(key, (Long)object);
        }
        editor.commit();
    }

    /**
     * 得到保存数据的方法，我们根据默认值得到保存的数据的具体类型，然后调用相对于的方法获取值
     * @param context
     * @param key
     * @param defaultObject
     * @return
     */
    public static Object getParam(Context context , String key, Object defaultObject){
        if(context == null){
          return "";
        }
        String type = defaultObject.getClass().getSimpleName();
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        if("String".equals(type)){
            return sp.getString(key, (String)defaultObject);
        }
        else if("Integer".equals(type)){
            return sp.getInt(key, (Integer)defaultObject);
        }
        else if("Boolean".equals(type)){
            return sp.getBoolean(key, (Boolean)defaultObject);
        }
        else if("Float".equals(type)){
            return sp.getFloat(key, (Float)defaultObject);
        }
        else if("Long".equals(type)){
            return sp.getLong(key, (Long)defaultObject);
        }
        return "";
    }

    /**
     * 清除所有数据
     * @param context
     */
    public static void clear(Context context) {
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.clear().commit();
    }

    /**
     * 清除指定数据
     * @param context
     */
    public static void clearAll(Context context) {
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.remove("定义的键名");
        editor.commit();
    }

    /**
     * 获取保存的位置
     * @return
     */
    public static SaveAddress getAddress(Context context){
        SaveAddress address;
        String json = MySpUtil.getParam(context, MySpUtil.ADDRESS_NAME,"").toString();
        if(StringUtils.isNullOrEmpty(json)){
            address = new SaveAddress();
        }else{
            address = new Gson().fromJson(json,SaveAddress.class);
        }
        return address;
    }

    /**
     * 获取保存的主控板数据
     * @return
     */
    public static  SaveControlInfo getControlData(Context context){
        SaveControlInfo controlInfo;
        String json = MySpUtil.getParam(context, MySpUtil.MAIN_CONTROL_STATUS,"").toString();
        if(StringUtils.isNullOrEmpty(json)){
            controlInfo = new SaveControlInfo();
        }else{
            controlInfo = new Gson().fromJson(json,SaveControlInfo.class);
        }
        return controlInfo;
    }

    /**
     * 获取定时时间
     * @return
     */
    public static SaveTimingInfo getTimingData(Context context){
        SaveTimingInfo saveTimingInfo;
        String json = MySpUtil.getParam(context, MySpUtil.TIMING_SET,"").toString();
        if(StringUtils.isNullOrEmpty(json)){
            saveTimingInfo = new SaveTimingInfo();
        }else{
            saveTimingInfo = new Gson().fromJson(json,SaveTimingInfo.class);
        }
        return saveTimingInfo;
    }



    /**
     * 获取滤网数据
     * @return
     */
    public static SaveFilterScreen getFilterScreen(Context context){
        SaveFilterScreen filterScreen;
        String json = MySpUtil.getParam(context,MySpUtil.FILTER_SCREEN_DATA,"").toString();
        if(StringUtils.isNullOrEmpty(json)){
            filterScreen = new SaveFilterScreen();
        }else{
            filterScreen = new Gson().fromJson(json,SaveFilterScreen.class);
        }
        return filterScreen;
    }

    /**
     * 风量设置成功，保存数据到本地
     */
    public static void saveValueToLocal(Context context,int type, String smallValue, String middleValue, String highValue) {
        Object object = MySpUtil.getParam(context, MySpUtil.FAN_DATA, "");
        if (object != null) {
            String json = object.toString();
            SaveFanInfo saveFanInfo;
            if (!StringUtils.isNullOrEmpty(json)) {
                saveFanInfo = new Gson().fromJson(json, SaveFanInfo.class);
            } else {
                saveFanInfo = new SaveFanInfo();
            }
            if (type == 1) {
                saveFanInfo.setWind1Small(smallValue);
                saveFanInfo.setWind1Middle(middleValue);
                saveFanInfo.setWind1High(highValue);
            } else if (type == 2) {
                saveFanInfo.setWind2Small(smallValue);
                saveFanInfo.setWind2Middle(middleValue);
                saveFanInfo.setWind2High(highValue);
            } else if (type == 3) {
                saveFanInfo.setCircle1Small(smallValue);
                saveFanInfo.setCircle1Middle(middleValue);
                saveFanInfo.setCircle1High(highValue);
            } else if (type == 4) {
                saveFanInfo.setCircle2Small(smallValue);
                saveFanInfo.setCircle2Middle(middleValue);
                saveFanInfo.setCircle2High(highValue);
            }
            MySpUtil.setParam(context, MySpUtil.FAN_DATA, new Gson().toJson(saveFanInfo));
        }
    }

    /**
     * 获取保存的城市
     * @param context
     * @return
     */
    public static String getCity(Context context){
        SaveAddress saveAddress = MySpUtil.getAddress(context);
        String city = "深圳";
        if (saveAddress != null && !StringUtils.isNullOrEmpty(saveAddress.getCityName())) {
            city = saveAddress.getCityName();
        }
        return city;
    }
}
