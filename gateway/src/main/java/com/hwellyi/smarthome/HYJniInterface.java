package com.hwellyi.smarthome;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.util.Log;

public class HYJniInterface extends Service
{
    HYJniService mJniService = null;
    @Override
    public void onCreate()
    {
        mJniService = new HYJniService(true);
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        PublicUse.JniPort = intent.getIntExtra("port", 0);
        mJniService.onJYJniReRegisterEnvInfo();
        return super.onStartCommand(intent, flags, startId);
    }

    //定义内部类MyRemoteServiceImpl,继承我们的AIDL文件自动生成的内部类，
    //并且实现我们AIDL文件定义的接口方法
    private IBinder mJniAIDLInterface = new IMyAidlInterface.Stub()
    {
        public void onPrintLogToJni(String logstr)
        {
            mJniService.onPrintLogToJni(logstr);
        }
        public boolean onGetNetWorkStatus()//得到当前网络是否连接
        {
            return mJniService.onGetNetWorkStatus();
        }
        public String onGetSerial()//得到网关序列号
        {
            return mJniService.onGetSerial();
        }
        public String onGetToken()//得到服务器token
        {
            return mJniService.onGetToken();
        }
        public String onGetServerIP()//得到服务器IP
        {
            return mJniService.onGetServerIP();
        }
        public String onGetZigbeeNetInfo()//得到zigbee网络信息
        {
            return mJniService.onGetZigbeeNetInfo();
        }
        public boolean onSetSceneGWHidden(long sceneid, int value)//设置场景网关是否显示属性
        {
            return mJniService.onSetSceneGWHidden(sceneid, value);
        }
        public void onDisAlarmInfo(int devid, int type)//确认安防报警
        {
            mJniService.onDisAlarmInfo(devid, type);
        }
        public void onSetSceneStatus(long sceneid)
        {
            mJniService.onSetSceneStatus(sceneid);
        }
        public String onGetDeviceList(int flag)//得到zigbee网络信息
        {
            return mJniService.onGetDeviceList(flag);
        }
        public String onGetDeviceTypeInfo(int devid,int type)//得到设备列表
        {
            return mJniService.onGetDeviceTypeInfo(devid,type);
        }
        public String onGetRoomList()//得到房间列表
        {
            return mJniService.onGetRoomList();
        }
        public String onGetSceneList()//得到场景列表
        {
            return mJniService.onGetSceneList();
        }
        public void onCheckAlarmStatus()//检查当前报警状态 如果有报警，会通过通知发上来
        {
            mJniService.onCheckAlarmStatus();
        }
        public boolean onSetDeviceStatus(int devid, int subid, int status)
        {
            return mJniService.onSetDeviceStatus(devid, subid, status);
        }
        public void onJYJniReRegisterEnvInfo()
        {
            mJniService.onJYJniReRegisterEnvInfo();
        }
    };

    @Override
    public IBinder onBind(Intent arg0)
    {
        Bundle b = arg0.getExtras();
        PublicUse.JniPort = b.getInt("port");
        //返回AIDL实现
        return mJniAIDLInterface;
    }

    @Override
    public void onDestroy()
    {
        PublicUse.onPrintLogToJni("Release MyService");
        super.onDestroy();
    }

}
