package com.hy.greenbuilding.ui.activity;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.gson.Gson;
import com.hy.greenbuilding.HyApplication;
import com.hy.greenbuilding.R;
import com.hy.greenbuilding.config.SaveControlInfo;
import com.hy.greenbuilding.config.SaveFilterScreen;
import com.hy.greenbuilding.event.ResetSystemEvent;
import com.hy.greenbuilding.event.SetStatusEvent;
import com.hy.greenbuilding.event.SettingUpdateEvent;
import com.hy.greenbuilding.event.VersionUpdateEvent;
import com.hy.greenbuilding.presenter.BasePresenter;
import com.hy.greenbuilding.protocol.FunctionObject;
import com.hy.greenbuilding.protocol.ResPonseInfo.CustomDataInfo;
import com.hy.greenbuilding.protocol.ResPonseInfo.ElectricityMeterInfo;
import com.hy.greenbuilding.protocol.SpDataProcessor;
import com.hy.greenbuilding.protocol.command.ControlCommand;
import com.hy.greenbuilding.protocol.command.CustomCommand;
import com.hy.greenbuilding.protocol.command.MeterCommand;
import com.hy.greenbuilding.protocol.command.OTARequestCommand;
import com.hy.greenbuilding.ui.fragment.AddRoomFragment;
import com.hy.greenbuilding.ui.fragment.SettingCodeShowFragment;
import com.hy.greenbuilding.ui.fragment.SettingElectricShowFragment;
import com.hy.greenbuilding.ui.fragment.SettingHumidityTempFragment;
import com.hy.greenbuilding.ui.fragment.SettingLocationFrament;
import com.hy.greenbuilding.ui.fragment.SettingScreenSetFragment;
import com.hy.greenbuilding.ui.fragment.SettingTimeSetFragment;
import com.hy.greenbuilding.utils.AppManagerUtil;
import com.hy.greenbuilding.utils.MySpUtil;
import com.hy.greenbuilding.utils.PackageUtil;
import com.hy.greenbuilding.utils.StringUtils;
import com.hy.greenbuilding.utils.ToastUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SettingActivity extends BaseActivity implements CompoundButton.OnCheckedChangeListener {
    private final static String INDEX_PAGE = "index";
    @BindView(R.id.iv_manager)
    ImageView mManagerButton;
    @BindView(R.id.li_back)
    ImageView mReturnView;
    @BindView(R.id.set_power_switch)
    Switch set_power_switch;
    @BindView(R.id.set_care_switch)
    Switch careSwitch;
    @BindView(R.id.bt_system_setting)
    Button mSystemSetting;

    @BindView(R.id.tv_control_version)
    TextView mControlVersion;
    @BindView(R.id.tv_app_version)
    TextView mAppVersion;
    @BindView(R.id.tv_version_update)
    TextView mVersionUpdate;

    @BindView(R.id.electric_tv)
    TextView mElectricTv;
    @BindView(R.id.humidity_tv)
    TextView mHumidityTv;
    @BindView(R.id.screen_tv)
    TextView mScreenTv;
    @BindView(R.id.room_tv)
    TextView mRoomTv;
    @BindView(R.id.time_tv)
    TextView mTimeTv;
    @BindView(R.id.location_tv)
    TextView mLocationTv;
    @BindView(R.id.code_tv)
    TextView mCodeTv;

    @BindView(R.id.reset_bt)
    Button mResetBt;

    private AlertDialog mPwdDialog;
    private AlertDialog mResetDialog;

    private SettingElectricShowFragment electricShowFragment;
    private SettingHumidityTempFragment humidityTempFragment;
    private SettingScreenSetFragment screenSetFragment;
    private SettingTimeSetFragment timeSetFragment;
    private SettingLocationFrament locationFrament;
    private SettingCodeShowFragment codeShowFragment;
    private AddRoomFragment addRoomFragment;

    private FragmentManager mFragmentManager;
    private Fragment mCurrentFragment;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initWindow();
        setContentView(R.layout.setting_main);
        ButterKnife.bind(this);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        if (savedInstanceState != null) {
            savedInstanceState.clear();
        }

        initView();
        initFragment();

        AppManagerUtil.getAppManager().addActivity(this);

        sendCustomCommand();//获取杂项数据

        sendMeterCommand();//获取电量
    }

    public void sendCustomCommand() {
        CustomCommand command = new CustomCommand(FunctionObject.GET_CUSTOM_DATA);
        SpDataProcessor.getInstance().send(command);
    }

    private void initWindow() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
    }

    private void initFragment() {
        electricShowFragment = new SettingElectricShowFragment();
        humidityTempFragment = new SettingHumidityTempFragment();
        screenSetFragment = new SettingScreenSetFragment();
        timeSetFragment = new SettingTimeSetFragment();
        locationFrament = new SettingLocationFrament();
        codeShowFragment = new SettingCodeShowFragment();
        addRoomFragment = new AddRoomFragment();
        mFragmentManager = getSupportFragmentManager();
        if (getIntent() != null) {
            String time = getIntent().getStringExtra("timing");
            if (!TextUtils.isEmpty(time)) {
                initFragmentColor();
                mTimeTv.setBackgroundColor(getResources().getColor(R.color.setting_type_select));
                switchFragment(timeSetFragment);
            } else {
                initFragmentColor();
                mHumidityTv.setBackgroundColor(getResources().getColor(R.color.setting_type_select));
                switchFragment(humidityTempFragment);
            }
        }
    }

    private void initView() {
        set_power_switch.setOnCheckedChangeListener(this);
        careSwitch.setOnCheckedChangeListener(this);
        if (getIntent() != null) {
            String updateText = getIntent().getStringExtra("updateStatus");
            if (!StringUtils.isNullOrEmpty(updateText)) {
                mVersionUpdate.setVisibility(View.VISIBLE);
                mVersionUpdate.setText(updateText);
            }
        }
        mAppVersion.setText("v" + PackageUtil.getVersion(this));
        SaveControlInfo controlInfo = MySpUtil.getControlData(SettingActivity.this);
        if (controlInfo != null) {
            if (controlInfo.getControl_version() == null) {
                mControlVersion.setText("");
            } else {
                mControlVersion.setText("v" + controlInfo.getControl_version());
            }

        }
        boolean isCareMode = (boolean) MySpUtil.getParam(this, MySpUtil.CARE_MODE, false);
        careSwitch.setChecked(isCareMode);
    }

    private void initFragmentColor() {
        mElectricTv.setBackgroundColor(getResources().getColor(R.color.setting_type_normal));
        mHumidityTv.setBackgroundColor(getResources().getColor(R.color.setting_type_normal));
        mScreenTv.setBackgroundColor(getResources().getColor(R.color.setting_type_normal));
        mRoomTv.setBackgroundColor(getResources().getColor(R.color.setting_type_normal));
        mTimeTv.setBackgroundColor(getResources().getColor(R.color.setting_type_normal));
        mLocationTv.setBackgroundColor(getResources().getColor(R.color.setting_type_normal));
        mCodeTv.setBackgroundColor(getResources().getColor(R.color.setting_type_normal));
    }

    public void switchFragment(Fragment fragment) {
        if (mCurrentFragment != fragment) {
            FragmentTransaction transaction = mFragmentManager.beginTransaction();
            if (mCurrentFragment == null) {
                transaction.add(R.id.fragment_content, fragment, fragment.getTag()).commit();
            } else {
                if (!fragment.isAdded()) {
                    transaction.hide(mCurrentFragment).add(R.id.fragment_content, fragment).commit();
                } else {
                    transaction.hide(mCurrentFragment).show(fragment).commit();
                }
            }
            mCurrentFragment = fragment;
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.clear();
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        // super.onSaveInstanceState(outState, outPersistentState);
    }

    private void sendMeterCommand() {
        MeterCommand meterCommand = new MeterCommand(1);
        SpDataProcessor.getInstance().send(meterCommand);
        mHandler.removeCallbacks(mRunnable);
        mHandler.postDelayed(mRunnable, 1000 * 5);
    }

    private Handler mHandler = new Handler();
    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            sendMeterCommand();
        }
    };

    @OnClick({R.id.bt_system_setting})
    public void onSettingClick(View view) {
        Intent intent = new Intent();
        ComponentName componentName = new ComponentName("com.android.settings", "com.android.settings.Settings");
        intent.setComponent(componentName);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @OnClick({R.id.iv_manager})
    public void onManagerClick(View view) {
        showPasswordDialog();
    }

    @OnClick({R.id.reset_bt})
    public void onResetClick(View view) {
        showResetDialog();
    }

    @OnClick({R.id.electric_tv})
    public void onElectricClick(View view) {
        initFragmentColor();
        mElectricTv.setBackgroundColor(getResources().getColor(R.color.setting_type_select));
        switchFragment(electricShowFragment);
    }

    @OnClick({R.id.humidity_tv})
    public void onHumidityTempClick(View view) {
        initFragmentColor();
        mHumidityTv.setBackgroundColor(getResources().getColor(R.color.setting_type_select));
        switchFragment(humidityTempFragment);
    }

    @OnClick({R.id.screen_tv})
    public void onScreenClick(View view) {
        initFragmentColor();
        mScreenTv.setBackgroundColor(getResources().getColor(R.color.setting_type_select));
        switchFragment(screenSetFragment);
    }

    @OnClick({R.id.room_tv})
    public void onRoomClick(View view) {
        initFragmentColor();
        mRoomTv.setBackgroundColor(getResources().getColor(R.color.setting_type_select));
        switchFragment(addRoomFragment);
    }


    @OnClick({R.id.time_tv})
    public void onTimeClick(View view) {
        initFragmentColor();
        mTimeTv.setBackgroundColor(getResources().getColor(R.color.setting_type_select));
        switchFragment(timeSetFragment);
    }

    @OnClick({R.id.location_tv})
    public void onLocationClick(View view) {
        initFragmentColor();
        mLocationTv.setBackgroundColor(getResources().getColor(R.color.setting_type_select));
        switchFragment(locationFrament);
    }

    @OnClick({R.id.code_tv})
    public void onCodeClick(View view) {
        initFragmentColor();
        mCodeTv.setBackgroundColor(getResources().getColor(R.color.setting_type_select));
        switchFragment(codeShowFragment);
    }

    //管理员密码框
    private void showPasswordDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.manager_pwd_dialog, null, false);
        mPwdDialog = new AlertDialog.Builder(this).setView(view).create();
        Button sure = view.findViewById(R.id.bt_pwd_sure);
        EditText mEtManagerPwd = view.findViewById(R.id.et_manager_pwd);
        sure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String password = mEtManagerPwd.getText().toString().trim();
                if (!StringUtils.isNullOrEmpty(password) && password.equals(StringUtils.INIT_PASSWORD)) {
                    Intent intent = new Intent(SettingActivity.this, ManagerActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    mPwdDialog.dismiss();
                } else {
                    ToastUtil.showToast(SettingActivity.this, "请输入正确的管理员密码！");
                }

            }
        });
        mPwdDialog.show();
        mPwdDialog.getWindow().setLayout(550, 460);
        mPwdDialog.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        mPwdDialog.getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
                uiOptions |= 0x00001000;
                mPwdDialog.getWindow().getDecorView().setSystemUiVisibility(uiOptions);
            }
        });
    }

    private void showResetDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.reset_system_dialog, null, false);
        mResetDialog = new AlertDialog.Builder(this).setView(view).create();
        Button sure = view.findViewById(R.id.bt_reset_sure);
        Button cancel = view.findViewById(R.id.bt_reset_cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mResetDialog.dismiss();
            }
        });
        sure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OTARequestCommand otaRequestCommand = new OTARequestCommand(3);
                SpDataProcessor.getInstance().send3(otaRequestCommand);
                mResetDialog.dismiss();
            }
        });
        mResetDialog.show();
        mResetDialog.getWindow().setLayout(550, 460);
    }

    @OnClick({R.id.li_back})
    public void onReturnClick(View view) {
        finish();
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdateEvent(VersionUpdateEvent event) {
        if (event != null) {
            if (event.getType() == 1) {
                mVersionUpdate.setVisibility(View.VISIBLE);
                mVersionUpdate.setText(event.getMessage());
            } else if (event.getType() == 2) {
                mControlVersion.setText(event.getMessage());
            } else if (event.getType() == 3) {
                mVersionUpdate.setVisibility(View.INVISIBLE);
            }
        }
    }

    //杂项数据
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCustomEvent(CustomDataInfo info) {
        if (info != null) {
            if (humidityTempFragment != null && humidityTempFragment.isAdded()) {
                humidityTempFragment.initHumiditySwitch(info.getHumiditySwitch());
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onResetEvent(ResetSystemEvent resetSystemEvent) {
        if (resetSystemEvent != null) {
            if (resetSystemEvent.isSuccess()) {
                ToastUtil.showToast(this, "设置成功！");
            } else {
                ToastUtil.showToast(this, "设置失败！");
            }
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void settingUpdateEvent(SettingUpdateEvent settingUpdateEvent) {
        if (settingUpdateEvent != null) {
            if (settingUpdateEvent.getType() == 1) {
                if (humidityTempFragment != null) {
                    humidityTempFragment.updateTemp(settingUpdateEvent);
                }
                sendCustomCommand();
            } else if (settingUpdateEvent.getType() == 5) {
                if (timeSetFragment != null) {
                    timeSetFragment.updateTiming();
                }
            } else if (settingUpdateEvent.getType() == 6) {
                if (screenSetFragment != null) {
                    screenSetFragment.initScreen(true);
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onElectricEvent(ElectricityMeterInfo info) {
        if (electricShowFragment != null && electricShowFragment.isAdded()) {
            electricShowFragment.updateElectic(info);
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onStatusEvent(SetStatusEvent event) {
        if (event != null) {
            if (event.getType() == 1) {
                if (event.getStatus()) {
                    if (HyApplication.isIsReboot()) {
                        ToastUtil.showToast(SettingActivity.this, "低功耗设置成功！");
                        try {
                            Runtime.getRuntime().exec("reboot -p");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    ToastUtil.showToast(SettingActivity.this, "低功耗设置失败！");
                }
            }
            if (event.getType() == 2) {
                if (event.getStatus()) {
                    ToastUtil.showToast(this, "湿度设置成功！");
                } else {
                    ToastUtil.showToast(this, "湿度设置失败！");
                }
            } else if (event.getType() == 3) {
                if (event.getStatus()) {
                    ToastUtil.showToast(this, "温度设置成功！");
                } else {
                    ToastUtil.showToast(this, "温度设置失败！");
                }
            } else if (event.getType() == 4) {
                if (event.getStatus()) {
                    ToastUtil.showToast(this, "重置成功!");
                } else {
                    ToastUtil.showToast(this, "重置失败!");
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        if (mHandler != null) {
            mHandler.removeCallbacks(mRunnable);
            mHandler = null;
        }
        super.onDestroy();
    }


    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        switch (compoundButton.getId()) {
            case R.id.set_power_switch:
                ControlCommand controlCommand = new ControlCommand(FunctionObject.SET_LOW_POWER);
                if (b) {
                    byte[] sendData = {(byte) 0x01};
                    controlCommand.setData(sendData);
                } else {
                    byte[] sendData = {(byte) 0x00};
                    controlCommand.setData(sendData);
                }
                HyApplication.setIsReboot(true);
                SpDataProcessor.getInstance().send(controlCommand);
                break;

            case R.id.set_care_switch:
                if (!careSwitch.isPressed()) {
                    return;
                }
                if (b) {
                    initFontScale((float) 1);
                    MySpUtil.setParam(this, MySpUtil.CARE_MODE, true);
                } else {
                    initFontScale((float) 1);
                    MySpUtil.setParam(this, MySpUtil.CARE_MODE, false);
                }
                EventBus.getDefault().post("changeMode");
                this.recreate();
                break;
        }
    }
}
