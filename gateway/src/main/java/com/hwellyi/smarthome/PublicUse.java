package com.hwellyi.smarthome;

import android.content.Context;
import android.util.Log;

import androidx.fragment.app.Fragment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by xia_w on 2017/9/13.
 */

public class PublicUse {
    public static final String Tag = "Log_View";
    public static final int AnimationSnow = 0x11;
    public static final int AnimationSunny = 0x12;
    public static final int AnimationCloudy = 0x13;
    public static final int AnimationRain = 0x14;
    public static final int AnimationStart = 0x01;
    public static final int AnimationBackHome = 0x21;
    public static final int AnimationLeaveHome = 0x22;
    public static final int AnimationSleep = 0x23;
    public static MainGatewayActivity mainActivity = null;
    public static HYJniFunCB mJniFunCB = null;
    public static int JniPort = 36688;
    public static android.os.Handler mSettingHandler = null;
    public static final int SCENE_HOME = 100;
    public static final int SCENE_LEAVE = 101;
    public static final int SCENE_SLEEP = 102;//
    public static List<Fragment> mFragmentList = new ArrayList<>();
    public static Context GlobalcContext = null;
    public static boolean isDebug = true;
    public static boolean isDemoFlag = false;

    public static String onGetFormatMD5(String value) {
        String retMD5 = "";
        int tempCount = 32 - value.length();
        if (tempCount < 0) tempCount = 0;
        for (int i = 0; i < tempCount; i++) {
            retMD5 += "0";
        }
        retMD5 += value;

        return retMD5;
    }

    public static void onPrintLogToJni(String logstr)
    {
        if(PublicUse.mJniFunCB != null)
        {
            PublicUse.mJniFunCB.onPrintLogToJni(logstr);
        }
        else
        {
            Log.i(PublicUse.Tag, logstr);
        }
    }

    public static boolean onCheckFileIsExit(String name)
    {
        try
        {
            File winobeQrcode = new File(name);
            if(!winobeQrcode.exists())
            {
                return false;
            }

        }catch (Exception e) {
            // handle exception
            return false;
        }
        return true;
    }
}