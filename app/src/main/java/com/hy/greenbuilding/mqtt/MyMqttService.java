package com.hy.greenbuilding.mqtt;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.gson.Gson;
import com.hy.greenbuilding.HyApplication;
import com.hy.greenbuilding.R;
import com.hy.greenbuilding.config.SaveAddress;
import com.hy.greenbuilding.config.SaveFilterScreen;
import com.hy.greenbuilding.config.SaveTimingInfo;
import com.hy.greenbuilding.model.LogsReportUploadInfo;
import com.hy.greenbuilding.model.MqttResponseInfo;
import com.hy.greenbuilding.model.MqttStatusResponseInfo;
import com.hy.greenbuilding.service.DownloadUtil;
import com.hy.greenbuilding.utils.ByteUtils;
import com.hy.greenbuilding.utils.MySpUtil;
import com.hy.greenbuilding.utils.NetworkStatus;
import com.hy.greenbuilding.utils.PackageUtil;
import com.hy.greenbuilding.utils.StringUtils;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Date;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;


public class MyMqttService extends Service {
    private static String mClientId = "hy_1_" + PackageUtil.getSerialNumber();
    private static IGetMessageCallBack IGetMessageCallBack;
    private static Context mContext;
    //ota服务器
    private static String host = "tcp://8.129.88.64:1883";
    private static String userName = "test02";
    private static String passWord = "123456";
    private static MqttClient client;
    private static MqttConnectOptions conOpt;

    //绿建服务器
    private static String host_green = "ssl://app.lowcarn.com:8883";
    private static String userName_green = "test";
    private static String passWord_green = "test";
    private static MqttClient client_green;
    private static MqttConnectOptions conOpt_green;

    private static final int DELAY_TIME1 = 60 * 1000;
    private static final int DELAY_TIME2 = 5 * 1000;

    private String appUrl = "";
    private String appVersion = "";
    private String controlVersion = "";
    private String controlUrl = "";

    private String destFileDir = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "app";

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        mHandler = new Handler(Looper.getMainLooper());

        // 提前初始化，保证 reConnect 调用时对象不为 null
        initMqtt();
        initGreenMqtt();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    private void initMqtt() {
        String uri = host;
        try {
            client = new MqttClient(uri, "hy_1_" + PackageUtil.getSerialNumber(), new MemoryPersistence());
        } catch (MqttException e) {
            e.printStackTrace();
        }
        client.setCallback(mqttCallback);
        conOpt = new MqttConnectOptions();
        conOpt.setCleanSession(true);
        //设置自动重连
        // conOpt.setAutomaticReconnect(true);
        conOpt.setConnectionTimeout(10);
        conOpt.setKeepAliveInterval(15);
        conOpt.setUserName(userName);
        conOpt.setPassword(passWord.toCharArray());
        doClientConnection();
    }

    private void initGreenMqtt() {
        String uri = host_green;
        try {
            client_green = new MqttClient(uri, "hy_1_" + PackageUtil.getSerialNumber(), new MemoryPersistence());
        } catch (MqttException e) {
            e.printStackTrace();
        }
        client_green.setCallback(callbackGreen);
        conOpt_green = new MqttConnectOptions();
        conOpt_green.setHttpsHostnameVerificationEnabled(false);//不认证证书
        //设置自动重连
//        conOpt.setAutomaticReconnect(true);
        conOpt_green.setCleanSession(true); //清除缓存
        conOpt_green.setConnectionTimeout(10);//设置超时时间，单位：秒
        conOpt_green.setKeepAliveInterval(15);//心跳包发送间隔，单位：秒
        conOpt_green.setUserName(userName_green);
        conOpt_green.setWill("L/" + PackageUtil.getMAC() + "/V", new byte[0], 2, false);
        conOpt_green.setPassword(passWord_green.toCharArray());
        try {
            InputStream caCrtFile = getResources().openRawResource(R.raw.ca);
            conOpt_green.setSocketFactory(getSingleSocketFactory(caCrtFile));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static SSLSocketFactory getSingleSocketFactory(InputStream caCrtFileInputStream) throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        X509Certificate caCert = null;

        BufferedInputStream bis = new BufferedInputStream(caCrtFileInputStream);
        CertificateFactory cf = CertificateFactory.getInstance("X.509");

        while (bis.available() > 0) {
            caCert = (X509Certificate) cf.generateCertificate(bis);
        }
        KeyStore caKs = KeyStore.getInstance(KeyStore.getDefaultType());
        caKs.load(null, null);
        caKs.setCertificateEntry("cert-certificate", caCert);
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(caKs);
        SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
        sslContext.init(null, tmf.getTrustManagers(), null);
        return sslContext.getSocketFactory();
    }

    //数据上报到ota服务器
    public static void publish(String msg, String clientId) {
        mClientId = clientId;
        String topic = "devices/report/client_id/" + clientId;
        Log.i("service", "publish ota data ---" + msg);
        try {
            if (client != null && client.isConnected() && msg != null) {
                client.publish(topic, msg.getBytes(), 0, false);
                // client.publish(topic, msg.getBytes(), 0, false, null, null);
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    //数据上报绿建服务器
    public static void publishGreen(byte[] bytes, String clientId, String topicType) {
        mClientId = clientId;
        String topic = "L/" + PackageUtil.getMAC() + "/" + topicType;
        Log.i("info", "ota topic---" + topic);
        try {
            if (client_green != null && client_green.isConnected() && bytes != null) {
                client_green.publish(topic, bytes, 0, false);

                // client_green.publish(topic, bytes, 0, false, null, null);
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private static long currentTime;

    //错误日志上报到ota服务器
    public static void logsReport(String errorMsg) {
        if (System.currentTimeMillis() - currentTime < 60 * 1000) {
            return;
        }
        currentTime = System.currentTimeMillis();
        LogsReportUploadInfo uploadInfo = new LogsReportUploadInfo();
        uploadInfo.setIp_addr(NetworkStatus.getLocalIpAddress(mContext));
        SaveAddress saveAddress = MySpUtil.getAddress(mContext);
        if (StringUtils.isNullOrEmpty(saveAddress.getCityName())) {
            uploadInfo.setDevice_addr("");
        } else {
            String address = saveAddress.getProvinceName() + saveAddress.getCityName() + saveAddress.getAddressDetail();
            uploadInfo.setDevice_addr(address);
        }
        uploadInfo.setApp_version(PackageUtil.getVersion(mContext));
        uploadInfo.setClient_id("hy_1_" + PackageUtil.getSerialNumber());
        uploadInfo.setControl_version(HyApplication.getControlVersion());
        uploadInfo.setDevice_code(PackageUtil.getSerialNumber());
        uploadInfo.setDevice_type(1);
        uploadInfo.setTimestamp(StringUtils.simpleDateFormat.format(new Date()));
        uploadInfo.setVendor_id("1");
        uploadInfo.setLog_type(1);
        uploadInfo.setMsg(errorMsg);
        String topic = "devices/logReport/client_id/" + mClientId;
        String json = new Gson().toJson(uploadInfo);
        try {
            if (client != null && client.isConnected() && json != null) {
                client.publish(topic, json.getBytes(), 0, false);
                //client.publish(topic, json.getBytes(), 0, false, null, null);
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        try {
            if (client != null && client.isConnected()) {
                client.disconnect();
                client = null;
            }
            if (client_green != null && client_green.isConnected()) {
                client_green.disconnect();
                client_green = null;
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }
        if (mHandler != null) {
            mHandler.removeCallbacks(mRunnable);
        }
        if (handler1 != null) {
            handler1.removeCallbacks(runnable1);
        }
        stopSelf();
        super.onDestroy();
    }

    /**
     * 连接MQTT服务器
     */
    private void doClientConnection() {
        // 1. 检查 client 是否已经初始化
        if (client != null) {
            if (!client.isConnected()) {
                try {
                    client.connect(conOpt);
                    hyMqttConnectSuccess();
                } catch (MqttException e) {
                    e.printStackTrace();
                    Log.i("service", "hy ota mqtt connect error---" + e);
                    hyMqttConnectError();
                }
            }
        } else {
            Log.e("service", "doClientConnection: ota client is null, check initMqtt()");
        }

        // 2. 检查 client_green 是否已经初始化
        if (client_green != null) {
            if (!client_green.isConnected()) {
                try {
                    client_green.connect(conOpt_green);
                    greenMqttConnectSuccess();
                } catch (MqttException e) {
                    e.printStackTrace();
                    Log.i("service", "green ota mqtt connect error---" + e);
                    greenMqttConnectError();
                }
            }
        } else {
            Log.e("service", "doClientConnection: green client is null, check initGreenMqtt()");
        }
    }


    /**
     * 重连
     */
    public static void reConnect(boolean isNetWork, boolean isConnect) {
        if (IGetMessageCallBack != null && isNetWork) {
            IGetMessageCallBack.updateWeather(isConnect);
        }
        if (!isConnect) {
            return;
        }
        if (client != null && !client.isConnected()) {
            try {
                client.connect(conOpt);
                hyMqttConnectSuccess();
            } catch (MqttException e) {
                e.printStackTrace();
                Log.i("service", "hy ota mqtt connect error---" + e);
                hyMqttConnectError();
            }
        }
        if (client_green != null && !client_green.isConnected()) {
            try {
                client_green.connect(conOpt_green);
                greenMqttConnectSuccess();
            } catch (MqttException e) {
                e.printStackTrace();
                Log.i("service", "green ota mqtt connect error---" + e);
                greenMqttConnectError();
            }
        }
    }

    private static void hyMqttConnectSuccess() {
        if (mHandler != null) {
            mHandler.removeCallbacks(mRunnable);
        }
        Log.i("service", "ota  mqtt connect success....");
        String[] topic = {"devices/upgrade/device_type/1",
                "devices/upgrade/client_id/" + mClientId,
                "devices/status_change_notify/client_id/" + mClientId};
        try {
            // 订阅myTopic话题
            for (int i = 0; i < 3; i++) {
                if (client != null && client.isConnected()) {
                    client.subscribe(topic[i], 1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void hyMqttConnectError() {
        if (mHandler != null) {
            mHandler.removeCallbacks(mRunnable);
            mHandler.postDelayed(mRunnable, DELAY_TIME1);
        }
    }
//    private static IMqttActionListener iMqttActionListener = new IMqttActionListener() {
//        @Override
//        public void onSuccess(IMqttToken arg0) {
//
//        }
//
//        @Override
//        public void onFailure(IMqttToken arg0, Throwable arg1) {
//            arg1.printStackTrace();
//
//        }
//    };

    private static void greenMqttConnectSuccess() {
        Log.i("service", "green mqtt connect success---" + mClientId);
        publishGreen(PackageUtil.getMAC().getBytes(), mClientId, "A");
        String topic = "L/" + PackageUtil.getMAC() + "/L";
        if (mHandler != null) {
            mHandler.removeCallbacks(mRunnable);
        }
        if (handler1 != null) {
            handler1.removeCallbacks(runnable1);
            handler1.postDelayed(runnable1, DELAY_TIME2);
        }
        try {
            if (client_green != null && client_green.isConnected())
                client_green.subscribe(topic, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void greenMqttConnectError() {
        if (mHandler != null) {
            mHandler.removeCallbacks(mRunnable);
            mHandler.postDelayed(mRunnable, DELAY_TIME1);
        }
    }


    //    private static IMqttActionListener iMqttActionListener1 = new IMqttActionListener() {
//        @Override
//        public void onSuccess(IMqttToken arg0) {
//
//        }
//
//        @Override
//        public void onFailure(IMqttToken arg0, Throwable arg1) {
//            arg1.printStackTrace();
//
//
//        }
//    };
    static Handler handler1 = new Handler();
    static Runnable runnable1 = new Runnable() {
        @Override
        public void run() {
            MqttUploadManager.getInstance().uploadData();
            handler1.removeCallbacks(runnable1);
            handler1.postDelayed(runnable1, DELAY_TIME2);
        }
    };
    static Handler mHandler = new Handler(Looper.getMainLooper());
    static Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            reConnect(false, true);
        }
    };

    private MqttCallback mqttCallback = new MqttCallback() {
        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
            Log.i("service", "--messageArrived---" + topic);
            String str1 = new String(message.getPayload());
            if (topic != null && topic.contains("status_change_notify")) {
                //启用/禁用
                MqttStatusResponseInfo info = new Gson().fromJson(str1, MqttStatusResponseInfo.class);
                if (info.getDevice_code().equals(PackageUtil.getSerialNumber())) {
                    if (info.getStatus() == 0) {
                        IGetMessageCallBack.sendOtaStatus(true);
                    } else {
                        IGetMessageCallBack.sendOtaStatus(false);
                    }
                }
            } else {
                //升级
                MqttResponseInfo responseInfo = new Gson().fromJson(str1, MqttResponseInfo.class);
                appUrl = responseInfo.getApp_url();
                appVersion = responseInfo.getApp_version();
                controlVersion = responseInfo.getControl_version();
                controlUrl = responseInfo.getControl_url();
                if (!controlUrl.equals("")) {
                    downFile(controlUrl, controlVersion, 1);
                } else {
                    if (!appUrl.equals("")) {
                        downFile(appUrl, appVersion, 2);
                    }
                }
            }


        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken arg0) {
            Log.i("service", "HY数据上报成功！消息序号: " + arg0.getMessageId());

        }

        @Override
        public void connectionLost(Throwable arg0) {
            Log.i("service", "ota connectionLost---" + arg0.toString());
            if (mHandler != null) {
                mHandler.removeCallbacks(mRunnable);
                mHandler.postDelayed(mRunnable, DELAY_TIME1);
            }
        }
    };

    private MqttCallback callbackGreen = new MqttCallback() {
        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
            mHandler.post(() -> {
                try {
                    IGetMessageCallBack.sendMessage3(message.getPayload());
                } catch (Exception e) {
                    Log.e("service", "12green connectionLost---: " + e.toString());
                    e.printStackTrace();
                }
            });
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken arg0) {
            // --- 这就是你要的回调！ ---
            // 当消息成功到达 MQTT 代理（Broker）后，这个方法会被触发。
            Log.i("service", "数据上报成功！消息序号: " + arg0.getMessageId());
        }

        @Override
        public void connectionLost(Throwable arg0) {
            Log.i("service", "green connectionLost---" + arg0.toString());
            if (mHandler != null) {
                mHandler.removeCallbacks(mRunnable);
                mHandler.postDelayed(mRunnable, DELAY_TIME1);
            }
        }
    };

    /**
     * 判断网络是否连接
     */
    private boolean isConnectIsNormal() {
        ConnectivityManager connectivityManager = (ConnectivityManager) this.getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        if (info != null && info.isAvailable()) {
            String name = info.getTypeName();
            Log.i("service", "MQTT当前网络名称：" + name);
            return true;
        } else {
            Log.i("service", "MQTT 没有可用网络");
            return false;
        }
    }


    private void downFile(String url, String fileName, int type) {
        DownloadUtil.get().download(url, destFileDir, fileName, type,
                new DownloadUtil.OnDownloadListener() {
                    @Override
                    public void onDownloadSuccess(File file) {
                        // OTARequestCommand
                        // 在下载成功时，发送进度 100% 确保 UI 结束
                        if (IGetMessageCallBack != null) {
                            IGetMessageCallBack.onDownloadProgressUpdate(100, type, controlVersion);
                        }

                        if (type == 1) {//主板程序下载成功
                            if (!StringUtils.isNullOrEmpty(appUrl)) {
                                downFile(appUrl, appVersion, 2);
                            } else {
                                IGetMessageCallBack.setMessage(controlVersion);
                            }
                        } else {//APK文件下载成功
                            if (!StringUtils.isNullOrEmpty(controlUrl)) {
                                IGetMessageCallBack.setMessage(controlVersion);
                                IGetMessageCallBack.setMessage2(appVersion);
                            } else {
                                IGetMessageCallBack.setMessage1(appVersion);
                            }
                        }
                    }

                    @Override
                    public void onDownloading(int progress) {
                        Log.i("info", "download ----" + progress);
                        if (IGetMessageCallBack != null) {
                            IGetMessageCallBack.onDownloadProgressUpdate(progress, type, controlVersion);
                        }
                    }

                    @Override
                    public void onDownloadFailed(Exception e) {
                        Log.i("info", "download failed----" + e);
                        if (IGetMessageCallBack != null) {
                            IGetMessageCallBack.onDownloadProgressUpdate(-1, type, controlVersion); // 使用 -1 表示失败
                        }
                    }
                });
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.e(getClass().getName(), "onBind");
        return new CustomBinder();
    }

    public void setIGetMessageCallBack(IGetMessageCallBack IGetMessageCallBack) {
        this.IGetMessageCallBack = IGetMessageCallBack;
        // 既然对象在 onCreate 已经创建好了，这里只需要确保它是连接状态即可
        reConnect(true, true);
    }

    public class CustomBinder extends Binder {
        public MyMqttService getService() {
            return MyMqttService.this;
        }
    }


    //设置数据上报
    public static void sendDataToServer() {
        HDTopic hdTopic = MqttUploadManager.getInstance().getmHDTopic();
        HXTopic hxTopic = MqttUploadManager.getInstance().getmHxTopic();
        //定时数据
        SaveTimingInfo timingInfo = MySpUtil.getTimingData(mContext);
        if (timingInfo != null) {
            hdTopic.setTimingDay(ByteUtils.int16ToByteArray(timingInfo.getOpenDay()));
            if (!StringUtils.isNullOrEmpty(timingInfo.getBeforeTime1())) {
                hdTopic.setTimeBefore1((byte) Integer.parseInt(timingInfo.getBeforeTime1()));
            }
            if (!StringUtils.isNullOrEmpty(timingInfo.getAfterTime1())) {
                hdTopic.setTimeAfter1((byte) Integer.parseInt(timingInfo.getAfterTime1()));
            }
            if (!StringUtils.isNullOrEmpty(timingInfo.getBeforeTime2())) {
                hdTopic.setTimeBefore2((byte) Integer.parseInt(timingInfo.getBeforeTime2()));
            }
            if (!StringUtils.isNullOrEmpty(timingInfo.getAfterTime2())) {
                hdTopic.setTimeAfter2((byte) Integer.parseInt(timingInfo.getAfterTime2()));
            }
            if (!StringUtils.isNullOrEmpty(timingInfo.getBeforeTime3())) {
                hdTopic.setTimeBefore3((byte) Integer.parseInt(timingInfo.getBeforeTime3()));
            }
            if (!StringUtils.isNullOrEmpty(timingInfo.getAfterTime3())) {
                hdTopic.setTimeAfter3((byte) Integer.parseInt(timingInfo.getAfterTime3()));
            }
        }

        //滤网设定
        SaveFilterScreen saveFilterScreen = MySpUtil.getFilterScreen(mContext);
        Log.e("TAG", "sendDataToServer: "+new Gson().toJson(saveFilterScreen));
        if (saveFilterScreen != null) {
            ByteBuffer byteBuffer = ByteBuffer.allocate(24);
            byteBuffer.put(ByteUtils.changeBytes(ByteUtils.stringToByteArray(saveFilterScreen.getFreshAirChange())));
            byteBuffer.put(ByteUtils.changeBytes(ByteUtils.stringToByteArray(saveFilterScreen.getExhaustChange())));
            byteBuffer.put(ByteUtils.changeBytes(ByteUtils.stringToByteArray(saveFilterScreen.getCircle1Change())));
            byteBuffer.put(ByteUtils.changeBytes(ByteUtils.stringToByteArray(saveFilterScreen.getCircle2Change())));
            if (saveFilterScreen.getFreshAirUse() != null) {
                byteBuffer.put(ByteUtils.changeBytes(ByteUtils.stringToByteArray(Long.valueOf(saveFilterScreen.getFreshAirUse()) / 3600 + "")));
            } else {
                byteBuffer.put(ByteUtils.changeBytes(ByteUtils.stringToByteArray(saveFilterScreen.getFreshAirUse())));
            }
            if (saveFilterScreen.getExhaustUse() != null) {
                byteBuffer.put(ByteUtils.changeBytes(ByteUtils.stringToByteArray(Long.valueOf(saveFilterScreen.getExhaustUse()) / 3600 + "")));
            } else {
                byteBuffer.put(ByteUtils.changeBytes(ByteUtils.stringToByteArray(saveFilterScreen.getExhaustUse())));
            }
            if (saveFilterScreen.getCircle1Use() != null) {
                byteBuffer.put(ByteUtils.changeBytes(ByteUtils.stringToByteArray(Long.valueOf(saveFilterScreen.getCircle1Use()) / 3600 + "")));
            } else {
                byteBuffer.put(ByteUtils.changeBytes(ByteUtils.stringToByteArray(saveFilterScreen.getCircle1Use())));
            }
            if (saveFilterScreen.getCircle2Use() != null) {
                byteBuffer.put(ByteUtils.changeBytes(ByteUtils.stringToByteArray(Long.valueOf(saveFilterScreen.getCircle2Use()) / 3600 + "")));
            } else {
                byteBuffer.put(ByteUtils.changeBytes(ByteUtils.stringToByteArray(saveFilterScreen.getCircle2Use())));
            }
            byteBuffer.put(ByteUtils.changeBytes(ByteUtils.stringToByteArray(saveFilterScreen.getFreshAirPressure())));
            byteBuffer.put(ByteUtils.changeBytes(ByteUtils.stringToByteArray(saveFilterScreen.getExhaustPressure())));
            byteBuffer.put(ByteUtils.changeBytes(ByteUtils.stringToByteArray(saveFilterScreen.getCircle1Pressure())));
            byteBuffer.put(ByteUtils.changeBytes(ByteUtils.stringToByteArray(saveFilterScreen.getCircle2Pressure())));
            Log.e("TAG", "sendDataToServer1122: "+ByteUtils.byteArrayToHexString(byteBuffer.array(),0,byteBuffer.array().length));

            hxTopic.setScreenFilterSet(byteBuffer.array());
        }
    }
}
