package com.hy.greenbuilding.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;


import androidx.annotation.RequiresApi;

import com.orhanobut.logger.Logger;


public class NetworkStatus {

    public static boolean checkState(Context context){
        if(context==null){
            return false;
        }
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP){
            return checkState_23orNew(context);
        }
        return checkState_23(context);

    }
    //API版本23以下时调用此方法进行检测
    //因为API23后getNetworkInfo(int networkType)方法被弃用
    private static boolean checkState_23(Context context) {
        //步骤1：通过Context.getSystemService(Context.CONNECTIVITY_SERVICE)获得ConnectivityManager对象
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        //步骤2：获取ConnectivityManager对象对应的NetworkInfo对象
        //NetworkInfo对象包含网络连接的所有信息
        //步骤3：根据需要取出网络连接信息
        //获取WIFI连接的信息
        try {
            NetworkInfo networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            Boolean isWifiConn = networkInfo.isConnected();
            //获取移动数据连接的信息
            networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            Boolean isMobileConn = networkInfo.isConnected();
            return isMobileConn || isWifiConn;
        }catch (Exception e){
            Logger.e(e.getMessage());
            return false;
        }

    }

     // API 23及以上时调用此方法进行网络的检测
    // getAllNetworks() 在API 21后开始使用
    //步骤非常类似
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static boolean checkState_23orNew(Context context) {
        //获得ConnectivityManager对象
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        try {
            //获取所有网络连接的信息
            Network[] networks = connMgr.getAllNetworks();
            //用于存放网络连接信息
            StringBuilder sb = new StringBuilder();
            //通过循环将网络信息逐个取出来
            boolean connected = false;
            for (int i = 0; i < networks.length; i++) {
                //获取ConnectivityManager对象对应的NetworkInfo对象
                NetworkInfo networkInfo = connMgr.getNetworkInfo(networks[i]);
                connected = networkInfo.isConnected();
                if (connected) {
                    return true;
                }
            }
        }catch (Exception e){
            Logger.e(e.getMessage());
        }

        return false;
    }

    /**
     * 将ip的整数形式转换成ip形式
     *
     * @param ipInt
     * @return
     */
    public static String int2ip(int ipInt) {
        StringBuilder sb = new StringBuilder();
        sb.append(ipInt & 0xFF).append(".");
        sb.append((ipInt >> 8) & 0xFF).append(".");
        sb.append((ipInt >> 16) & 0xFF).append(".");
        sb.append((ipInt >> 24) & 0xFF);
        return sb.toString();
    }

    /**
     * 获取当前ip地址
     *
     * @param context
     * @return
     */
    public static String getLocalIpAddress(Context context) {
        try {
            WifiManager wifiManager = (WifiManager) context
                    .getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            int i = wifiInfo.getIpAddress();
            return int2ip(i);
        } catch (Exception ex) {
            return "";
        }
        // return null;
    }

    // 网络状态监听回调（替代轮询）
    public interface OnNetworkStateChangedListener {
        void onNetworkConnected(boolean isConnected);
    }

    private static ConnectivityManager.NetworkCallback networkCallback;

    /**
     * 注册网络状态监听（仅在状态变化时回调）
     */
    public static void registerNetworkListener(Context context, OnNetworkStateChangedListener listener) {
        if (context == null || listener == null) return;
        ConnectivityManager connMgr = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connMgr == null) return;

        // 取消原有监听（避免重复注册）
        unregisterNetworkListener(context);

        // 创建网络监听回调
        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                super.onAvailable(network);
                listener.onNetworkConnected(checkState(context));
            }

            @Override
            public void onLost(Network network) {
                super.onLost(network);
                // 二次确认是否真的断网（避免临时波动）
                listener.onNetworkConnected(checkState(context));
            }
        };

        // 注册监听（监听WiFi/移动数据）
        NetworkRequest networkRequest = new NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .build();
        connMgr.registerNetworkCallback(networkRequest, networkCallback);
    }


    /**
     * 取消网络状态监听（页面销毁时调用）
     */
    public static void unregisterNetworkListener(Context context) {
        if (networkCallback == null || context == null) return;
        ConnectivityManager connMgr = (ConnectivityManager) context.getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connMgr != null) {
            connMgr.unregisterNetworkCallback(networkCallback);
            networkCallback = null;
        }
    }
}
