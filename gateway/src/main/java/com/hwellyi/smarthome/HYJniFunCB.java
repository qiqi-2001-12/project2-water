package com.hwellyi.smarthome;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import static java.lang.Thread.sleep;

public class HYJniFunCB {
    private IMyAidlInterface jniInterface = null;
    private HYJniService debugJniService = null;
    NetWork netWork;

    public HYJniFunCB() {

        //捕获异常重启
        if (!PublicUse.isDebug) {
            ErrorCaught crashHandler = ErrorCaught.getInstance();
            crashHandler.init(PublicUse.mainActivity.getApplicationContext());
            netWork = new NetWork();//创建UDP网络
            //启动jni服务
            Intent intent = new Intent(PublicUse.mainActivity, HYJniInterface.class);
            Bundle bundle = new Bundle();
            bundle.putInt("port", PublicUse.JniPort);
            intent.putExtras(bundle);
            PublicUse.mainActivity.startService(intent);
            PublicUse.mainActivity.bindService(intent, conn, Context.BIND_AUTO_CREATE);
        } else {
            //直接调用jni 方便查看打印
            debugJniService = new HYJniService(false);
            debugJniService.onJYJniReRegisterEnvInfo();
        }
    }

    ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //返回AIDL接口对象，然后可以调用AIDL方法
            PublicUse.onPrintLogToJni("onServiceConnected");
            Log.i(PublicUse.Tag, "onServiceConnected");
            jniInterface = IMyAidlInterface.Stub.asInterface(service);
        }

        //        @Override
//        public void onServiceDisconnected(ComponentName name) {
//            PublicUse.onPrintLogToJni("onServiceDisconnected");
//            Log.i(PublicUse.Tag, "onServiceDisconnected");
//            try {
//                sleep(1000);//既然奔溃了，那延时一下启动吧
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            //重新启动jni服务
//            Intent intent = new Intent(PublicUse.mainActivity, HYJniInterface.class);
//            Bundle bundle = new Bundle();
//            bundle.putInt("port", PublicUse.JniPort);
//            intent.putExtras(bundle);
//            PublicUse.mainActivity.startService(intent);
//            PublicUse.mainActivity.bindService(intent, conn, Context.BIND_AUTO_CREATE);
//            PublicUse.onPrintLogToJni("onService Reconnect");
//        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    // 重新绑定的逻辑（注意切换到主线程）
                    PublicUse.mainActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(PublicUse.mainActivity, HYJniInterface.class);
                            Bundle bundle = new Bundle();
                            bundle.putInt("port", PublicUse.JniPort);
                            intent.putExtras(bundle);
                            PublicUse.mainActivity.startService(intent);
                            PublicUse.mainActivity.bindService(intent, conn, Context.BIND_AUTO_CREATE);
                        }
                    });
                }
            }).start();
        }
    };

    public String onGetJniSerial() {
        if (jniInterface != null) {
            try {
                return jniInterface.onGetSerial();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            if (debugJniService != null) {
                return debugJniService.onGetSerial();
            }
        }
        return "";
    }

    public void onPrintLogToJni(String logstr) {
        if (jniInterface != null) {
            try {
                jniInterface.onPrintLogToJni(logstr);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            if (debugJniService != null) {
                debugJniService.onPrintLogToJni(logstr);
            }
        }
    }

    public boolean onGetNetWorkStatus() {
        if (jniInterface != null) {
            try {
                return jniInterface.onGetNetWorkStatus();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            if (debugJniService != null) {
                return debugJniService.onGetNetWorkStatus();
            }
        }
        return false;
    }

    public String onGetJniZigbeeNetInfo() {
        if (jniInterface != null) {
            try {
                return jniInterface.onGetZigbeeNetInfo();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            if (debugJniService != null) {
                return debugJniService.onGetZigbeeNetInfo();
            }
        }
        return "";
    }

    public String onGetJniServerIP() {
        if (jniInterface != null) {
            try {
                return jniInterface.onGetServerIP();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            if (debugJniService != null) {
                return debugJniService.onGetServerIP();
            }
        }
        return "";
    }

    public boolean onSetSceneGWHidden(long sceneid, int value) {
        if (jniInterface != null) {
            try {
                return jniInterface.onSetSceneGWHidden(sceneid, value);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            if (debugJniService != null) {
                return debugJniService.onSetSceneGWHidden(sceneid, value);
            }
        }
        return true;
    }

    public String onGetJniToken() {
        if (jniInterface != null) {
            try {
                return jniInterface.onGetToken();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            if (debugJniService != null) {
                return debugJniService.onGetToken();
            }
        }
        return "";
    }

    public void onDisAlarmInfo(int devid, int type) {
        if (jniInterface != null) {
            try {
                jniInterface.onDisAlarmInfo(devid, type);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            if (debugJniService != null) {
                debugJniService.onDisAlarmInfo(devid, type);
            }
        }
    }

    public void onSetSceneStatus(long sceneid) {
        if (jniInterface != null) {
            try {
                jniInterface.onSetSceneStatus(sceneid);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            if (debugJniService != null) {
                debugJniService.onSetSceneStatus(sceneid);
            }
        }
    }

    public String onGetSceneList() {
        if (jniInterface != null) {
            try {
                return jniInterface.onGetSceneList();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            if (debugJniService != null) {
                return debugJniService.onGetSceneList();
            }
        }
        return "";
    }

    public String onGetDeviceList(int flag) {
        if (jniInterface != null) {
            try {
                return jniInterface.onGetDeviceList(flag);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            if (debugJniService != null) {
                return debugJniService.onGetDeviceList(flag);
            }
        }
        return "";
    }

    public String onGetDeviceTypeInfo(int devid, int type) {
        if (jniInterface != null) {
            try {
                return jniInterface.onGetDeviceTypeInfo(devid, type);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            if (debugJniService != null) {
                return debugJniService.onGetDeviceTypeInfo(devid, type);
            }
        }
        return "";
    }

    public String onGetRoomList() {
        if (jniInterface != null) {
            try {
                return jniInterface.onGetRoomList();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            if (debugJniService != null) {
                return debugJniService.onGetRoomList();
            }
        }
        return "";
    }

    public void onJYJniReRegisterEnvInfo() {
        if (jniInterface != null) {
            try {
                jniInterface.onJYJniReRegisterEnvInfo();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            if (debugJniService != null) {
                debugJniService.onJYJniReRegisterEnvInfo();
            }
        }
    }

    public void onCheckAlarmStatus()//检查当前报警状态 如果有报警，会通过通知发上来
    {
        if (jniInterface != null) {
            try {
                jniInterface.onCheckAlarmStatus();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            if (debugJniService != null) {
                debugJniService.onCheckAlarmStatus();
            }
        }
    }

    public boolean onSetDeviceStatus(int devid, int subid, int status)//设置设备状态
    {
        if (jniInterface != null) {
            try {
                return jniInterface.onSetDeviceStatus(devid, subid, status);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            if (debugJniService != null) {
                return debugJniService.onSetDeviceStatus(devid, subid, status);
            }
        }
        return true;
    }
}
