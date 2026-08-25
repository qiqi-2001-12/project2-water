package com.hy.greenbuilding.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.hy.greenbuilding.utils.NetworkStatus;
import com.orhanobut.logger.Logger;

public class NetworkChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        if(NetworkStatus.checkState(context)){
            Logger.d("network connect: ");
         //   MyMqttService.reConnect(true,true);
        }else{
            Logger.d("network disconnect: ");
            //MyMqttService.reConnect(true,false);
        }
    }
}
