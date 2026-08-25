package com.hwellyi.smarthome;

import android.app.Activity;
import android.content.Context;

public class CommonModule {
    private static Context sAppContext;
    private static Activity sActivity;
    /**
     * 子模块和主模块需要共享全局上下文，故需要在app module初始化时传入
     */
    public static void init( Activity activity) {
        if(sActivity == null) {
          //  sAppContext = appContext.getApplicationContext();
            sActivity = activity;
        }
    }
    public static Context getAppContext() {
        return sAppContext;
    }
    public static Activity getAppActvity() {
        return sActivity;
    }
}
