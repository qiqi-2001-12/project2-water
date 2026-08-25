// IMyAidlInterface.aidl
package com.hwellyi.smarthome;

// Declare any non-default types here with import statements

interface IMyAidlInterface
{
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void onPrintLogToJni(String logstr);
    boolean onGetNetWorkStatus();
    String onGetSerial();
    String onGetToken();
    String onGetServerIP();
    String onGetZigbeeNetInfo();
    void onDisAlarmInfo(int devid, int type);
    void onSetSceneStatus(long sceneid);
    String onGetDeviceList(int flag);
    boolean onSetSceneGWHidden(long sceneid, int value);
    String onGetRoomList();
    String onGetSceneList();
    void onCheckAlarmStatus();
    void onJYJniReRegisterEnvInfo();
    boolean onSetDeviceStatus(int devid, int subid, int status);
    String onGetDeviceTypeInfo(int devid, int type);
}
