package com.hwellyi.smarthome;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import JavaType.TypeDownLoad;
import JavaType.TypeRoomInfo;
import JavaType.TypeSecurityInfo;
/**
 * Created by xia_w on 2017/9/17.
 */
//104701077062520398641c1b1d423b33 13b91e91a7f934a581bdae6308bc3edb
public class SettingActivity extends Activity implements View.OnClickListener
{
    final int TYPE_WPA_WPA2 = 1;
    final int TYPE_WEP  = 2;
    final int TYPE_NONE = 3;
    int lastSelectIndex = -1;
    boolean mIsChangging = false;
    boolean mWifiStatus = false;
    boolean mIsFinish = false;
    int mProductClickCount = 0;
    TypeDownLoad mDownLoadStatus = new TypeDownLoad();
    SupplicantState mWifiConnectStatus = SupplicantState.INVALID;
    List<TypeSecurityInfo> mDeviceList = new ArrayList<>();
    //把所有可用wifi都显示出来
    List<ScanResult> mWifiApList = new ArrayList<>();
    int mWifiApScanTime = 0;
    WifiManager mWifiManager;//
    boolean mWifiIsWorking = false;
    private  AlertDialog mAlarmDialog = null;
    private Ringtone mSoundHandle;

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting);
        mSoundHandle =  RingtoneManager.getRingtone(this, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        findViewById(R.id.image_set_return).setOnClickListener(this);
        findViewById(R.id.image_set_securtiy).setOnClickListener(this);
        findViewById(R.id.image_set_zone).setOnClickListener(this);
        findViewById(R.id.image_set_language).setOnClickListener(this);
        findViewById(R.id.image_set_wifi).setOnClickListener(this);
        findViewById(R.id.image_set_about).setOnClickListener(this);
        onSetImageBg(4);
        mWifiManager =  (WifiManager) PublicUse.GlobalcContext.getApplicationContext().getSystemService(WIFI_SERVICE);//管理wifi
        mWifiStatus = mWifiManager.isWifiEnabled();
        handler.sendEmptyMessageDelayed(1, 1000);
        PublicUse.mSettingHandler = handler;
        mAlarmDialog = null;
        //向jni请求当前是否有报警  有的话要弹出解除报警框
        PublicUse.mJniFunCB.onCheckAlarmStatus();
    }

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            super.handleMessage(msg);
            if (msg.what == 0x01)
            {
                if((lastSelectIndex == 3) && (mIsChangging == false))
                {
                    if(mWifiStatus != mWifiManager.isWifiEnabled())
                    {
                        mWifiConnectStatus = mWifiManager.getConnectionInfo().getSupplicantState();
                        mWifiStatus = mWifiManager.isWifiEnabled();
                        if((lastSelectIndex == 3) && (mIsChangging == false))
                        {
                            lastSelectIndex = 0;
                            onSetImageBg(3);
                        }
                    }
                    else if(mWifiConnectStatus != mWifiManager.getConnectionInfo().getSupplicantState())
                    {
                        mWifiConnectStatus = mWifiManager.getConnectionInfo().getSupplicantState();
                        mWifiStatus = mWifiManager.isWifiEnabled();
                        if((lastSelectIndex == 3) && (mIsChangging == false))
                        {
                            lastSelectIndex = 0;
                            onSetImageBg(3);
                        }
                    }

                    //5s扫描一次
                    mWifiApScanTime++;
                    if(mWifiApScanTime == 2)
                    {
                        mWifiManager.startScan();
                    }
                    else if(mWifiApScanTime > 5)
                    {
                        mWifiApScanTime = 0;
                        List<ScanResult> tempWifiList = mWifiManager.getScanResults();
                        //去掉无效的
                        WifiInfo connectWifi = mWifiManager.getConnectionInfo();
                        String connectWifiName = "";
                        if(connectWifi != null)
                        {
                            connectWifiName = mWifiManager.getConnectionInfo().getSSID();
                            if(connectWifiName.length() > 2)
                            {
                                connectWifiName = connectWifiName.substring(1, connectWifiName.length() - 1);
                            }
                        }
                        for (int i = 0; i < tempWifiList.size();)
                        {
                            if((tempWifiList.get(i).SSID == null) || ((tempWifiList.get(i).SSID != null) && (tempWifiList.get(i).SSID.length() == 0)))
                            {
                                tempWifiList.remove(i);
                                continue;
                            }
                            //与当前连接的wifi名称相同也不要显示了
                            if(tempWifiList.get(i).SSID.equals(connectWifiName))
                            {
                                tempWifiList.remove(i);
                                continue;
                            }
                            i++;
                        }

                        if(mWifiApList.size() != tempWifiList.size())
                        {
                            mWifiApList.clear();
                            for (int i = 0; i < tempWifiList.size(); i++)
                            {
                                mWifiApList.add(tempWifiList.get(i));
                            }

                            //将搜索到的wifi根据信息强度进行排序
                            for (int i = 0; i < mWifiApList.size(); i++)
                            {
                                for (int j = 0; j < mWifiApList.size(); j++)
                                {
                                    if(mWifiApList.get(i).level > mWifiApList.get(j).level)//level 属性wifi强度
                                    {
                                        ScanResult temp = null;
                                        temp = mWifiApList.get(i);
                                        mWifiApList.set(i, mWifiApList.get(j));
                                        mWifiApList.set(j, temp);
                                    }
                                }
                            }
                            if((lastSelectIndex == 3) && (mIsChangging == false))
                            {
                                lastSelectIndex = 0;
                                onSetImageBg(3);
                            }
                        }
                    }
                }

                if((PublicUse.mSettingHandler != null) && (mIsFinish == false))
                {
                    handler.sendEmptyMessageDelayed(1, 1000);
                }

                //1s一次  3s一次检测固件版本
            }
            else if(msg.what == 2)
            {
                if(lastSelectIndex == 0)
                {
                    lastSelectIndex = 1;
                    onSetImageBg(0);
                }
            }
            else if(msg.what == 3)
            {
                onSetImageBg(4);
            }
            else if(msg.what == 4)
            {
                if(lastSelectIndex == 0)
                {
                    lastSelectIndex = 1;
                    onSetImageBg(0);//更新一下安防设备
                }
            }
            else if(msg.what == 7)
            {
                onAlarmDlg(msg.arg1, msg.arg2, (String) msg.obj);
            }
            else if(msg.what == 8)
            {
                onDisAlarmDlg();
            }
        }
    };

    boolean onSetImageBg(int index)
    {
        mIsChangging = true;
        if(index < 0) index = 0;
        else if(index > 4) index = 4;
        if(lastSelectIndex != index)
        {
            switch (lastSelectIndex)
            {
                case 0:findViewById(R.id.image_set_securtiy).setBackgroundResource(R.drawable.set_security_off);break;
                case 1:findViewById(R.id.image_set_zone).setBackgroundResource(R.drawable.set_zone_off);break;
                case 2:findViewById(R.id.image_set_language).setBackgroundResource(R.drawable.set_language_off);break;
                case 3:findViewById(R.id.image_set_wifi).setBackgroundResource(R.drawable.set_wifi_off);break;
                case 4:findViewById(R.id.image_set_about).setBackgroundResource(R.drawable.set_about_off);break;
                default:break;
            }
            lastSelectIndex = index;
            ScrollView scrollSetting = (ScrollView)findViewById(R.id.scrollSetting);
            RelativeLayout relativeLayout = new RelativeLayout(this);
            if(lastSelectIndex != 4)
            {
                mDownLoadStatus.status = 0;
            }
            switch (lastSelectIndex)
            {
                case 0://安防设备状态
                {
                    scrollSetting.removeAllViews();
                    relativeLayout.removeAllViews();
                    findViewById(R.id.image_set_securtiy).setBackgroundResource(R.drawable.set_security_on);
                    //先得到房间列表
                    String tempRoomListJsonString = PublicUse.mJniFunCB.onGetRoomList();
                    //解析出一个房间列表
                    ArrayList<TypeRoomInfo> tempRoomList = new ArrayList<>();
                    TypeRoomInfo tempRoomInfo;
                    JSONObject tempJson = null;
                    try {
                        tempJson = new JSONObject(tempRoomListJsonString);
                        if (!tempJson.isNull("roomlist"))
                        {
                            JSONArray tempArray = new JSONArray(tempJson.getString("roomlist"));
                            if (tempArray.length() > 0)
                            {
                                for (int i = 0; i < tempArray.length(); i++)
                                {
                                    JSONObject tempDeviceJson = tempArray.getJSONObject(i);
                                    tempRoomInfo = new TypeRoomInfo(0, 0, "");
                                    if (!tempDeviceJson.isNull("roomid"))
                                    {
                                        tempRoomInfo.roomID = tempDeviceJson.getInt("roomid");
                                    }
                                    if (!tempDeviceJson.isNull("iconid"))
                                    {
                                        tempRoomInfo.iconID = tempDeviceJson.getInt("iconid");
                                    }
                                    if (!tempDeviceJson.isNull("name"))
                                    {
                                        tempRoomInfo.name = tempDeviceJson.getString("name");
                                    }
                                    tempRoomList.add(tempRoomInfo);
                                }
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }      //更新安防设备列表
                    String tempJsonString = PublicUse.mJniFunCB.onGetDeviceList((1 << HYJniService.SUB_DEVICE_TYPE_DOOR_WINDOE) | (1 << HYJniService.SUB_DEVICE_TYPE_PIR));
                    try
                    {
                        tempJson = new JSONObject(tempJsonString);
                        if (!tempJson.isNull("devlist"))
                        {
                            JSONArray tempArray = new JSONArray(tempJson.getString("devlist"));
                            if (tempArray.length() == 0)
                            {
                                //目前没有这个安防设备，需要添加当然我需要把整个家庭的安防设备都添加进来咯
                                TextView tempText = new TextView(this);
                                tempText.setText("目前还没有可布防的设备，添加一个试试~！");
                                tempText.setTextColor(Color.WHITE);
                                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
                                layoutParams.topMargin = 100;
                                layoutParams.leftMargin = 30;
                                layoutParams.rightMargin = 20;
                                layoutParams.bottomMargin = 20;
                                relativeLayout.addView(tempText, layoutParams);
                            } else
                            {
                                mDeviceList.clear();
                                TypeSecurityInfo tempDeviceInfo;
                                int tempTopValue = 0;
                                for (int i = 0; i < tempArray.length(); i++)
                                {
                                    JSONObject tempDeviceJson = tempArray.getJSONObject(i);
                                    tempDeviceInfo = new TypeSecurityInfo();
                                    if (tempDeviceJson != null)
                                    {
                                        if (!tempDeviceJson.isNull("id"))
                                        {
                                            tempDeviceInfo.keyID = tempDeviceJson.getInt("id");
                                        }
                                        if (!tempDeviceJson.isNull("subid"))
                                        {
                                            tempDeviceInfo.subID = tempDeviceJson.getInt("subid");
                                        }
                                        if (!tempDeviceJson.isNull("subtype"))
                                        {
                                            tempDeviceInfo.subType = tempDeviceJson.getInt("subtype");
                                        }
                                        if (!tempDeviceJson.isNull("status"))
                                        {
                                            tempDeviceInfo.securityStatus = tempDeviceJson.getInt("status");
                                        }
                                        if (!tempDeviceJson.isNull("roomid"))
                                        {
                                            int tempRoomID = tempDeviceJson.getInt("roomid");
                                            for (int j = 0; j < tempRoomList.size(); j++)
                                            {
                                                tempRoomInfo = tempRoomList.get(j);
                                                if((tempRoomInfo != null) && (tempRoomInfo.roomID == tempRoomID))
                                                {
                                                    tempDeviceInfo.roomName = tempRoomInfo.name;
                                                    break;
                                                }
                                            }
                                        }
                                        if (!tempDeviceJson.isNull("name"))
                                        {
                                            tempDeviceInfo.deviceName = tempDeviceJson.getString("name");
                                        }
                                        if (tempDeviceInfo.keyID != 0)
                                        {
                                            mDeviceList.add(tempDeviceInfo);
                                            {
                                                //添加ICON 到 view
                                                ImageView imageIcon = new ImageView(this);
                                                if (tempDeviceInfo.subType == HYJniService.SUB_DEVICE_TYPE_DOOR_WINDOE)//TYPE_DEVICE_SUB_DOOR_WINDOW
                                                {
                                                    imageIcon.setBackgroundResource(R.drawable.set_security_door);
                                                } else
                                                {
                                                    imageIcon.setBackgroundResource(R.drawable.set_security_pir);
                                                }
                                                RelativeLayout.LayoutParams iconLayoutParams = new RelativeLayout.LayoutParams(44, 44);
                                                iconLayoutParams.topMargin = 10 + tempTopValue;
                                                iconLayoutParams.leftMargin = 20;
                                                relativeLayout.addView(imageIcon, iconLayoutParams);
                                            }
                                            {
                                                //添加房间名称
                                                TextView textRoomName = new TextView(this);
                                                textRoomName.setText(tempDeviceInfo.roomName);
                                                textRoomName.setTextColor(Color.WHITE);
                                                textRoomName.setTextSize(20);
                                                RelativeLayout.LayoutParams roomNameLayoutParams = new RelativeLayout.LayoutParams(250, 100);
                                                roomNameLayoutParams.topMargin = 8 + tempTopValue;
                                                roomNameLayoutParams.leftMargin = 20 + 44 + 20;
                                                relativeLayout.addView(textRoomName, roomNameLayoutParams);
                                            }
                                            {
                                                //添加设备名称
                                                TextView textDeviceName = new TextView(this);
                                                textDeviceName.setText(tempDeviceInfo.deviceName);
                                                textDeviceName.setTextColor(Color.argb(153, 255, 255, 255));
                                                textDeviceName.setTextSize(16);
                                                RelativeLayout.LayoutParams deviceNameLayoutParams = new RelativeLayout.LayoutParams(250, 100);
                                                deviceNameLayoutParams.topMargin = 8 + tempTopValue + 4 + 20;
                                                deviceNameLayoutParams.leftMargin = 20 + 44 + 20;
                                                relativeLayout.addView(textDeviceName, deviceNameLayoutParams);
                                            }
                                            {
                                                //添加按钮
                                                final ImageView statusImageView = new ImageView(this);
                                                if (tempDeviceInfo.securityStatus == 0)
                                                {
                                                    statusImageView.setBackgroundResource(R.drawable.set_off);
                                                } else
                                                {
                                                    statusImageView.setBackgroundResource(R.drawable.set_on);
                                                }
                                                RelativeLayout.LayoutParams statusLayoutParams = new RelativeLayout.LayoutParams(60, 36);
                                                statusLayoutParams.topMargin = 16 + tempTopValue;
                                                statusLayoutParams.leftMargin = 267;
                                                statusImageView.setTag(mDeviceList.get(i));
                                                statusImageView.setOnClickListener(new View.OnClickListener()
                                                {
                                                    @Override
                                                    public void onClick(View view)
                                                    {
                                                        TypeSecurityInfo tempInfo = (TypeSecurityInfo)view.getTag();
                                                        if(tempInfo.securityStatus == 0)
                                                        {
                                                            tempInfo.securityStatus = 1;
                                                            view.setBackgroundResource(R.drawable.set_on);
                                                        }
                                                        else
                                                        {
                                                            tempInfo.securityStatus = 0;
                                                            view.setBackgroundResource(R.drawable.set_off);
                                                        }
                                                        PublicUse.mJniFunCB.onSetDeviceStatus(tempInfo.keyID, tempInfo.subID, tempInfo.securityStatus);
                                                    }
                                                });
                                                relativeLayout.addView(statusImageView, statusLayoutParams);
                                            }
                                            {
                                                //添加分隔线
                                                ImageView lineImageView = new ImageView(this);
                                                lineImageView.setBackgroundResource(R.drawable.set_securtiy_line);
                                                RelativeLayout.LayoutParams lineLayoutParams = new RelativeLayout.LayoutParams(314, 1);
                                                lineLayoutParams.topMargin = 68 + tempTopValue;
                                                lineLayoutParams.leftMargin = 20;
                                                relativeLayout.addView(lineImageView, lineLayoutParams);
                                            }


                                            tempTopValue += 69;
                                        }
                                    }
                                }
                            }
                            scrollSetting.addView(relativeLayout);
                        }
                    } catch (JSONException e)
                    {
                        e.printStackTrace();
                    }
                }
                break;
                case 1://时区
                {
                    findViewById(R.id.image_set_zone).setBackgroundResource(R.drawable.set_zone_on);
                    scrollSetting.removeAllViews();
                    relativeLayout.removeAllViews();
                    int tempTopValue = 0;
                    {
                        TextView zoneTitle = new TextView(this);
                        zoneTitle.setText("时区选择");
                        zoneTitle.setTextColor(Color.argb(153, 255, 255, 255));
                        zoneTitle.setTextSize(16);
                        RelativeLayout.LayoutParams zoneTitleParams = new RelativeLayout.LayoutParams(100, 100);
                        zoneTitleParams.topMargin = 8 + tempTopValue;
                        zoneTitleParams.leftMargin = 22;
                        relativeLayout.addView(zoneTitle, zoneTitleParams);
                        tempTopValue += 38;
                        //添加分隔线
                        ImageView lineImageView = new ImageView(this);
                        lineImageView.setBackgroundResource(R.drawable.set_securtiy_line);
                        RelativeLayout.LayoutParams lineLayoutParams = new RelativeLayout.LayoutParams(314, 1);
                        lineLayoutParams.topMargin = tempTopValue;
                        lineLayoutParams.leftMargin = 20;
                        relativeLayout.addView(lineImageView, lineLayoutParams);
                    }
                    {
                        int tempZoneSelect = 0;
                        //添加内容
                        for (int i = 0; i < 1; i++)
                        {
                            //添加一个时间
                            TextView zoneValueText = new TextView(this);
                            zoneValueText.setText(onGetZoneWithIndex(i));
                            zoneValueText.setTextColor(Color.WHITE);
                            zoneValueText.setTextSize(20);
                            RelativeLayout.LayoutParams zoneValueParams = new RelativeLayout.LayoutParams(100, 100);
                            zoneValueParams.topMargin = 10 + tempTopValue;
                            zoneValueParams.leftMargin = 22;
                            relativeLayout.addView(zoneValueText, zoneValueParams);

                            if (tempZoneSelect == i)
                            {
                                //添加一个勾
                                ImageView statusImageView = new ImageView(this);
                                statusImageView.setBackgroundResource(R.drawable.set_zone_yes);
                                RelativeLayout.LayoutParams statusLayoutParams = new RelativeLayout.LayoutParams(24, 24);
                                statusLayoutParams.topMargin = 12 + tempTopValue;
                                statusLayoutParams.leftMargin = 300;
                                relativeLayout.addView(statusImageView, statusLayoutParams);
                            }
                            //添加分隔线
                            ImageView lineImageView = new ImageView(this);
                            lineImageView.setBackgroundResource(R.drawable.set_securtiy_line);
                            RelativeLayout.LayoutParams lineLayoutParams = new RelativeLayout.LayoutParams(314, 1);
                            lineLayoutParams.topMargin = 50 + tempTopValue;
                            lineLayoutParams.leftMargin = 20;
                            relativeLayout.addView(lineImageView, lineLayoutParams);

                            tempTopValue += 51;
                        }
                    }
                    scrollSetting.addView(relativeLayout);
                }
                    break;
                case 2://语言
                {
                    findViewById(R.id.image_set_language).setBackgroundResource(R.drawable.set_language_on);
                    scrollSetting.removeAllViews();
                    relativeLayout.removeAllViews();
                    int tempTopValue = 0;
                    {
                        TextView languageTitle = new TextView(this);
                        languageTitle.setText("系统语言选择");
                        languageTitle.setTextColor(Color.argb(153, 255, 255, 255));
                        languageTitle.setTextSize(16);
                        RelativeLayout.LayoutParams languageParams = new RelativeLayout.LayoutParams(100, 100);
                        languageParams.topMargin = 8 + tempTopValue;
                        languageParams.leftMargin = 22;
                        relativeLayout.addView(languageTitle, languageParams);
                        tempTopValue += 38;
                        //添加分隔线
                        ImageView lineImageView = new ImageView(this);
                        lineImageView.setBackgroundResource(R.drawable.set_securtiy_line);
                        RelativeLayout.LayoutParams lineLayoutParams = new RelativeLayout.LayoutParams(314, 1);
                        lineLayoutParams.topMargin = tempTopValue;
                        lineLayoutParams.leftMargin = 20;
                        relativeLayout.addView(lineImageView, lineLayoutParams);
                    }
                    {
                        //添加内容
                        int tempLanguageSelect = 0;
                        for (int i = 0; i < 1; i++)
                        {
                            //添加一个时间
                            TextView languageText = new TextView(this);
                            languageText.setText(onGetLanguageWithIndex(i));
                            languageText.setTextColor(Color.WHITE);
                            languageText.setTextSize(20);
                            RelativeLayout.LayoutParams languageParams = new RelativeLayout.LayoutParams(100, 100);
                            languageParams.topMargin = 10 + tempTopValue;
                            languageParams.leftMargin = 22;
                            relativeLayout.addView(languageText, languageParams);

                            if (tempLanguageSelect == i)
                            {
                                //添加一个勾
                                ImageView statusImageView = new ImageView(this);
                                statusImageView.setBackgroundResource(R.drawable.set_zone_yes);
                                RelativeLayout.LayoutParams statusLayoutParams = new RelativeLayout.LayoutParams(24, 24);
                                statusLayoutParams.topMargin = 12 + tempTopValue;
                                statusLayoutParams.leftMargin = 300;
                                relativeLayout.addView(statusImageView, statusLayoutParams);
                            }
                            //添加分隔线
                            ImageView lineImageView = new ImageView(this);
                            lineImageView.setBackgroundResource(R.drawable.set_securtiy_line);
                            RelativeLayout.LayoutParams lineLayoutParams = new RelativeLayout.LayoutParams(314, 1);
                            lineLayoutParams.topMargin = 50 + tempTopValue;
                            lineLayoutParams.leftMargin = 20;
                            relativeLayout.addView(lineImageView, lineLayoutParams);

                            tempTopValue += 51;
                        }
                    }
                    scrollSetting.addView(relativeLayout);
                }
                    break;
                case 3://wifi
                {
                    PublicUse.onPrintLogToJni("wifi upadte! mWifiConnectStatus = " + String.valueOf(mWifiConnectStatus));
                    if(mWifiConnectStatus == SupplicantState.DISCONNECTED)
                    {
                        Toast.makeText(getApplicationContext(), "wifi 连接失败!", Toast.LENGTH_SHORT).show();
                    }
                    findViewById(R.id.image_set_wifi).setBackgroundResource(R.drawable.set_wifi_on);
                    int tempTopValue = 0;
                    scrollSetting.removeAllViews();
                    mDeviceList.clear();
                    relativeLayout.removeAllViews();
                    {
                        ConnectivityManager conn =(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                        NetworkInfo networkInfo = conn.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);
                        //添加wifi状态区域
                        TextView wifiSwitchText = new TextView(this);
                        wifiSwitchText.setText("WiFi");
                        RelativeLayout.LayoutParams wifiSwitchParams = new RelativeLayout.LayoutParams(100, 100);
                        wifiSwitchText.setTextColor(Color.WHITE);
                        wifiSwitchText.setTextSize(20);
                        wifiSwitchParams.topMargin = 2 + tempTopValue;
                        wifiSwitchParams.leftMargin = 22;
                        relativeLayout.addView(wifiSwitchText, wifiSwitchParams);

                        //有线连接提示
                        TextView eth0Text = new TextView(this);
                        RelativeLayout.LayoutParams eth0Params = new RelativeLayout.LayoutParams(250, 100);
                        eth0Text.setTextColor(Color.argb(153, 255, 255, 255));
                        eth0Text.setTextSize(16);
                        eth0Params.topMargin = 25 + tempTopValue;
                        eth0Params.leftMargin = 22;
                        relativeLayout.addView(eth0Text, eth0Params);
                        if((networkInfo != null) && networkInfo.isConnected())
                        {
                            eth0Text.setText("有线已连接:" + onGetIPAddress("eth0"));
                        }
                        else
                        {
                            eth0Text.setText("有线未连接");
                        }
                        //增加分隔线
                        ImageView lineImageView = new ImageView(this);
                        lineImageView.setBackgroundResource(R.drawable.set_securtiy_line);
                        RelativeLayout.LayoutParams lineLayoutParams = new RelativeLayout.LayoutParams(314, 1);
                        lineLayoutParams.topMargin = 55 + tempTopValue;
                        lineLayoutParams.leftMargin = 20;
                        relativeLayout.addView(lineImageView, lineLayoutParams);
                        //增加一个开关
                        //添加按钮
                        ImageView statusImageView = new ImageView(this);
                        RelativeLayout.LayoutParams statusLayoutParams = new RelativeLayout.LayoutParams(60, 36);
                        statusLayoutParams.topMargin = 10 + tempTopValue;
                        statusLayoutParams.leftMargin = 267;
                        relativeLayout.addView(statusImageView, statusLayoutParams);
                        statusImageView.setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View view)
                            {
                                if(!mWifiIsWorking)
                                {
                                    mWifiIsWorking = true;
                                    if (mWifiManager.isWifiEnabled())
                                    {
                                        //当前打开状态 关闭wifi
                                        mWifiManager.setWifiEnabled(false);
                                        view.setBackgroundResource(R.drawable.set_off);
                                        mWifiManager.saveConfiguration();
                                    }
                                    else
                                    {
                                        //当前关闭状态 打开wifi
                                        mWifiManager.setWifiEnabled(true);
                                        view.setBackgroundResource(R.drawable.set_on);
                                        mWifiManager.saveConfiguration();
                                    }
                                    mWifiIsWorking = false;
                                }
                            }
                        });
                        if (mWifiStatus)
                        {
                            //添加选择网络
                            tempTopValue += 51;
                            statusImageView.setBackgroundResource(R.drawable.set_on);
                            //得到当前是否已经连接上了wifi
                            WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
                            if(mWifiConnectStatus == SupplicantState.COMPLETED)//已经连接上
                            {
                                //把当前连接的wifi显示出来
                                //显示信号图标
                                ImageView wifiConnectImage = new ImageView(this);
                                if(wifiInfo.getRssi() > -60)
                                {
                                    wifiConnectImage.setBackgroundResource(R.drawable.set_wifi_3);
                                }
                                else if(wifiInfo.getRssi() > -80)
                                {
                                    wifiConnectImage.setBackgroundResource(R.drawable.set_wifi_2);
                                }
                                else if(wifiInfo.getRssi() > -100)
                                {
                                    wifiConnectImage.setBackgroundResource(R.drawable.set_wifi_1);
                                }
                                else
                                {
                                    wifiConnectImage.setBackgroundResource(R.drawable.set_wifi_0);
                                }
                                RelativeLayout.LayoutParams wifiConnectParams = new RelativeLayout.LayoutParams(18, 18);
                                wifiConnectParams.topMargin = 15 + tempTopValue;
                                wifiConnectParams.leftMargin = 22;
                                relativeLayout.addView(wifiConnectImage, wifiConnectParams);

                                //添加一个名称
                                TextView wifiConnectName = new TextView(this);
                                wifiConnectName.setText(wifiInfo.getSSID());
                                wifiConnectName.setTextColor(Color.WHITE);
                                wifiConnectName.setTextSize(20);
                                RelativeLayout.LayoutParams wifiConnectNameParams = new RelativeLayout.LayoutParams(300, 40);
                                wifiConnectNameParams.topMargin = 5 + tempTopValue;
                                wifiConnectNameParams.leftMargin = 50;
                                //添加一个长按   取消保存
                                wifiConnectName.setOnLongClickListener(new View.OnLongClickListener()
                                {
                                    @Override
                                    public boolean onLongClick(View view)
                                    {
                                        final AlertDialog.Builder normalDialog = new AlertDialog.Builder(SettingActivity.this);
                                        normalDialog.setIcon(R.drawable.alarm);
                                        normalDialog.setTitle("警告");
                                        normalDialog.setMessage("取消保存这个网络?");
                                        normalDialog.setPositiveButton("确定",
                                                new DialogInterface.OnClickListener()
                                                {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which)
                                                    {
                                                        WifiInfo tempWifiInfo = mWifiManager.getConnectionInfo();
                                                        if(tempWifiInfo.getNetworkId() >= 0)
                                                        {
                                                            //并且要断开这个连接
                                                            int netId = tempWifiInfo.getNetworkId();
                                                            //如果之前有类似的配置
                                                            int retNetID = getNetworkId(tempWifiInfo.getSSID());
                                                            if(retNetID  >= 0)
                                                            {
                                                                PublicUse.onPrintLogToJni("old");
                                                                //则清除旧有配置
                                                                mWifiManager.removeNetwork(retNetID);
                                                            }
                                                            else
                                                            {
                                                                PublicUse.onPrintLogToJni("new");
                                                            }
                                                            mWifiManager.disableNetwork(netId);
                                                            mWifiManager.disconnect();
                                                            mWifiManager.saveConfiguration();
                                                        }
                                                    }
                                                });
                                        normalDialog.setNegativeButton("关闭", null);
                                        normalDialog.show();
                                        return true;
                                    }
                                });
                                relativeLayout.addView(wifiConnectName, wifiConnectNameParams);
                                //添加一个本机IP
                                TextView wifiConnectIPText = new TextView(this);
                                RelativeLayout.LayoutParams wifiConnectIPParams = new RelativeLayout.LayoutParams(250, 100);
                                wifiConnectIPText.setTextColor(Color.argb(153, 255, 255, 255));
                                String tempIpAddr = onGetIPAddress("wlan0");
                                if(tempIpAddr.length() < 5)
                                {
                                    mWifiConnectStatus = SupplicantState.ASSOCIATING;
                                }
                                wifiConnectIPText.setText("IP:" + tempIpAddr);
                                wifiConnectIPText.setTextSize(16);
                                wifiConnectIPParams.topMargin = 28 + tempTopValue;
                                wifiConnectIPParams.leftMargin = 50;
                                relativeLayout.addView(wifiConnectIPText, wifiConnectIPParams);

                                //添加一个勾
                                ImageView wifiConnectYesImageView = new ImageView(this);
                                wifiConnectYesImageView.setBackgroundResource(R.drawable.set_zone_yes);
                                RelativeLayout.LayoutParams wifiConnectYesParams = new RelativeLayout.LayoutParams(24, 24);
                                wifiConnectYesParams.topMargin = 12 + tempTopValue;
                                wifiConnectYesParams.leftMargin = 300;
                                relativeLayout.addView(wifiConnectYesImageView, wifiConnectYesParams);
                                //添加分隔线
                                ImageView wifiConnectListImageView = new ImageView(this);
                                wifiConnectListImageView.setBackgroundResource(R.drawable.set_securtiy_line);
                                RelativeLayout.LayoutParams wifiConnectLineParams = new RelativeLayout.LayoutParams(314, 1);
                                wifiConnectLineParams.topMargin = 50 + tempTopValue;
                                wifiConnectLineParams.leftMargin = 20;
                                relativeLayout.addView(wifiConnectListImageView, wifiConnectLineParams);
                                tempTopValue += 51;
                            }
                            //添加一个wifi标签
                            TextView wifiTitle = new TextView(this);
                            wifiTitle.setText("选取网络");
                            wifiTitle.setTextColor(Color.argb(153, 255, 255, 255));
                            wifiTitle.setTextSize(16);
                            RelativeLayout.LayoutParams wifiParams = new RelativeLayout.LayoutParams(100, 100);
                            wifiParams.topMargin = 8 + tempTopValue;
                            wifiParams.leftMargin = 22;
                            relativeLayout.addView(wifiTitle, wifiParams);
                            tempTopValue += 38;
                            //添加分隔线
                            ImageView line2ImageView = new ImageView(this);
                            line2ImageView.setBackgroundResource(R.drawable.set_securtiy_line);
                            RelativeLayout.LayoutParams line2LayoutParams = new RelativeLayout.LayoutParams(314, 1);
                            line2LayoutParams.topMargin = tempTopValue;
                            line2LayoutParams.leftMargin = 20;
                            relativeLayout.addView(line2ImageView, line2LayoutParams);


                            //全部显示出来
                            for (int i = 0; i < mWifiApList.size(); i++)
                            {
                                ScanResult tempResult = mWifiApList.get(i);
                                if(tempResult.SSID.length() > 0)
                                {
                                    //先显示一个信号图标
                                    ImageView signalImage = new ImageView(this);
                                    if(tempResult.level > -50)
                                    {
                                        signalImage.setBackgroundResource(R.drawable.set_wifi_3);
                                    }
                                    else if(tempResult.level > -70)
                                    {
                                        signalImage.setBackgroundResource(R.drawable.set_wifi_2);
                                    }
                                    else if(tempResult.level > -90)
                                    {
                                        signalImage.setBackgroundResource(R.drawable.set_wifi_1);
                                    }
                                    else
                                    {
                                        signalImage.setBackgroundResource(R.drawable.set_wifi_0);
                                    }
                                    RelativeLayout.LayoutParams signalParams = new RelativeLayout.LayoutParams(18, 18);
                                    signalParams.topMargin = 15 + tempTopValue;
                                    signalParams.leftMargin = 22;
                                    relativeLayout.addView(signalImage, signalParams);

                                    //添加一个名称
                                    TextView wifiName = new TextView(this);
                                    wifiName.setText(tempResult.SSID);
                                    wifiName.setTextColor(Color.WHITE);
                                    wifiName.setTextSize(20);
                                    RelativeLayout.LayoutParams wifiNameParams = new RelativeLayout.LayoutParams(300, 40);
                                    wifiNameParams.topMargin = 8 + tempTopValue;
                                    wifiNameParams.leftMargin = 50;
                                    relativeLayout.addView(wifiName, wifiNameParams);
                                    wifiName.setTag(tempResult);
                                    wifiName.setOnClickListener(new View.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(View view)
                                        {
                                            final ScanResult tempResult = (ScanResult)view.getTag();
                                            if(tempResult.SSID.length() > 0)
                                            {
                                                if(tempResult.capabilities.equals("[ESS]"))
                                                {
                                                    //不需要输入密码
                                                    //createWifiConfig主要用于构建一个WifiConfiguration，代码中的例子主要用于连接不需要密码的Wifi
                                                    //WifiManager的addNetwork接口，传入WifiConfiguration后，得到对应的NetworkId
                                                    int netId = mWifiManager.addNetwork(createWifiConfig(tempResult.SSID, "", TYPE_NONE));
                                                    //WifiManager的enableNetwork接口，就可以连接到netId对应的wifi了
                                                    //其中boolean参数，主要用于指定是否需要断开其它Wifi网络
                                                    mWifiManager.enableNetwork(netId, true);
                                                    mWifiManager.reconnect();
                                                    mWifiManager.saveConfiguration();
                                                }
                                                else
                                                {
                                                    //弹出密码输入框
                                                    final EditText editText = new EditText(SettingActivity.this);
                                                    editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                                                    new AlertDialog.Builder(SettingActivity.this)
                                                            .setIcon(R.drawable.alarm)
                                                            .setTitle(tempResult.SSID)
                                                            .setView(editText)
                                                            .setPositiveButton("连接", new DialogInterface.OnClickListener()
                                                            {
                                                                @Override
                                                                public void onClick(DialogInterface dialogInterface, int i)
                                                                {
                                                                    int tempType = TYPE_WPA_WPA2;
                                                                    if(tempResult.capabilities.indexOf("WEP") > 0)
                                                                    {
                                                                        tempType = TYPE_WEP;
                                                                    }
                                                                    String passwordString = editText.getText().toString();
                                                                    //createWifiConfig主要用于构建一个WifiConfiguration，代码中的例子主要用于连接不需要密码的Wifi
                                                                    //WifiManager的addNetwork接口，传入WifiConfiguration后，得到对应的NetworkId
                                                                    int netId = mWifiManager.addNetwork(createWifiConfig(tempResult.SSID, passwordString, tempType));
                                                                    //WifiManager的enableNetwork接口，就可以连接到netId对应的wifi了
                                                                    //其中boolean参数，主要用于指定是否需要断开其它Wifi网络
                                                                    mWifiManager.enableNetwork(netId, true);
                                                                    mWifiManager.reconnect();
                                                                    mWifiManager.saveConfiguration();
                                                                }
                                                            })
                                                            .setNegativeButton("取消", null).show();
                                                }
                                            }
                                        }
                                    });
                                    //添加分隔线
                                    ImageView wifiListImageView = new ImageView(this);
                                    wifiListImageView.setBackgroundResource(R.drawable.set_securtiy_line);
                                    RelativeLayout.LayoutParams wifiLineParams = new RelativeLayout.LayoutParams(314, 1);
                                    wifiLineParams.topMargin = 50 + tempTopValue;
                                    wifiLineParams.leftMargin = 20;
                                    relativeLayout.addView(wifiListImageView, wifiLineParams);
                                    tempTopValue += 51;
                                }
                            }
                        }
                        else
                        {
                            statusImageView.setBackgroundResource(R.drawable.set_off);
                        }
                    }
                    scrollSetting.addView(relativeLayout);
                }
                    break;
                case 4://关于
                {
                    //创建一个线程去更新版本号
                    if((!mDownLoadStatus.isGetting) && (mDownLoadStatus.status == 0))
                    {
                        new Thread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                try
                                {
                                    mDownLoadStatus.isGetting = true;
                                    mDownLoadStatus.onInit();
                                    mDownLoadStatus.status = 1;
                                    PublicUse.onPrintLogToJni("正在服务器版本……");
                                    URL url = new URL("http://" + PublicUse.mJniFunCB.onGetJniServerIP() + "/dists/gateway/winoble.txt");
                                    //打开连接
                                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                                    if(200 == urlConnection.getResponseCode())
                                    {
                                        mDownLoadStatus.status = 2;
                                        //得到输入流
                                        InputStream is =urlConnection.getInputStream();
                                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                        byte[] buffer = new byte[1024];
                                        int len = 0;
                                        while(-1 != (len = is.read(buffer))){
                                            baos.write(buffer,0,len);
                                            baos.flush();
                                        }
                                        try
                                        {
                                            JSONObject tempJson = new JSONObject(baos.toString("utf-8"));
                                            if(!tempJson.isNull("debug"))
                                            {
                                                mDownLoadStatus.debug = tempJson.getString("debug");
                                            }
                                            if(!tempJson.isNull("release"))
                                            {
                                                mDownLoadStatus.release = tempJson.getString("release");
                                            }
                                            if(!tempJson.isNull("app_ver"))
                                            {
                                                mDownLoadStatus.app_ver = tempJson.getString("app_ver");
                                            }
                                            if(!tempJson.isNull("md5sum"))
                                            {
                                                mDownLoadStatus.md5sum = tempJson.getString("md5sum");
                                            }
                                            if(!tempJson.isNull("md5sum_release"))
                                            {
                                                mDownLoadStatus.md5sum_release = tempJson.getString("md5sum_release");
                                            }

                                            mDownLoadStatus.status = 3;//更新完成
                                            PublicUse.onPrintLogToJni("服务器版本获取成功!");
                                        } catch (JSONException e)
                                        {
                                            e.printStackTrace();
                                        }
                                    }
                                    else
                                    {
                                        mDownLoadStatus.status = -1;
                                        PublicUse.onPrintLogToJni("服务器版本获取失败!");
                                    }
                                }  catch (IOException e)
                                {
                                    e.printStackTrace();
                                    mDownLoadStatus.status = -2;
                                    PublicUse.onPrintLogToJni("服务器版本获取异常!");
                                }
                                //发送一消息更新界面
                                while((lastSelectIndex == 4) && (mIsChangging == true))
                                {
                                    //等待一下
                                    try
                                    {
                                        Thread.sleep(10);
                                    } catch (InterruptedException e)
                                    {
                                        e.printStackTrace();
                                    }
                                }
                                if((lastSelectIndex == 4) && (mIsChangging == false))
                                {
                                    lastSelectIndex = 3;
                                    handler.sendEmptyMessage(3);//更新界面
                                }
                                mDownLoadStatus.isGetting = false;
                            }
                        }).start();
                    }
                    findViewById(R.id.image_set_about).setBackgroundResource(R.drawable.set_about_on);
                    scrollSetting.removeAllViews();
                    relativeLayout.removeAllViews();
                    //添加log
                    int tempTopValue = 0;
                    Bitmap bitmap = null;
                    if(PublicUse.onCheckFileIsExit("/sdcard/" + PublicUse.mJniFunCB.onGetJniSerial() + ".png"))
                    {
                        try {
                            FileInputStream fis = new FileInputStream("/sdcard/" + PublicUse.mJniFunCB.onGetJniSerial() + ".png");
                            bitmap = BitmapFactory.decodeStream(fis);  ///把流转化为Bitmap图片

                        }
                        catch (FileNotFoundException e)
                        {
                            e.printStackTrace();
                        }
                        if(bitmap != null)
                        {
                            //添加一个二维码
                            ImageView winobleQRCode = new ImageView(this);
                            winobleQRCode.setImageBitmap(bitmap);
                            RelativeLayout.LayoutParams winobleQRCodeParams = new RelativeLayout.LayoutParams(138, 138);
                            winobleQRCodeParams.topMargin = 36 + tempTopValue;
                            winobleQRCodeParams.leftMargin = 107;
                            relativeLayout.addView(winobleQRCode, winobleQRCodeParams);
                        }
                    }
                    if(bitmap == null)
                    {
                        ImageView winobleQRCode = new ImageView(this);
                        winobleQRCode.setBackgroundResource(R.drawable.set_about_qrcode);
                        RelativeLayout.LayoutParams winobleQRCodeParams = new RelativeLayout.LayoutParams(138, 138);
                        winobleQRCodeParams.topMargin = 36 + tempTopValue;
                        winobleQRCodeParams.leftMargin = 107;
                        relativeLayout.addView(winobleQRCode, winobleQRCodeParams);
                    }

                    //添加文件描述
                    TextView qrcodeText = new TextView(this);
                    qrcodeText.setTextColor(Color.WHITE);
                    qrcodeText.setText("扫一扫二维码，加入家庭");
                    RelativeLayout.LayoutParams qrcodeTextParams = new RelativeLayout.LayoutParams(180, 40);
                    qrcodeTextParams.topMargin = 10 + 36 + 138 + tempTopValue;
                    qrcodeTextParams.leftMargin = 100;
                    relativeLayout.addView(qrcodeText, qrcodeTextParams);
                    mProductClickCount = 0;
                    relativeLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v)
                        {
                            mProductClickCount++;
                            if(mProductClickCount >= 10)
                            {
                                mProductClickCount = 0;
                                Intent intent = new Intent(Settings.ACTION_SETTINGS);
                                startActivity(intent); // 打开系统设置界面
                            }

                        }
                    });
                    scrollSetting.addView(relativeLayout);

                    tempTopValue += 20 + 50 + 138;

                    //添加软件 版本
                    TextView softVerTextView = new TextView(this);
                    softVerTextView.setTextColor(Color.WHITE);
                    String tempSoftText = "软件版本(正在检查更新……)";
                    String tempSoftValue = "V" + getVersion();
                    switch(mDownLoadStatus.status)
                    {
                        case 0://正在检查更新
                        case 1:
                        case 2:
                            if(PublicUse.isDebug)
                            {
                                tempSoftText     = "软件版本(正在检查更新...)";
                            }
                            else
                            {
                                tempSoftText     = "软件版本(正在检查更新……)";
                            }
                            break;
                        case 3://更新完成
                        {
                            float tempCheckVer = Float.parseFloat(mDownLoadStatus.app_ver);
                            float currentVer = Float.parseFloat(getVersion());
                            if(tempCheckVer > currentVer)
                            {
                                //不相等  更新版本
                                tempSoftText = "软件可用更新:V" + getVersion() + "->V" + mDownLoadStatus.app_ver;
                                tempSoftValue = "点击更新";
                            }
                            else
                            {
                                tempSoftText = "软件版本(已是最新)";
                                tempSoftValue = "V" + getVersion();
                            }

                        }
                            break;
                        default://检查失败
                            tempSoftText = "软件版本(检查失败)";
                            tempSoftValue = "V" + getVersion();
                            break;
                    }
                    softVerTextView.setText(tempSoftText);
                    RelativeLayout.LayoutParams softVerParams = new RelativeLayout.LayoutParams(280, 40);
                    softVerParams.topMargin = 15 + tempTopValue;
                    softVerParams.leftMargin = 20;
                    relativeLayout.addView(softVerTextView, softVerParams);

                    TextView softValueView = new TextView(this);
                    softValueView.setTextColor(Color.WHITE);
                    softValueView.setText(tempSoftValue);
                    RelativeLayout.LayoutParams softValeParams = new RelativeLayout.LayoutParams(100, 40);
                    softValeParams.topMargin = 15 + tempTopValue;
                    softValeParams.leftMargin = 280;
                    relativeLayout.addView(softValueView, softValeParams);
                    softValueView.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {
                            if((mDownLoadStatus.status == 3) && !mDownLoadStatus.app_ver.equals(getVersion()))
                            {

                                //弹出对话框提示升级
                                new AlertDialog.Builder(SettingActivity.this)
                                        .setIcon(R.drawable.alarm)
                                        .setTitle("警告")
                                        .setMessage("升级过程中请耐心等待！")
                                        .setPositiveButton("确定", new DialogInterface.OnClickListener()
                                        {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i)
                                            {
                                                //显示一个进度条。并且规定时间内无法取消
                                                final int MAX_PROGRESS = 100;
                                                final ProgressDialog progressDialog =
                                                        new ProgressDialog(SettingActivity.this);
                                                progressDialog.setProgress(0);
                                                progressDialog.setTitle("正在获取文件");
                                                progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                                                progressDialog.setMax(MAX_PROGRESS);
                                                progressDialog.setCanceledOnTouchOutside(false);
                                                progressDialog.show();
                                                new Thread(new Runnable() {
                                                    @Override
                                                    public void run()
                                                    {
                                                        //先去服务器下载这个apk
                                                        try
                                                        {
                                                            String debugtype = "";
                                                            String md5SumValue = "";
                                                            if(PublicUse.isDebug)
                                                            {
                                                                debugtype = mDownLoadStatus.debug;
                                                                md5SumValue = mDownLoadStatus.md5sum;
                                                            }
                                                            else
                                                            {
                                                                debugtype = mDownLoadStatus.release;
                                                                md5SumValue = mDownLoadStatus.md5sum_release;
                                                            }
                                                            URL url = new URL("http://" + PublicUse.mJniFunCB.onGetJniServerIP() + "/dists/gateway/" + debugtype);
                                                            //打开连接
                                                            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                                                            if(200 == urlConnection.getResponseCode())
                                                            {
                                                                //得到输入流
                                                                InputStream is =urlConnection.getInputStream();
                                                                File file = new File(Environment.getExternalStorageDirectory(), debugtype);
                                                                FileOutputStream fileOutputStream = new FileOutputStream(file);
                                                                //md5sum验证一下么?
                                                                MessageDigest digest = MessageDigest.getInstance("MD5");
                                                                int mLength = urlConnection.getContentLength();
                                                                byte[] buf = new byte[1024];
                                                                int ch = -1;
                                                                int totalDownCount = 0;
                                                                int lastPecent = 0;
                                                                while ((ch = is.read(buf)) != -1)
                                                                {
                                                                    fileOutputStream.write(buf, 0, ch);
                                                                    digest.update(buf, 0, ch);
                                                                    totalDownCount += ch;
                                                                    if((totalDownCount * 100 / mLength) != lastPecent)
                                                                    {
                                                                        lastPecent = (totalDownCount * 100 / mLength);
                                                                        progressDialog.setProgress(lastPecent);
                                                                    }
                                                                }
                                                                BigInteger bigInt = new BigInteger(1, digest.digest());
                                                                String retMD5 = PublicUse.onGetFormatMD5(bigInt.toString(16));
                                                                if(retMD5.equals(md5SumValue))
                                                                {
                                                                    //开始更新app
                                                                    Intent intent = new Intent();
                                                                    intent.setAction(Intent.ACTION_VIEW);
                                                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                                    intent.setDataAndType(Uri.fromFile(new File(Environment
                                                                                    .getExternalStorageDirectory(), debugtype)),
                                                                            "application/vnd.android.package-archive");
                                                                    SettingActivity.this.startActivity(intent);
                                                                }
                                                                else
                                                                {
                                                                    Toast.makeText(PublicUse.GlobalcContext, "文件校验失败!", Toast.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                            else
                                                            {
                                                                Toast.makeText(PublicUse.GlobalcContext, "文件获取失败!", Toast.LENGTH_SHORT).show();
                                                            }
                                                        }  catch (IOException e)
                                                        {
                                                            e.printStackTrace();
                                                            Toast.makeText(PublicUse.GlobalcContext, "文件获取异常!", Toast.LENGTH_SHORT).show();
                                                        } catch (NoSuchAlgorithmException e)
                                                        {
                                                            e.printStackTrace();
                                                        }
                                                        progressDialog.dismiss();
                                                    }
                                                }).start();
                                            }
                                        })
                                        .setNegativeButton("取消", null)
                                        .show();
                            }
                            else
                            {
                                mProductClickCount++;
                            }
                        }
                    });

                    //添加分隔线
                    ImageView winobleLine3 = new ImageView(this);
                    winobleLine3.setBackgroundResource(R.drawable.set_securtiy_line);
                    RelativeLayout.LayoutParams winobleLine3Params = new RelativeLayout.LayoutParams(314, 1);
                    winobleLine3Params.topMargin = 50 + tempTopValue;
                    winobleLine3Params.leftMargin = 20;
                    relativeLayout.addView(winobleLine3, winobleLine3Params);
                    tempTopValue += 51;

                    //打印zigbee网络相关信息
                    TextView textZigbeeInfo = new TextView(this);
                    textZigbeeInfo.setTextColor(Color.WHITE);
                    textZigbeeInfo.setText(PublicUse.mJniFunCB.onGetJniZigbeeNetInfo());
                    RelativeLayout.LayoutParams textZigbeeInfoParams = new RelativeLayout.LayoutParams(314, 80);
                    textZigbeeInfoParams.topMargin = 15 + tempTopValue;
                    textZigbeeInfoParams.leftMargin = 20;
                    relativeLayout.addView(textZigbeeInfo, textZigbeeInfoParams);
                }
                    break;
                default:break;
            }
        }
        mIsChangging = false;
        return true;
    }

    /**
     * 获取版本号
     * @return 当前应用的版本号
     */
    private String getVersion() {
        try {
            PackageManager manager = this.getPackageManager();
            PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
            return info.versionName;
        } catch (Exception e) {
            e.printStackTrace();
            return "1.0";
        }
    }

    String onGetIPAddress(String type)
    {
        String retIP = "未知";
        // 获取本地设备的所有网络接口
        Enumeration<NetworkInterface> enumerationNi = null;
        try
        {
            enumerationNi = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e)
        {
            e.printStackTrace();
        }
        if(enumerationNi != null)
        {
            while (enumerationNi.hasMoreElements()) {
                NetworkInterface networkInterface = enumerationNi.nextElement();
                String interfaceName = networkInterface.getDisplayName();
                // 如果是有限网卡
                if (interfaceName.equals(type))
                {
                    Enumeration<InetAddress> enumIpAddr = networkInterface
                            .getInetAddresses();
                    while (enumIpAddr.hasMoreElements()) {
                        // 返回枚举集合中的下一个IP地址信息
                        InetAddress inetAddress = enumIpAddr.nextElement();
                        // 不是回环地址，并且是ipv4的地址
                        if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address)
                        {
                            retIP = inetAddress.getHostAddress();
                            break;
                        }
                    }
                }
            }
        }
        return retIP;
    }

    private WifiConfiguration createWifiConfig(String ssid, String password, int type)
    {
        //初始化WifiConfiguration
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();

        //指定对应的SSID
        config.SSID = "\"" + ssid + "\"";

        //如果之前有类似的配置
        int retNetID = getNetworkId(ssid);
        if(retNetID >= 0) {
            //则清除旧有配置
            mWifiManager.removeNetwork(retNetID);
        }

        //不需要密码的场景
        if(type == TYPE_NONE) {
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            //以WEP加密的场景
        } else if(type == TYPE_WEP) {
            config.hiddenSSID = true;
            config.wepKeys[0]= "\""+password+"\"";
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
            //以WPA加密的场景，自己测试时，发现热点以WPA2建立时，同样可以用这种配置连接
        } else if(type == TYPE_WPA_WPA2) {
            config.preSharedKey = "\""+password+"\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.status = WifiConfiguration.Status.ENABLED;
        }
        return config;
    }

    private int getNetworkId(String wifissid)
    {
        List<WifiConfiguration> wifiConfigurationList = mWifiManager.getConfiguredNetworks();
        if(wifiConfigurationList != null && wifiConfigurationList.size() != 0)
        {
            for (int i = 0; i < wifiConfigurationList.size(); i++)
            {
                WifiConfiguration wifiConfiguration = wifiConfigurationList.get(i);
                // wifiSSID就是SSID
                if (wifiConfiguration.SSID != null && wifiConfiguration.SSID.equals(wifissid))
                {
                    return wifiConfiguration.networkId;
                }
            }
        }
        return -1;
    }

    String onGetLanguageWithIndex(int index)
    {
        switch(index)
        {
            case 0:
                return "简体中文";
                /*
            case 1:
                return "繁體中文";
            case 2:
                return "English";*/
            default:return "简体中文";
        }
    }
    String onGetZoneWithIndex(int index)
    {
        switch(index)
        {
            case 0:return "UTC+8";
            /*
            case 1:return "UTC+1";
            case 2:return "UTC+2";
            case 3:return "UTC+3";
            case 4:return "UTC+4";
            case 5:return "UTC+5";
            case 6:return "UTC+6";
            case 7:return "UTC+7";
            case 8:return "UTC+8";
            case 9:return "UTC+9";
            case 10:return "UTC+10";
            case 11:return "UTC+11";
            case 12:return "UTC+12";
            case 13:return "UTC-11";
            case 14:return "UTC-10";
            case 15:return "UTC-9";
            case 16:return "UTC-8";
            case 17:return "UTC-7";
            case 18:return "UTC-6";
            case 19:return "UTC-5";
            case 20:return "UTC-4";
            case 21:return "UTC-3";
            case 22:return "UTC-2";
            case 23:return "UTC-1";*/
            default:return "UTC";
        }
    }

    @Override
    public void onClick(View view)
    {
        if(view.getId() == R.id.image_set_return){
            mIsFinish = true;
            PublicUse.mSettingHandler = null;
            finish();
        }else if(view.getId() == R.id.image_set_securtiy){
            onSetImageBg(0);
        }else if(view.getId() == R.id.image_set_zone){
            onSetImageBg(1);
        }else if(view.getId() == R.id.image_set_language){
            onSetImageBg(2);
        }else if(view.getId() == R.id.image_set_wifi){
            onSetImageBg(3);
        }else if(view.getId() == R.id.image_set_about){
            onSetImageBg(4);
        }

    }

    public void onDisAlarmDlg()
    {
        if (mSoundHandle.isPlaying())
        {
            mSoundHandle.stop();
        }
        if((mAlarmDialog != null) && mAlarmDialog.isShowing())
        {
            mAlarmDialog.dismiss();
        }
    }

    public void onAlarmDlg(final int deviceid, int devtype, String msgstr)
    {
        //播放一下声音  如果已经在播放就重新播放
        if (mSoundHandle.isPlaying())
        {
            mSoundHandle.stop();
        }
        mSoundHandle.play();
        String chefangStr = "";
        if((devtype == HYJniService.SUB_DEVICE_TYPE_PIR) || (devtype == HYJniService.SUB_DEVICE_TYPE_DOOR_WINDOE))
        {
            chefangStr = "撤防";
        }
        AlertDialog.Builder tempDialog = new AlertDialog.Builder(this);
        tempDialog.setIcon(R.drawable.alarm);
        tempDialog.setTitle("警告");
        tempDialog.setMessage(msgstr);
        if(chefangStr.length() > 0)
        {
            tempDialog.setPositiveButton(chefangStr,
                    new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            //撤防
                            PublicUse.mJniFunCB.onDisAlarmInfo(deviceid, 2);
                        }
                    });
        }
        tempDialog.setNegativeButton("解除报警", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                //解除报警
                PublicUse.mJniFunCB.onDisAlarmInfo(deviceid, 1);
            }
        });
        if((mAlarmDialog != null) && mAlarmDialog.isShowing())
        {
            mAlarmDialog.dismiss();
        }

        mAlarmDialog = tempDialog.create();
        //mAlarmDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        mAlarmDialog.show();
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        PublicUse.GlobalcContext = this;
    }

    /**
     * 监听Back键按下事件,方法2:
     * 注意:
     * 返回值表示:是否能完全处理该事件
     * 在此处返回false,所以会继续传播该事件.
     * 在具体项目中此处的返回值视情况而定.
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        //Log.i(PublicUse.Tag, "SettingActivity key=" + keyCode);
        if(keyCode == KeyEvent.KEYCODE_BACK)
        {
            //返回UI
            mIsFinish = true;
            PublicUse.mSettingHandler = null;
            finish();
            return true;
        }
        else
        {
            return true;
        }
    }

    @Override
    protected void onDestroy()
    {
        mIsFinish = true;
        PublicUse.mSettingHandler = null;
        mAlarmDialog = null;
        PublicUse.onPrintLogToJni("SettingActivity out!");
        super.onDestroy();
    }
}
