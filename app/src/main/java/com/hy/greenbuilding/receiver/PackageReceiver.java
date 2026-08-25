package com.hy.greenbuilding.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.hy.greenbuilding.ui.activity.HomeActivity;


public class PackageReceiver extends BroadcastReceiver {
    private static final String ACTION_BOOT = "android.intent.action.BOOT_COMPLETED";
    @Override
    public void onReceive(Context context, Intent intent) {
        if ((intent.getAction().equals("android.intent.action.PACKAGE_ADDED") )) {
            Log.i("info","---action.PACKAGE_ADDED--");
        }
        if(intent.getAction().equals("android.intent.action.PACKAGE_REPLACED")){
            Log.i("info","---action.PACKAGE_REPLACED---");
            Intent intent1 = new Intent(context, HomeActivity.class);
            intent1.setAction(Intent.ACTION_MAIN);
            intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent1.addCategory(Intent.CATEGORY_LAUNCHER);
            context.startActivity(intent1);
        }
    }
}
