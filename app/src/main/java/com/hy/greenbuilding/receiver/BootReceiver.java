package com.hy.greenbuilding.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by 18229736612@163.com on 2018/9/28.
 */

public class BootReceiver extends BroadcastReceiver {
    private static final String ACTION_BOOT = "android.intent.action.BOOT_COMPLETED";
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals(ACTION_BOOT)) {
            Intent toIntent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
            context.startActivity(toIntent);
        }
    }
}
