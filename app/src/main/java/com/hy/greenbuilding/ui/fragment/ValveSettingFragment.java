package com.hy.greenbuilding.ui.fragment;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;

import com.google.gson.Gson;
import com.hy.greenbuilding.R;
import com.hy.greenbuilding.protocol.FunctionObject;
import com.hy.greenbuilding.protocol.ResPonseInfo.DCFanStatusInfo;
import com.hy.greenbuilding.protocol.SpDataProcessor;
import com.hy.greenbuilding.protocol.command.DCFanCommand;
import com.hy.greenbuilding.ui.activity.BaseActivity;
import com.hy.greenbuilding.ui.widget.KeyboardEditText;
import com.hy.greenbuilding.utils.ByteUtils;
import com.hy.greenbuilding.utils.Hex;
import com.hy.greenbuilding.utils.MySpUtil;
import com.hy.greenbuilding.utils.StringUtils;
import com.hy.greenbuilding.utils.ToastUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class ValveSettingFragment extends BaseDialogFragment {
    @BindView(R.id.li_back)
    ImageView mReturnView;
    @BindView(R.id.expansion_open_et)
    KeyboardEditText expansionOpenEt;

    @BindView(R.id.radio_group_1)
    RadioGroup radioGroup1;

    @BindView(R.id.radio_group_2)
    RadioGroup radioGroup2;

    @BindView(R.id.pid_value_refrigeration)
    KeyboardEditText pidValueRefrigeration;//制冷
    @BindView(R.id.pid_value_heating)
    KeyboardEditText pidValueHeating; //制热
    @BindView(R.id.pid_value_dehumidify)
    KeyboardEditText pidValueDehumidify;//除湿
    @BindView(R.id.regular_value_et)
    KeyboardEditText regularValueEt;//除湿


    @BindView(R.id.radio_group_type)
    RadioGroup radioGroupType;

    @BindView(R.id.radio_group_xb)
    RadioGroup radioGroupXb;

    @BindView(R.id.radio_group_mode1)
    RadioGroup radioGroupMode1;
    @BindView(R.id.radio_group_mode2)
    RadioGroup radioGroupMode2;

    @BindView(R.id.set_expansion_switch_1)
    Switch setExpansionSwitch1;

    @BindView(R.id.set_expansion_switch_2)
    Switch setExpansionSwitch2;

    @BindView(R.id.switch_shineng1)
    Switch switchShineng1;

    @BindView(R.id.switch_shineng2)
    Switch switchShineng2;

    @BindView(R.id.switch_shineng4)
    Switch switchShineng4;

    @BindView(R.id.switch_shineng3)
    Switch switchShineng3;

    @BindView(R.id.rb_mode2)
    RadioButton rbMode2;

    @BindView(R.id.rb_manual2)
    RadioButton rbManual2;

    @BindView(R.id.rb_pid2)
    RadioButton rbPid2;

    @BindView(R.id.rb_mode1)
    RadioButton rbMode1;

    @BindView(R.id.rb_manual1)
    RadioButton rbManual1;

    @BindView(R.id.rb_pid1)
    RadioButton rbPid1;

    @BindView(R.id.rb_fan_board_1)
    RadioButton rbFanBoard1;
    @BindView(R.id.rb_fan_board_2)
    RadioButton rbFanBoard2;

    @BindView(R.id.rb_type_4p)
    RadioButton rbType4p;
    @BindView(R.id.rb_type_2p3p)
    RadioButton rbType2p3p;

    @BindView(R.id.tv_eev_1)
    TextView tvEev1;
    @BindView(R.id.tv_eev_2)
    TextView tvEev2;

    @BindView(R.id.ll_main_1)
    LinearLayout llMain1;

    @BindView(R.id.ll_eev)
    LinearLayout llEev;
    @BindView(R.id.ll_eev2)
    LinearLayout llEev2;
    @BindView(R.id.ll_expansion_open)
    LinearLayout llExpansionOpen;
    @BindView(R.id.ll_expansion_open2)
    LinearLayout llExpansionOpen2;
    @BindView(R.id.ll_pid_value_heating)
    LinearLayout llPidValueHeating;
    @BindView(R.id.ll_pid_value_heating2)
    LinearLayout llPidValueHeating2;
    @BindView(R.id.ll_pid_value_refrigeration)
    LinearLayout llPidValueRefrigeration;
    @BindView(R.id.ll_pid_value_refrigeration2)
    LinearLayout llPidValueRefrigeration2;
    @BindView(R.id.ll_manual_mode)
    LinearLayout llManualMode;
    @BindView(R.id.ll_manual_mode2)
    LinearLayout llManualMode2;
    @BindView(R.id.cl_switch_shineng2)
    ConstraintLayout clSwitchShineng2;
    @BindView(R.id.cl_switch_shineng4)
    ConstraintLayout clSwitchShineng4;

    @BindView(R.id.tv_switch_1)
    TextView tvSwitch1;
    @BindView(R.id.tv_pid)
    TextView tvPid;
    @BindView(R.id.tv_pid2)
    TextView tvPid2;

    @BindView(R.id.tv_switch_2)
    TextView tvSwitch2;
    @BindView(R.id.tv_electronic_expansion)
    TextView tvElectronicExpansion;
    @BindView(R.id.tv_electronic_expansion2)
    TextView tvElectronicExpansion2;
    @BindView(R.id.cl_electronic_expansion)
    ConstraintLayout clElectronicExpansion;
    @BindView(R.id.cl_electronic_expansion3)
    ConstraintLayout clElectronicExpansion3;
    @BindView(R.id.tv_dian)
    TextView tvDian;
    @BindView(R.id.tv_dian2)
    TextView tvDian2;

    @BindView(R.id.pid_value_dehumidify_target)
    KeyboardEditText pidValueDehumidifyTarget;

    @BindView(R.id.ll_control_method1)
    LinearLayout llControlMethod1;

    @BindView(R.id.ll_control_method2)
    LinearLayout llControlMethod2;


    private View mView;
    private Unbinder unbinder;
    private Context mContext;

    private byte fanboardType = (byte) 0x02;
    private byte heatingDehumidType = 0x02;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.DialogFullScreen); //dialog全屏
    }

    // **1. 定义 Handler 和 Runnable 成员变量**
    private Handler mHandler = new Handler();
    private Runnable mStatusChecker;
    private static final int STATUS_CHECK_INTERVAL = 2000; // 5秒

    @Override
    public void onResume() {
        super.onResume();
        setupEditTextListeners();
        startStatusChecking();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        this.getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        mView = inflater.inflate(R.layout.valve_setting_page, null);
        mContext = this.getActivity();
        unbinder = ButterKnife.bind(this, mView);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        //获取DC风机状态
        DCFanCommand dcFanCommand = new DCFanCommand(FunctionObject.GET_DC_FAN_STATUS);
        SpDataProcessor.getInstance().send(dcFanCommand);

        radioGroup2.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                if (!radioGroup2.findViewById(checkedId).isPressed()) {
                    return;
                }
                MySpUtil.setParam(mContext, MySpUtil.KEY_RADIO_GROUP2_CHECKED, checkedId);

                DCFanCommand dcFanCommand = new DCFanCommand(FunctionObject.SET_DC_FAN_SWITCH);
                byte[] sendData;
                switch (checkedId) {
                    case R.id.radio_btn_open2:
                        sendData = new byte[]{0x02, (byte) 0x01};
                        dcFanCommand.setData(sendData);

                        break;
                    case R.id.radio_btn_close2:
                        sendData = new byte[]{0x02, (byte) 0x00};
                        dcFanCommand.setData(sendData);
                        break;
                }
                SpDataProcessor.getInstance().send(dcFanCommand);
            }
        });

        radioGroup1.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                MySpUtil.setParam(mContext, MySpUtil.KEY_RADIO_GROUP1_CHECKED, checkedId);
                if (!radioGroup1.findViewById(checkedId).isPressed()){
                    return;
                }
                DCFanCommand dcFanCommand = new DCFanCommand(FunctionObject.SET_DC_FAN_SWITCH);
                byte[] sendData;
                switch (checkedId) {
                    case R.id.radio_btn_open:
                        sendData = new byte[]{0x01, (byte) 0x01};
                        dcFanCommand.setData(sendData);

                        break;
                    case R.id.radio_btn_close:
                        sendData = new byte[]{0x01, (byte) 0x00};
                        dcFanCommand.setData(sendData);
                        break;
                }
                SpDataProcessor.getInstance().send(dcFanCommand);
            }
        });

//        batterySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                if (!batterySwitch.isPressed()) {
//                    return;
//                }
//                DCFanCommand dcFanCommand = new DCFanCommand(FunctionObject.SET_DC_FAN_SWITCH);
//                byte eevType = 0x01;//DCF1：冷热电磁阀,0x01 DCF2：除湿电磁阀,0x02
//                if (isChecked) {
//                    byte[] sendData = {eevType,(byte) 0x01};
//                    dcFanCommand.setData(sendData);
//                } else {
//                    byte[] sendData = {eevType,(byte) 0x00};
//                    dcFanCommand.setData(sendData);
//                }
////
//                SpDataProcessor.getInstance().send(dcFanCommand);
//            }
//        });
        setupRadioGroupListener();
        setupExpansionSwitchListener();
        setupExpansionSwitchListener2();
        setKeyboardVisibilityListener(mView);
        return mView;
    }

    private void setupEditTextListeners() {
        // 设置制冷输入框的焦点监听
        pidValueRefrigeration.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    String text = pidValueRefrigeration.getText().toString();
                    if (!StringUtils.isNullOrEmpty(text)) {
                        DCFanCommand dcFanCommand = new DCFanCommand(FunctionObject.SET_EXPANSION_PID_VALUE);
                        byte[] openBytes = ByteUtils.int16ToByteArray(Integer.parseInt(text));
                        byte[] bytes = new byte[3];
                        bytes[0] = 0x01;
                        bytes[1] = openBytes[0];
                        bytes[2] = openBytes[1];

                        Log.e("TAG", "onFocusChange: "+Hex.bytesToHexString(bytes));
                        dcFanCommand.setData(bytes);
//                        Log.e("TAG", "onFocusChange: "+Hex.bytesToHexString(dcFanCommand.getBytes()));
                        SpDataProcessor.getInstance().send(dcFanCommand);
                        startStatusChecking();
                    }
                } else {
                    stopStatusChecking();
                }
            }
        });

        // 设置制热输入框的焦点监听
        pidValueHeating.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    // 失去焦点时：只保证键盘隐藏
                    String text = pidValueHeating.getText().toString();
                    if (!StringUtils.isNullOrEmpty(text)) {
                        DCFanCommand dcFanCommand = new DCFanCommand(FunctionObject.SET_EXPANSION_PID_VALUE);
                        byte[] openBytes = ByteUtils.int16ToByteArray(Integer.parseInt(text));
                        byte[] bytes = new byte[3];
                        bytes[0] = 0x02;
                        bytes[1] = openBytes[0];
                        bytes[2] = openBytes[1];
                        dcFanCommand.setData(bytes);
                        SpDataProcessor.getInstance().send(dcFanCommand);
                        startStatusChecking();
                    }
                } else {
                    stopStatusChecking();
                }
            }
        });
        // 设置除湿输入框的焦点监听
        pidValueDehumidifyTarget.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    // 失去焦点时：只保证键盘隐藏
                    Log.d("PidKeyboard", "除湿输入框失去焦点: hasFocus=false");

                    String text = pidValueDehumidifyTarget.getText().toString();
                    if (!StringUtils.isNullOrEmpty(text)) {
                        DCFanCommand dcFanCommand = new DCFanCommand(FunctionObject.SET_EXPANSION_PID_VALUE);
                        byte[] openBytes = ByteUtils.int16ToByteArray(Integer.parseInt(text));
                        byte[] bytes = new byte[3];
                        bytes[0] = 0x03;
                        bytes[1] = openBytes[0];
                        bytes[2] = openBytes[1];
                        dcFanCommand.setData(bytes);
                        SpDataProcessor.getInstance().send(dcFanCommand);
                        startStatusChecking();
                    }
                } else {
                    stopStatusChecking();
                }
            }
        });

        // 设置固定开度调节输入框的焦点监听
        regularValueEt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    // 失去焦点时：只保证键盘隐藏
                    Log.d("PidKeyboard", "固定开度调节输入框失去焦点: hasFocus=false");
                    String text = regularValueEt.getText().toString();
                    if (!StringUtils.isNullOrEmpty(text)) {
                        int parseInt = Integer.parseInt(text);
                        if (parseInt < 0){
                            ToastUtil.showToast(getActivity(),"不得小于0");
                            return;
                        }
                        if (parseInt > 480){
                            ToastUtil.showToast(getActivity(),"不得大于480");
                            return;
                        }

                        DCFanCommand dcFanCommand = new DCFanCommand(FunctionObject.SET_EXPANSION_REGULAR_VALUE);
                        byte[] bytes = ByteUtils.int16ToByteArray(Integer.parseInt(text));
                        dcFanCommand.setData(bytes);
                        SpDataProcessor.getInstance().send(dcFanCommand);
                        startStatusChecking();
                    }
                } else {
                    stopStatusChecking();
                }
            }
        });
        // 设置固定开度调节输入框的焦点监听
        expansionOpenEt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    // 失去焦点时：只保证键盘隐藏
                    Log.d("PidKeyboard", "固定开度调节输入框失去焦点: hasFocus=false");
                    String text = expansionOpenEt.getText().toString();
                    if (!StringUtils.isNullOrEmpty(text)) {
                        int parseInt = Integer.parseInt(text);
                        if (parseInt < 0){
                            ToastUtil.showToast(getActivity(),"不得小于0");
                            return;
                        }
                        if (parseInt > 480){
                            ToastUtil.showToast(getActivity(),"不得大于480");
                            return;
                        }


                        DCFanCommand dcFanCommand = new DCFanCommand(FunctionObject.SET_EXPANSION_OPEN);
                        byte[] openBytes = ByteUtils.int16ToByteArray(parseInt);
                        MySpUtil.setParam(mContext, MySpUtil.KEY_EXPANSION_OPEN, text);
                        byte[] bytes = new byte[3];
                        bytes[0] = 0x01;
                        bytes[1] = openBytes[0];
                        bytes[2] = openBytes[1];
                        dcFanCommand.setData(bytes);
                        SpDataProcessor.getInstance().send(dcFanCommand);
                        startStatusChecking();
                    }
                } else {
                    stopStatusChecking();
                }
            }
        });
        pidValueDehumidify.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    // 失去焦点时：只保证键盘隐藏
                    Log.d("PidKeyboard", "固定开度调节输入框失去焦点: hasFocus=false");
                    String text = pidValueDehumidify.getText().toString();
                    if (!StringUtils.isNullOrEmpty(text)) {

                        int parseInt = Integer.parseInt(text);
                        if (parseInt < 0){
                            ToastUtil.showToast(getActivity(),"不得小于0");
                            return;
                        }
                        if (parseInt > 480){
                            ToastUtil.showToast(getActivity(),"不得大于480");
                            return;
                        }

                        DCFanCommand dcFanCommand = new DCFanCommand(FunctionObject.SET_EXPANSION_OPEN);
                        byte[] openBytes = ByteUtils.int16ToByteArray(parseInt);
                        MySpUtil.setParam(mContext, MySpUtil.KEY_PID_DEHUMIDIFY, text);


                        byte[] bytes = new byte[3];
                        bytes[0] = 0x02;
                        bytes[1] = openBytes[0];
                        bytes[2] = openBytes[1];
                        dcFanCommand.setData(bytes);

                        SpDataProcessor.getInstance().send(dcFanCommand);
                        startStatusChecking();
                    }
                } else {
                    stopStatusChecking();
                }
            }
        });
    }

    private void setupRadioGroupListener() {
        radioGroupType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (!radioGroupType.findViewById(checkedId).isPressed()) {
                    return;
                }
                switch (checkedId) {
                    case R.id.rb_type_4p:
                        heatingDehumidType = (byte) 0x02;
                        break;
                    case R.id.rb_type_2p3p:
                        heatingDehumidType = (byte) 0x01;
                        break;
                    case -1:
                        Log.d("RadioListener", "清除了选中状态");
                        break;
                }
                sendTypeSettingCommand();
            }
        });
        radioGroupXb.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (!radioGroupXb.findViewById(checkedId).isPressed()) {
                    return;
                }

                switch (checkedId) {
                    case R.id.rb_fan_board_1:
                        fanboardType = (byte) 0x01;
                        break;
                    case R.id.rb_fan_board_2:
                        fanboardType = (byte) 0x02;
                        break;
                    case -1:
                        Log.d("RadioListener", "清除了选中状态");
                        break;
                }
                sendTypeSettingCommand();
            }
        });
    }

    private byte[] configData1 = new byte[]{(byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01};// 1. 路数选择: 1路 2. 路数使能: 0x01 (使能) 3. 控制方式选择: 0x01 (自动) 4. 电子膨胀阀使能: 0x01 (使能) 5. 电磁阀使能: 0x01 (使能)

    private void setupExpansionSwitchListener() {

        setExpansionSwitch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (!setExpansionSwitch1.isPressed()) {
                    return;
                }
                if (isChecked) {
                    configData1[1] = (byte) 0x01;
                } else {
                    configData1[1] = (byte) 0x00;
                }
                sendFengJiCommand(configData1);
            }
        });
        radioGroupMode1.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                if (!radioGroupMode1.findViewById(checkedId).isPressed()) {
                    return;
                }

                switch (checkedId) {
                    case R.id.rb_mode1:
                        configData1[2] = (byte) 0x01;
                        break;
                    case R.id.rb_manual1:
                        configData1[2] = (byte) 0x02;
                        break;
                    case R.id.rb_pid1:
                        configData1[2] = (byte) 0x03;
                        break;
                    case -1:
                        Log.d("RadioListener", "清除了选中状态");
                        break;
                }
                sendFengJiCommand(configData1);
            }
        });
        switchShineng1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (!switchShineng1.isPressed()) {
                    return;
                }

                if (isChecked) {
                    configData1[3] = (byte) 0x01;
                } else {
                    configData1[3] = (byte) 0x00;
                }
                sendFengJiCommand(configData1);
            }
        });

        switchShineng2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (!switchShineng2.isPressed()) {
                    return;
                }

                if (isChecked) {
                    configData1[4] = (byte) 0x01;
                } else {
                    configData1[4] = (byte) 0x00;
                }
                sendFengJiCommand(configData1);
            }
        });
    }

    private byte[] configData2 = new byte[]{(byte) 0x02, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01};//1. 路数选择: 1路 2. 路数使能: 0x01 (使能) 3. 控制方式选择: 0x01 (自动) 4. 电子膨胀阀使能: 0x01 (使能) 5. 电磁阀使能: 0x01 (使能)

    private void setupExpansionSwitchListener2() {

        setExpansionSwitch2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (!setExpansionSwitch2.isPressed()) {
                    return;
                }

                if (isChecked) {
                    configData2[1] = (byte) 0x01;
                } else {
                    configData2[1] = (byte) 0x00;
                }
                sendFengJiCommand(configData2);
            }
        });
        radioGroupMode2.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                if (!radioGroupMode2.findViewById(checkedId).isPressed()) {
                    return;
                }

                switch (checkedId) {
                    case R.id.rb_mode2:
                        configData2[2] = (byte) 0x01;
                        break;
                    case R.id.rb_manual2:
                        configData2[2] = (byte) 0x02;
                        break;
                    case R.id.rb_pid2:
                        configData2[2] = (byte) 0x03;
                        break;
                    case -1:
                        Log.d("RadioListener", "清除了选中状态");
                        break;
                }
                sendFengJiCommand(configData2);
            }
        });
        switchShineng3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (!switchShineng3.isPressed()) {
                    return;
                }

                if (isChecked) {
                    configData2[3] = (byte) 0x01;
                } else {
                    configData2[3] = (byte) 0x00;
                }
                sendFengJiCommand(configData2);
            }
        });

        switchShineng4.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (!switchShineng4.isPressed()) {
                    return;
                }

                if (isChecked) {
                    configData2[4] = (byte) 0x01;
                } else {
                    configData2[4] = (byte) 0x00;
                }
                sendFengJiCommand(configData2);
            }
        });
    }

    private void sendFengJiCommand(byte[] configData) {
        // 2. 实例化命令构造器
        DCFanCommand fanConfigCommand = new DCFanCommand(FunctionObject.SET_MAINBOARD_CONFIG);
        fanConfigCommand.setData(configData);
        SpDataProcessor.getInstance().send(fanConfigCommand);
    }

    private void sendTypeSettingCommand() {
        Log.e("TAG", "sendTypeSettingCommand: ");
        DCFanCommand setTypeCommand = new DCFanCommand(FunctionObject.SET_HEATING_DEHUMIDIFICATION_TYPE);
        byte[] sendData = {heatingDehumidType, fanboardType};
        setTypeCommand.setData(sendData);
        SpDataProcessor.getInstance().send(setTypeCommand);
    }

    @OnClick({R.id.li_back})
    public void onReturnClick(View view) {
        stopStatusChecking();
        this.dismiss();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDCFanEvent(DCFanStatusInfo info) {
        Log.e("TAG", "onDCFanEvent: " + new Gson().toJson(info));
        if (info != null) {
            switch (info.getType()) {//
                case FunctionObject.GET_DC_FAN_STATUS:
                    info.sendData();


                    // 1. 读取expansion_open_et的值（字符串转int）
                    int path1EevManualOpen = Integer.parseInt((String) MySpUtil.getParam(mContext, MySpUtil.KEY_EXPANSION_OPEN, "0"));

                    // 2. 读取radio_group_1的选中ID（转byte，0=关闭，1=打开）
                    int radioGroup1CheckedId = (int) MySpUtil.getParam(mContext, MySpUtil.KEY_RADIO_GROUP1_CHECKED, R.id.radio_btn_close);

                    // 3. 读取pid_value_dehumidify的值（字符串转int）
                    int path2EevManualOpen = Integer.parseInt((String) MySpUtil.getParam(mContext, MySpUtil.KEY_PID_DEHUMIDIFY, "0"));

                    // 4. 读取radio_group_2的选中ID（转byte，0=关闭，1=打开）
                    int radioGroup2CheckedId = (int) MySpUtil.getParam(mContext, MySpUtil.KEY_RADIO_GROUP2_CHECKED, R.id.radio_btn_close2);


                    info.setPath1EevManualOpen(ByteUtils.changeBytes(ByteUtils.int16ToByteArray(path1EevManualOpen)));
                    info.setPath1SolenoidValveManualStatus((radioGroup1CheckedId == R.id.radio_btn_open) ? (byte) 0x01 : (byte) 0x00);
                    info.setPath2EevManualOpen(ByteUtils.changeBytes(ByteUtils.int16ToByteArray(path2EevManualOpen)));
                    info.setPath2SolenoidValveManualStatus((radioGroup2CheckedId == R.id.radio_btn_open2) ? (byte) 0x01 : (byte) 0x00);


                    if (info.getPath1EnableStatus() == 1) {
                        setExpansionSwitch1.setChecked(true);
                        llControlMethod1.setVisibility(View.VISIBLE);
                        tvElectronicExpansion.setVisibility(View.GONE);
                        clElectronicExpansion.setVisibility(View.GONE);
                        llEev.setVisibility(View.GONE);
                        llExpansionOpen.setVisibility(View.GONE);
                        tvPid.setVisibility(View.GONE);
                        llPidValueHeating.setVisibility(View.GONE);
                        llPidValueRefrigeration.setVisibility(View.GONE);
                        tvDian.setVisibility(View.GONE);
                        clSwitchShineng2.setVisibility(View.GONE);
                        llManualMode.setVisibility(View.GONE);
                        tvSwitch1.setVisibility(View.VISIBLE);
                    } else {
                        setExpansionSwitch1.setChecked(false);
                        llControlMethod1.setVisibility(View.GONE);
                        tvElectronicExpansion.setVisibility(View.GONE);
                        clElectronicExpansion.setVisibility(View.GONE);
                        llEev.setVisibility(View.GONE);
                        llExpansionOpen.setVisibility(View.GONE);
                        tvPid.setVisibility(View.GONE);
                        llPidValueHeating.setVisibility(View.GONE);
                        llPidValueRefrigeration.setVisibility(View.GONE);
                        tvDian.setVisibility(View.GONE);
                        clSwitchShineng2.setVisibility(View.GONE);
                        llManualMode.setVisibility(View.GONE);
                        tvSwitch1.setVisibility(View.GONE);

                    }

                    if (info.getPath2EnableStatus() == 1) {
                        setExpansionSwitch2.setChecked(true);
                        llControlMethod2.setVisibility(View.VISIBLE);
                        tvElectronicExpansion2.setVisibility(View.GONE);
                        clElectronicExpansion3.setVisibility(View.GONE);
                        llEev2.setVisibility(View.GONE);
                        llExpansionOpen2.setVisibility(View.GONE);
                        tvPid2.setVisibility(View.GONE);
                        llPidValueHeating2.setVisibility(View.GONE);
                        llPidValueRefrigeration2.setVisibility(View.GONE);
                        tvDian2.setVisibility(View.GONE);
                        clSwitchShineng4.setVisibility(View.GONE);
                        llManualMode2.setVisibility(View.GONE);
                        tvSwitch2.setVisibility(View.VISIBLE);
                    } else {
                        setExpansionSwitch2.setChecked(false);
                        llControlMethod2.setVisibility(View.GONE);
                        tvElectronicExpansion2.setVisibility(View.GONE);
                        clElectronicExpansion3.setVisibility(View.GONE);
                        llEev2.setVisibility(View.GONE);
                        llExpansionOpen2.setVisibility(View.GONE);
                        tvPid2.setVisibility(View.GONE);
                        llPidValueHeating2.setVisibility(View.GONE);
                        llPidValueRefrigeration2.setVisibility(View.GONE);
                        tvDian2.setVisibility(View.GONE);
                        clSwitchShineng4.setVisibility(View.GONE);
                        llManualMode2.setVisibility(View.GONE);
                        tvSwitch2.setVisibility(View.GONE);
                    }

                    tvSwitch2.setText("开关状态：" + (info.getDehumSolenoidValveSwitch() == 1 ? "开" : "关"));

                    tvEev2.setText(info.getDehumExpansionOpen() + "");

                    pidValueDehumidifyTarget.setText(info.getDehumPidValue() + "");
                    if (info.getHeatingDehumType() == 1) {
                        rbType2p3p.setChecked(true);
                        llMain1.setVisibility(View.GONE);
                    } else if (info.getHeatingDehumType() == 2) {
                        rbType4p.setChecked(true);
                        llMain1.setVisibility(View.VISIBLE);
                    }

                    tvSwitch1.setText("开关状态：\u0020" + (info.getColdHotSolenoidValveSwitch() == 1 ? "开" : "关"));

                    tvEev1.setText(info.getColdHotEevOpen() + "");

                    if (info.getFanBoardMode() == 1) {
                        rbFanBoard1.setChecked(true);
                    } else if (info.getFanBoardMode() == 2) {
                        rbFanBoard2.setChecked(true);
                    }

                    switchShineng1.setChecked(info.getPath1ColdHotEevEnableStatus() == 1);
                    switchShineng2.setChecked(info.getPath1ColdHotSolenoidValveEnableStatus() == 1);
                    setExpansionSwitch2.setChecked(info.getPath2EnableStatus() == 1);
                    configData1[1] = (byte) info.getPath1EnableStatus();
                    configData1[2] = (byte) info.getPath1ControlType();
                    configData1[3] = (byte) info.getPath1ColdHotEevEnableStatus();
                    configData1[4] = (byte) info.getPath1ColdHotSolenoidValveEnableStatus();

                    configData2[1] = (byte) info.getPath2EnableStatus();
                    configData2[2] = (byte) info.getPath2ControlType();
                    configData2[3] = (byte) info.getPath2DehumEevEnableStatus();
                    configData2[4] = (byte) info.getPath2DehumSolenoidValveEnableStatus();

                    if (info.getPath1ControlType() == 1 && info.getPath1EnableStatus() == 1) {
                        rbMode1.setChecked(true);
                        switchShineng1.setEnabled(false);
                        switchShineng2.setEnabled(false);
                        tvElectronicExpansion.setVisibility(View.VISIBLE);
                        clElectronicExpansion.setVisibility(View.VISIBLE);
                        llEev.setVisibility(View.VISIBLE);
                        llExpansionOpen.setVisibility(View.INVISIBLE);
                        tvPid.setVisibility(View.GONE);
                        llPidValueHeating.setVisibility(View.GONE);
                        llPidValueRefrigeration.setVisibility(View.GONE);
                        tvDian.setVisibility(View.VISIBLE);
                        clSwitchShineng2.setVisibility(View.VISIBLE);
                        llManualMode.setVisibility(View.GONE);
                        tvSwitch1.setVisibility(View.VISIBLE);
                    } else if (info.getPath1ControlType() == 2 && info.getPath1EnableStatus() == 1) {
                        rbManual1.setChecked(true);

                        if (switchShineng1.isChecked()) {
                            llEev.setVisibility(View.VISIBLE);
                            llExpansionOpen.setVisibility(View.VISIBLE);
                            tvPid.setVisibility(View.VISIBLE);
                            llPidValueHeating.setVisibility(View.VISIBLE);
                            llPidValueRefrigeration.setVisibility(View.VISIBLE);
                        } else {
                            llEev.setVisibility(View.GONE);
                            llExpansionOpen.setVisibility(View.GONE);
                            tvPid.setVisibility(View.GONE);
                            llPidValueHeating.setVisibility(View.GONE);
                            llPidValueRefrigeration.setVisibility(View.GONE);

                        }

                        if (switchShineng2.isChecked()) {
                            tvSwitch1.setVisibility(View.VISIBLE);
                            llManualMode.setVisibility(View.VISIBLE);
                        } else {
                            tvSwitch1.setVisibility(View.GONE);
                            llManualMode.setVisibility(View.GONE);
                        }

                        tvElectronicExpansion.setVisibility(View.VISIBLE);
                        clElectronicExpansion.setVisibility(View.VISIBLE);
                        tvDian.setVisibility(View.VISIBLE);
                        clSwitchShineng2.setVisibility(View.VISIBLE);
                        switchShineng1.setEnabled(true);
                        switchShineng2.setEnabled(true);
                    } else if (info.getPath1ControlType() == 3 && info.getPath1EnableStatus() == 1) {
                        rbPid1.setChecked(true);
                        switchShineng1.setEnabled(false);
                        switchShineng2.setEnabled(false);
                        tvElectronicExpansion.setVisibility(View.VISIBLE);
                        clElectronicExpansion.setVisibility(View.VISIBLE);
                        llEev.setVisibility(View.VISIBLE);
                        llExpansionOpen.setVisibility(View.INVISIBLE);
                        tvPid.setVisibility(View.VISIBLE);
                        llPidValueHeating.setVisibility(View.VISIBLE);
                        llPidValueRefrigeration.setVisibility(View.VISIBLE);
                        tvDian.setVisibility(View.VISIBLE);
                        clSwitchShineng2.setVisibility(View.VISIBLE);
                        llManualMode.setVisibility(View.GONE);
                        tvSwitch1.setVisibility(View.VISIBLE);
                    }

                    switchShineng3.setChecked(info.getPath2DehumEevEnableStatus() == 1);
                    switchShineng4.setChecked(info.getPath2DehumSolenoidValveEnableStatus() == 1);


                    if (info.getPath2ControlType() == 1 && info.getPath2EnableStatus() == 1) {
                        rbMode2.setChecked(true);
                        switchShineng3.setEnabled(false);
                        switchShineng4.setEnabled(false);
                        tvDian2.setVisibility(View.VISIBLE);
                        tvElectronicExpansion2.setVisibility(View.VISIBLE);
                        clElectronicExpansion3.setVisibility(View.VISIBLE);
                        llEev2.setVisibility(View.VISIBLE);
                        llExpansionOpen2.setVisibility(View.GONE);
                        tvPid2.setVisibility(View.GONE);
                        llPidValueHeating2.setVisibility(View.GONE);
                        llPidValueRefrigeration2.setVisibility(View.VISIBLE);
                        clSwitchShineng4.setVisibility(View.VISIBLE);
                        llManualMode2.setVisibility(View.GONE);
                        tvSwitch2.setVisibility(View.VISIBLE);
                    } else if (info.getPath2ControlType() == 2 && info.getPath2EnableStatus() == 1) {
                        rbManual2.setChecked(true);

                        if (switchShineng3.isChecked()) {
                            llEev2.setVisibility(View.VISIBLE);
                            llExpansionOpen2.setVisibility(View.VISIBLE);
                            tvPid2.setVisibility(View.VISIBLE);
                            llPidValueHeating2.setVisibility(View.VISIBLE);
                            llPidValueRefrigeration2.setVisibility(View.VISIBLE);
                        } else {
                            llEev2.setVisibility(View.GONE);
                            llExpansionOpen2.setVisibility(View.GONE);
                            tvPid2.setVisibility(View.GONE);
                            llPidValueHeating2.setVisibility(View.GONE);
                            llPidValueRefrigeration2.setVisibility(View.GONE);
                        }

                        if (switchShineng4.isChecked()) {
                            tvSwitch2.setVisibility(View.VISIBLE);
                            llManualMode2.setVisibility(View.VISIBLE);
                        } else {
                            tvSwitch2.setVisibility(View.GONE);
                            llManualMode2.setVisibility(View.GONE);
                        }

                        switchShineng3.setEnabled(true);
                        switchShineng4.setEnabled(true);
                        tvElectronicExpansion2.setVisibility(View.VISIBLE);
                        clElectronicExpansion3.setVisibility(View.VISIBLE);
                        tvDian2.setVisibility(View.VISIBLE);
                        clSwitchShineng4.setVisibility(View.VISIBLE);
                    } else if (info.getPath2ControlType() == 3 && info.getPath2EnableStatus() == 1) {
                        rbPid2.setChecked(true);

                        switchShineng3.setEnabled(false);
                        switchShineng4.setEnabled(false);

                        tvElectronicExpansion2.setVisibility(View.VISIBLE);
                        clElectronicExpansion3.setVisibility(View.VISIBLE);
                        llEev2.setVisibility(View.VISIBLE);
                        llExpansionOpen2.setVisibility(View.GONE);
                        tvPid2.setVisibility(View.VISIBLE);
                        llPidValueHeating2.setVisibility(View.VISIBLE);
                        llPidValueRefrigeration2.setVisibility(View.VISIBLE);
                        tvDian2.setVisibility(View.VISIBLE);
                        clSwitchShineng4.setVisibility(View.VISIBLE);
                        llManualMode2.setVisibility(View.GONE);
                        tvSwitch2.setVisibility(View.VISIBLE);
                    }
                    pidValueRefrigeration.setText(info.getColdHotSolenoidValveCoolingPidValue() + "");
                    pidValueHeating.setText(info.getColdHotSolenoidValveHeatingPidValue() + "");
                    regularValueEt.setText(info.getDehumEevFixedOpenValue()+"");

                    break;
                case FunctionObject.SET_DC_FAN_SPEED:
                    if (info.getSuccess()) {
                        ToastUtil.showToast(getActivity(), getString(R.string.set_success));
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                DCFanCommand dcFanCommand = new DCFanCommand(FunctionObject.GET_DC_FAN_STATUS);
                                SpDataProcessor.getInstance().send(dcFanCommand);
                                Log.d("TAG", "延迟查询指令已发送，重启定时器。");
                            }
                        }, 200);
                    } else {
                        ToastUtil.showToast(getActivity(), getString(R.string.set_fail));
                    }
                    break;
            }
        }
    }

    /**
     * 启动定时任务
     */
    private void startStatusChecking() {
        if (mStatusChecker == null) {
            mStatusChecker = new Runnable() {
                @Override
                public void run() {
                    Log.d("TAG", "ValveSettingFragment: 定时获取 DC 风机状态");
                    DCFanCommand dcFanCommand = new DCFanCommand(FunctionObject.GET_DC_FAN_STATUS);
                    SpDataProcessor.getInstance().send(dcFanCommand);
                    mHandler.postDelayed(mStatusChecker, STATUS_CHECK_INTERVAL);
                }
            };
        }
        mHandler.removeCallbacks(mStatusChecker);
        mHandler.postDelayed(mStatusChecker, STATUS_CHECK_INTERVAL);
    }

    /**
     * 停止定时任务
     */
    private void stopStatusChecking() {
        if (mHandler != null && mStatusChecker != null) {
            mHandler.removeCallbacks(mStatusChecker);
            Log.d("TAG", "ValveSettingFragment: 停止定时获取 DC 风机状态");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // 界面从前台切换到后台或不再可见时，停止定时器
        stopStatusChecking();
    }

    @Override
    public void onDestroyView() {
        if (unbinder != null) {
            unbinder.unbind();
        }
        EventBus.getDefault().unregister(this);
        stopStatusChecking();

        super.onDestroyView();
    }

    private void setKeyboardVisibilityListener(final View rootLayout) {
        rootLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            private boolean isKeyboardVisible = false;

            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                rootLayout.getWindowVisibleDisplayFrame(r);

                int heightDiff = rootLayout.getRootView().getHeight() - (r.bottom - r.top);

                int softKeyBoardThreshold = rootLayout.getRootView().getHeight() / 5;

                if (heightDiff > softKeyBoardThreshold) {
                    // 键盘已弹出
                    if (!isKeyboardVisible) {
                        isKeyboardVisible = true;
                        onSoftKeyboardShown(true);
                    }
                } else {
                    // 键盘已关闭
                    if (isKeyboardVisible) {
                        isKeyboardVisible = false;
                        onSoftKeyboardShown(false);
                    }
                }
            }
        });
    }

    /**
     * 软键盘状态变化回调
     *
     * @param isVisible true 表示软键盘弹出，false 表示软键盘隐藏
     */
    private void onSoftKeyboardShown(boolean isVisible) {
        if (!isVisible) {
            if (pidValueRefrigeration != null && pidValueRefrigeration.hasFocus()) {
                pidValueRefrigeration.clearFocus();
            }
            if (pidValueHeating != null && pidValueHeating.hasFocus()) {
                pidValueHeating.clearFocus();
            }
            if (expansionOpenEt != null && expansionOpenEt.hasFocus()) {
                expansionOpenEt.clearFocus();
            }
            if (pidValueDehumidify != null && pidValueDehumidify.hasFocus()) {
                pidValueDehumidify.clearFocus();
            }
            if (regularValueEt != null && regularValueEt.hasFocus()) {
                regularValueEt.clearFocus();
            }
            if (pidValueDehumidifyTarget != null && pidValueDehumidifyTarget.hasFocus()) {
                pidValueDehumidifyTarget.clearFocus();
            }
        }
    }
}
