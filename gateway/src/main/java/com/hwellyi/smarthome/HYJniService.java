package com.hwellyi.smarthome;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class HYJniService {
    public static int SUB_DEVICE_TYPE_LIGHT = 1;
    public static int SUB_DEVICE_TYPE_DIMMER = 3;
    public static int SUB_DEVICE_TYPE_CURTAIN = 4;
    public static int SUB_DEVICE_TYPE_SWITCH = 5;
    public static int SUB_DEVICE_TYPE_GAS = 6;
    public static int SUB_DEVICE_TYPE_IR_REMOTE = 7;
    public static int SUB_DEVICE_TYPE_PIR = 8;
    public static int SUB_DEVICE_TYPE_SMOKE = 9;
    public static int SUB_DEVICE_TYPE_FLOOD = 10;
    public static int SUB_DEVICE_TYPE_DOOR_WINDOE = 11;
    public static int SUB_DEVICE_TYPE_ENV_DETECTOR = 12;
    public static int SUB_DEVICE_TYPE_WATER_LEAKAGE_DETECTOR = 13;
    public static int SUB_DEVICE_TYPE_GAS_ARM = 14;
    public static int SUB_DEVICE_TYPE_WATER_LEAKAGE_POINT = 15;
    public static int SUB_DEVICE_TYPE_CLOTHES_HANGER = 16;
    public static int SUB_DEVICE_TYPE_RS485_TRANSFER = 17;
    public static int SUB_DEVICE_TYPE_SOS = 18;

    public static int JNI_NOTIFY_UPDATE_DEVLIST = 1;
    public static int JNI_NOTIFY_UPDATE_DEVSTAUS = 1 << 1;
    public static int JNI_NOTIFY_UPDATE_DEVNAME = 1 << 2;
    public static int JNI_NOTIFY_UPDATE_SCENELIST = 1 << 3;
    public static int JNI_NOTIFY_UPDATE_SCENESTATUS = 1 << 4;
    public static int JNI_NOTIFY_UPDATE_SCENENAME = 1 << 5;
    public static int JNI_NOTIFY_UPDATE_ROOMLIST = 1 << 6;
    public static int JNI_NOTIFY_UPDATE_ROOMNAME = 1 << 7;
    public static int JNI_NOTIFY_NET_STATUS = 1 << 8;
    public static int JNI_NOTIFY_ALARM = 1 << 9;
    public static int JNI_NOTIFY_TEST = 1 << 10;

    static {
        System.loadLibrary("protobuf_lite");
        System.loadLibrary("sqlite3");
        System.loadLibrary("CWinobleLib");
    }

    public native void onPrintLogToJni(String logchars);//打印Log

    /*
   flag:当前模式:true=debug false=release
   s1name:主模块的串口名称
   s1baud:主模块的串口波特率
   s2name:从模块的串口名称
   s2baud:从模块的串口波特率
    */
    public native void onHYJniInit(boolean flag, String s1name, int s1baud, String s2name, int s2baud);//初始化JNI

    public native boolean onGetNetWorkStatus();//得到当前网络是否连接

    /*
    flag:输入要获取的设备类型 比如:SUB_DEVICE_TYPE_LIGHT | SUB_DEVICE_TYPE_DIMMER | SUB_DEVICE_TYPE_CURTAIN
    返回:json 字符串
     */
    public native boolean onSetDeviceStatus(int devid, int subid, int status);//设置设备状态

    public native void onCheckAlarmStatus();//检查当前报警状态 如果有报警，会通过通知发上来

    public native String onGetRoomList();//得到房间列表

    public native String onGetSceneList();//得到场景列表

    public native boolean onSetSceneGWHidden(long sceneid, int value);//设置场景网关是否显示属性

    public native String onGetDeviceList(int flag);//得到设备列表

    public native String onGetDeviceTypeInfo(int devid, int type);//得到设备列表

    public native void onSetSceneStatus(long sceneid);

    /*
    devid 设备ID 通知时由jni输入
    type 1=解除报警 2=撤防/取消
     */
    public native void onDisAlarmInfo(int devid, int type);

    public native String onGetSerial();//得到网关序列号

    public native String onGetToken();//得到网关登陆的token

    public native String onGetServerIP();//得到服务器IP

    public native String onGetZigbeeNetInfo();//得到zigbee网络信息

    public native void onRegisterNotifyFlag(int flag);//注册设备通知

    public native void onJYJniReRegisterEnvInfo();//重新注册jni通知相关的信息

    boolean mIsMultiProgress = false;
    private DatagramSocket mUDPChannel;

    public HYJniService(boolean flag) {
        mIsMultiProgress = flag;
        if (mIsMultiProgress) {
            //代表是多进程
            try {
                mUDPChannel = new DatagramSocket();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Log.i("Log_View","HYJniService----");
        //注册通知
        onRegisterNotifyFlag(JNI_NOTIFY_UPDATE_DEVLIST | JNI_NOTIFY_TEST | JNI_NOTIFY_UPDATE_DEVSTAUS | JNI_NOTIFY_UPDATE_DEVNAME |
                JNI_NOTIFY_NET_STATUS | JNI_NOTIFY_ALARM | JNI_NOTIFY_UPDATE_SCENELIST | JNI_NOTIFY_UPDATE_SCENENAME);
        onHYJniInit(PublicUse.isDebug, "/dev/ttyAS2", 115200, "", 0);
    }

    /*
    nid: 通知的类型，如JNI_NOTIFY_*定义
    ncmd:命令ID
    value:命令long参数
    strvalue:命令str参数
    返回:无
     */
    public boolean onJniNotificationCB(int tnotifyid, long tlcmd1, long tlcmd2, long tlvalue, String tstrvalue) {
        if (mIsMultiProgress) {
            //多进程使用UDP通知主线程
            JSONObject sendJson = new JSONObject();
            try {
                sendJson.put("notifyid", tnotifyid);
                sendJson.put("lcmd1", tlcmd1);
                sendJson.put("lcmd2", tlcmd2);
                sendJson.put("lvalue", tlvalue);
                sendJson.put("strvalue", tstrvalue);
                onPrintLogToJni("Java UDP S:" + sendJson.toString());
                //send
                new mUDPClientSendThread("127.0.0.1", PublicUse.JniPort, sendJson.toString()).start();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            // 修复点1：增加mainActivity非空校验
            if (PublicUse.mainActivity == null) {
                Log.e("HYJniService", "onJniNotificationCB: mainActivity is null, skip notify! tnotifyid=" + tnotifyid);
                return false; // 或return true，根据业务场景选择
            }
            try {
                PublicUse.mainActivity.onJniNotifyCB(tnotifyid, tlcmd1, tlcmd2, tlvalue, tstrvalue);
            } catch (Exception e) {
                Log.e("HYJniService", "onJniNotificationCB: invoke onJniNotifyCB failed", e);
            }
        }
        return true;
    }

    private class mUDPClientSendThread extends Thread {
        String ip;
        int port;
        String json;

        public mUDPClientSendThread(String tempip, int tempport, String tempjson) {
            ip = tempip;
            port = tempport;
            json = tempjson;
        }

        @Override
        public void run() {
            if (json == null) return;
            byte[] sendData = json.getBytes();
            try {
                InetAddress serviceaddr = InetAddress.getByName(ip);
                DatagramPacket packet = new DatagramPacket(sendData, sendData.length, serviceaddr, port);
                if (mUDPChannel != null) {
                    mUDPChannel.send(packet);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
