package com.hy.greenbuilding.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.hy.greenbuilding.R;

public class AssistService extends Service {
    private static final String TAG = "aaa";

    public class LocalBinder extends Binder {
        public AssistService getService() {
            return AssistService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "Assist1Service: onBind()");
        return new LocalBinder();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String ID = "com.hy.greenbuilding";	//这里的id里面输入自己的项目的包的路径
        String NAME = "LEFTBAR";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.drawable.pv_icon);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            //修改安卓8.1以上系统报错
            NotificationChannel notificationChannel = new NotificationChannel(ID, NAME, NotificationManager.IMPORTANCE_MIN);
            notificationChannel.enableLights(false);//如果使用中的设备支持通知灯，则说明此通知通道是否应显示灯
            notificationChannel.setShowBadge(false);//是否显示角标
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_SECRET);
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            manager.createNotificationChannel(notificationChannel);
            builder.setChannelId(ID);
        }
        startForeground(1, builder.build());
        // 开启一条线程，去移除DaemonService弹出的通知
        new Thread(new Runnable() {
            @Override
            public void run() {
                SystemClock.sleep(1000);
                // 取消CancelNoticeService的前台
                stopForeground(true);
                // 移除DaemonService弹出的通知
                NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                manager.cancel(1);
                // 任务完成，终止自己
               stopSelf();

            }
        });

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
      //  stopForeground(true);
        Log.i(TAG, "Assist1Service: onDestroy()");
    }

}
