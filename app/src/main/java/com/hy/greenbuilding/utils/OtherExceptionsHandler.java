package com.hy.greenbuilding.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.hy.greenbuilding.mqtt.MyMqttService;

import java.io.PrintWriter;
import java.io.StringWriter;

public class OtherExceptionsHandler  implements Thread.UncaughtExceptionHandler {
    private static Context mAppContext; //上下文对象

    private static OtherExceptionsHandler instance;  //单例引用，这里我们做成单例的，因为我们一个应用程序里面只需要一个UncaughtExceptionHandler实例

    private OtherExceptionsHandler(){}

    public synchronized static OtherExceptionsHandler getInstance(){  //同步方法，以免单例多线程环境下出现异常

        if (instance == null){
            instance = new OtherExceptionsHandler();

        }

        return instance;

    }

    /**需要上下文对象的初始化*/

    public synchronized OtherExceptionsHandler init(Context context){  //初始化，把当前对象设置成UncaughtExceptionHandler处理器

        Thread.setDefaultUncaughtExceptionHandler(this);

        mAppContext = context.getApplicationContext();

        return instance;

    }

    public synchronized OtherExceptionsHandler init(){  //初始化，把当前对象设置成UncaughtExceptionHandler处理器

        Thread.setDefaultUncaughtExceptionHandler(this);

        return instance;

    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        MyMqttService.logsReport(ex.toString());
//        if(mAppContext != null){
//            otherException(thread, ex, mAppContext);
//
//        }
//        otherException(thread, ex);
//        Intent intent1 = new Intent(mAppContext, MainActivity.class);
//        intent1.setAction(Intent.ACTION_MAIN);
//        intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        intent1.addCategory(Intent.CATEGORY_LAUNCHER);
//        mAppContext.startActivity(intent1);
        restartApplication();
    }
    private void restartApplication() {
        Intent intent = mAppContext.getPackageManager().getLaunchIntentForPackage(mAppContext.getPackageName());
        PendingIntent pendingIntent = PendingIntent.getActivity(mAppContext, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmManager = (AlarmManager)mAppContext. getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, pendingIntent);
        System.exit(2);
    }
    /**

     * 处理异常的一般情况

     * @param thread

     * @param ex

     */

    public void otherException(Thread thread, Throwable ex){
        StringWriter sw = new StringWriter();

        ex.printStackTrace(new PrintWriter(sw, true));

        Log.e("uncaughtException", "thread: " + thread

                + " name: " + thread.getName()

                + " id: " + thread.getId()

                + "exception: " + Log.getStackTraceString(ex));

        System.exit(0);

    }

    /**

     * 处理异常的特殊情况，一般是需要用到上下文对象context

     * @param thread

     * @param ex

     * @param context

     */

    public void otherException(Thread thread, Throwable ex, Context context){
    }

}
