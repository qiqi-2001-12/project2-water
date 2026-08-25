package com.hwellyi.smarthome;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;
import com.bumptech.glide.Glide;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainGatewayActivity extends AppCompatActivity {
    float mSensorValue = 0;
    int mCheckVerCount = 10;
    ViewPager mViewPage;
    private AlertDialog mAlarmDialog = null;
    private Ringtone mSoundHandle;

    private ImageView mQrCodeView;
    private ImageView mReturnView;
    private TextView mZigbeeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mSoundHandle = RingtoneManager.getRingtone(this, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        PublicUse.isDebug = true;
        PublicUse.onPrintLogToJni("isDebug=" + (PublicUse.isDebug ? "TRUE" : "FALSE"));
        setContentView(R.layout.viewpage);


        mQrCodeView = findViewById(R.id.iv_qrCode);
        mReturnView = findViewById(R.id.li_back);
        mReturnView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        mZigbeeView = findViewById(R.id.tv_zigbee);
        if (PublicUse.onCheckFileIsExit("/sdcard/hwellyiconfig.txt")) {
            PublicUse.isDemoFlag = true;
        }
        PublicUse.mainActivity = this;
        //创建适配器对象
        if (PublicUse.mJniFunCB == null) {
            PublicUse.mJniFunCB = new HYJniFunCB();
            PublicUse.mFragmentList.add(new SceneCtrlFragment());
            PublicUse.mFragmentList.add(new MainFragment());
        }

        //启动屏幕开关事件
        handler.sendEmptyMessage(1);
        //延时更新一下jni网络状态
        Message msg = new Message();
        msg.what = 4;
        msg.arg1 = 20;
        handler.sendMessageDelayed(msg, 1000);
        handler.sendEmptyMessageDelayed(200, 1000);

        if (getIntent() != null) {
            String s = getIntent().getStringExtra("main");
            if (s.equals("1")) {
                handler.sendEmptyMessageDelayed(500, 3000);
            }
        }
    }

    public void onJniNotifyCB(int tnotifyid, long tlcmd1, long tlcmd2, long tlvalue, String tstrvalue) {
        if (tnotifyid == HYJniService.JNI_NOTIFY_NET_STATUS) {
            Message msg = new Message();
            msg.what = 4;
            msg.arg1 = (int) tlcmd1;
            msg.obj = tstrvalue;
            handler.sendMessage(msg);
        } else if (tnotifyid == HYJniService.JNI_NOTIFY_ALARM) {
            if (tlcmd1 > 0) {
                //弹出报警
                Message msg = new Message();
                msg.what = 7;
                msg.arg1 = (int) tlcmd1;//设备ID
                msg.arg2 = (int) tlvalue;//设备类型
                msg.obj = tstrvalue;
                mSensorValue = 10000;
                handler.sendEmptyMessage(2);
                if (PublicUse.GlobalcContext == MainGatewayActivity.this) {
                    handler.removeMessages(7);
                    handler.sendMessage(msg);
                } else {
                    if (PublicUse.mSettingHandler != null) {
                        PublicUse.mSettingHandler.removeMessages(7);
                        PublicUse.mSettingHandler.sendMessage(msg);
                    }
                }
            } else {
                if (PublicUse.GlobalcContext == MainGatewayActivity.this) {
                    handler.sendEmptyMessage(8);
                } else {
                    if (PublicUse.mSettingHandler != null) {
                        PublicUse.mSettingHandler.sendEmptyMessage(8);
                    }
                }
            }
        } else if (tnotifyid == HYJniService.JNI_NOTIFY_UPDATE_DEVSTAUS) {
            if ((tlvalue == HYJniService.SUB_DEVICE_TYPE_DOOR_WINDOE) || (tlvalue == HYJniService.SUB_DEVICE_TYPE_PIR)) {
                //如果安防在线就更新一下安防列表
                if (PublicUse.mSettingHandler != null) {
                    //发送一个消息更新一下列表
                    PublicUse.mSettingHandler.sendEmptyMessage(4);
                }
            }
        } else if (tnotifyid == HYJniService.JNI_NOTIFY_UPDATE_DEVNAME) {
            if ((tlvalue == HYJniService.SUB_DEVICE_TYPE_DOOR_WINDOE) || (tlvalue == HYJniService.SUB_DEVICE_TYPE_PIR)) {
                //如果安防在线就更新一下安防列表
                if (PublicUse.mSettingHandler != null) {
                    //发送一个消息更新一下列表
                    PublicUse.mSettingHandler.sendEmptyMessage(4);
                }
            }
        } else if ((tnotifyid == HYJniService.JNI_NOTIFY_UPDATE_SCENELIST) | (tnotifyid == HYJniService.JNI_NOTIFY_UPDATE_SCENENAME)) {
            Log.i(PublicUse.Tag, String.valueOf(mViewPage.getCurrentItem()));
            if (mViewPage.getCurrentItem() == 0) {
                SceneCtrlFragment tempSceneCtrlFragment = (SceneCtrlFragment) PublicUse.mFragmentList.get(0);
                if (tempSceneCtrlFragment != null) {
                    tempSceneCtrlFragment.onUpdateSceneView(false, tempSceneCtrlFragment.getContext());
                }
            }
        } else if (tnotifyid == HYJniService.JNI_NOTIFY_TEST) {
            if (tlcmd1 == 1) {
                int[] ttx = new int[1];
                ttx[2] = 1;
            }
        }
    }

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

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1://唤醒屏幕事件
                    // onCheckSensorValue();
                    //并且还要延时一个自动消息
                    sendEmptyMessageDelayed(1, 1000);//1s 一次延时来处理待机问题
                    mCheckVerCount--;
                    if (mCheckVerCount <= 0) {
                        mCheckVerCount = 3600 * 24;//一天后再检查
                        //检查一下版本
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                //先去服务器下载这个apk
                                try {
                                    URL url = new URL("http://" + PublicUse.mJniFunCB.onGetJniServerIP() + "/dists/gateway/winoble.txt");
                                    //打开连接
                                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                                    if (200 == urlConnection.getResponseCode()) {
                                        //得到输入流
                                        InputStream is = urlConnection.getInputStream();
                                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                        byte[] buffer = new byte[1024];
                                        int len = 0;
                                        while (-1 != (len = is.read(buffer))) {
                                            baos.write(buffer, 0, len);
                                            baos.flush();
                                        }
                                        //读到正确的数据 解析一下jason 如果当前版本小于提示版本就提示  小于最低版本就强制升级
                                        try {
                                            String appCheckVer = "";
                                            JSONObject tempJson = new JSONObject(baos.toString("utf-8"));
                                            if (!tempJson.isNull("app_checkver")) {
                                                appCheckVer = tempJson.getString("app_checkver");
                                            }
                                            if (appCheckVer.length() > 0) {
                                                //转换成一个
                                                float tempCheckVer = Float.parseFloat(appCheckVer);
                                                float currentVer = Float.parseFloat(getVersion());
                                                if (currentVer < tempCheckVer) {
                                                    PublicUse.mainActivity.handler.sendEmptyMessage(10);
                                                }
                                            }

                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                    }
                    break;
                case 2:
                    break;
                case 4:
                    if (msg.arg1 == 0) {
                        Toast.makeText(PublicUse.GlobalcContext, "连接成功!", Toast.LENGTH_SHORT).show();
                        Log.i(PublicUse.Tag, PublicUse.mJniFunCB.onGetJniSerial() + "=====" + PublicUse.mJniFunCB.onGetJniServerIP());
                        String fileName = "/sdcard/" + PublicUse.mJniFunCB.onGetJniSerial() + ".png";
                        File winobeQrcode = new File(fileName);
                        if (winobeQrcode.exists()) {
                            winobeQrcode.delete();
                        }

                        if (!PublicUse.onCheckFileIsExit(fileName)) {
                            //检查一下二维码是否存在，不存在就生成一张
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        URL url = new URL("http://" + PublicUse.mJniFunCB.onGetJniServerIP() + "/qrcode/gen?w=138&c=" + PublicUse.mJniFunCB.onGetJniSerial());
                                        //打开连接
                                        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                                        if (200 == urlConnection.getResponseCode()) {
                                            // Log.i(PublicUse.Tag, PublicUse.mJniFunCB.onGetJniSerial() + "11111" + PublicUse.mJniFunCB.onGetJniServerIP());
                                            //得到输入流
                                            InputStream is = urlConnection.getInputStream();
                                            File file = new File(Environment.getExternalStorageDirectory(), PublicUse.mJniFunCB.onGetJniSerial() + ".png");
                                            FileOutputStream fileOutputStream = new FileOutputStream(file);
                                            byte[] buf = new byte[1024];
                                            int ch = -1;
                                            while ((ch = is.read(buf)) != -1) {
                                                fileOutputStream.write(buf, 0, ch);
                                            }
                                            handler.sendEmptyMessageDelayed(200, 500);
                                        }
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }).start();
                        }

                    } else if (msg.arg1 < 10) {
                        MainFragment tempmainFragment = (MainFragment) PublicUse.mFragmentList.get(1);
                        if ((tempmainFragment != null) && (tempmainFragment.mBtnSet != null)) {
                            tempmainFragment.mBtnSet.setBackgroundResource(R.drawable.animatormainset_ex);
                        }
                        Toast.makeText(PublicUse.GlobalcContext, (String) msg.obj, Toast.LENGTH_SHORT).show();
                    } else if (msg.arg1 == 20) {
                        //检查一下网络状态
                        MainFragment tempmainFragment = (MainFragment) PublicUse.mFragmentList.get(1);
                        if ((tempmainFragment != null) && (tempmainFragment.mBtnSet != null)) {
                            if (PublicUse.mJniFunCB.onGetNetWorkStatus()) {
                                tempmainFragment.mBtnSet.setBackgroundResource(R.drawable.animatormainset);
                            } else {
                                tempmainFragment.mBtnSet.setBackgroundResource(R.drawable.animatormainset_ex);
                            }
                        } else {
                            //延时更新一下jni网络状态
                            handler.sendEmptyMessageDelayed(20, 3000);
                        }
                    } else if (msg.arg1 == 21) {
                        Toast.makeText(PublicUse.GlobalcContext, (String) msg.obj, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case 200:
                    onSetImageBg();
                    break;
                case 0x06:
                    if (PublicUse.mSettingHandler != null) {
                        PublicUse.mSettingHandler.sendEmptyMessage(2);
                    }
                    break;//安防设备列表有更新
                case 7://弹出报警对话框
                    onAlarmDlg(msg.arg1, msg.arg2, (String) msg.obj);
                    break;
                case 500:
                    finish();
                    break;
                case 8:
                    onDisAlarmDlg();
                    break;
            }
        }
    };


    public void onDisAlarmDlg() {
        if (mSoundHandle.isPlaying()) {
            mSoundHandle.stop();
        }
        if ((mAlarmDialog != null) && mAlarmDialog.isShowing()) {
            mAlarmDialog.dismiss();
        }
    }

    public void onAlarmDlg(final int deviceid, int devtype, String msgstr) {
        if (mSoundHandle.isPlaying()) {
            mSoundHandle.stop();
        }
        mSoundHandle.play();
        String chefangStr = "";
        if ((devtype == HYJniService.SUB_DEVICE_TYPE_PIR) || (devtype == HYJniService.SUB_DEVICE_TYPE_DOOR_WINDOE)) {
            chefangStr = "撤防";
        }
        AlertDialog.Builder tempDialog = new AlertDialog.Builder(this);
        tempDialog.setIcon(R.drawable.alarm);
        tempDialog.setTitle("警告");
        tempDialog.setMessage(msgstr);
        if (chefangStr.length() > 0) {
            tempDialog.setPositiveButton(chefangStr, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //撤防
                    PublicUse.mJniFunCB.onDisAlarmInfo(deviceid, 2);
                }
            });
        }
        tempDialog.setNegativeButton("解除报警", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //解除报警
                PublicUse.mJniFunCB.onDisAlarmInfo(deviceid, 1);
            }
        });
        if ((mAlarmDialog != null) && mAlarmDialog.isShowing()) {
            mAlarmDialog.dismiss();
        }

        mAlarmDialog = tempDialog.create();
    }

    boolean onSetImageBg() {

        if (isFinishing() || isDestroyed()) {
            return false;
        }

        File qrFile = new File("/sdcard/" + PublicUse.mJniFunCB.onGetJniSerial() + ".png");
        if (qrFile.exists()) {
            Glide.with(this).load(qrFile).placeholder(R.drawable.set_about_qrcode).into(mQrCodeView);
        }
        mZigbeeView.setText(PublicUse.mJniFunCB.onGetJniZigbeeNetInfo());
        return true;
    }


    @Override
    protected void onStart() {
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
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            return true;
        } else {
            return true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }
}
