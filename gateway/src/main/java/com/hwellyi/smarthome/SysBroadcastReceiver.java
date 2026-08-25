package com.hwellyi.smarthome;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


/**
 * Created by xia_w on 2017/10/13.
 */

public class SysBroadcastReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()))
        {
            PublicUse.onPrintLogToJni("网关开机了~~");

        }
        else if (Intent.ACTION_USER_PRESENT.equals(intent.getAction()))
        {

        }
    }
}
