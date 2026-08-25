package com.hy.greenbuilding;

import android.app.Application;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import com.hy.greenbuilding.protocol.SpDataProcessor;
import com.hy.greenbuilding.utils.Hex;
import com.hy.greenbuilding.utils.OtherExceptionsHandler;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.DiskLogAdapter;
import com.orhanobut.logger.Logger;
import com.tencent.bugly.crashreport.CrashReport;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;


public class HyApplication extends Application {
    private boolean DEBUG = false;
    private static boolean isFlu;
    private static byte[] ntcError;//NTC故障
    private static boolean isReboot;
    private static BigDecimal outTemp = new BigDecimal(0);
    public static boolean isCare;
    public static String controlVersion = "";

    public static int screenH;
    public static int screenW;
    public static boolean isTopShowing = false;
    public static boolean isLocking;//锁定状态

    // 创建一个最大容量为 1000 的 Map，超过 1000 会自动删掉最老的一条
    private static final Map<Integer, Object> stateMap = Collections.synchronizedMap(
            new LinkedHashMap<Integer, Object>(128, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<Integer, Object> eldest) {
                    // 当数量超过 1000 时，返回 true，内部就会自动执行移除操作
                    return size() > 1000;
                }
            }
    );

    @Override
    public void onCreate() {
        super.onCreate();
        if (DEBUG) {
            Logger.addLogAdapter(new AndroidLogAdapter());
        } else {
            Logger.addLogAdapter(new DiskLogAdapter());
        }

        SpDataProcessor.getInstance().init();
        OtherExceptionsHandler.getInstance().init(this);

        screenH = getWindowPixle(this)[1];
        screenW = getWindowPixle(this)[0];


        // 👇 这一行初始化 Bugly
        CrashReport.initCrashReport(getApplicationContext(), "1ba1bdfb7c", false);
    }

    public static void setForceFlu(boolean flu) {
        isFlu = flu;
    }

    public static boolean isForceFlu() {
        return isFlu;
    }

    public static void setNtcError(byte[] bytes) {
        ntcError = bytes;
    }

    public static byte[] getNtcError() {
        return ntcError;
    }

    public static void setRoomError(byte[] bytes) {
        roomError = Hex.bytesToHexString(bytes);
    }

    public static String getRoomError() {
        return roomError;
    }

    private static String roomError = "0000";//环境检测故障

    public static boolean isIsReboot() {
        return isReboot;
    }

    public static void setIsReboot(boolean reBoot) {
        isReboot = reBoot;
    }

    public static void setOutTemp(BigDecimal temp) {
        Log.e("TAG", "setOutTemp: "+temp);
        outTemp = temp;
    }

    public static BigDecimal getOutTemp() {
        return outTemp;
    }
    public static String getControlVersion() {
        return controlVersion;
    }

    public static void setControlVersion(String controlVersion) {
        HyApplication.controlVersion = controlVersion;
    }


    /**
     * 获取手机长宽像素
     *
     * @return int[0] = 宽 int[1] = 高
     */
    public static int[] getWindowPixle(Context context) {
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        manager.getDefaultDisplay().getMetrics(metrics);
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;
        return new int[]{width, height};
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    // 必须加锁，确保同一时间只有一个线程能操作
    public static synchronized void putState(Integer key, Object value) {
        stateMap.put(key, value);
        Log.e("TAG", "putState: "+stateMap.size());
    }

    /**
     * 检查 Key 是否存在，如果有则取出并清除，没有则返回 null
     */
    public static synchronized Object getAndRemoveState(int key) {
        // remove 方法会返回被删除的值
        return stateMap.remove(key);
    }
}
