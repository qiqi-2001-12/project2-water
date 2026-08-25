package com.hy.greenbuilding.ui.fragment;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hwellyi.smarthome.MainGatewayActivity;
import com.hwellyi.smarthome.PublicUse;
import com.hy.greenbuilding.HyApplication;
import com.hy.greenbuilding.R;
import com.hy.greenbuilding.config.SaveAddress;
import com.hy.greenbuilding.config.SaveControlInfo;
import com.hy.greenbuilding.config.SaveFilterScreen;
import com.hy.greenbuilding.config.SaveTimingInfo;
import com.hy.greenbuilding.event.DefrostChangeEvent;
import com.hy.greenbuilding.event.ModeSwitchUpdateEvent;
import com.hy.greenbuilding.event.OTAErrorEvent;
import com.hy.greenbuilding.event.OTAStatusEvent;
import com.hy.greenbuilding.event.ReceiveMcuDataEvent;
import com.hy.greenbuilding.event.RoomChangeEvent;
import com.hy.greenbuilding.event.RunModeEvent;
import com.hy.greenbuilding.event.SetStatusEvent;
import com.hy.greenbuilding.event.SettingUpdateEvent;
import com.hy.greenbuilding.event.TempControlEvent;
import com.hy.greenbuilding.event.TempStatusUpdateEvent;
import com.hy.greenbuilding.event.TempSwitchEvent;
import com.hy.greenbuilding.event.TempSwitchUpdateEvent;
import com.hy.greenbuilding.event.VersionUpdateEvent;
import com.hy.greenbuilding.event.WeatherDataEvent;
import com.hy.greenbuilding.model.FanDataInfo;
import com.hy.greenbuilding.model.FanTypeCount;
import com.hy.greenbuilding.model.MqttUploadInfo;
import com.hy.greenbuilding.model.RoomInfo;
import com.hy.greenbuilding.mqtt.HDTopic;
import com.hy.greenbuilding.mqtt.HXTopic;
import com.hy.greenbuilding.mqtt.HyServiceConnection;
import com.hy.greenbuilding.mqtt.IGetMessageCallBack;
import com.hy.greenbuilding.mqtt.MqttUploadManager;
import com.hy.greenbuilding.mqtt.MyMqttService;
import com.hy.greenbuilding.protocol.FunctionObject;
import com.hy.greenbuilding.protocol.ResPonseInfo.CO2StatusInfo;
import com.hy.greenbuilding.protocol.ResPonseInfo.CustomDataInfo;
import com.hy.greenbuilding.protocol.ResPonseInfo.DCFanStatusInfo;
import com.hy.greenbuilding.protocol.ResPonseInfo.ElectricityMeterInfo;
import com.hy.greenbuilding.protocol.ResPonseInfo.EnvironmentDataInfo;
import com.hy.greenbuilding.protocol.ResPonseInfo.FanStatusInfo;
import com.hy.greenbuilding.protocol.ResPonseInfo.MainControlInfo;
import com.hy.greenbuilding.protocol.ResPonseInfo.OutDoorStatusInfo;
import com.hy.greenbuilding.protocol.ResPonseInfo.PIDStatusInfo;
import com.hy.greenbuilding.protocol.ResPonseInfo.PVStatusInfo;
import com.hy.greenbuilding.protocol.ResPonseInfo.UpTempStatusInfo;
import com.hy.greenbuilding.protocol.SpDataProcessor;
import com.hy.greenbuilding.protocol.command.ControlCommand;
import com.hy.greenbuilding.protocol.command.CustomCommand;
import com.hy.greenbuilding.protocol.command.DCFanCommand;
import com.hy.greenbuilding.protocol.command.EnvironmentCommand;
import com.hy.greenbuilding.protocol.command.FanCommand;
import com.hy.greenbuilding.protocol.command.LowTempCommand;
import com.hy.greenbuilding.protocol.command.MeterCommand;
import com.hy.greenbuilding.protocol.command.OTARequestCommand;
import com.hy.greenbuilding.protocol.command.PIDCommand;
import com.hy.greenbuilding.protocol.command.PVCommand;
import com.hy.greenbuilding.protocol.command.UpTempCommand;
import com.hy.greenbuilding.ui.widget.SaturationView;
import com.hy.greenbuilding.utils.AppManagerUtil;
import com.hy.greenbuilding.utils.ByteUtils;
import com.hy.greenbuilding.utils.Hex;
import com.hy.greenbuilding.utils.InputLimitUtil;
import com.hy.greenbuilding.utils.MySpUtil;
import com.hy.greenbuilding.utils.NetworkStatus;
import com.hy.greenbuilding.utils.PackageUtil;
import com.hy.greenbuilding.utils.StringUtils;
import com.hy.greenbuilding.utils.TimingUtils;
import com.hy.greenbuilding.utils.ToastUtil;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class HomeFragment extends Fragment implements IGetMessageCallBack, SettingTimeSetDialogFragment.TimingSwitchListener, View.OnClickListener {

    @BindView(R.id.ll_element)
    RelativeLayout llElement;

    @BindView(R.id.ll_data)
    LinearLayout llData;

    @BindView(R.id.ll_classic_mode)
    LinearLayout llClassicMode;
    @BindView(R.id.ll_caring_mode)
    LinearLayout llCaringMode;

    @BindView(R.id.ll_bottom)
    LinearLayout llBottom;
    @BindView(R.id.ll_home)
    LinearLayout llHome;
    @BindView(R.id.tv_element)
    TextView tvElement;
    @BindView(R.id.tv_time)
    TextView mTimeView;
    @BindView(R.id.close_image)
    ImageView closeImage;
    @BindView(R.id.tv_date)
    TextView mDateView;
    @BindView(R.id.tv_termMode)
    TextView mTermModeView;

    @BindView(R.id.tv_tempMode)
    TextView mTempModeView;
    @BindView(R.id.iv_tempMode)
    ImageView mTempModeBg;
    @BindView(R.id.set_wind_switch)
    LinearLayout mWindSwitch;
    @BindView(R.id.set_circle_switch)
    LinearLayout mCircleSwitch;
    RadioButton mWindSmall;//新风低
    RadioButton mWindMiddle;//新风中
    RadioButton mWindHigh;//新风高
    RadioButton btWindNo;//新风关
    RadioButton btCircleNo;//内循环关
    RadioButton mCircleSmall;//内循环低
    RadioButton mCircleMiddle;//内循环中
    RadioButton mCircleHigh;//内循环高
    @BindView(R.id.bt_runMode)
    LinearLayout mRunModeButton;

    @BindView(R.id.tv_runMode)
    TextView tvRunMode;
    @BindView(R.id.bt_timing)
    LinearLayout mTimingButton;
    @BindView(R.id.bt_locking)
    LinearLayout mLockingButton;
    @BindView(R.id.tv_locking)
    TextView tvLocking;
    @BindView(R.id.tv_timing)
    TextView tvTiming;
    @BindView(R.id.tv_weather_temp)
    TextView mWeatherTempView;
    @BindView(R.id.tv_pollution)
    TextView tvPollution;
    @BindView(R.id.image_temp)
    ImageView imageTemp;
    @BindView(R.id.tv_inRoom_temp)
    TextView mRoomTemp;
    @BindView(R.id.tv_inRoom_humidity)
    TextView mRoomHumidity;
    @BindView(R.id.tv_inRoom_co2)
    TextView mRoomCo2;
    @BindView(R.id.tv_inRoom_pm)
    TextView mRoomPm;
    @BindView(R.id.tv_menu)
    TextView tvMenu;
    @BindView(R.id.radio_group1)
    LinearLayout radioGroup1;
    @BindView(R.id.radio_group2)
    LinearLayout radioGroup2;

    @BindView(R.id.ll_heating)
    LinearLayout mLayHeating; // 制热
    @BindView(R.id.ll_dehumidification)
    LinearLayout mLayDehumidification; // 除湿
    @BindView(R.id.ll_refrigeration)
    LinearLayout llRefrigeration; // 制冷
    @BindView(R.id.iv_wifi)
    ImageView mWifiIcon;
    @BindView(R.id.outTerm_error_info)
    TextView outTermError;
    @BindView(R.id.room_error_info)
    TextView roomError;
    @BindView(R.id.tv_element_pop)
    TextView tvElementPop;
    @BindView(R.id.ntc_error_info)
    TextView ntcErrorInfo;
    @BindView(R.id.tv_set_wind_switch)
    TextView tvSetWindSwitch;
    @BindView(R.id.tv_set_circle_switch)
    TextView tvSetCircleSwitch;
    @BindView(R.id.tv_switch)
    TextView tvSwitch;
    @BindView(R.id.close_tv)
    LinearLayout mCloseTv;

    @BindView(R.id.image_runMode)
    ImageView imageRunMode;
    private Timer mTimer;
    private List<RoomInfo> roomList = new ArrayList<>();
    private HyServiceConnection serviceConnection;
    private int mRunMode = 2;//运行模式(1.主动模式，2.自动模式)
    private int windStatus = 1;//新风状态(1.低，2.中,3.高)
    private int circleStatus = 1;//循环风状态(1.低，2.中,3.高)
    private boolean isTiming;//定时开启状态
    private static final int ACTION_REQUEST_PERMISSIONS = 0x001;
    private boolean isUpdating;//升级状态
    NetWorkChangeReceiver netWorkChangeReceiver;
    private String appVersion = "";//APP版本号
    private String controlVersion = "";//主板程序版本号
    private boolean isAnimation;
    private List<FanDataInfo> fanList = new ArrayList<>();
    private SaveTimingInfo timingInfo;
    private int SEND_MESSAGE_DELAY1 = 15 * 1000;
    private static final long LONG_PRESS_TIME_MS_3 = 3 * 1000;
    private long mDuration = -1;
    private static String[] NEEDED_PERMISSIONS = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
    };
    EnvironmentCommand getEnvironmentCommand;
    FanCommand getFanCommand;
    ControlCommand getControlCommand;
    HDTopic hdTopic;
    HXTopic hxTopic;
    private PopupWindow popupWindowWind;
    private boolean mTempSwitch;
    private int manualMode; //手动模式
    private DCFanStatusInfo dcFanStatusInfo;
    private boolean humiditySwitch;
    private SettingTimeSetDialogFragment dialogFragment;

    public HomeFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        serviceConnection = new HyServiceConnection();
        serviceConnection.setIGetMessageCallBack(this);

        hdTopic = MqttUploadManager.getInstance().getmHDTopic();
        hxTopic = MqttUploadManager.getInstance().getmHxTopic();

        View inflate = inflater.inflate(R.layout.fragment_home, container, false);
        if (!checkPermissions(NEEDED_PERMISSIONS)) {
            ActivityCompat.requestPermissions(getActivity(), NEEDED_PERMISSIONS, ACTION_REQUEST_PERMISSIONS);
        } else {
            startGatewayActivity();
        }


        ButterKnife.bind(this, inflate);
        initView();
        return inflate;
    }

    public void resetToDefaultView() {
        llData.setVisibility(View.VISIBLE);
        boolean isCareMode = (boolean) MySpUtil.getParam(getActivity(), MySpUtil.CARE_MODE, false);
        updateLayoutForCareMode(isCareMode);
    }


    private void initView() {
        init();
        NetworkStatus.registerNetworkListener(getActivity(), isConnected -> {
            // 仅在网络状态变化时执行逻辑
            getActivity().runOnUiThread(() -> {
                mWifiIcon.setVisibility(isConnected ? View.VISIBLE : View.INVISIBLE);
            });
        });

        //关闭低功耗命令
        ControlCommand controlCommand = new ControlCommand(FunctionObject.SET_LOW_POWER);
        byte[] sendData = {(byte) 0x00};
        controlCommand.setData(sendData);
        HyApplication.setIsReboot(false);
        SpDataProcessor.getInstance().send(controlCommand);

        //发送环境检测命令
        getEnvironmentCommand = new EnvironmentCommand(FunctionObject.GET_ENVIRONMENT_STATUS);
        SpDataProcessor.getInstance().send(getEnvironmentCommand);

        getFanCommand = new FanCommand(FunctionObject.GET_FAN_STATUS);
        SpDataProcessor.getInstance().send(getFanCommand);

        //获取杂项数据
        CustomCommand command = new CustomCommand(FunctionObject.GET_CUSTOM_DATA);
        SpDataProcessor.getInstance().send(command);

        getControlCommand = new ControlCommand(FunctionObject.GET_CONTROL_STATUS);
        mHandler.removeCallbacks(mRunnable);
        mHandler.postDelayed(mRunnable, SEND_MESSAGE_DELAY1);

        boolean param = (boolean) MySpUtil.getParam(getActivity(), MySpUtil.CLOSE_STATUS, true);
        hxTopic.setSystemSwitch((byte) ((param ? 1 : 0) & 0xff));

        int manualMode = (int) MySpUtil.getParam(getActivity(), MySpUtil.MANUAL_Mode_STATUS, 0);//保存主控板数据
        mRunMode = ((int) MySpUtil.getParam(getActivity(), MySpUtil.RUN_Mode_STATUS, 0) == 0) ? 2 : 1;//保存主控板数据
        mRunModeButton.setSelected(true);
        tvRunMode.setText(mRunMode == 2 ? "自动" : "手动");
        imageRunMode.setImageResource(mRunMode == 2 ? R.drawable.icon_auto_white : R.drawable.icon_manual_white);

        MeterCommand meterCommand = new MeterCommand(1);
        SpDataProcessor.getInstance().send(meterCommand);


        if (!mCloseTv.isSelected() || !closeImage.isSelected()) {

            EventBus.getDefault().post(new TempSwitchUpdateEvent(false));
            EventBus.getDefault().post(new TempSwitchEvent(false));

            initSwitchClickable();
            llRefrigeration.setSelected(false);
            mLayHeating.setSelected(false);
            mLayDehumidification.setSelected(false);

            sendManualCommand(false);
            sendWindCommand((byte) 0x00, false);
            sendCircleCommand((byte) 0x02, false);

            mCloseTv.setSelected(false);
            closeImage.setSelected(false);


            closeTempSwitch();
            tvSwitch.setText("关");
        } else {
            tvSwitch.setText("开");
            mCloseTv.setSelected(true);
            closeImage.setSelected(true);

            boolean isSaveTiming = (boolean) MySpUtil.getParam(getActivity(), MySpUtil.TIMING_STATUS, false);
            if (isSaveTiming) {
                int hour = Integer.parseInt((StringUtils.simpleDateFormat3.format(new Date())));
                if (!TimingUtils.timeValueIsNull(getActivity())) {
                    openTiming(hour);
                }
            } else {
                if (mRunMode == 2) {
                    //发送自动命令
                    if (!isTiming && !isOtaOpen) {
                        sendAutoCommand(false);
                    }
                } else if (mRunMode == 1) {
                    ControlCommand controlCommand1 = new ControlCommand(FunctionObject.SET_CONTROL_MODE);
                    if (isTiming) {
                        byte[] sendData1 = {(byte) 0x01, (byte) 0x01, (byte) manualMode};
                        controlCommand1.setData(sendData1);
                    } else {
                        byte[] sendData1 = {(byte) 0x00, (byte) 0x01, (byte) manualMode};
                        controlCommand1.setData(sendData1);
                    }
                    SpDataProcessor.getInstance().send(controlCommand1);

                    EventBus.getDefault().post(new TempSwitchUpdateEvent((boolean) MySpUtil.getParam(getActivity(), MySpUtil.TEMP_SWITCH, false)));
                    EventBus.getDefault().post(new TempSwitchEvent((boolean) MySpUtil.getParam(getActivity(), MySpUtil.TEMP_SWITCH, false)));

                    windStatus = (int) MySpUtil.getParam(getActivity(), MySpUtil.WIND_STATUS, 0);
                    sendWindCommand((byte) 0x00, true);
                    circleStatus = (int) MySpUtil.getParam(getActivity(), MySpUtil.CIRCLE_STATUS, 0);
                    Log.e("TAG", "initView: " + circleStatus);
                    sendCircleCommand((byte) 0x02, true);
                    //切换手动模式时获取风机状态
                    SpDataProcessor.getInstance().send(getFanCommand);
                }
            }
        }


        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        netWorkChangeReceiver = new NetWorkChangeReceiver();
        getActivity().registerReceiver(netWorkChangeReceiver, intentFilter);

        Intent intent = new Intent(getActivity(), MyMqttService.class);
        getActivity().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

        mLockingButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // 记录起始点的坐标
                        mDuration = System.currentTimeMillis();
                        break;
                    case MotionEvent.ACTION_UP:
                        // 判断是否是长按的结束点
                        if (System.currentTimeMillis() - mDuration > LONG_PRESS_TIME_MS_3) {
                            if (HyApplication.isLocking) {
                                mLockingButton.setSelected(false);
                                tvLocking.setText("关");
                                HyApplication.isLocking = false;
                                initSwitchClickable();
                                View rootView = getActivity().getWindow().getDecorView();
                                SaturationView.getInstance().saturationView(rootView, 1f);
                            } else {
                                mLockingButton.setSelected(true);
                                tvLocking.setText("开");
                                HyApplication.isLocking = true;
                                initSwitchClickable();
                                View rootView = getActivity().getWindow().getDecorView();
                                SaturationView.getInstance().saturationView(rootView, 0.5f);
                            }
                        }
                        break;
                }
                return true;
            }
        });

        mCloseTv.setSelected(param);
        closeImage.setSelected(param);
        tvSwitch.setText(param ? "开" : "关");
    }


    public void onRefrigerationRadioButtonClick(View view, int mode) {
        View layout;
        if (mode == 0) {
            // 新风
            layout = LayoutInflater.from(getActivity()).inflate(R.layout.popup_refrigeration_menu, null, false);
            mWindSmall = layout.findViewById(R.id.bt_wind_small);
            mWindMiddle = layout.findViewById(R.id.bt_wind_middle);
            mWindHigh = layout.findViewById(R.id.bt_wind_high);
            btWindNo = layout.findViewById(R.id.bt_wind_no);

            // 高中低点击：设置主按钮选中，并执行对应速度逻辑
            mWindSmall.setOnClickListener(v -> {
                mWindSwitch.setSelected(true);
                tvSetWindSwitch.setText("低");
                onWindSmall();
            });

            mWindMiddle.setOnClickListener(v -> {
                mWindSwitch.setSelected(true);
                tvSetWindSwitch.setText("中");
                onWindMiddleClick();
            });

            mWindHigh.setOnClickListener(v -> {
                mWindSwitch.setSelected(true);
                tvSetWindSwitch.setText("高");
                onWindHighClick();
            });

            if (windStatus == 1) {
                mWindSmall.setChecked(true);
            } else if (windStatus == 2) {
                mWindMiddle.setChecked(true);
            } else if (windStatus == 3) {
                mWindHigh.setChecked(true);
            } else {
                btWindNo.setChecked(true);
            }

            // 关按钮点击：取消主按钮选中，发送关闭命令
            btWindNo.setOnClickListener(v -> {
                mWindSwitch.setSelected(false);
                tvSetWindSwitch.setText("关");
                windStatus = 0;
                sendWindCommand((byte) 0x00, false);
                btWindNo.setChecked(true);
                popupWindowWind.dismiss();
            });
        } else {
            // 循环/净化
            layout = LayoutInflater.from(getActivity()).inflate(R.layout.popup_refrigeration_circle_menu, null, false);
            mCircleSmall = layout.findViewById(R.id.bt_circle_small);
            mCircleMiddle = layout.findViewById(R.id.bt_circle_middle);
            mCircleHigh = layout.findViewById(R.id.bt_circle_high);
            btCircleNo = layout.findViewById(R.id.bt_circle_no);

            // 高中低点击：设置主按钮选中，并执行对应速度逻辑
            mCircleSmall.setOnClickListener(v -> {
                mCircleSwitch.setSelected(true);
                tvSetCircleSwitch.setText("低");
                onCircleSmallClick(v);
            });
            mCircleMiddle.setOnClickListener(v -> {
                mCircleSwitch.setSelected(true);
                tvSetCircleSwitch.setText("中");
                onCircleMiddleClick(v);
            });
            mCircleHigh.setOnClickListener(v -> {
                mCircleSwitch.setSelected(true);
                tvSetCircleSwitch.setText("高");
                onCircleHighClick(v);
            });

            if (circleStatus == 1) {
                mCircleSmall.setChecked(true);
            } else if (circleStatus == 2) {
                mCircleMiddle.setChecked(true);
            } else if (circleStatus == 3) {
                mCircleHigh.setChecked(true);
            } else {
                btCircleNo.setChecked(true);
            }

            // 关按钮点击：取消主按钮选中，发送关闭命令，并处理互斥
            btCircleNo.setOnClickListener(v -> {
                if (mRunMode != 0) {
                    EventBus.getDefault().post(new TempSwitchUpdateEvent(false));
                    EventBus.getDefault().post(new TempSwitchEvent(false));
                    // 关闭调温/制冷/制热/除湿开关
                }

                popupWindowWind.dismiss();
                // 正常的关闭操作
                mCircleSwitch.setSelected(false);
                tvSetCircleSwitch.setText("关");
                circleStatus = 0;
                sendCircleCommand((byte) 0x02, false);
            });

        }


        boolean isCareMode = (boolean) MySpUtil.getParam(getActivity(), MySpUtil.CARE_MODE, false);
        int width = 0;
        if (isCareMode) {
            width = (int) (view.getWidth() * 2.0 / 3.0);
        } else {
            width = view.getWidth();
        }

        popupWindowWind = new PopupWindow(layout, width, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindowWind.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        layout.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);

        layout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        int popupHeight = layout.getMeasuredHeight();
        int xoff = (view.getWidth() - width) / 2;
        int extraOffset = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics());
        int yoff = -(popupHeight + view.getHeight() + extraOffset);
        // 显示 PopUpWindow
        popupWindowWind.showAsDropDown(view, xoff, yoff);
    }

//    private void startGatewayActivity() {
//        mHandler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                if (!HyApplication.isCare) {
//                    Intent intent1 = new Intent(getActivity(), MainGatewayActivity.class);
//                    intent1.putExtra("main", "1");
//                    startActivity(intent1);
//                }
//            }
//        }, 1000);
//    }

    private void startGatewayActivity() {
        // 检查 Fragment 是否已销毁或未挂载
        if (!isAdded() || getActivity() == null) {
            return;
        }

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isAdded() && getActivity() != null && !HyApplication.isCare) {
                    Intent intent1 = new Intent(getActivity(), MainGatewayActivity.class);
                    intent1.putExtra("main", "1");
                    startActivity(intent1);
                }
            }
        }, 1000);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case ACTION_REQUEST_PERMISSIONS:
                startGatewayActivity();
                break;
        }
    }

    @Override
    public void onTimingStateChanged(boolean isTimingOn) {
        setTimingStatus(isTimingOn);

        int hour = Integer.parseInt((StringUtils.simpleDateFormat3.format(new Date())));
        if (isTimingOn) {
            closeTiming();
        } else {
            openTiming(hour);
        }
    }

    @Override
    public void onClick(View v) {

        if (!mCloseTv.isSelected()) {
            ToastUtil.showToast(getActivity(), "请先开机");
            return;
        }

        // 关闭已有弹窗（避免叠加）
        if (popupWindowWind != null && popupWindowWind.isShowing()) {
            popupWindowWind.dismiss();
            popupWindowWind = null;
        }

        if (HyApplication.isLocking || isOtaOpen || isTiming || mRunMode == 2 || InputLimitUtil.isFastDoubleClick()) {
            return;
        }

        if (v.getId() == R.id.set_wind_switch) {
            onRefrigerationRadioButtonClick(mWindSwitch, 0);
        } else if (v.getId() == R.id.set_circle_switch) {
            onRefrigerationRadioButtonClick(mCircleSwitch, 1);
        }
    }

    public class NetWorkChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
                NetworkInfo info = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
                if (info != null) {
                    if (NetworkInfo.State.CONNECTED == info.getState() && info.isAvailable()) {
                        MyMqttService.reConnect(true, true);
                    }
                }
            }
        }
    }

    private void init() {
        mWindSwitch.setOnClickListener(this);
        mCircleSwitch.setOnClickListener(this);
        initSwitchClickable();
        boolean isCareMode = (boolean) MySpUtil.getParam(getActivity(), MySpUtil.CARE_MODE, false);

        updateLayoutForCareMode(isCareMode);

        //定时断电保存
        boolean isSaveTiming = (boolean) MySpUtil.getParam(getActivity(), MySpUtil.TIMING_STATUS, false);
        if (isSaveTiming) {
            int hour = Integer.parseInt((StringUtils.simpleDateFormat3.format(new Date())));
            if (!TimingUtils.timeValueIsNull(getActivity())) {
                openTiming(hour);
            }
        }
        //启用禁用状态
        isOtaOpen = (boolean) MySpUtil.getParam(getActivity(), MySpUtil.OTA_STATUS, false);
        if (isOtaOpen) {
            sendManualCommand(false);
            EventBus.getDefault().post(new TempSwitchUpdateEvent(false));
            EventBus.getDefault().post(new TempSwitchEvent(false));
        }
    }

    public void updateLayoutForCareMode(boolean isCareMode) {

        hxTopic.setSystemInterfaceMode((byte) (isCareMode ? (1 & 0xff) : (0 & 0xff)));

        int newHeightPx;
        int newMarginPx;
        int newMarginRightPx;

        if (isCareMode) {
            // 1. 设置主容器可见性和方向
            llBottom.setVisibility(View.GONE);
            llClassicMode.setVisibility(View.GONE);
            llCaringMode.setVisibility(View.VISIBLE);
            llBottom.setOrientation(LinearLayout.VERTICAL);
            newHeightPx = dpToPx(getActivity(), 130); // 这是一个较大的高度
            newMarginPx = dpToPx(getActivity(), 47);   // 较大的边距
            newMarginRightPx = dpToPx(getActivity(), 56);
            applyRadioGroupLayoutParams(radioGroup1, ViewGroup.LayoutParams.MATCH_PARENT, 0f, newMarginRightPx, newMarginPx, newHeightPx);
            applyRadioGroupLayoutParams(radioGroup2, ViewGroup.LayoutParams.MATCH_PARENT, 0f, newMarginRightPx, newMarginPx, newHeightPx);

            ViewGroup.LayoutParams llParams = llBottom.getLayoutParams();
            llParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            llBottom.setLayoutParams(llParams);
        } else {
            // --- 切换到 经典模式 (水平/一行) ---
            llBottom.setVisibility(View.VISIBLE);
            llClassicMode.setVisibility(View.VISIBLE);
            llCaringMode.setVisibility(View.GONE);
            llBottom.setOrientation(LinearLayout.HORIZONTAL);
            // 2. 定义尺寸
            newHeightPx = dpToPx(getActivity(), 47);
            // 3. 应用参数到 RadioGroup (水平模式：宽度 0dp, 权重 1.0f)
            // 两个 RadioGroup 平分宽度，并且清除 topMargin
            applyRadioGroupLayoutParams(radioGroup1, 0, 1.0f, 15, 0, newHeightPx);
            applyRadioGroupLayoutParams(radioGroup2, 0, 1.0f, 23, 0, newHeightPx);

            // 确保 llBottom 的高度是 WRAP_CONTENT
            ViewGroup.LayoutParams llParams = llBottom.getLayoutParams();
            llParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            llBottom.setLayoutParams(llParams);
        }

        boolean param = (boolean) MySpUtil.getParam(getActivity(), MySpUtil.CLOSE_STATUS, true);
        mCloseTv.setSelected(param);
        closeImage.setSelected(param);
        tvSwitch.setText(param ? "开" : "关");
        hxTopic.setSystemSwitch((byte) ((param ? 1 : 0) & 0xff));
    }
    //-----------------------------------------------------------------

    /**
     * 统一设置 RadioGroup 的布局参数
     *
     * @param rg          目标 RadioGroup
     * @param width       新的宽度 (MATCH_PARENT, WRAP_CONTENT, 或 0)
     * @param weight      新的权重 (0f 或 1.0f)
     * @param rightMargin 新的右边距 (px)
     * @param topMargin   新的上边距 (px)
     * @param height      新的高度 (px)
     */
    private void applyRadioGroupLayoutParams(View rg, int width, float weight, int rightMargin, int topMargin, int height) {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) rg.getLayoutParams();

        // 布局尺寸
        params.width = width;
        params.weight = weight;
        params.height = height;
        // 边距
        params.rightMargin = rightMargin;
        params.leftMargin = topMargin;
        rg.setLayoutParams(params);
    }

    // 辅助方法：将 DP 转换为 PX
    public static int dpToPx(Context context, float dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return (int) (dp * density + 0.5f);
    }

    private void initSwitchClickable() {
        boolean isClickable = true;
        if (mRunMode == 2 || isTiming || HyApplication.isLocking || isOtaOpen) {
            isClickable = false;
        }
//        mWindSwitch.setEnabled(isClickable);
//        mCircleSwitch.setEnabled(isClickable);

    }

    //更新自动按钮
    private void updateAutoButton() {
        mRunMode = 2;

        mRunModeButton.setSelected(true);
        mLayDehumidification.setSelected(false);
        mLayHeating.setSelected(false);
        llRefrigeration.setSelected(false);
        tvRunMode.setText("自动");

//        imageRunMode.setImageResource(R.drawable.select_auto);
        imageRunMode.setImageResource(R.drawable.icon_auto_white);
        initSwitchClickable();
        EventBus.getDefault().post(new ModeSwitchUpdateEvent(true));
    }

    //更新手动按钮
    private void updateManualButton() {
        mRunMode = 1;
        mRunModeButton.setSelected(true);
        tvRunMode.setText("手动");
//        imageRunMode.setImageResource(R.drawable.select_manual);
        imageRunMode.setImageResource(R.drawable.icon_manual_white);
//        mRunModeButton.setBackground(getDrawable(R.drawable.btn_bg_common));
//        mRunModeButton.setText("手动");
//        Drawable drawable = getResources().getDrawable(R.drawable.shoudongtongbu);
//        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
//        mRunModeButton.setCompoundDrawables(null, drawable, null, null);
        initSwitchClickable();
        EventBus.getDefault().post(new ModeSwitchUpdateEvent(false));
//        mTempLl.setBackground(getDrawable(R.drawable.main_bottom_bg));
//        mNewWindLl.setBackground(getDrawable(R.drawable.main_bottom_bg));
//        mCircleLl.setBackground(getDrawable(R.drawable.main_bottom_bg));
    }


    @Override
    public void onResume() {
        super.onResume();
        // 1. 主动校验当前网络状态（首次打开直接用这个值初始化UI）
        mWifiIcon.setVisibility(NetworkStatus.checkState(requireActivity()) ? View.VISIBLE : View.INVISIBLE);

        startTimer();
    }


    @Override
    public void onStart() {
        super.onStart();
        isOtaOpen = (boolean) MySpUtil.getParam(getActivity(), MySpUtil.OTA_STATUS, false);
        if (isOtaOpen) {
            initSwitchClickable();
        }
    }

    @OnClick({R.id.close_tv, R.id.close_image})
    public void onCloseClick(View view) {
        close(mCloseTv.isSelected() || closeImage.isSelected());
    }


    private void close(boolean isClosed) {
        if (HyApplication.isLocking) {
            ToastUtil.showToast(getActivity(), getString(R.string.device_locked));
            return;
        }
        if (isOtaOpen) {
            ToastUtil.showToast(getActivity(), getString(R.string.server_not_permission));
            return;
        }
        if (InputLimitUtil.isFastDoubleClick()) {
            return;
        }
        if (isClosed) {
            initSwitchClickable();
            llRefrigeration.setSelected(false);
            mLayHeating.setSelected(false);
            mLayDehumidification.setSelected(false);
            sendManualCommand(false);
            sendWindCommand((byte) 0x00, false);
            sendCircleCommand((byte) 0x02, false);
            mCloseTv.setSelected(false);
            closeImage.setSelected(false);
            EventBus.getDefault().post(new TempSwitchUpdateEvent(false));
            EventBus.getDefault().post(new TempSwitchEvent(false));
            closeTempSwitch();
            tvSwitch.setText("关");
        } else {
            tvSwitch.setText("开");
            mCloseTv.setSelected(true);
            closeImage.setSelected(true);
            int manualMode = (int) MySpUtil.getParam(getActivity(), MySpUtil.MANUAL_Mode_STATUS, 0);//保存主控板数据
            int runMode = (int) MySpUtil.getParam(getActivity(), MySpUtil.RUN_Mode_STATUS, 0);//保存主控板数据
            boolean isSaveTiming = (boolean) MySpUtil.getParam(getActivity(), MySpUtil.TIMING_STATUS, false);
            if (isSaveTiming) {
                if (!TimingUtils.timeValueIsNull(getActivity())) {
                    openTiming(Integer.parseInt((StringUtils.simpleDateFormat3.format(new Date()))));
                }
            } else {
                if (runMode == 0) {
                    //发送自动命令
                    if (!isTiming && !isOtaOpen) {
                        sendAutoCommand(false);
                    }
                } else if (runMode == 1) {

                    EventBus.getDefault().post(new TempSwitchUpdateEvent((boolean) MySpUtil.getParam(getActivity(), MySpUtil.TEMP_SWITCH, false)));
                    EventBus.getDefault().post(new TempSwitchEvent((boolean) MySpUtil.getParam(getActivity(), MySpUtil.TEMP_SWITCH, false)));

                    ControlCommand controlCommand = new ControlCommand(FunctionObject.SET_CONTROL_MODE);
                    if (isTiming) {
                        byte[] sendData = {(byte) 0x01, (byte) 0x01, (byte) manualMode};
                        controlCommand.setData(sendData);
                    } else {
                        byte[] sendData = {(byte) 0x00, (byte) 0x01, (byte) manualMode};
                        controlCommand.setData(sendData);
                    }
                    SpDataProcessor.getInstance().send(controlCommand);

                    windStatus = (int) MySpUtil.getParam(getActivity(), MySpUtil.WIND_STATUS, 0);
                    sendWindCommand((byte) 0x00, true);

                    circleStatus = (int) MySpUtil.getParam(getActivity(), MySpUtil.CIRCLE_STATUS, 0);
                    sendCircleCommand((byte) 0x02, true);
                    //切换手动模式时获取风机状态
                    SpDataProcessor.getInstance().send(getFanCommand);
                }
            }
        }
        hxTopic.setSystemSwitch((byte) ((isClosed ? 0 : 1) & 0xff));
        MySpUtil.setParam(getActivity(), MySpUtil.CLOSE_STATUS, (mCloseTv.isSelected() || closeImage.isSelected()));
    }

    @OnClick({R.id.ll_refrigeration, R.id.ll_heating, R.id.ll_dehumidification})
    public void onModeClick(View view) {
        if (HyApplication.isLocking) {
            ToastUtil.showToast(getActivity(), getString(R.string.device_locked));
            return;
        }
        if (isOtaOpen) {
            ToastUtil.showToast(getActivity(), getString(R.string.server_not_permission));
            return;
        }
        if (isTiming || InputLimitUtil.isFastDoubleClick()) {
            return;
        }

        if (!mCloseTv.isSelected()) {
            ToastUtil.showToast(getActivity(), "请先开机");
            return;
        }
        mRunMode = 1;
        if (view.getId() == R.id.ll_refrigeration) {
            llRefrigeration.setSelected(!llRefrigeration.isSelected());
            mLayHeating.setSelected(false);
            mLayDehumidification.setSelected(false);
        } else if (view.getId() == R.id.ll_heating) {
            mLayHeating.setSelected(!mLayHeating.isSelected());
            llRefrigeration.setSelected(false);
            mLayDehumidification.setSelected(false);
        } else if (view.getId() == R.id.ll_dehumidification) {
            mLayDehumidification.setSelected(!mLayDehumidification.isSelected());
            llRefrigeration.setSelected(false);
            mLayHeating.setSelected(false);
            if (!humiditySwitch) {
                ControlCommand humiTempCom = new ControlCommand(FunctionObject.SET_HUMI_SWITCH);
                HDTopic hdTopic = MqttUploadManager.getInstance().getmHDTopic();
                humiTempCom.setData(new byte[]{(byte) 1});
                SpDataProcessor.getInstance().send(humiTempCom);
                hdTopic.setDeHumiditySwitch((byte) 1);
            }
        }
        sendManualCommand(false);


        if (!mCircleSwitch.isSelected() && circleStatus == 0) {
            circleStatus = 2;
            sendCircleCommand((byte) 0x02, true);
            tvSetCircleSwitch.setText("中");
        }

        if (!mTempSwitch) {
            EventBus.getDefault().post(new TempSwitchUpdateEvent(true));
            EventBus.getDefault().post(new TempSwitchEvent(true));
        }

    }

    /**
     * 发送新风+排风
     * 新风type(0x00)
     * 排风type(0x01)
     */
    private void sendWindCommand(byte type, boolean isOpen) {
        FanCommand fanCommand = new FanCommand(FunctionObject.SET_SPEED);
        if (isOpen) {
            if (windStatus == 1) {
                byte[] sendData = {type, (byte) 0x01};
                fanCommand.setData(sendData);
                hdTopic.setWindStatus((byte) 0x01);
            } else if (windStatus == 2) {
                byte[] sendData = {type, (byte) 0x02};
                hdTopic.setWindStatus((byte) 0x02);
                fanCommand.setData(sendData);
            } else if (windStatus == 3) {
                byte[] sendData = {type, (byte) 0x03};
                hdTopic.setWindStatus((byte) 0x02);
                fanCommand.setData(sendData);
            } else {
                byte[] sendData = {type, (byte) 0x00};
                hdTopic.setWindStatus((byte) 0x03);
                fanCommand.setData(sendData);
            }
        } else {
            byte[] sendData = {type, (byte) 0x00};
            hdTopic.setWindStatus((byte) 0x00);
            fanCommand.setData(sendData);
        }
        SpDataProcessor.getInstance().send(fanCommand);
        if (type == 0) {
            sendWindCommand((byte) 0x01, isOpen);
        }
    }

    /**
     * 发送循环风1+循环风2
     * 循环风1type(0x02)
     * 循环风2type(0x03)
     */
    private void sendCircleCommand(byte type, boolean isOpen) {
        FanCommand fanCommand = new FanCommand(FunctionObject.SET_SPEED);
        if (isOpen) {
            if (circleStatus == 1) {
                byte[] sendData = {type, (byte) 0x01};
                fanCommand.setData(sendData);
                hdTopic.setCircleStatus((byte) 0x01);
            } else if (circleStatus == 2) {
                byte[] sendData = {type, (byte) 0x02};
                fanCommand.setData(sendData);
                hdTopic.setCircleStatus((byte) 0x02);
            } else if (circleStatus == 3) {
                byte[] sendData = {type, (byte) 0x03};
                fanCommand.setData(sendData);
                hdTopic.setCircleStatus((byte) 0x03);
            } else {
                byte[] sendData = {type, (byte) 0x00};
                fanCommand.setData(sendData);
                hdTopic.setCircleStatus((byte) 0x03);
            }
        } else {
            byte[] sendData = {type, (byte) 0x00};
            fanCommand.setData(sendData);
            hdTopic.setCircleStatus((byte) 0x00);
        }
        Log.e("TAG", "sendCircleCommand: " + Hex.bytesToHexString(fanCommand.getBytes()));
        SpDataProcessor.getInstance().send(fanCommand);
        if (type == 2) {
            sendCircleCommand((byte) 0x03, isOpen);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(String info) {
        //修改关怀模式时重新加载activity
        if (!StringUtils.isNullOrEmpty(info) && "changeMode".equals(info)) {
            HyApplication.isCare = true;
            getActivity().recreate();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTempSwitchStatusChange(TempSwitchEvent event) {
        if (mCloseTv.isSelected() || closeImage.isSelected()) {
            MySpUtil.setParam(getContext(), MySpUtil.TEMP_SWITCH, event.isChecked());
        }
        mTempSwitch = event.isChecked();
        Log.e("TAG", "onTempSwitchStatusChange: " + new Gson().toJson(event) + "===========" + circleStatus);
        if (event.isChecked()) {
            //打开调温时，开启内循环
            if (!mCircleSwitch.isSelected() && circleStatus == 0) {
                circleStatus = 2;
//                openCircleSwitch();
                sendCircleCommand((byte) 0x02, true);
                tvSetCircleSwitch.setText("中");
            }

            ControlCommand controlCommand = new ControlCommand(FunctionObject.GET_TEMP_SWITCH);
            byte[] sendData = {(byte) 0x01};
            controlCommand.setData(sendData);
            SpDataProcessor.getInstance().send(controlCommand);
        } else {
            closeTempSwitch();
        }
    }

    //风速按钮点击状态
    private boolean fanSpeedClickable(LinearLayout switchType) {
        if (HyApplication.isLocking) {
            ToastUtil.showToast(getActivity(), getString(R.string.device_locked));
            return false;
        }
        if (isOtaOpen) {
            ToastUtil.showToast(getActivity(), getString(R.string.server_not_permission));
            return false;
        }
        if (!switchType.isSelected() || mRunMode == 2 || isTiming) {
            return false;
        }
        return true;
    }

    @OnClick({R.id.tv_menu})
    public void ontvMenu(View view) {

        if (!mCloseTv.isSelected()) {
            ToastUtil.showToast(getActivity(), "请先开机");
            return;
        }

        llData.setVisibility(View.GONE);
        llClassicMode.setVisibility(View.GONE);
        llCaringMode.setVisibility(View.GONE);
        llBottom.setVisibility(View.VISIBLE);
        llBottom.setOrientation(LinearLayout.VERTICAL);
        // 获取 llBottom 的当前布局参数
        ViewGroup.LayoutParams params = llBottom.getLayoutParams();
        // 设置高度为 MATCH_PARENT (填满父视图的剩余空间)
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        llBottom.setLayoutParams(params);
    }

    @OnClick({R.id.ll_home})
    public void onLlBottom() {
        if (llClassicMode.getVisibility() == View.GONE && llData.getVisibility() == View.GONE && llCaringMode.getVisibility() == View.GONE) {
            llData.setVisibility(View.VISIBLE);
            updateLayoutForCareMode(true);
        }
    }

    public void onWindSmall() {
        if (fanSpeedClickable(mWindSwitch)) {
            if (windStatus != 1) {
                windStatus = 1;
                mWindSmall.setSelected(true);
                sendWindCommand((byte) 0x00, mWindSwitch.isSelected());
            }
        }
        popupWindowWind.dismiss();
    }

    public void onWindMiddleClick() {
        if (fanSpeedClickable(mWindSwitch)) {
            if (windStatus != 2) {
                mWindMiddle.setSelected(true);
                windStatus = 2;
                sendWindCommand((byte) 0x00, mWindSwitch.isSelected());
            }
        }
        popupWindowWind.dismiss();
    }

    public void onWindHighClick() {
        if (fanSpeedClickable(mWindSwitch)) {
            if (windStatus != 3) {
                mWindHigh.setSelected(true);
                windStatus = 3;
                sendWindCommand((byte) 0x00, mWindSwitch.isSelected());
            }
        }
        popupWindowWind.dismiss();
    }


    public void onCircleSmallClick(View view) {
        if (fanSpeedClickable(mCircleSwitch)) {
            if (circleStatus != 1) {
                mCircleSmall.setSelected(true);
                circleStatus = 1;
                sendCircleCommand((byte) 0x02, mCircleSwitch.isSelected());
                tvSetCircleSwitch.setText("低");
            }
        }
        popupWindowWind.dismiss();
    }

    public void onCircleMiddleClick(View v) {
        if (fanSpeedClickable(mCircleSwitch)) {
            if (circleStatus != 2) {
                mCircleMiddle.setSelected(true);
                circleStatus = 2;
                sendCircleCommand((byte) 0x02, mCircleSwitch.isSelected());
                tvSetCircleSwitch.setText("中");
            }
        }
        popupWindowWind.dismiss();
    }

    public void onCircleHighClick(View view) {
        if (fanSpeedClickable(mCircleSwitch)) {
            if (circleStatus != 3) {
                mCircleHigh.setSelected(true);
                circleStatus = 3;
                sendCircleCommand((byte) 0x02, mCircleSwitch.isSelected());
                tvSetCircleSwitch.setText("高");
            }
        }
        popupWindowWind.dismiss();
    }

    @OnClick({R.id.bt_runMode})
    public void onRunModeClick(View view) {
        if (HyApplication.isLocking) {
            ToastUtil.showToast(getActivity(), getString(R.string.device_locked));
            return;
        }
        if (isOtaOpen) {
            ToastUtil.showToast(getActivity(), getString(R.string.server_not_permission));
            return;
        }
        if (isTiming || InputLimitUtil.isFastDoubleClick()) {
            return;
        }
        if (mRunMode == 1) {
            mRunMode = 2;
            sendAutoCommand(false);
        } else if (mRunMode == 2) {
            sendManualCommand(false);
        }
    }

    @OnClick({R.id.bt_timing})
    public void onTimingClick(View view) {
        if (HyApplication.isLocking) {
            ToastUtil.showToast(getActivity(), getString(R.string.device_locked));
            return;
        }
        if (isOtaOpen) {
            ToastUtil.showToast(getActivity(), getString(R.string.server_not_permission));
            return;
        }

        if (!mCloseTv.isSelected()) {
            ToastUtil.showToast(getActivity(), "请先开机");
            return;
        }
//        if (TimingUtils.timeValueIsNull(getActivity()) && !isTiming) {
//            ToastUtil.showToast(getActivity(), "请先设置时间段！");
////            Intent intent = new Intent(getActivity(), SettingActivity.class);
////            intent.putExtra("timing", "1");
////            startActivity(intent);
//            return;
//        }
        dialogFragment = SettingTimeSetDialogFragment.newInstance(isTiming);
        dialogFragment.setTargetFragment(this, 0);
        dialogFragment.show(getActivity().getSupportFragmentManager(), "TimingSetDialog");
    }

    //退出定时模式
    private void closeTiming() {
        MySpUtil.setParam(getActivity(), MySpUtil.TIMING_STATUS, false);
//        mTimingButton.setBackground(getDrawable(R.drawable.btn_bg_common1));
        if (mRunMode == 2) {
            sendAutoCommand(false);
        } else {
            sendManualCommand(false);
            //退出定时，如果处于主动模式，获取风机状态
            SpDataProcessor.getInstance().send(getFanCommand);
        }
        isTiming = false;
        initSwitchClickable();
        hdTopic.setTimingSwitch((byte) 0x00);
        mTimingButton.setSelected(false);
        tvTiming.setText("关");
    }

    //开启定时
    private void openTiming(int hour) {
        MySpUtil.setParam(getActivity(), MySpUtil.TIMING_STATUS, true);
        Log.e("TAG", "openTiming: " + TimingUtils.timeSlot(getActivity(), hour));
        if (TimingUtils.timeSlot(getActivity(), hour)) {
            sendAutoCommand(true);
        } else {
            sendManualCommand(true);
        }
        isTiming = true;
        initSwitchClickable();
        hdTopic.setTimingSwitch((byte) 0x01);

        mTimingButton.setSelected(true);
        tvTiming.setText("开");
    }

    public void setTimingStatus(boolean isTiingStatus) {
        Log.e("TAG", "setTimingStatus: " + isTiingStatus);
        SaveTimingInfo timingInfo = MySpUtil.getTimingData(getActivity());
        timingInfo.setStartTimeStamp(isTiingStatus ? 0L : System.currentTimeMillis());
        MySpUtil.setParam(getActivity(), MySpUtil.TIMING_SET, new Gson().toJson(timingInfo));
    }

    @OnClick({R.id.bt_locking})
    public void onLockingClick(View view) {
        if (isOtaOpen) {
            ToastUtil.showToast(getActivity(), getString(R.string.server_not_permission));
            return;
        }
    }

    private int tempMin;
    private int tempMax;

    //接收MCU下发控制
    public void receiveData(byte[] data) {
        if (data == null) {
            return;
        }
        EventBus.getDefault().post(new ReceiveMcuDataEvent(Hex.bytesToHexString(data)));
        if (isOtaOpen) {
            return;
        }


        if (data.length < 7) {
            return;
        }
        byte[] type = Arrays.copyOfRange(data, 2, 3);
        byte[] lengthBytes = Arrays.copyOfRange(data, 3, 7);
        byte[] receiveData = Arrays.copyOfRange(data, 7, data.length - 2);
        switch (type[0]) {
            case 0x01:
                int tempSwitch = ByteUtils.byteArrayToInt(receiveData, 0, receiveData.length, ByteUtils.Endian.Little);
                if (tempSwitch == 0 && mTempSwitch) {
                    EventBus.getDefault().post(new TempSwitchUpdateEvent(false));
                    EventBus.getDefault().post(new TempSwitchEvent(false));
                } else if (tempSwitch == 1 && !mTempSwitch) {
                    EventBus.getDefault().post(new TempSwitchUpdateEvent(true));
                    EventBus.getDefault().post(new TempSwitchEvent(true));
                }
                break;
            case 0x02:
                if (mRunMode == 2) {
                    return;
                }

                if (!mCloseTv.isSelected()) {
                    ToastUtil.showToast(getActivity(), "请先开机");
                    return;
                }

                int status = ByteUtils.byteArrayToInt(receiveData, 0, receiveData.length, ByteUtils.Endian.Little);
                if (status == 0 && mWindSwitch.isSelected()) {
                    mWindSwitch.setSelected(false);
                    windStatus = 0;
                    sendWindCommand((byte) 0x00, mWindSwitch.isSelected());
                    tvSetWindSwitch.setText("关");
                } else if (status == 1) {
                    mWindSwitch.setSelected(true);
                    windStatus = 1;
                    tvSetWindSwitch.setText("低");
                    sendWindCommand((byte) 0x00, mWindSwitch.isSelected());

                } else if (status == 2) {
                    mWindSwitch.setSelected(true);
                    windStatus = 2;
                    tvSetWindSwitch.setText("中");
                    sendWindCommand((byte) 0x00, mWindSwitch.isSelected());
                } else if (status == 3) {
                    mWindSwitch.setSelected(true);
                    windStatus = 3;
                    tvSetWindSwitch.setText("高");
                    sendWindCommand((byte) 0x00, mWindSwitch.isSelected());

                }
                break;
            case 0x03:
                if (mRunMode == 2) {
                    return;
                }

                if (!mCloseTv.isSelected()) {
                    ToastUtil.showToast(getActivity(), "请先开机");
                    return;
                }

                int Status1 = ByteUtils.byteArrayToInt(receiveData, 0, receiveData.length, ByteUtils.Endian.Little);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (Status1 == 0 && mCircleSwitch.isSelected()) {
                            mCircleSwitch.setSelected(false);
                            EventBus.getDefault().post(new TempSwitchUpdateEvent(false));
                            EventBus.getDefault().post(new TempSwitchEvent(false));
                            circleStatus = 0;
                            sendCircleCommand((byte) 0x02, mCircleSwitch.isSelected());
                            tvSetCircleSwitch.setText("关");
                        } else if (Status1 == 1) {
                            mCircleSwitch.setSelected(true);
                            circleStatus = 1;
                            sendCircleCommand((byte) 0x02, true);
                            tvSetCircleSwitch.setText("低");
                        } else if (Status1 == 2) {
                            mCircleSwitch.setSelected(true);
                            circleStatus = 2;
                            sendCircleCommand((byte) 0x02, true);
                            tvSetCircleSwitch.setText("中");
                        } else if (Status1 == 3) {
                            mCircleSwitch.setSelected(true);
                            circleStatus = 3;
                            sendCircleCommand((byte) 0x02, true);
                            tvSetCircleSwitch.setText("高");

                        }
                    }
                });

                break;
            case 0x04:
                if (isTiming) {
                    return;
                }
                if (mRunMode == 1 && ByteUtils.byteArrayToInt(receiveData, 0, receiveData.length, ByteUtils.Endian.Little) == 1) {
                    sendAutoCommand(false);
                } else if (mRunMode == 2 && ByteUtils.byteArrayToInt(receiveData, 0, receiveData.length, ByteUtils.Endian.Little) == 0) {
                    sendManualCommand(false);
                }
                break;
            case 0x05:
                tempMin = ByteUtils.byteArrayToInt(receiveData, 0, receiveData.length, ByteUtils.Endian.Little) * 10;
                if (tempMax == 0) {
                    SaveControlInfo controlInfo = MySpUtil.getControlData(getActivity());
                    if (controlInfo != null) {
                        tempMax = Integer.parseInt(controlInfo.getTempMax()) * 10;
                    }
                }
                byte[] temp1 = ByteUtils.int16ToByteArray(tempMax);
                byte[] temp2 = ByteUtils.int16ToByteArray(tempMin);
                byte[] data3 = ByteUtils.splicingBytes(temp1, temp2);
                SettingUpdateEvent settingUpdateEvent = new SettingUpdateEvent(1);
                settingUpdateEvent.setTempMin(tempMin / 10 + "");
                settingUpdateEvent.setTempMax(tempMax / 10 + "");
                EventBus.getDefault().post(settingUpdateEvent);
                ControlCommand controlCommand = new ControlCommand(FunctionObject.SET_TEMP_SECTION);
                controlCommand.setData(data3);
                SpDataProcessor.getInstance().send(controlCommand);
                break;
            case 0x06:
                tempMax = ByteUtils.byteArrayToInt(receiveData, 0, receiveData.length, ByteUtils.Endian.Little) * 10;
                if (tempMin == 0) {
                    SaveControlInfo controlInfo = MySpUtil.getControlData(getActivity());
                    if (controlInfo != null) {
                        tempMin = Integer.parseInt(controlInfo.getTempMin()) * 10;
                    }
                }
                SettingUpdateEvent settingUpdateEvent1 = new SettingUpdateEvent(1);
                settingUpdateEvent1.setTempMin(tempMin / 10 + "");
                settingUpdateEvent1.setTempMax(tempMax / 10 + "");
                EventBus.getDefault().post(settingUpdateEvent1);
                byte[] data4 = ByteUtils.splicingBytes(ByteUtils.int16ToByteArray(tempMax), ByteUtils.int16ToByteArray(tempMin));
                ControlCommand controlCommand1 = new ControlCommand(FunctionObject.SET_TEMP_SECTION);
                controlCommand1.setData(data4);
                SpDataProcessor.getInstance().send(controlCommand1);
                break;
            case 0x07:
                byte[] timeData = Arrays.copyOfRange(data, 7, data.length);
                int timingSwitch = ByteUtils.byteArrayToInt(Arrays.copyOfRange(timeData, 0, 1), 0, Arrays.copyOfRange(timeData, 0, 1).length, ByteUtils.Endian.Little);
                byte[] dayBytes = Arrays.copyOfRange(timeData, 1, 3);
                int lengthDay = ByteUtils.byteArrayToInt(dayBytes, 0, dayBytes.length, ByteUtils.Endian.Little);
                Log.e("TAG", "receiveData: " + lengthDay);

                int before1 = ByteUtils.byteArrayToInt(Arrays.copyOfRange(timeData, 3, 4), 0, Arrays.copyOfRange(timeData, 3, 4).length, ByteUtils.Endian.Little);
                int after1 = ByteUtils.byteArrayToInt(Arrays.copyOfRange(timeData, 4, 5), 0, Arrays.copyOfRange(timeData, 4, 5).length, ByteUtils.Endian.Little);
                int before2 = ByteUtils.byteArrayToInt(Arrays.copyOfRange(timeData, 5, 6), 0, Arrays.copyOfRange(timeData, 5, 6).length, ByteUtils.Endian.Little);
                int after2 = ByteUtils.byteArrayToInt(Arrays.copyOfRange(timeData, 6, 7), 0, Arrays.copyOfRange(timeData, 6, 7).length, ByteUtils.Endian.Little);
                int before3 = ByteUtils.byteArrayToInt(Arrays.copyOfRange(timeData, 7, 8), 0, Arrays.copyOfRange(timeData, 7, 8).length, ByteUtils.Endian.Little);
                int after3 = ByteUtils.byteArrayToInt(Arrays.copyOfRange(timeData, 8, 9), 0, Arrays.copyOfRange(timeData, 8, 9).length, ByteUtils.Endian.Little);

                SaveTimingInfo timingInfo = MySpUtil.getTimingData(getActivity());
                timingInfo.setOpenDay(lengthDay);
                timingInfo.setBeforeTime1(before1 + "");
                timingInfo.setAfterTime1(after1 + "");
                timingInfo.setBeforeTime2(before2 + "");
                timingInfo.setAfterTime2(after2 + "");
                timingInfo.setBeforeTime3(before3 + "");
                timingInfo.setAfterTime3(after3 + "");
                MySpUtil.setParam(getActivity(), MySpUtil.TIMING_SET, new Gson().toJson(timingInfo));
                SettingUpdateEvent time = new SettingUpdateEvent(5);
                EventBus.getDefault().post(time);
                if (TimingUtils.timeValueIsNull(getActivity()) && !isTiming) {
                    return;
                }
                int hour = Integer.parseInt((StringUtils.simpleDateFormat3.format(new Date())));
                Log.e("TAG", "receiveData: " + hour);

                if (timingSwitch == 0 && isTiming) {
                    closeTiming();
                    setTimingStatus(true);
                } else if (timingSwitch == 1) {
                    openTiming(hour);
                    setTimingStatus(false);
                }
            case 0x08:
                break;
            case 0x09:
                int humidity = ByteUtils.byteArrayToInt(receiveData, 0, receiveData.length, ByteUtils.Endian.Little);
                humidity = clampHumidityValue(humidity, InputLimitUtil.DEHUMIDIFY_MIN, InputLimitUtil.DEHUMIDIFY_MAX);
                SettingUpdateEvent settingUpdateEvent3 = new SettingUpdateEvent(1);
                settingUpdateEvent3.setHumidity(humidity + "");
                EventBus.getDefault().post(settingUpdateEvent3);
                SaveControlInfo controlInfo = MySpUtil.getControlData(getActivity());
                if (controlInfo != null) {
                    int h = clampHumidityValue(controlInfo.getHumidity1(), InputLimitUtil.HUMIDIFY_MIN, InputLimitUtil.HUMIDIFY_MAX);
                    ByteBuffer byteBuffer = ByteBuffer.allocate(3);
                    byteBuffer.put(ByteUtils.int16ToByteArray(humidity));
                    byteBuffer.put((byte) h);
                    ControlCommand controlCommand3 = new ControlCommand(FunctionObject.SET_HUMIDITY);
                    controlCommand3.setData(byteBuffer.array());
                    SpDataProcessor.getInstance().send(controlCommand3);
                }
                break;
            case 0x0B:
                ControlCommand switchCom = new ControlCommand(FunctionObject.SET_HUMI_SWITCH);
                switchCom.setData(new byte[]{(byte) ByteUtils.byteArrayToInt(receiveData, 0, receiveData.length, ByteUtils.Endian.Little)});
                SpDataProcessor.getInstance().send(switchCom);
                SettingUpdateEvent updateEvent = new SettingUpdateEvent(1);
                EventBus.getDefault().post(updateEvent);
                break;
            case 0x0E:
                receiveFan(1, receiveData);
                break;
            case 0x0F:
                receiveFan(2, receiveData);
                break;
            case 0x10:
                receiveFan(3, receiveData);
                break;
            case 0x11:
                receiveFan(4, receiveData);
                break;
            case 0x12:
                SaveFilterScreen saveFilterScreen = MySpUtil.getFilterScreen(getActivity());
                int changeType = ByteUtils.byteArrayToInt(Arrays.copyOfRange(receiveData, 0, 1), 0, Arrays.copyOfRange(receiveData, 0, 1).length, ByteUtils.Endian.Little);
                byte[] bytes = Arrays.copyOfRange(receiveData, 1, 3);
                int Value = ByteUtils.byteArrayToInt(bytes, 0, bytes.length, ByteUtils.Endian.Little);
                if (saveFilterScreen != null) {
                    if (changeType == 0) {
                        saveFilterScreen.setFreshAirChange(Value + "");
                    } else if (changeType == 1) {
                        saveFilterScreen.setExhaustChange(Value + "");
                    } else if (changeType == 2) {
                        saveFilterScreen.setCircle1Change(Value + "");
                    } else if (changeType == 3) {
                        saveFilterScreen.setCircle2Change(Value + "");
                    }
                    MySpUtil.setParam(getActivity(), MySpUtil.FILTER_SCREEN_DATA, new Gson().toJson(saveFilterScreen));
                }
                SettingUpdateEvent filterScreen = new SettingUpdateEvent(6);
                EventBus.getDefault().post(filterScreen);
                break;
            case 0x13:
                SaveFilterScreen saveFilterScreen1 = MySpUtil.getFilterScreen(getActivity());
                int changeType1 = ByteUtils.byteArrayToInt(Arrays.copyOfRange(receiveData, 0, 1), 0, Arrays.copyOfRange(receiveData, 0, 1).length, ByteUtils.Endian.Little);
                byte[] bytes1 = Arrays.copyOfRange(receiveData, 1, 3);
                int Value1 = ByteUtils.byteArrayToInt(bytes1, 0, bytes1.length, ByteUtils.Endian.Little);
                if (saveFilterScreen1 != null) {
                    if (changeType1 == 0) {
                        saveFilterScreen1.setFreshAirPressure(Value1 + "");
                    } else if (changeType1 == 1) {
                        saveFilterScreen1.setExhaustPressure(Value1 + "");
                    } else if (changeType1 == 2) {
                        saveFilterScreen1.setCircle1Pressure(Value1 + "");
                    } else if (changeType1 == 3) {
                        saveFilterScreen1.setCircle2Pressure(Value1 + "");
                    }
                    MySpUtil.setParam(getActivity(), MySpUtil.FILTER_SCREEN_DATA, new Gson().toJson(saveFilterScreen1));
                }
                SettingUpdateEvent filterScreen1 = new SettingUpdateEvent(5);
                EventBus.getDefault().post(filterScreen1);
                break;
            case 0x14:
                //风阀小板
                break;
            case 0x15:
                //Co2
                EnvironmentCommand command = new EnvironmentCommand(FunctionObject.SET_CO2_VALUE);
                ByteBuffer byteBufferCO2 = ByteBuffer.allocate(6);
                byteBufferCO2.put(ByteUtils.changeBytes(Arrays.copyOfRange(receiveData, 0, 2)));
                byteBufferCO2.put(ByteUtils.changeBytes(Arrays.copyOfRange(receiveData, 2, 4)));
                byteBufferCO2.put(ByteUtils.changeBytes(Arrays.copyOfRange(receiveData, 4, 6)));
                command.setData(byteBufferCO2.array());
                SpDataProcessor.getInstance().send(command);
                EventBus.getDefault().post(("update"));
                break;
            case 0x16:
                //PM2.5
                EnvironmentCommand commandPm = new EnvironmentCommand(FunctionObject.SET_PM_VALUE);
                ByteBuffer byteBufferPM = ByteBuffer.allocate(6);
                byteBufferPM.put(ByteUtils.changeBytes(Arrays.copyOfRange(receiveData, 0, 2)));
                byteBufferPM.put(ByteUtils.changeBytes(Arrays.copyOfRange(receiveData, 2, 4)));
                byteBufferPM.put(ByteUtils.changeBytes(Arrays.copyOfRange(receiveData, 4, 6)));
                commandPm.setData(byteBufferPM.array());
                SpDataProcessor.getInstance().send(commandPm);
                EventBus.getDefault().post(("update"));
                break;
            case 0x17:
                //PID
                ByteBuffer byteBufferPID = ByteBuffer.allocate(10);
                byteBufferPID.put(ByteUtils.changeBytes(Arrays.copyOfRange(receiveData, 0, 2)));
                byteBufferPID.put(ByteUtils.changeBytes(Arrays.copyOfRange(receiveData, 2, 4)));
                byteBufferPID.put(ByteUtils.changeBytes(Arrays.copyOfRange(receiveData, 4, 6)));
                byteBufferPID.put(ByteUtils.changeBytes(Arrays.copyOfRange(receiveData, 6, 8)));
                byteBufferPID.put(ByteUtils.changeBytes(Arrays.copyOfRange(receiveData, 8, 10)));
                PIDCommand pidCommand = new PIDCommand(FunctionObject.SET_PID_VALUE);
                pidCommand.setData(byteBufferPID.array());
                SpDataProcessor.getInstance().send(pidCommand);
                EventBus.getDefault().post(("pid_update"));
                break;
            case 0x18:
                PIDCommand command1 = new PIDCommand(FunctionObject.SET_OUT_TEMP);
                command1.setData(ByteUtils.changeBytes(receiveData));
                SpDataProcessor.getInstance().send(command1);
                EventBus.getDefault().post(("pid_update"));
                break;
            case 0x19:
                PIDCommand command2 = new PIDCommand(FunctionObject.SET_PID_TEMP1);
                command2.setData(ByteUtils.changeBytes(receiveData));
                SpDataProcessor.getInstance().send(command2);
                EventBus.getDefault().post(("pid_update"));
                break;
            case 0x1A:
                PIDCommand command3 = new PIDCommand(FunctionObject.SET_PID_TEMP2);
                command3.setData(ByteUtils.changeBytes(receiveData));
                SpDataProcessor.getInstance().send(command3);
                EventBus.getDefault().post(("pid_update"));
                break;
            case 0x20:
                int humidity1 = ByteUtils.byteArrayToInt(receiveData, 0, receiveData.length, ByteUtils.Endian.Little);
                humidity1 = clampHumidityValue(humidity1, InputLimitUtil.HUMIDIFY_MIN, InputLimitUtil.HUMIDIFY_MAX);
                SettingUpdateEvent event = new SettingUpdateEvent(1);
                event.setHumidity1(humidity1 + "");
                EventBus.getDefault().post(event);
                SaveControlInfo controlInfo1 = MySpUtil.getControlData(getActivity());
                if (controlInfo1 != null) {
                    String humidity2 = controlInfo1.getHumidity();
                    int dehumidify = clampHumidityValue(parseHumidityValue(humidity2, InputLimitUtil.DEHUMIDIFY_MIN),
                            InputLimitUtil.DEHUMIDIFY_MIN, InputLimitUtil.DEHUMIDIFY_MAX);
                    ByteBuffer buffer = ByteBuffer.allocate(3);
                    buffer.put(ByteUtils.int16ToByteArray(dehumidify));
                    buffer.put((byte) humidity1);
                    ControlCommand control = new ControlCommand(FunctionObject.SET_HUMIDITY);
                    control.setData(buffer.array());
                    SpDataProcessor.getInstance().send(control);
                }
                break;
            case 0x21:
                int coldTemp = ByteUtils.byteArrayToInt(receiveData, 0, receiveData.length, ByteUtils.Endian.Little);
                ControlCommand control = new ControlCommand(FunctionObject.SET_COLD_TEMP);
                control.setData(new byte[]{(byte) coldTemp});
                SpDataProcessor.getInstance().send(control);
                hxTopic.setColdTemp((byte) coldTemp);
                break;
            case 0x22:
                int humiTemp = ByteUtils.byteArrayToInt(receiveData, 0, receiveData.length, ByteUtils.Endian.Little);
                ControlCommand HumiTempCom = new ControlCommand(FunctionObject.SET_HUMI_TEMP);
                HumiTempCom.setData(new byte[]{(byte) humiTemp});

                SpDataProcessor.getInstance().send(HumiTempCom);
                hxTopic.setHumidityTemp((byte) humiTemp);
                break;
            case 0x23:
                FanCommand modelSelectionCommand = new FanCommand(FunctionObject.SEARCH_FAN_TYPE_MODEL);
                modelSelectionCommand.setData(receiveData);
                SpDataProcessor.getInstance().send(modelSelectionCommand);
                break;
            case 0x24:
                //0001030406080A0C
                // 循环处理数据，每两个字节为一组
                for (int i = 0; i < receiveData.length; i += 2) {
                    // 创建风扇命令
                    FanCommand fanSelectionCommand = new FanCommand(FunctionObject.SET_FAN_TYPE);
                    byte[] currentData = Arrays.copyOfRange(receiveData, i, Math.min(i + 2, receiveData.length));
                    byte[] bytes2 = new byte[2];
                    switch (currentData[0]) {
                        case 0:
                            bytes2[0] = 0;
                            break;
                        case 0x03:
                            bytes2[0] = 1;
                            break;
                        case 0x06:
                            bytes2[0] = 2;
                            break;
                        case 0x0A:
                            bytes2[0] = 3;
                            break;
                    }
                    switch (currentData[1]) {
                        case 0x01:
                        case 0x07:
                        case 0x04:
                        case 0X0B:
                            bytes2[1] = 0x00;//PWM风机
                            break;
                        case 0x02:
                        case 0x05:
                        case 0x08:
                        case 0x0C:
                            bytes2[1] = 0x01;//485风机
                            break;
                        case 0x09:
                            bytes2[2] = 0x02;//02DC风机
                            break;
                        case 0x0D:
                            bytes2[2] = 0x0D;//无
                            break;
                    }
                    fanSelectionCommand.setData(bytes2);
                    SpDataProcessor.getInstance().send(fanSelectionCommand);
                }
                break;
            case 0x25:
                int externalMachineSelectionTemp = ByteUtils.byteArrayToInt(receiveData, 0, receiveData.length, ByteUtils.Endian.Little);
                ControlCommand externalMachineSelectionCommand = new ControlCommand(FunctionObject.SET_OUTDOOR_TYPE);
                byte[] sendData = {0x00, (byte) externalMachineSelectionTemp};
                externalMachineSelectionCommand.setData(sendData);
                SpDataProcessor.getInstance().send(externalMachineSelectionCommand);
                break;
            case 0x26:
                hxTopic.setScreenChange((byte) ByteUtils.byteArrayToInt(receiveData, 0, receiveData.length, ByteUtils.Endian.Little));
                break;

            case 0x27:
                int InternalCirculation1DCFanSpeedTemp = ByteUtils.byteArrayToInt(receiveData, 0, receiveData.length, ByteUtils.Endian.Little);
                DCFanCommand dcFanCommand = new DCFanCommand(FunctionObject.SET_DC_FAN_SPEED);
                dcFanCommand.setData(ByteUtils.int16ToByteArray(InternalCirculation1DCFanSpeedTemp));
                SpDataProcessor.getInstance().send(dcFanCommand);
                break;
            case 0x28:
                break;
            case 0x29:
                break;
            case 0x2A:
                int forcedDefrostingTemp = ByteUtils.byteArrayToInt(receiveData, 0, receiveData.length, ByteUtils.Endian.Little);
                UpTempCommand forcedDefrostingCommand = new UpTempCommand(FunctionObject.UP_DEFROST_STATUS);
                forcedDefrostingCommand.setData(new byte[]{(byte) 0x00, (byte) forcedDefrostingTemp});
                SpDataProcessor.getInstance().send(forcedDefrostingCommand);
                break;
            case 0x2B:
                int defrostModeTemp = ByteUtils.byteArrayToInt(receiveData, 0, receiveData.length, ByteUtils.Endian.Little);
                UpTempCommand defrostModeCommand = new UpTempCommand(FunctionObject.UP_DEFROST_MODE);
                defrostModeCommand.setData(new byte[]{(byte) 0x00, (byte) defrostModeTemp});
                SpDataProcessor.getInstance().send(defrostModeCommand);
                break;
            case 0x2C:
                int compressorModelTemp = ByteUtils.byteArrayToInt(receiveData, 0, receiveData.length, ByteUtils.Endian.Little);
                UpTempCommand compressorModelCommand = new UpTempCommand(FunctionObject.UP_PRESS_TYPE);
                compressorModelCommand.setData(ByteUtils.int16ToByteArray(compressorModelTemp));
                SpDataProcessor.getInstance().send(compressorModelCommand);
                break;
            case 0x2D:
                int externalDriveTemp = ByteUtils.byteArrayToInt(receiveData, 0, receiveData.length, ByteUtils.Endian.Little);
                UpTempCommand externalDriveCommand = new UpTempCommand(FunctionObject.UP_SET_TYPE);
                externalDriveCommand.setData(ByteUtils.int16ToByteArray(externalDriveTemp));
                SpDataProcessor.getInstance().send(externalDriveCommand);
                break;
            case 0x2E:
                int manualFrequencyTemp = ByteUtils.byteArrayToInt(receiveData, 0, receiveData.length, ByteUtils.Endian.Little);
                UpTempCommand manualFrequencyCommand = new UpTempCommand(FunctionObject.UP_FREQUNCY);
                manualFrequencyCommand.setData(ByteUtils.int16ToByteArray(manualFrequencyTemp));
                SpDataProcessor.getInstance().send(manualFrequencyCommand);
                break;
            case 0x2F:
                int mainEEVModeTemp = ByteUtils.byteArrayToInt(receiveData, 0, receiveData.length, ByteUtils.Endian.Little);
                UpTempCommand mainEEVModeCommand = new UpTempCommand(FunctionObject.UP_MAIN_EEV_MODE);
                mainEEVModeCommand.setData(ByteUtils.int16ToByteArray(mainEEVModeTemp));
                SpDataProcessor.getInstance().send(mainEEVModeCommand);
                break;
            case 0x30:
                int primaryEEVOMainRoadTemp = ByteUtils.byteArrayToInt(receiveData, 0, receiveData.length, ByteUtils.Endian.Little);
                UpTempCommand primaryEEVOMainRoadCommand = new UpTempCommand(FunctionObject.UP_MAIN_EEV_OPEN);
                primaryEEVOMainRoadCommand.setData(ByteUtils.int16ToByteArray(primaryEEVOMainRoadTemp));
                SpDataProcessor.getInstance().send(primaryEEVOMainRoadCommand);
                break;
            case 0x31:
                //空着就行
                break;
            case 0x32:
                int auxiliaryEEVModeTemp = ByteUtils.byteArrayToInt(receiveData, 0, receiveData.length, ByteUtils.Endian.Little);
                UpTempCommand auxiliaryEEVModeCommand = new UpTempCommand(FunctionObject.UP_AUX_EEV_MODE);
                auxiliaryEEVModeCommand.setData(ByteUtils.int16ToByteArray(auxiliaryEEVModeTemp));
                SpDataProcessor.getInstance().send(auxiliaryEEVModeCommand);
                break;
            case 0x33:
                int auxiliaryEEVOpenTemp = ByteUtils.byteArrayToInt(receiveData, 0, receiveData.length, ByteUtils.Endian.Little);
                UpTempCommand auxiliaryEEVOpenCommand = new UpTempCommand(FunctionObject.UP_AUX_EEV_OPEN);
                auxiliaryEEVOpenCommand.setData(ByteUtils.int16ToByteArray(auxiliaryEEVOpenTemp));
                SpDataProcessor.getInstance().send(auxiliaryEEVOpenCommand);
                break;
            case 0x34:
                int auxiliaryEEVOpenMinTemp = ByteUtils.byteArrayToInt(receiveData, 0, receiveData.length, ByteUtils.Endian.Little);
                UpTempCommand auxiliaryEEVOpenMinCommand = new UpTempCommand(FunctionObject.UP_AUX_EEV_OPEN_MIN);
                auxiliaryEEVOpenMinCommand.setData(ByteUtils.int16ToByteArray(auxiliaryEEVOpenMinTemp));
                SpDataProcessor.getInstance().send(auxiliaryEEVOpenMinCommand);
                break;
            case 0x35:
                int auxiliaryEEVFanNumTemp = ByteUtils.byteArrayToInt(receiveData, 0, receiveData.length, ByteUtils.Endian.Little);
                UpTempCommand auxiliaryEEVFanNumCommand = new UpTempCommand(FunctionObject.UP_FAN_NUM);
                auxiliaryEEVFanNumCommand.setData(ByteUtils.int16ToByteArray(auxiliaryEEVFanNumTemp));
                SpDataProcessor.getInstance().send(auxiliaryEEVFanNumCommand);
                break;
            case 0x36:
                int auxiliaryEEVFanSpeedMaxTemp = ByteUtils.byteArrayToInt(receiveData, 0, receiveData.length, ByteUtils.Endian.Little);
                UpTempCommand auxiliaryEEVFanSpeedMaxCommand = new UpTempCommand(FunctionObject.UP_FAN_SPEED_MAX);
                auxiliaryEEVFanSpeedMaxCommand.setData(ByteUtils.int16ToByteArray(auxiliaryEEVFanSpeedMaxTemp));
                SpDataProcessor.getInstance().send(auxiliaryEEVFanSpeedMaxCommand);
                break;
            case 0x37:
                int auxiliaryEEVFanSpeedMinTemp = ByteUtils.byteArrayToInt(receiveData, 0, receiveData.length, ByteUtils.Endian.Little);
                UpTempCommand auxiliaryEEVFanSpeedMinCommand = new UpTempCommand(FunctionObject.UP_FAN_SPEED_MIN);
                auxiliaryEEVFanSpeedMinCommand.setData(ByteUtils.int16ToByteArray(auxiliaryEEVFanSpeedMinTemp));
                SpDataProcessor.getInstance().send(auxiliaryEEVFanSpeedMinCommand);
                break;
            case 0x38:
                int auxiliaryEEVSpeedStatusTemp = ByteUtils.byteArrayToInt(receiveData, 0, receiveData.length, ByteUtils.Endian.Little);
                UpTempCommand auxiliaryEEVSpeedStatusCommand = new UpTempCommand(FunctionObject.UP_SPEED_STATUS);
                auxiliaryEEVSpeedStatusCommand.setData(ByteUtils.int16ToByteArray(auxiliaryEEVSpeedStatusTemp));
                SpDataProcessor.getInstance().send(auxiliaryEEVSpeedStatusCommand);
                break;
            case 0x39:
                int auxiliaryEEVSetSpeedTemp = ByteUtils.byteArrayToInt(receiveData, 0, receiveData.length, ByteUtils.Endian.Little);
                UpTempCommand auxiliaryEEVSetSpeedCommand = new UpTempCommand(FunctionObject.UP_SET_SPEED);
                auxiliaryEEVSetSpeedCommand.setData(ByteUtils.int16ToByteArray(auxiliaryEEVSetSpeedTemp));
                SpDataProcessor.getInstance().send(auxiliaryEEVSetSpeedCommand);
                break;
            case 0x3A:
                //空着就行
//                int auxiliaryEEVUpSetCommonDataTemp = ByteUtils.byteArrayToInt(receiveData, 0, receiveData.length, ByteUtils.Endian.Little);
//                UpTempCommand auxiliaryEEVUpSetCommonDataCommand = new UpTempCommand(FunctionObject.UP_SET_COMMON_DATA);
//                try{
//                    auxiliaryEEVUpSetCommonDataCommand.setData(ByteUtils.int16ToByteArray(auxiliaryEEVUpSetCommonDataTemp));
//                    SpDataProcessor.getInstance().send4(auxiliaryEEVUpSetCommonDataCommand);
//                }catch (NumberFormatException e){
//                    ToastUtil.showToast(getActivity(),""+e);
//                }
                break;
            case 0x3B:
                int setDcFanSwitchTemp = ByteUtils.byteArrayToInt(receiveData, 0, receiveData.length, ByteUtils.Endian.Little);
                DCFanCommand setDcFanSwitchCommand = new DCFanCommand(FunctionObject.SET_DC_FAN_SWITCH);
                setDcFanSwitchCommand.setData(ByteUtils.int16ToByteArray(setDcFanSwitchTemp));
                SpDataProcessor.getInstance().send(setDcFanSwitchCommand);
                break;
            case 0x3C:
                //新增未完成
                int setexpansionOpenTemp = ByteUtils.byteArrayToInt(receiveData, 0, receiveData.length, ByteUtils.Endian.Little);
                DCFanCommand setexpansionOpenCommand = new DCFanCommand(FunctionObject.SET_EXPANSION_OPEN);
                setexpansionOpenCommand.setData(ByteUtils.int16ToByteArray(setexpansionOpenTemp));
                SpDataProcessor.getInstance().send(setexpansionOpenCommand);

                break;
            case 0x3D:
                int setExpansionTemp = ByteUtils.byteArrayToInt(receiveData, 0, receiveData.length, ByteUtils.Endian.Little);
                DCFanCommand setExpansionCommand = new DCFanCommand(FunctionObject.SET_EXPANSION_TYPE);
                setExpansionCommand.setData(ByteUtils.int16ToByteArray(setExpansionTemp));
                SpDataProcessor.getInstance().send(setExpansionCommand);
                break;
            case 0x3E:
                int setExpansionRegularValueTemp = ByteUtils.byteArrayToInt(receiveData, 0, receiveData.length, ByteUtils.Endian.Little);
                DCFanCommand setExpansionRegularValueCommand = new DCFanCommand(FunctionObject.SET_EXPANSION_REGULAR_VALUE);
                setExpansionRegularValueCommand.setData(ByteUtils.int16ToByteArray(setExpansionRegularValueTemp));
                SpDataProcessor.getInstance().send(setExpansionRegularValueCommand);
                break;
            case 0x3F:
                int setExpansionPidValueTemp = ByteUtils.byteArrayToInt(receiveData, 0, receiveData.length, ByteUtils.Endian.Little);
                DCFanCommand setExpansionPidValueCommand = new DCFanCommand(FunctionObject.SET_EXPANSION_PID_VALUE);
                setExpansionPidValueCommand.setData(ByteUtils.int16ToByteArray(setExpansionPidValueTemp));
                SpDataProcessor.getInstance().send(setExpansionPidValueCommand);
                break;
            case 0x40:
                int setDcFanSpeedTemp = ByteUtils.byteArrayToInt(receiveData, 0, receiveData.length, ByteUtils.Endian.Little);
                DCFanCommand setDcFanSpeedCommand = new DCFanCommand(FunctionObject.SET_DC_FAN_SPEED);
                setDcFanSpeedCommand.setData(ByteUtils.int16ToByteArray(setDcFanSpeedTemp));
                SpDataProcessor.getInstance().send(setDcFanSpeedCommand);
                break;
            case 0x41:
                //空着就行
                break;
            case 0x42:
                int setStaticPressureModeTemp1 = ByteUtils.byteArrayToInt(receiveData, 0, receiveData.length, ByteUtils.Endian.Little);
                FanCommand setStaticPressureModeCommand1 = new FanCommand(FunctionObject.SET_STATIC_PRESSURE_MODE);
                setStaticPressureModeCommand1.setData(new byte[]{(byte) 0x01, (byte) setStaticPressureModeTemp1});
                SpDataProcessor.getInstance().send(setStaticPressureModeCommand1);
                break;
            case 0x43:
                int setFanPressureValueTemp1 = ByteUtils.byteArrayToInt(receiveData, 0, receiveData.length, ByteUtils.Endian.Little);
                FanCommand setFanPressureValueCommand1 = new FanCommand(FunctionObject.SET_FAN_PRESSURE_VALUE);
                ByteBuffer byteBuffer1 = ByteBuffer.allocate(3);
                byteBuffer1.put((byte) 0x01);
                byteBuffer1.put(ByteUtils.int16ToByteArray(setFanPressureValueTemp1));
                setFanPressureValueCommand1.setData(byteBuffer1.array());
                SpDataProcessor.getInstance().send(setFanPressureValueCommand1);
                break;
            case 0x44:
                int setStaticPressureModeTemp0 = ByteUtils.byteArrayToInt(receiveData, 0, receiveData.length, ByteUtils.Endian.Little);
                FanCommand setStaticPressureModeCommand0 = new FanCommand(FunctionObject.SET_STATIC_PRESSURE_MODE);
                setStaticPressureModeCommand0.setData(new byte[]{(byte) 0x00, (byte) setStaticPressureModeTemp0});
                SpDataProcessor.getInstance().send(setStaticPressureModeCommand0);
                break;
            case 0x45:
                int setFanPressureValueTemp0 = ByteUtils.byteArrayToInt(receiveData, 0, receiveData.length, ByteUtils.Endian.Little);
                FanCommand setFanPressureValueCommand0 = new FanCommand(FunctionObject.SET_FAN_PRESSURE_VALUE);
                ByteBuffer byteBuffer0 = ByteBuffer.allocate(3);
                byteBuffer0.put((byte) 0x00);
                byteBuffer0.put(ByteUtils.int16ToByteArray(setFanPressureValueTemp0));
                setFanPressureValueCommand0.setData(byteBuffer0.array());
                SpDataProcessor.getInstance().send(setFanPressureValueCommand0);
                break;
            case 0x46:
                int setStaticPressureModeTemp2 = ByteUtils.byteArrayToInt(receiveData, 0, receiveData.length, ByteUtils.Endian.Little);
                FanCommand setStaticPressureModeCommand2 = new FanCommand(FunctionObject.SET_STATIC_PRESSURE_MODE);
                setStaticPressureModeCommand2.setData(new byte[]{(byte) 0x02, (byte) setStaticPressureModeTemp2});
                SpDataProcessor.getInstance().send(setStaticPressureModeCommand2);
                break;
            case 0x47:
                int setFanPressureValueTemp2 = ByteUtils.byteArrayToInt(receiveData, 0, receiveData.length, ByteUtils.Endian.Little);
                FanCommand setFanPressureValueCommand2 = new FanCommand(FunctionObject.SET_FAN_PRESSURE_VALUE);
                ByteBuffer byteBuffer2 = ByteBuffer.allocate(3);
                byteBuffer2.put((byte) 0x02);
                byteBuffer2.put(ByteUtils.int16ToByteArray(setFanPressureValueTemp2));
                setFanPressureValueCommand2.setData(byteBuffer2.array());
                SpDataProcessor.getInstance().send(setFanPressureValueCommand2);
                break;
            case 0x48:
                //空着就行
                break;
            case 0x49:
                //空着就行
                break;
            case 0x0D: // 墨迹天气数据推送
                if (receiveData.length == 8) {
                    // 1. 获取第一个字节
                    int outdoorTemp = (int) receiveData[0];
                    mWeatherTempView.setText(outdoorTemp + "");
                    // 2. 解析室外湿度 (1 字节)
                    byte[] humidityBytes = Arrays.copyOfRange(receiveData, 1, 2);
                    int outdoorHumidity = ByteUtils.byteArrayToInt(humidityBytes, 0, humidityBytes.length, ByteUtils.Endian.Little);

                    // 3. 解析室外 PM2.5 (2 字节)
                    // 截取 2 字节数组
                    byte[] pm25Bytes = Arrays.copyOfRange(receiveData, 2, 4);
                    int pm25Raw = ByteUtils.byteArrayToInt(pm25Bytes, 0, pm25Bytes.length, ByteUtils.Endian.Little);
                    // 处理精度：原始值 / 10.0
                    double outdoorPM25 = pm25Raw / 10.0;

                    // 4. 解析天气情况 (1 字节)
                    byte[] weatherBytes = Arrays.copyOfRange(receiveData, 4, 5);
                    int weatherCode = ByteUtils.byteArrayToInt(weatherBytes, 0, weatherBytes.length, ByteUtils.Endian.Little);

                    // 5. 解析风向 (1 字节)
                    byte[] windDirBytes = Arrays.copyOfRange(receiveData, 5, 6);
                    int windDirectionCode = ByteUtils.byteArrayToInt(windDirBytes, 0, windDirBytes.length, ByteUtils.Endian.Little);

                    // 6. 解析风力 (1 字节)
                    byte[] windForceBytes = Arrays.copyOfRange(receiveData, 6, 7);
                    int windForce = ByteUtils.byteArrayToInt(windForceBytes, 0, windForceBytes.length, ByteUtils.Endian.Little);

                    // 7. 解析污染程度 (1 字节)
                    byte[] pollutionBytes = Arrays.copyOfRange(receiveData, 7, 8);
                    int pollutionLevel = ByteUtils.byteArrayToInt(pollutionBytes, 0, pollutionBytes.length, ByteUtils.Endian.Little);

                    int resId = getResources().getIdentifier(getWeatherImageName(weatherCode), "drawable", getActivity().getPackageName());
                    if (resId != 0) {
                        Glide.with(getActivity()).load(resId).into(imageTemp);
                    }
                    tvPollution.setText(getPollutionText(pollutionLevel));
                    EventBus.getDefault().post(new WeatherDataEvent(outdoorTemp, outdoorHumidity, outdoorPM25, weatherCode, windDirectionCode, windForce, pollutionLevel));
                }
                break;

            case 0x4A:
                configData1[1] = (byte) (receiveData[0]);
                sendFengJiCommand(configData1);
                break;
            case 0x4B:
                configData1[2] = (byte) (receiveData[0] + 1);
                sendFengJiCommand(configData1);
                break;
            case 0x4C:
                configData1[3] = (byte) (receiveData[0]);
                sendFengJiCommand(configData1);
                break;
            case 0x4D:
                DCFanCommand dcFanCommandManualOpening = new DCFanCommand(FunctionObject.SET_EXPANSION_OPEN);
                byte[] bytesManualOpening = new byte[3];
                bytesManualOpening[0] = 0x01;
                bytesManualOpening[1] = receiveData[1];
                bytesManualOpening[2] = receiveData[0];
                dcFanCommandManualOpening.setData(bytesManualOpening);
                SpDataProcessor.getInstance().send(dcFanCommandManualOpening);
                break;
            case 0x4E:
                configData1[4] = (byte) (receiveData[0]);
                sendFengJiCommand(configData1);
                break;
            case 0x4F:
                DCFanCommand dcFanCommandManual = new DCFanCommand(FunctionObject.SET_DC_FAN_SWITCH);
                dcFanCommandManual.setData(new byte[]{0x01, (byte) (receiveData[0])});
                SpDataProcessor.getInstance().send(dcFanCommandManual);

                break;
            case 0x50:
                DCFanCommand dcFanCommandRefrigeration = new DCFanCommand(FunctionObject.SET_EXPANSION_PID_VALUE);
                byte[] bytesRefrigeration = new byte[3];
                bytesRefrigeration[0] = 0x01;
                bytesRefrigeration[1] = (byte) receiveData[1];
                bytesRefrigeration[2] = (byte) receiveData[0];
                dcFanCommandRefrigeration.setData(bytesRefrigeration);
                SpDataProcessor.getInstance().send(dcFanCommandRefrigeration);
                break;
            case 0x51:
                DCFanCommand dcFanCommandHeating = new DCFanCommand(FunctionObject.SET_EXPANSION_PID_VALUE);
                byte[] bytesHeating = new byte[3];
                bytesHeating[0] = 0x02;
                bytesHeating[1] = (byte) receiveData[1];
                bytesHeating[2] = (byte) receiveData[0];
                dcFanCommandHeating.setData(bytesHeating);
                SpDataProcessor.getInstance().send(dcFanCommandHeating);
                break;
            case 0x52:
                configData2[1] = (byte) receiveData[0];
                sendFengJiCommand(configData2);
                break;
            case 0x53:
                configData2[2] = (byte) (receiveData[0] + 1);
                sendFengJiCommand(configData2);
                break;
            case 0x54:
                configData2[3] = (byte) receiveData[0];
                sendFengJiCommand(configData2);
                break;
            case 0x55:
                DCFanCommand dcFanCommandManualOpening2 = new DCFanCommand(FunctionObject.SET_EXPANSION_OPEN);
                byte[] ManualOpening2 = new byte[3];
                ManualOpening2[0] = 0x02;
                ManualOpening2[1] = (byte) receiveData[1];
                ManualOpening2[2] = (byte) receiveData[0];
                dcFanCommandManualOpening2.setData(ManualOpening2);
                SpDataProcessor.getInstance().send(dcFanCommandManualOpening2);
                break;
            case 0x56:
                configData2[4] = (byte) receiveData[0];
                sendFengJiCommand(configData2);
                break;
            case 0x57:
                DCFanCommand dcFanCommandManual2 = new DCFanCommand(FunctionObject.SET_DC_FAN_SWITCH);
                dcFanCommandManual2.setData(new byte[]{0x02, (byte) 0x01});
                SpDataProcessor.getInstance().send(dcFanCommandManual2);
                break;
            case 0x58:
                DCFanCommand dcFanCommandHeating2 = new DCFanCommand(FunctionObject.SET_EXPANSION_PID_VALUE);
                byte[] bytesHeating2 = new byte[3];
                bytesHeating2[0] = 0x03;
                bytesHeating2[1] = (byte) receiveData[1];
                bytesHeating2[2] = (byte) receiveData[0];
                dcFanCommandHeating2.setData(bytesHeating2);
                SpDataProcessor.getInstance().send(dcFanCommandHeating2);
                break;

            // --- 新增协议解析 20260121 ---
            case 0x59:
                int parseInt = ByteUtils.byteArrayToInt(receiveData, 0, receiveData.length, ByteUtils.Endian.Little);
                Log.e("TAG", "receiveData: " + parseInt);
                if (parseInt < 0) {
                    ToastUtil.showToast(getActivity(), "不得小于0");
                    return;
                }
                if (parseInt > 480) {
                    ToastUtil.showToast(getActivity(), "不得大于480");
                    return;
                }
                DCFanCommand dcFanCommand2 = new DCFanCommand(FunctionObject.SET_EXPANSION_REGULAR_VALUE);
                dcFanCommand2.setData(ByteUtils.int16ToByteArray(parseInt));
                SpDataProcessor.getInstance().send(dcFanCommand2);
                break;
            case 0x5a: // 系统开关机
                close(ByteUtils.byteArrayToInt(receiveData, 0, receiveData.length, ByteUtils.Endian.Little) == 0);
                break;
            case 0x5b: // 附加手动模式 (0:自动温控, 1:手动制冷, 2:手动制热, 3:手动除湿)
                int manualMode = ByteUtils.byteArrayToInt(receiveData, 0, receiveData.length, ByteUtils.Endian.Little);
                Log.e("TAG", "receiveData: " + manualMode);
                if (mRunMode == 2) {
                    return;
                }

                if (!mCloseTv.isSelected()) {
                    ToastUtil.showToast(getActivity(), "请先开机");
                    return;
                }

                mRunMode = 1;
                if (manualMode == 1) {
                    llRefrigeration.setSelected(!llRefrigeration.isSelected());
                    mLayHeating.setSelected(false);
                    mLayDehumidification.setSelected(false);
                } else if (manualMode == 2) {
                    mLayHeating.setSelected(!mLayHeating.isSelected());
                    llRefrigeration.setSelected(false);
                    mLayDehumidification.setSelected(false);
                } else if (manualMode == 3) {
                    mLayDehumidification.setSelected(!mLayDehumidification.isSelected());
                    llRefrigeration.setSelected(false);
                    mLayHeating.setSelected(false);
                    if (!humiditySwitch) {
                        ControlCommand humiTempCom = new ControlCommand(FunctionObject.SET_HUMI_SWITCH);
                        HDTopic hdTopic = MqttUploadManager.getInstance().getmHDTopic();
                        humiTempCom.setData(new byte[]{(byte) 1});
                        SpDataProcessor.getInstance().send(humiTempCom);
                        hdTopic.setDeHumiditySwitch((byte) 1);
                    }
                } else {
                    llRefrigeration.setSelected(false);
                    mLayHeating.setSelected(false);
                    mLayDehumidification.setSelected(false);
                }
                sendManualCommand(false);
                if (!mCircleSwitch.isSelected() && circleStatus == 0) {
                    circleStatus = 2;
                    sendCircleCommand((byte) 0x02, true);
                    tvSetCircleSwitch.setText("中");
                }
                if (!mTempSwitch) {
                    EventBus.getDefault().post(new TempSwitchUpdateEvent(true));
                    EventBus.getDefault().post(new TempSwitchEvent(true));
                }

                break;
            case 0x5c: // 童锁状态 (0:解锁, 1:上锁)
                int childLock = ByteUtils.byteArrayToInt(receiveData, 0, receiveData.length, ByteUtils.Endian.Little);
                Log.e("TAG", "receiveData: " + childLock);
                if (childLock == 1) {
                    mLockingButton.setSelected(true);
                    tvLocking.setText("开");
                    HyApplication.isLocking = true;
                    initSwitchClickable();
                    View rootView = getActivity().getWindow().getDecorView();
                    SaturationView.getInstance().saturationView(rootView, 0.5f);
                } else {
                    mLockingButton.setSelected(false);
                    tvLocking.setText("关");
                    HyApplication.isLocking = false;
                    initSwitchClickable();
                    View rootView = getActivity().getWindow().getDecorView();
                    SaturationView.getInstance().saturationView(rootView, 1f);

                }
                break;
            case 0x5d: // 系统界面模式 (0:经典模式, 1:关怀模式)
                int uiMode = ByteUtils.byteArrayToInt(receiveData, 0, receiveData.length, ByteUtils.Endian.Little);
                MySpUtil.setParam(getActivity(), MySpUtil.CARE_MODE, uiMode == 1);
                EventBus.getDefault().post("changeMode");
                break;
            case 0x5e: // 安卓数据初始化
                if (ByteUtils.byteArrayToInt(receiveData, 0, receiveData.length, ByteUtils.Endian.Little) == 1) {
                    OTARequestCommand otaRequestCommand = new OTARequestCommand(3);
                    SpDataProcessor.getInstance().send3(otaRequestCommand);
                }
                break;
        }
    }

    private void sendFengJiCommand(byte[] configData) {
        // 2. 实例化命令构造器
        DCFanCommand fanConfigCommand = new DCFanCommand(FunctionObject.SET_MAINBOARD_CONFIG);
        fanConfigCommand.setData(configData);
        SpDataProcessor.getInstance().send(fanConfigCommand);
    }

    private byte[] configData1 = new byte[]{(byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01};// 1. 路数选择: 1路 2. 路数使能: 0x01 (使能) 3. 控制方式选择: 0x01 (自动) 4. 电子膨胀阀使能: 0x01 (使能) 5. 电磁阀使能: 0x01 (使能)
    private byte[] configData2 = new byte[]{(byte) 0x02, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01};//1. 路数选择: 1路 2. 路数使能: 0x01 (使能) 3. 控制方式选择: 0x01 (自动) 4. 电子膨胀阀使能: 0x01 (使能) 5. 电磁阀使能: 0x01 (使能)

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDCFanEvent(DCFanStatusInfo info) {
        dcFanStatusInfo = info;
        if (info != null) {
            switch (info.getType()) {//
                case FunctionObject.GET_DC_FAN_STATUS:
                    info.sendData();
                    configData1[1] = (byte) info.getPath1EnableStatus();
                    configData1[2] = (byte) info.getPath1ControlType();
                    configData1[3] = (byte) info.getPath1ColdHotEevEnableStatus();
                    configData1[4] = (byte) info.getPath1ColdHotSolenoidValveEnableStatus();

                    configData2[1] = (byte) info.getPath2EnableStatus();
                    configData2[2] = (byte) info.getPath2ControlType();
                    configData2[3] = (byte) info.getPath2DehumEevEnableStatus();
                    configData2[4] = (byte) info.getPath2DehumSolenoidValveEnableStatus();
                    break;
                case FunctionObject.SET_DC_FAN_SPEED:
                    if (info.getSuccess()) {
                        ToastUtil.showToast(getActivity(), getString(R.string.set_success));
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                DCFanCommand dcFanCommand = new DCFanCommand(FunctionObject.GET_DC_FAN_STATUS);
                                SpDataProcessor.getInstance().send(dcFanCommand);
                            }
                        }, 200);
                    } else {
                        ToastUtil.showToast(getActivity(), getString(R.string.set_fail));
                    }
                    break;
            }
        }
    }

    public static String getPollutionText(int code) {
        switch (code) {
            case 1:
                return "优";
            case 2:
                return "良";
            case 3:
                return "轻度污染";
            case 4:
                return "中度污染";
            case 5:
                return "重度污染";
            case 6:
                return "严重污染";
            case 7:
                return "爆表";
            default:
                return "未知";
        }
    }

    public static String getWeatherImageName(int code) {
        String imageTemp;
        switch (code) {
            case 1:
                imageTemp = "weather_qing"; // 晴
                break;
            case 2:
                imageTemp = "weather_duoyun"; // 多云
                break;
            case 3:
                imageTemp = "weather_yin"; // 阴
                break;
            case 4:
                imageTemp = "weather_zhenyu"; // 阵雨
                break;
            case 5:
                imageTemp = "weather_leizhenyu"; // 雷阵雨
                break;
            case 6:
                imageTemp = "weather_bingbao"; // 冰雹
                break;
            case 7:
                imageTemp = "weather_yujiaxue"; // 雨夹雪 (根据文档补充)
                break;
            case 8:
                imageTemp = "weather_xiaoyu"; // 小雨
                break;
            case 9:
                imageTemp = "weather_zhongyu"; // 中雨
                break;
            case 10:
                imageTemp = "weather_dayu"; // 大雨 (根据文档，原为大雨)
                break;
            case 11:
                imageTemp = "weather_baoyu"; // 暴雨 (根据文档，原为暴雨)
                break;
            case 12:
                imageTemp = "weather_zhenxue"; // 阵雪
                break;
            case 13:
                imageTemp = "weather_xiaoxue"; // 小雪
                break;
            case 14:
                imageTemp = "weather_zhongxue"; // 中雪
                break;
            case 15:
                imageTemp = "weather_daxue"; // 大雪
                break;
            case 16:
                imageTemp = "weather_baoxue"; // 暴雪
                break;
            case 17:
                imageTemp = "weather_wu"; // 雾 (根据文档补充)
                break;
            case 18:
                imageTemp = "weather_dongyu"; // 冻雨 (根据文档补充)
                break;
            case 19:
                imageTemp = "weather_shachenbao"; // 沙尘暴 (根据文档补充)
                break;
            case 20:
                imageTemp = "weather_fuchen"; // 浮尘 (根据文档补充)
                break;
            case 21:
                imageTemp = "weather_mai"; // 霾 (根据文档补充)
                break;
            default:
                imageTemp = "weather_unknown"; // 未知
                break;
        }
        return imageTemp;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onStatusEvent(SetStatusEvent event) {
        if (event != null) {
            if (event.getType() == 2 || event.getType() == 3) {
                SpDataProcessor.getInstance().send(getControlCommand);
            }
        }
    }

    private void receiveFan(int type, byte[] receiveData) {
        byte[] fanType = Arrays.copyOfRange(receiveData, 0, 1);
        byte[] byteSmall = ByteUtils.changeBytes(Arrays.copyOfRange(receiveData, 1, 3));
        byte[] byteMiddle = ByteUtils.changeBytes(Arrays.copyOfRange(receiveData, 3, 5));
        byte[] byteHigh = ByteUtils.changeBytes(Arrays.copyOfRange(receiveData, 5, 7));
        int small = ByteUtils.byteArrayToInt16(byteSmall);
        int middle = ByteUtils.byteArrayToInt16(byteMiddle);
        int high = ByteUtils.byteArrayToInt16(byteHigh);
        ByteBuffer byteBuffer = ByteBuffer.allocate(8);
        byteBuffer.put(fanType);
        if (type == 1) {
            byteBuffer.put((byte) 0x00);
        } else if (type == 2) {
            byteBuffer.put((byte) 0x01);
        } else if (type == 3) {
            byteBuffer.put((byte) 0x02);
        } else if (type == 4) {
            byteBuffer.put((byte) 0x03);
        }
        byteBuffer.put(ByteUtils.int16ToByteArray(small));
        byteBuffer.put(ByteUtils.int16ToByteArray(middle));
        byteBuffer.put(ByteUtils.int16ToByteArray(high));
        FanCommand fanCommand = new FanCommand(FunctionObject.SET_SPEED_VALUE);
        fanCommand.setData(byteBuffer.array());
        SpDataProcessor.getInstance().send(fanCommand);
        EventBus.getDefault().post("fan_update");
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopTimer();
        EventBus.getDefault().unregister(this);

        Context context = getActivity();
        if (context != null) {
            try {
                context.unbindService(serviceConnection);
                context.unregisterReceiver(netWorkChangeReceiver);
                NetworkStatus.unregisterNetworkListener(context);
            } catch (Exception e) {
                // 防止重复解绑导致的异常
                e.printStackTrace();
            }
        }

        if (mHandler != null) {
//            mHandler.removeCallbacks(mRunnable);
            mHandler.removeCallbacksAndMessages(null);
        }
        if (otaHandler != null) {
//            otaHandler.removeCallbacks(otaRunnable);
            otaHandler.removeCallbacksAndMessages(null);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // 移除所有待执行的延时任务
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
    }

    private void startTimer() {
        if (mTimer == null) {
            mTimer = new Timer(true);
            mTimer.schedule(new DateTimeTask(), 0, 1000);
        }
    }

    private void stopTimer() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    @Override
    public void setMessage(String message) {
        controlVersion = message;
        sendUpdateRequest();

    }

    @Override
    public void sendMessage3(byte[] bytes) {
        receiveData(bytes);
    }


    @Override
    public void sendOtaStatus(boolean status) {
        isOtaOpen = (boolean) MySpUtil.getParam(getActivity(), MySpUtil.OTA_STATUS, false);
        if (isOtaOpen != status) {
            MySpUtil.setParam(getActivity(), MySpUtil.OTA_STATUS, status);
            isOtaOpen = status;
        }
        if (isOtaOpen) {
            //禁用 关闭网关页面
            boolean isTop = AppManagerUtil.getAppManager().isTopActivity("com.hy.green_building", "com.hwellyi.smarthome.MainGatewayActivity", getActivity());
            if (isTop && PublicUse.mainActivity != null) {
                PublicUse.mainActivity.finish();
            }
            AppManagerUtil.getAppManager().finishAllActivity();
            MySpUtil.setParam(getActivity(), MySpUtil.TIMING_STATUS, false);
            mTimingButton.setSelected(false);
//            mTimingButton.setBackground(getActivity().getDrawable(R.drawable.btn_bg_common1));
            isTiming = false;
            setTimingStatus(isTiming);

            hdTopic.setTimingSwitch((byte) 0x00);
            initSwitchClickable();
            sendManualCommand(false);
            sendWindCommand((byte) 0x00, false);
            sendCircleCommand((byte) 0x02, false);
            EventBus.getDefault().post(new TempSwitchUpdateEvent(false));
            EventBus.getDefault().post(new TempSwitchEvent(false));
            closeTempSwitch();
        } else {
            //启用
            initSwitchClickable();
        }
    }

    // 用于显示下载进度的弹窗
    private ProgressDialog progressDialog;

    @Override
    public void onDownloadProgressUpdate(int progress, int fileType, String message) {
        getActivity().runOnUiThread(() -> {
            if (progressDialog == null) {
                // 1. 第一次收到进度时，创建并显示弹窗
                progressDialog = new ProgressDialog(getActivity());
                progressDialog.setTitle("OTA 升级下载");
                progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progressDialog.setCancelable(false); // 不允许用户手动取消
                progressDialog.setMax(100);
                progressDialog.show();
            }

            progressDialog.setTitle((fileType == 1) ? "主板程序 升级下载" : "app 升级下载");

            if (progress == -1) {
                // 3. 下载失败，关闭弹窗并提示用户
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                    progressDialog = null;
                }
                Toast.makeText(getActivity(), "文件下载失败！", Toast.LENGTH_LONG).show();
            } else if (progress == 100) {
                // 4. 下载成功，更新文本，并等待下一个文件或关闭
                String fileName = (fileType == 1) ? "主板程序下载完成，准备校验，请稍后..." : "准备安装app中，请稍后";
                progressDialog.setMessage(fileName + "");
            } else {
                // 2. 正常更新进度
                String fileName = (fileType == 1) ? "主板程序" : "APK 文件";
                progressDialog.setMessage("正在下载 V" + message + "-" + fileName + "...");
                progressDialog.setProgress(progress);
            }
        });
    }

    private boolean isOtaOpen;
    private String updateStatus = "";
    private Handler otaHandler = new Handler();
    private Runnable otaRunnable = new Runnable() {
        @Override
        public void run() {
            if (isUpdating) {
                sendUpdateRequest();
            }
        }
    };

    /**
     * 主控板升级
     */
    private void sendUpdateRequest() {
        File file_path = new File(StringUtils.destFileDir, controlVersion + ".bin");
        byte[] fileBytes = StringUtils.readFile(file_path);
        if (fileBytes != null) {
            OTARequestCommand command = new OTARequestCommand(1);
            command.setByteLength(fileBytes.length);
            command.setCrc(fileBytes);
            int bigDecimal = new BigDecimal(controlVersion).setScale(1, BigDecimal.ROUND_DOWN).multiply(new BigDecimal(10)).intValue();
            byte[] bytes = ByteUtils.int16ToByteArray(bigDecimal);
            command.setVersion(bytes);
            SpDataProcessor.getInstance().send1(command);
            updateStatus = "正在升级" + controlVersion + ".bin";
            EventBus.getDefault().post(new VersionUpdateEvent(1, updateStatus));
            isUpdating = true;
            otaHandler.removeCallbacks(otaRunnable);
            otaHandler.postDelayed(otaRunnable, 2000);
            if (progressDialog == null) {
                // 1. 第一次收到进度时，创建并显示弹窗
                progressDialog = new ProgressDialog(getActivity());
                progressDialog.setTitle("主板程序 升级下载");
                progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progressDialog.setCancelable(false); // 不允许用户手动取消
                progressDialog.setMax(100);
                progressDialog.show();
            }
            progressDialog.setProgress(100);
            progressDialog.setMessage("主板程序 升级下载完成" + updateStatus + "");

        }

    }

    @Override
    public void setMessage1(String message) {
        File file = new File(StringUtils.destFileDir, message + ".apk");
        PackageUtil.installAPK(getActivity(), file.getPath());
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    @Override
    public void updateWeather(boolean isConnect) {
        if (isConnect) {
            mWifiIcon.setVisibility(View.VISIBLE);
        } else {
            mWifiIcon.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void setMessage2(String message) {
        appVersion = message;
    }

    private void closeTempSwitch() {
        ControlCommand controlCommand = new ControlCommand(FunctionObject.GET_TEMP_SWITCH);
        byte[] sendData = {(byte) 0x00};
        controlCommand.setData(sendData);
        SpDataProcessor.getInstance().send(controlCommand);
        setTempMode(0, 0, 0);
    }

    //开启内循环
    private void openCircleSwitch() {
        if (circleStatus == 1) {
            mCircleSmall.setSelected(true);
        } else if (circleStatus == 2) {
            mCircleMiddle.setSelected(true);
        } else if (circleStatus == 3) {
            mCircleHigh.setSelected(true);
        } else {
            btCircleNo.setSelected(true);
        }
        sendCircleCommand((byte) 0x02, true);
    }

    private class DateTimeTask extends TimerTask {
        @Override
        public void run() {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showDatetime();
                }
            });
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRunModeEvent(RunModeEvent event) {
        if (event != null) {
            SettingUpdateEvent updateEvent = new SettingUpdateEvent(7);
            updateEvent.setTimingSwitch(!event.isModeEvent());
            EventBus.getDefault().post(updateEvent);
            Log.e("TAG", "onRunModeEvent: " + new Gson().toJson(event));
            if (event.isModeEvent()) {
                if (mCloseTv.isSelected() || closeImage.isSelected()) {
                    MySpUtil.setParam(getActivity(), MySpUtil.MANUAL_Mode_STATUS, event.getManualMode());//保存主控板数据
                    MySpUtil.setParam(getActivity(), MySpUtil.RUN_Mode_STATUS, event.getMode());//保存主控板数据
                }
                if (event.getMode() == 0) {
                    updateAutoButton();
                    //自动模式下打开调温开关
                    if (!mTempSwitch) {
                        EventBus.getDefault().post(new TempSwitchUpdateEvent(true));
                        EventBus.getDefault().post(new TempSwitchEvent(true));
                    }

                    mLayDehumidification.setSelected(false);
                    mLayHeating.setSelected(false);
                    llRefrigeration.setSelected(false);
                } else if (event.getMode() == 1) {
                    updateManualButton();
                    //切换手动模式时获取风机状态
                    SpDataProcessor.getInstance().send(getFanCommand);
                    if (isTiming) {
                        mRunModeButton.setSelected(false);
                        tvRunMode.setText("手动");
                        EventBus.getDefault().post(new TempSwitchUpdateEvent(false));
                        EventBus.getDefault().post(new TempSwitchEvent(false));
                    }
                    mLayDehumidification.setSelected(false);
                    mLayHeating.setSelected(false);
                    llRefrigeration.setSelected(false);
                    manualMode = event.getManualMode();
                    hxTopic.setAdditionalManualMode((byte) (event.getManualMode() & 0xFF));
                    if (event.getManualMode() == 1 && mCloseTv.isSelected()) {
                        llRefrigeration.setSelected(true);
                    } else if (event.getManualMode() == 2 && mCloseTv.isSelected()) {
                        mLayHeating.setSelected(true);
                    } else if (event.getManualMode() == 3 && mCloseTv.isSelected()) {
                        mLayDehumidification.setSelected(true);
                    }
                } /*else if (event.getMode() == 2) {
                    updateManualButton(2);
                    //切换手动模式时获取风机状态
                    SpDataProcessor.getInstance().send(getFanCommand);
                    if (isTiming) {
                        EventBus.getDefault().post(new TempSwitchUpdateEvent(false));
                    }
                } else if (event.getMode() == 3) {
                    updateManualButton(3);
                    //切换手动模式时获取风机状态
                    SpDataProcessor.getInstance().send(getFanCommand);
                    if (isTiming) {
                        EventBus.getDefault().post(new TempSwitchUpdateEvent(false));
                    }
                }*/
            } /*else {
                updateAutoButton();
                mLayDehumidification.setSelected(false);
                mLayHeating.setSelected(false);
                llRefrigeration.setSelected(false);
            }*/
        }
    }

    private int parseHumidityValue(String value, int fallback) {
        if (TextUtils.isEmpty(value)) {
            return fallback;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private int clampHumidityValue(int value, int min, int max) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onControlEvent(MainControlInfo info) {
        //主控机状态返回,并保存到本地
        if (info != null) {
            SaveControlInfo saveControlInfo = new SaveControlInfo();
            int dehumidifySetting = clampHumidityValue(info.getHumidity().intValue(),
                    InputLimitUtil.DEHUMIDIFY_MIN, InputLimitUtil.DEHUMIDIFY_MAX);
            int humidifySetting = clampHumidityValue(info.getHumidity1().intValue(),
                    InputLimitUtil.HUMIDIFY_MIN, InputLimitUtil.HUMIDIFY_MAX);

            saveControlInfo.setControl_version(info.softwareVersion());
            saveControlInfo.setOutTermType(info.getOutTermType());
            saveControlInfo.setHumidity(String.valueOf(dehumidifySetting));
            saveControlInfo.setHumidity1(humidifySetting);
            saveControlInfo.setLowPower(info.lowPower());
            saveControlInfo.setTempMax(info.setTempMax().toString());
            saveControlInfo.setTempMin(info.setTempMin().toString());
            saveControlInfo.setRunMode(info.runMode());
            saveControlInfo.setManualMode(info.newControlField());
            String saveJson = new Gson().toJson(saveControlInfo);
            SettingUpdateEvent controlUpdateEvent = new SettingUpdateEvent(1);
            controlUpdateEvent.setHumidity(saveControlInfo.getHumidity());
            controlUpdateEvent.setHumidity1(saveControlInfo.getHumidity1() + "");
            controlUpdateEvent.setTempMax(saveControlInfo.getTempMax());
            controlUpdateEvent.setTempMin(saveControlInfo.getTempMin());
            EventBus.getDefault().post(controlUpdateEvent);
            MySpUtil.setParam(getActivity(), MySpUtil.MAIN_CONTROL_STATUS, saveJson);//保存主控板数据
            setTempMode(info.tempControlMode(), info.getDefrostStatus(), info.delayProtectStatus());//调温状态显示

            EventBus.getDefault().post(new TempSwitchUpdateEvent(info.getTempControlEnable() == 0 ? false : true));


            byte[] bytes = ByteUtils.getBitArray((byte) info.ntcError());
            HyApplication.setNtcError(bytes);
            if (isDefrost) {
                bytes[2] = 1;
            }
            ntcErrorInfo.setVisibility(View.GONE);
            MySpUtil.setParam(getActivity(), MySpUtil.NTC_DATA, info.getNtc());//保存NTC数据
            EventBus.getDefault().post(new VersionUpdateEvent(2, info.softwareVersion()));
            EventBus.getDefault().post(new SettingUpdateEvent(6));
            //ota服务端数据上报
            sendMqttRequest(info.softwareVersion(), info.getOutTermType());
            Log.e("TAG", "onControlEvent: " + mTempSwitch + "--------" + info.runMode());
            //云端数据上报
            if (info.runMode() == 0) {
                if (!mTempSwitch) {
                    EventBus.getDefault().post(new TempSwitchUpdateEvent(true));
                    EventBus.getDefault().post(new TempSwitchEvent(true));
                }
                hdTopic.setRunMode((byte) 0x01);
            } else {
                hdTopic.setRunMode((byte) 0x00);
            }
            hdTopic.setSetHumidity((byte) info.getHumidity().intValue());
            hdTopic.setTempMin((byte) info.setTempMin().intValue());
            hdTopic.setTempMax((byte) info.setTempMax().intValue());
            hxTopic.setHumidity1((byte) info.getHumidity1().intValue());

            int version1 = new BigDecimal(PackageUtil.getVersion(getActivity())).multiply(new BigDecimal(10)).intValue();//app版本号
            int version2 = new BigDecimal(info.softwareVersion()).multiply(new BigDecimal(10)).intValue();//主控机版本号
            byte[] softwareVersion = {(byte) 0x00, (byte) version1};
            hxTopic.setSoftwareVersion(softwareVersion);
            byte[] hardwareVersion = {(byte) 0x00, (byte) version2};
            hxTopic.setHardwareVersion(hardwareVersion);
            if (info.getOutTermType() == 1) {
                hxTopic.setOutTermChoice((byte) 0x01);
                LowTempCommand lowTempCommand = new LowTempCommand(FunctionObject.GET_OUT_STATUS);
                SpDataProcessor.getInstance().send(lowTempCommand);
            } else if (info.getOutTermType() == 2) {
                hxTopic.setOutTermChoice((byte) 0x02);
                PVCommand pvCommand = new PVCommand(FunctionObject.GET_OUT_STATUS);
                SpDataProcessor.getInstance().send(pvCommand);
            } else if (info.getOutTermType() == 3) {
                hxTopic.setOutTermChoice((byte) 0x03);
                UpTempCommand upTempCommand = new UpTempCommand(FunctionObject.UP_GET_OUT_STATUS);
                SpDataProcessor.getInstance().send(upTempCommand);
            }

            PIDCommand command = new PIDCommand(FunctionObject.GET_PID_STATUS);
            SpDataProcessor.getInstance().send(command);

            MeterCommand meterCommand = new MeterCommand(1);
            SpDataProcessor.getInstance().send(meterCommand);

            EnvironmentCommand environmentCommand = new EnvironmentCommand(FunctionObject.GET_PM_CO2);
            SpDataProcessor.getInstance().send(environmentCommand);

        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCO2Event(CO2StatusInfo info) {
        info.sendData();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onElectricEvent(ElectricityMeterInfo info) {
        info.sendData();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPIDEvent(PIDStatusInfo info) {
        if (info != null) {
            info.sendData();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(PVStatusInfo info) {
        if (info != null) {
            info.uploadData();
            byte[] bytesError1 = ByteUtils.getBitArray(info.faultMessage1());
            byte[] bytesError0 = ByteUtils.getBitArray(info.faultMessage2());
            if (Arrays.toString(bytesError1).contains("1") || Arrays.toString(bytesError0).contains("1"))
                outTermError.setVisibility(View.VISIBLE);
            else outTermError.setVisibility(View.GONE);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(OutDoorStatusInfo info) {
        if (info != null) {
            info.uploadData();
            byte[] bytesError1 = ByteUtils.getBitArray(info.faultMessage1());
            byte[] bytesError0 = ByteUtils.getBitArray(info.faultMessage2());
            if (Arrays.toString(bytesError1).contains("1") || Arrays.toString(bytesError0).contains("1"))
                outTermError.setVisibility(View.VISIBLE);
            else outTermError.setVisibility(View.GONE);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(UpTempStatusInfo info) {
        //info.uploadData();
    }

    private boolean isDefrost;

    private void setTempMode(int mode, int defrost, int delayProtect) {
        mTempSwitch = mode == 0 ? false : true;
        if (mTempSwitch) {
            hdTopic.setAirSwitch((byte) 0x01);
        } else {
            hdTopic.setAirSwitch((byte) 0x00);
        }
        Log.e("TAG", "setTempMode: " + HyApplication.isForceFlu());
        mTermModeView.setVisibility(View.VISIBLE);
        if (defrost == 1) {
            mTermModeView.setText("强制除霜");
        } else if (HyApplication.isForceFlu()) {
            mTermModeView.setText("强制除氟");
        } else if (delayProtect == 1 /*&& mTempSwitch&& mRunMode == 2*/) {
            mTermModeView.setText("延时停机");
        } else if (delayProtect == 2 /*&& mTempSwitch*/ && mRunMode == 2) {
            mTermModeView.setText("停机保护");
        } else {
            mTermModeView.setVisibility(View.GONE);
        }
        hdTopic.setAirMode((byte) 0x03);

        if (mode == 0 /*|| !mTempSwitch*/) {
            mTempModeBg.setVisibility(View.GONE);
            mTempModeView.setVisibility(View.INVISIBLE);
        } else if (mode == 1 && !HyApplication.isForceFlu()) {
            mTempModeBg.setVisibility(View.VISIBLE);
            mTempModeView.setVisibility(View.VISIBLE);
            mTempModeBg.setImageResource(R.drawable.select_refrigeration);
            mTempModeView.setText("制冷");
            hdTopic.setAirMode((byte) 0x00);
        } else if (mode == 2 && !HyApplication.isForceFlu()) {
            mTempModeBg.setVisibility(View.VISIBLE);
            mTempModeView.setVisibility(View.VISIBLE);
            mTempModeBg.setImageResource(R.drawable.select_heating);
            mTempModeView.setText("制热");
            hdTopic.setAirMode((byte) 0x01);
        } else if (mode == 4 && !HyApplication.isForceFlu()) {
            mTempModeBg.setVisibility(View.VISIBLE);
            mTempModeView.setVisibility(View.VISIBLE);
            mTempModeBg.setImageResource(R.drawable.select_dehumidification);
            mTempModeView.setText("除湿");
            hdTopic.setAirMode((byte) 0x02);
        } else {
            mTempModeBg.setVisibility(View.GONE);
            mTempModeView.setVisibility(View.INVISIBLE);
        }

        EventBus.getDefault().post(new TempStatusUpdateEvent(mTempSwitch, mode));
        if (defrost == 1) {
            isDefrost = true;
            EventBus.getDefault().post(new DefrostChangeEvent(true));
        } else {
            isDefrost = false;
            EventBus.getDefault().post(new DefrostChangeEvent(false));
        }
    }

    Handler mHandler = new Handler();
    Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            SpDataProcessor.getInstance().send(getEnvironmentCommand);
            SpDataProcessor.getInstance().send(getControlCommand);
            SpDataProcessor.getInstance().send(getFanCommand);
            setScreenUseTime();
            if (dcFanStatusInfo == null) {
                DCFanCommand dcFanCommand = new DCFanCommand(FunctionObject.GET_DC_FAN_STATUS);
                SpDataProcessor.getInstance().send(dcFanCommand);
            }
            mHandler.removeCallbacks(mRunnable);
            mHandler.postDelayed(mRunnable, SEND_MESSAGE_DELAY1);
        }
    };
    private long windUseTime = 0;
    private long exhaustTime = 0;
    private long circle1UseTime = 0;
    private long circle2UseTime = 0;

    private void setScreenUseTime() {
        SaveFilterScreen saveFilterScreen = MySpUtil.getFilterScreen(getActivity());
        String freshAirUse = saveFilterScreen.getFreshAirUse();
        String exhaustUse = saveFilterScreen.getExhaustUse();
        if (StringUtils.isNullOrEmpty(freshAirUse)) {
            windUseTime = 0;
        } else {
            windUseTime = Long.valueOf(freshAirUse);
        }
        if (StringUtils.isNullOrEmpty(exhaustUse)) {
            exhaustTime = 0;
        } else {
            exhaustTime = Long.valueOf(exhaustUse);
        }
        String circle1Use = saveFilterScreen.getCircle1Use();
        String circle2Use = saveFilterScreen.getCircle2Use();
        if (StringUtils.isNullOrEmpty(circle1Use)) {
            circle1UseTime = 0;
        } else {
            circle1UseTime = Long.valueOf(circle1Use);
        }
        if (StringUtils.isNullOrEmpty(circle2Use)) {
            circle2UseTime = 0;
        } else {
            circle2UseTime = Long.valueOf(circle2Use);
        }

        if (mWindSwitch.isSelected()) {
            boolean b1 = saveFilterScreen.isFreshAirUseTime();
            boolean b2 = saveFilterScreen.isExhaustUseTime();
            if (b1) {
                windUseTime += 15;
            }
            if (b2) {
                exhaustTime += 15;
            }
            saveFilterScreen.setFreshAirUse(windUseTime + "");
            saveFilterScreen.setExhaustUse(exhaustTime + "");
        }
        if (mCircleSwitch.isSelected()) {
            boolean b1 = saveFilterScreen.isCircle1UseTime();
            boolean b2 = saveFilterScreen.isCircle2UseTime();
            if (b1) {
                circle1UseTime += 15;
            }
            if (b2) {
                circle2UseTime += 15;
            }

            saveFilterScreen.setCircle1Use(circle1UseTime + "");
            saveFilterScreen.setCircle2Use(circle2UseTime + "");
        }
        MySpUtil.setParam(getActivity(), MySpUtil.FILTER_SCREEN_DATA, new Gson().toJson(saveFilterScreen));
    }

    /**
     * 服务器数据上报
     *
     * @param version
     * @param type
     */
    private void sendMqttRequest(String version, int type) {
        HyApplication.setControlVersion(version);
        MqttUploadInfo uploadInfo = new MqttUploadInfo();
        uploadInfo.setIp_addr(NetworkStatus.getLocalIpAddress(getActivity()));
        SaveAddress saveAddress = MySpUtil.getAddress(getActivity());
        if (StringUtils.isNullOrEmpty(saveAddress.getCityName())) {
            uploadInfo.setDevice_addr("");
        } else {
            String address = saveAddress.getProvinceName() + saveAddress.getCityName() + saveAddress.getAddressDetail();
            uploadInfo.setDevice_addr(address);
        }
        uploadInfo.setApp_version(PackageUtil.getVersion(getActivity()));
        uploadInfo.setClient_id("hy_1_" + PackageUtil.getSerialNumber());
        uploadInfo.setControl_version(version);
        uploadInfo.setDevice_code(PackageUtil.getSerialNumber());
        uploadInfo.setSerial("001|" + "00" + type);
        uploadInfo.setDevice_type(1);
        uploadInfo.setTimestamp(StringUtils.simpleDateFormat.format(new Date()));
        uploadInfo.setVendor_id("1");
        uploadInfo.setSensor_type(1);
        uploadInfo.setOutside_type(type);
        String json = MySpUtil.getParam(getActivity(), MySpUtil.FAN_COUNT, "").toString();
        if (!StringUtils.isNullOrEmpty(json)) {
            List<FanTypeCount> list = new Gson().fromJson(json, new TypeToken<List<FanTypeCount>>() {
            }.getType());
            if (list != null && list.size() == 2) {
                uploadInfo.setFan_type_obj(list);
            }
        }
        MyMqttService.publish(new Gson().toJson(uploadInfo), uploadInfo.getClient_id());
    }

    private File file_path;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onOtaEvent(OTAStatusEvent info) {
        //OTA升级请求状态返回
        if (info != null) {
            isUpdating = false;
            file_path = new File(StringUtils.destFileDir, controlVersion + ".bin");
            byte[] fileData = StringUtils.readFile(file_path);
            byte[] data = info.getOtaData();
            int offset = ByteUtils.byteArrayToInt(Arrays.copyOfRange(data, 0, 4), 0,
                    Arrays.copyOfRange(data, 0, 4).length);
            int serial = ByteUtils.byteArrayToInt16(Arrays.copyOfRange(data, 4, 6));
            int byteLength = ByteUtils.byteArrayToInt16(Arrays.copyOfRange(data, 6, 8));
            OTARequestCommand otaRequestCommand = new OTARequestCommand(2);
            otaRequestCommand.setByteLength(byteLength + 2);
            otaRequestCommand.setSerial(ByteUtils.shortToByteArray((short) serial));
            otaRequestCommand.setSendData(Arrays.copyOfRange(fileData, offset, offset + byteLength));
            SpDataProcessor.getInstance().send2(otaRequestCommand);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onOtaErrorEvent(OTAErrorEvent info) {
        //OTA状态上报
        if (info != null) {
            if (info.getType() == 1) {
                updateStatus = "";
                EventBus.getDefault().post(new VersionUpdateEvent(3, ""));
                if (!StringUtils.isNullOrEmpty(appVersion)) {
                    File file = new File(StringUtils.destFileDir, appVersion + ".apk");
                    PackageUtil.installAPK(getActivity(), file.getPath());
                }
                if (progressDialog != null) {
                    progressDialog.dismiss();
                }
            } else if (info.getType() == 0) {
                if (progressDialog != null) {
                    progressDialog.dismiss();
                }
            } else if (info.getType() == 4) {
                updateStatus = "升级超时";
                EventBus.getDefault().post(new VersionUpdateEvent(1, updateStatus));
                sendUpdateRequest();//重发升级请求
            } else {
                updateStatus = "升级失败";
                EventBus.getDefault().post(new VersionUpdateEvent(1, updateStatus));
                sendUpdateRequest();//重发升级请求
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRoomEvent(EnvironmentDataInfo info) {
        if (info != null) {
            byte[] errorBytes = info.getRoomError();
            HyApplication.setRoomError(errorBytes);
            if (Hex.bytesToHexString(errorBytes).equals("0000")) {
                roomError.setVisibility(View.VISIBLE);
            } else {
                roomError.setVisibility(View.GONE);
            }
            if (roomList != null) {
                roomList.clear();
                List<RoomInfo> mList = info.getRoomData(getActivity());
                RoomChangeEvent roomChangeEvent = new RoomChangeEvent("");
                EventBus.getDefault().post(roomChangeEvent);
                if (mList.size() > 0) {
                    roomList.addAll(mList);
                    SaveControlInfo saveControlInfo = MySpUtil.getControlData(getActivity());
                    if (!StringUtils.isNullOrEmpty(saveControlInfo.getTempMin()) && !StringUtils.isNullOrEmpty(saveControlInfo.getTempMax())) {
                        if (HyApplication.getOutTemp().intValue() < Integer.parseInt(saveControlInfo.getTempMin()) * 10) {
                            Collections.sort(roomList, new Comparator<RoomInfo>() {//温度显示最小值
                                public int compare(RoomInfo arg0, RoomInfo arg1) {
                                    return arg0.getTemp() - arg1.getTemp();
                                }
                            });
                        } else if (HyApplication.getOutTemp().intValue() > Integer.parseInt(saveControlInfo.getTempMax()) * 10) {
                            Collections.sort(roomList, new Comparator<RoomInfo>() {//温度显示最大值
                                public int compare(RoomInfo arg0, RoomInfo arg1) {
                                    return arg1.getTemp() - arg0.getTemp();
                                }
                            });
                        } else {
                            BigDecimal temp = new BigDecimal(saveControlInfo.getTempMin()).add(new BigDecimal(saveControlInfo.getTempMax()));
                            BigDecimal temp1 = temp.divide(new BigDecimal(2)).setScale(1, BigDecimal.ROUND_DOWN);
                            BigDecimal temp2 = temp1.multiply(new BigDecimal(10)).setScale(0, BigDecimal.ROUND_DOWN);
                            if (HyApplication.getOutTemp().intValue() < temp2.intValue()) {
                                Collections.sort(roomList, new Comparator<RoomInfo>() {//温度显示最小值
                                    public int compare(RoomInfo arg0, RoomInfo arg1) {
                                        return arg0.getTemp() - arg1.getTemp();
                                    }
                                });
                            } else {
                                Collections.sort(roomList, new Comparator<RoomInfo>() {//温度显示最大值
                                    public int compare(RoomInfo arg0, RoomInfo arg1) {
                                        return arg1.getTemp() - arg0.getTemp();
                                    }
                                });
                            }
                        }
                    }
                    if (roomList.get(0).getTemp() == 0) {
                        mRoomTemp.setText(roomList.get(0).getTemp() + "");
                        hdTopic.setInTemp((byte) roomList.get(0).getTemp());
                        for (int i = 0; i < roomList.size(); i++) {
                            if (roomList.get(i).getTemp() != 0) {
                                mRoomTemp.setText(roomList.get(i).getTemp() + "");
                                hdTopic.setInTemp((byte) roomList.get(i).getTemp());
                                break;
                            }
                        }
                    } else {
                        mRoomTemp.setText(roomList.get(0).getTemp() + "");
                        hdTopic.setInTemp((byte) roomList.get(0).getTemp());
                    }

                    Collections.sort(roomList, new Comparator<RoomInfo>() {//湿度显示最大值
                        public int compare(RoomInfo arg0, RoomInfo arg1) {
                            return arg1.getHumidity() - arg0.getHumidity();
                        }
                    });
                    mRoomHumidity.setText(roomList.get(0).getHumidity() + "");
                    hdTopic.setInHumidity((byte) roomList.get(0).getHumidity());

                    Collections.sort(roomList, new Comparator<RoomInfo>() {//CO2显示最大值
                        public int compare(RoomInfo arg0, RoomInfo arg1) {
                            return arg1.getCo2() - arg0.getCo2();
                        }
                    });
                    mRoomCo2.setText(roomList.get(0).getCo2() + "");
                    hdTopic.setInCo2(ByteUtils.int16ToByteArray(roomList.get(0).getCo2()));

                    Collections.sort(roomList, new Comparator<RoomInfo>() {//PM2.5显示最大值
                        public int compare(RoomInfo arg0, RoomInfo arg1) {
                            return arg1.getPm() - arg0.getPm();
                        }
                    });
                    mRoomPm.setText(roomList.get(0).getPm() + "");
                    hdTopic.setInPM(ByteUtils.int16ToByteArray(roomList.get(0).getPm()));
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTempEvent(TempControlEvent info) {
        if (info != null) {
            if (info.getType() == 1) {
                if (isTiming) {

                    int hour = Integer.parseInt((StringUtils.simpleDateFormat3.format(new Date())));
                    if (TimingUtils.timeSlot(getActivity(), hour)) {
                        sendAutoCommand(true);
                    } else {
                        sendManualCommand(true);
                    }


                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCustomEvent(CustomDataInfo info) {
        if (info != null) {
            info.sendData();
            humiditySwitch = info.getHumiditySwitch() == 1;
        }
    }

    private void initAnimation() {
        AnimationSet set = new AnimationSet(true);
        AlphaAnimation aa = new AlphaAnimation(0.2f, 1.3f);
        aa.setDuration(1000);
        aa.setRepeatMode(Animation.REVERSE);
        aa.setRepeatCount(Animation.INFINITE);
        set.addAnimation(aa);
        set.setInterpolator(new LinearInterpolator());
//        mPressureView.setAnimation(set);
//        mPressureView.startAnimation(set);
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(FanStatusInfo info) {
        if (info != null) {
            fanList.clear();
            fanList.addAll(info.getFanData());
            if (fanList != null && fanList.size() == 4) {
                if (fanList.get(0).getInterfaceType() == 1) {//485风机
                    hxTopic.setFanChoice1((byte) 0x02);
                    updateWindFanStatus(fanList.get(0).getFanStatus());
                } else if (fanList.get(0).getInterfaceType() == 0) {//PWM风机
                    hxTopic.setFanChoice1((byte) 0x01);
                    updateWindFanStatus(fanList.get(0).getPwmFanStatus());
                }
                if (fanList.get(1).getInterfaceType() == 1) {
                    hxTopic.setFanChoice2((byte) 0x05);
                } else if (fanList.get(1).getInterfaceType() == 0) {
                    hxTopic.setFanChoice2((byte) 0x04);
                }
                if (fanList.get(2).getInterfaceType() == 1) {//485风机
                    hxTopic.setFanChoice3((byte) 0x08);
                    updateCircleFanStatus(fanList.get(2).getFanStatus());
                } else if (fanList.get(2).getInterfaceType() == 0) {//PWM风机
                    hxTopic.setFanChoice3((byte) 0x07);
                    updateCircleFanStatus(fanList.get(2).getPwmFanStatus());
                }
                if (fanList.get(3).getInterfaceType() == 1) {
                    hxTopic.setFanChoice4((byte) 0x0C);
                } else if (fanList.get(3).getInterfaceType() == 0) {
                    hxTopic.setFanChoice4((byte) 0x0B);
                }

                //判断压差异常
                if (TimingUtils.isPressure(getActivity(), fanList.get(0).getScreenPressure(), fanList.get(1).getScreenPressure(),
                        fanList.get(2).getScreenPressure(), fanList.get(3).getScreenPressure())) {
                    if (!isAnimation) {
//                        mPressureView.setVisibility(View.VISIBLE);
//                        mPressureView.setText("压差异常");
//                        mPressureView.setTextColor(Color.RED);
                        initAnimation();
                        isAnimation = true;
                    }
                } else {
                    isAnimation = false;
//                    mPressureView.clearAnimation();
//                    mPressureView.setVisibility(View.INVISIBLE);
                }

                //判断风机异常
//                if (TimingUtils.initScreenError(getActivity())) {
//                    mWindErrorView.setVisibility(View.VISIBLE);
//                } else {
//                    mWindErrorView.setVisibility(View.INVISIBLE);
//                }
            }

        }
    }

    //更新排风机状态
    private void updateWindFanStatus(int fanStatus) {
        if (fanStatus == 0) {
            if (mWindSwitch.isSelected()) {
                mWindSwitch.setSelected(false);
                tvSetWindSwitch.setText("关");
            }
            windStatus = 0;
            hdTopic.setWindStatus((byte) 0x00);
        } else if (fanStatus == 1) {
            mWindSwitch.setSelected(true);
            windStatus = 1;
            hdTopic.setWindStatus((byte) 0x01);
            tvSetWindSwitch.setText("低");

        } else if (fanStatus == 2) {
            mWindSwitch.setSelected(true);
            windStatus = 2;
            hdTopic.setWindStatus((byte) 0x02);
            tvSetWindSwitch.setText("中");
        } else if (fanStatus == 3) {
            mWindSwitch.setSelected(true);
            windStatus = 3;
            hdTopic.setWindStatus((byte) 0x03);
            tvSetWindSwitch.setText("高");
        }


        if (mCloseTv.isSelected() || closeImage.isSelected()) {
            MySpUtil.setParam(getActivity(), MySpUtil.WIND_STATUS, fanStatus);
        }
    }

    //更新循环风状态
    private void updateCircleFanStatus(int fanStatus) {
        Log.e("TAG", "sendCircleCommand: " + fanStatus);
        if (fanStatus == 0) {
            circleStatus = 0;
            mCircleSwitch.setSelected(false);
            hdTopic.setCircleStatus((byte) 0x00);
            tvSetCircleSwitch.setText("关");
        } else if (fanStatus == 1) {
            mCircleSwitch.setSelected(true);
            circleStatus = 1;
            hdTopic.setCircleStatus((byte) 0x01);
            tvSetCircleSwitch.setText("低");
        } else if (fanStatus == 2) {
            mCircleSwitch.setSelected(true);
            circleStatus = 2;
            hdTopic.setCircleStatus((byte) 0x02);
            tvSetCircleSwitch.setText("中");
        } else if (fanStatus == 3) {
            mCircleSwitch.setSelected(true);
            circleStatus = 3;
            hdTopic.setCircleStatus((byte) 0x03);
            tvSetCircleSwitch.setText("高");
        }
        if (mCloseTv.isSelected() || closeImage.isSelected()) {
            MySpUtil.setParam(getActivity(), MySpUtil.CIRCLE_STATUS, circleStatus);
        }
    }

    private void showDatetime() {
        if (mTimeView != null && mDateView != null) {
            Date date = new Date();
            mDateView.setText(StringUtils.simpleDateFormat1.format(date) + "日");
            mTimeView.setText(StringUtils.simpleDateFormat2.format(date));

            if (isTiming) {
                timingInfo = MySpUtil.getTimingData(getActivity());
                if (timingInfo == null) return;
                int totalDays = timingInfo.getOpenDay();
                long startTimeStamp = timingInfo.getStartTimeStamp();
                long nowTime = System.currentTimeMillis();
                long elapsedDays = (nowTime - startTimeStamp) / (24L * 60 * 60 * 1000);
                int remainingDays = totalDays - (int) elapsedDays;

                // 3. 格式化工具（让日志变直观）
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.CHINA);

                Log.e("TAG", "--- 定时天数计算详情 ---");
                Log.e("TAG", "设置总天数: " + totalDays + " 天");
                Log.e("TAG", "任务开始时间: " + sdf.format(new java.util.Date(startTimeStamp)));
                Log.e("TAG", "当前系统时间: " + sdf.format(new java.util.Date(nowTime)));
                Log.e("TAG", "已经过去时间: " + ((nowTime - startTimeStamp) / 1000) + " 秒 (约 " + elapsedDays + " 天)");
                Log.e("TAG", "剩余有效天数: " + remainingDays + " 天");

                if (remainingDays <= 0) {
                    finishTiming();
                } else {
                    hdTopic.setTimingDay(ByteUtils.int16ToByteArray(remainingDays));
                    int hour = Integer.parseInt(StringUtils.simpleDateFormat3.format(date));
                    getTiming(hour);
                }
            }
        }
    }

    private void finishTiming() {
        sendAutoCommand(false);
        MySpUtil.setParam(getActivity(), MySpUtil.TIMING_STATUS, false);

        // 重置 timingInfo 数据
        if (timingInfo != null) {
            timingInfo.setOpenDay(0);
            timingInfo.setStartTimeStamp(0L);
            MySpUtil.setParam(getActivity(), MySpUtil.TIMING_SET, new Gson().toJson(timingInfo));
        }

        mTimingButton.setSelected(false);
        isTiming = false;
        setTimingStatus(false);
        hdTopic.setTimingSwitch((byte) 0x00);
        hdTopic.setTimingDay(ByteUtils.int16ToByteArray(0));
        SettingUpdateEvent updateEvent = new SettingUpdateEvent(7);
        updateEvent.setTimingSwitch(true);
        EventBus.getDefault().post(updateEvent);
    }

    /**
     * 发送自动命令
     *
     * @param isTiming 定时
     */
    private void sendAutoCommand(boolean isTiming) {
        if (!mCloseTv.isSelected()) {
            ToastUtil.showToast(getActivity(), "请先开机");
            return;
        }
        ControlCommand controlCommand = new ControlCommand(FunctionObject.SET_CONTROL_MODE);

        if (isTiming) {
            byte[] sendData = {(byte) 0x01, (byte) 0x00, (byte) manualMode};
            controlCommand.setData(sendData);
        } else {
            byte[] sendData = {(byte) 0x00, (byte) 0x00, (byte) manualMode};
            controlCommand.setData(sendData);
        }
        SpDataProcessor.getInstance().send(controlCommand);
    }

    /**
     * 发送手动命令
     *
     * @param isTiming 定时
     */
    private void sendManualCommand(boolean isTiming) {

        if (!mCloseTv.isSelected()) {
            ToastUtil.showToast(getActivity(), "请先开机");
            return;
        }
        ControlCommand controlCommand = new ControlCommand(FunctionObject.SET_CONTROL_MODE);
        int mode = (mRunMode == 2) ? manualMode : (llRefrigeration.isSelected() ? 1 : (mLayHeating.isSelected() ? 2 : (mLayDehumidification.isSelected() ? 3 : 0)));
        if (isTiming) {
            byte[] sendData = {(byte) 0x01, (byte) 0x01, (byte) mode};
            controlCommand.setData(sendData);
        } else {
            byte[] sendData = {(byte) 0x00, (byte) 0x01, (byte) mode};
            controlCommand.setData(sendData);
        }
        SpDataProcessor.getInstance().send(controlCommand);
    }

    private boolean isTimeSlotAuto = false;
    private boolean isTimeSlotManual = false;

    //定时逻辑判断
    public void getTiming(int hour) {
        SaveTimingInfo timingInfo = MySpUtil.getTimingData(getActivity());
        if (timingInfo == null) return;
        // 2. 根据状态改变发送指令，而不是只在整点发送
        if (TimingUtils.timeSlot(getActivity(), hour)) {
            // 如果应该运行，但当前记录的状态是停止，则开启
            if (!isTimeSlotAuto) {
                sendAutoCommand(true);
                isTimeSlotAuto = true;
                isTimeSlotManual = false; // 确保手动标志位同步
                Log.e("TAG", "命中定时区间，执行开启");
            }
            Log.e("TAG", "命中定时区间，执行开启2");
        } else {
            // 如果不该运行，但当前记录的状态是开启，则关闭
            if (isTimeSlotAuto) {
                sendManualCommand(true); // 或者你的关闭指令
                isTimeSlotAuto = false;
                Log.e("TAG", "定时区间结束，执行关闭");
            }
        }
    }
//    public void getTiming(int hour) {
//        SaveTimingInfo timingInfo = MySpUtil.getTimingData(getActivity());
//        if (timingInfo == null) {
//            return;
//        }
//        String before1 = timingInfo.getBeforeTime1();
//        String after1 = timingInfo.getAfterTime1();
//        String before2 = timingInfo.getBeforeTime2();
//        String after2 = timingInfo.getAfterTime2();
//        String before3 = timingInfo.getBeforeTime3();
//        String after3 = timingInfo.getAfterTime3();
//        if (!StringUtils.isNullOrEmpty(before1) && !StringUtils.isNullOrEmpty(after1)) {
//            if (hour == Integer.parseInt(before1)) {
//                if (!isTimeSlotAuto) {
//                    sendAutoCommand(true);
//                }
//                isTimeSlotAuto = true;
//            } else if (hour == Integer.parseInt(after1)) {
//                boolean isTimeSlot2 = !StringUtils.isNullOrEmpty(before2) && !StringUtils.isNullOrEmpty(after2) && TimingUtils.isSection(hour, Integer.parseInt(before2), Integer.parseInt(after2));//时间段2内
//                boolean isTimeSlot3 = !StringUtils.isNullOrEmpty(before3) && !StringUtils.isNullOrEmpty(after3) && TimingUtils.isSection(hour, Integer.parseInt(before3), Integer.parseInt(after3));//时间段3内
//                if (!isTimeSlot2 && !isTimeSlot3) {
//                    if (!isTimeSlotManual) {
//                        sendManualCommand(true);
//                    }
//                    isTimeSlotManual = true;
//                }
//            } else {
//                isTimeSlotAuto = false;
//                isTimeSlotManual = false;
//            }
//        } else if (!StringUtils.isNullOrEmpty(before2) && !StringUtils.isNullOrEmpty(after2)) {
//            if (hour == Integer.parseInt(before2)) {
//                if (!isTimeSlotAuto) {
//                    sendAutoCommand(true);
//                }
//                isTimeSlotAuto = true;
//            } else if (hour == Integer.parseInt(after2)) {
//                boolean isTimeSlot3 = !StringUtils.isNullOrEmpty(before3) && !StringUtils.isNullOrEmpty(after3) && TimingUtils.isSection(hour, Integer.parseInt(before3), Integer.parseInt(after3));//时间段3内
//                if (!isTimeSlot3) {
//                    if (!isTimeSlotManual) {
//                        sendManualCommand(true);
//                    }
//                    isTimeSlotManual = true;
//                }
//            } else {
//                isTimeSlotAuto = false;
//                isTimeSlotManual = false;
//            }
//        } else if (!StringUtils.isNullOrEmpty(before3) && !StringUtils.isNullOrEmpty(after3)) {
//            if (hour == Integer.parseInt(before3)) {
//                if (!isTimeSlotAuto) {
//                    sendAutoCommand(true);
//                }
//                isTimeSlotAuto = true;
//            } else if (hour == Integer.parseInt(after3)) {
//                if (!isTimeSlotManual) {
//                    sendManualCommand(true);
//                }
//                isTimeSlotManual = true;
//            } else {
//                isTimeSlotAuto = false;
//                isTimeSlotManual = false;
//            }
//        }
//    }

    private boolean checkPermissions(String[] neededPermissions) {
        if (neededPermissions == null || neededPermissions.length == 0) {
            return true;
        }
        boolean allGranted = true;
        for (String neededPermission : neededPermissions) {
            allGranted &= ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), neededPermission) == PackageManager.PERMISSION_GRANTED;
        }
        return allGranted;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void settingUpdateEvent(SettingUpdateEvent settingUpdateEvent) {
        if (settingUpdateEvent != null) {
            if (settingUpdateEvent.getType() == 6) {
                initScreen();
            }
        }
    }


    public void initScreen() {
        SaveFilterScreen saveFilterScreen = MySpUtil.getFilterScreen(this.getActivity());
        double overallFilterUsageText = 0;
        if (saveFilterScreen != null) {
            String[][] filterData = {
                    {saveFilterScreen.getFreshAirUse(), saveFilterScreen.getFreshAirChange()},   // 优先级 1: 新风
                    {saveFilterScreen.getExhaustUse(), saveFilterScreen.getExhaustChange()},     // 优先级 2: 排风
                    {saveFilterScreen.getCircle1Use(), saveFilterScreen.getCircle1Change()},     // 优先级 3: 内循环1
                    {saveFilterScreen.getCircle2Use(), saveFilterScreen.getCircle2Change()}      // 优先级 4: 内循环2
            };

            for (String[] data : filterData) {
                double result = calculateRemainingUsage(TextUtils.isEmpty(data[0]) ? 0 : Integer.parseInt(data[0]), TextUtils.isEmpty(data[1]) ? 0 : (Integer.parseInt(data[1]) * 3600));
                if (result > 0) {
                    overallFilterUsageText = result;
                    break;
                }
            }
            int utilizationRate = (int) (Math.round(overallFilterUsageText * 100 * 100) / 100.0);
            int overallFilterUsage = 100 - utilizationRate/*((int) (Math.round(overallFilterUsageText * 100 * 100) / 100.0))*/;

            if (overallFilterUsage > 100) {
                overallFilterUsage = 100;
            }

            if (overallFilterUsage < 0) {
                overallFilterUsage = 0;
            }

            if (overallFilterUsage >= 0 && overallFilterUsage < 20) {
                llElement.setBackground(getActivity().getDrawable(R.drawable.image_filter_element));
            } else if (overallFilterUsage >= 20 && overallFilterUsage < 40) {
                llElement.setBackground(getActivity().getDrawable(R.drawable.image_filter_element2));
            } else if (overallFilterUsage >= 40 && overallFilterUsage < 60) {
                llElement.setBackground(getActivity().getDrawable(R.drawable.image_filter_element3));
            } else if (overallFilterUsage >= 60 && overallFilterUsage < 80) {
                llElement.setBackground(getActivity().getDrawable(R.drawable.image_filter_element4));
            } else {
                llElement.setBackground(getActivity().getDrawable(R.drawable.image_filter_element5));
            }
            hdTopic.setScreenStatus((byte) utilizationRate);
            if (tvElement != null) {
                tvElement.setText(overallFilterUsage + "%");
                if (overallFilterUsage == 0) {
                    tvElement.setTextColor(Color.parseColor("#ff0000"));
                    tvElementPop.setVisibility(View.VISIBLE);
                } else {
                    tvElement.setTextColor(Color.parseColor("#000000"));
                    tvElementPop.setVisibility(View.INVISIBLE);

                }
            }
        } else if (tvElement != null) {
            tvElement.setText("0%");
        }
    }


    /**
     * 计算滤网剩余使用率。
     * * @param useTimeSeconds 滤网已使用时间（秒）
     *
     * @param changeTimeHours 设定的更换时间（小时）
     * @return 格式为 "XX%" 的字符串，如果数据无效或格式错误则返回 "-"。
     */
    private double calculateRemainingUsage(int useTimeSeconds, int changeTimeHours) {
        if (changeTimeHours == 0) {
            return 0.0;
        }
        double i = (double) useTimeSeconds / changeTimeHours;
        java.math.BigDecimal bd = new java.math.BigDecimal(i);
        bd = bd.setScale(4, java.math.BigDecimal.ROUND_HALF_UP);
        return bd.doubleValue();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTempSwitchStateUpdate(TempSwitchUpdateEvent event) {
        if (mCloseTv.isSelected() || closeImage.isSelected()) {
            MySpUtil.setParam(getContext(), MySpUtil.TEMP_SWITCH, event.getNewSwitchState());
        }
    }
}
