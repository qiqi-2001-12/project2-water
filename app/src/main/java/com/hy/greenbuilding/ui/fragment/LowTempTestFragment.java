package com.hy.greenbuilding.ui.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.gson.Gson;
import com.hy.greenbuilding.HyApplication;
import com.hy.greenbuilding.R;
import com.hy.greenbuilding.event.FunctionTestEvent;
import com.hy.greenbuilding.protocol.FunctionObject;
import com.hy.greenbuilding.protocol.ResPonseInfo.OutDoorStatusInfo;
import com.hy.greenbuilding.protocol.ResPonseInfo.PVStatusInfo;
import com.hy.greenbuilding.protocol.ResPonseInfo.UpTempStatusInfo;
import com.hy.greenbuilding.protocol.SpCommand;
import com.hy.greenbuilding.protocol.SpDataProcessor;
import com.hy.greenbuilding.protocol.command.FanCommand;
import com.hy.greenbuilding.protocol.command.LowTempCommand;
import com.hy.greenbuilding.protocol.command.PVCommand;
import com.hy.greenbuilding.protocol.command.UpTempCommand;
import com.hy.greenbuilding.utils.ByteUtils;
import com.hy.greenbuilding.utils.InputLimitUtil;
import com.hy.greenbuilding.utils.StringUtils;
import com.hy.greenbuilding.utils.ToastUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * 测试页面
 */
public class LowTempTestFragment extends BaseDialogFragment implements RadioGroup.OnCheckedChangeListener {
    @BindView(R.id.li_back)
    LinearLayout mReturnView;

    @BindView(R.id.li_aux_valve)
    LinearLayout mLiAuxValve;

    @BindView(R.id.radiogroup_test)
    RadioGroup mRadioGroup;

    @BindView(R.id.rb_hotE)
    RadioButton mRbHotE;
    @BindView(R.id.rb_hotA)
    RadioButton mRbHotA;
    @BindView(R.id.rb_hotB)
    RadioButton mRbHotB;
    @BindView(R.id.rb_hotC)
    RadioButton mRbHotC;
    @BindView(R.id.rb_hotD)
    RadioButton mRbHotD;
    @BindView(R.id.rb_coldA)
    RadioButton mRbColdA;
    @BindView(R.id.rb_coldB)
    RadioButton mRbColdB;
    @BindView(R.id.rb_coldC)
    RadioButton mRbColdC;
    @BindView(R.id.rb_coldD)
    RadioButton mRbColdD;

    @BindView(R.id.bt_function_test)
    Button mFunctionTest;
    @BindView(R.id.bt_force_flu)
    Button mForceFlu;
    @BindView(R.id.bt_force_defrost)
    Button mForceDefrost;

    @BindView(R.id.bt_frequency)
    Button mFrequency;
    @BindView(R.id.bt_main_valve)
    Button mMainValve;
    @BindView(R.id.bt_aux_valve)
    Button mAuxValve;

    @BindView(R.id.et_frequency)
    EditText mEtFrequency;
    @BindView(R.id.et_main_valve)
    EditText mEtMainValve;
    @BindView(R.id.et_aux_valve)
    EditText mEtAuxValve;

    private View mView;
    private Activity mContext;
    private Unbinder unbinder;
    private int mOutType = 1;//室外机类型
    private boolean isForceDefrost;//强制除霜是否开启
    private boolean isForceFlu;//强制除氟是否开启
    private boolean frequencyOpen;//压缩机开关
    private boolean mainValveOpen;//主膨胀阀开关
    private boolean auxValveOpen;//辅膨胀阀开关
    private boolean isFunctionTest;//能力测试是否开启
    private boolean isInput;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.DialogFullScreen); //dialog全屏
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        this.getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        mView = inflater.inflate(R.layout.low_temp_test, null);
        mContext = this.getActivity();
        unbinder = ButterKnife.bind(this, mView);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        init();
        isInput = true;
        if (mOutType == 1) {
            LowTempCommand lowTempCommand = new LowTempCommand(FunctionObject.GET_OUT_STATUS);
            SpDataProcessor.getInstance().send(lowTempCommand);
        } else if (mOutType == 2) {
            PVCommand pvCommand = new PVCommand(FunctionObject.GET_OUT_STATUS);
            SpDataProcessor.getInstance().send(pvCommand);
        } else if (mOutType == 3) {
            UpTempCommand pvCommand = new UpTempCommand(FunctionObject.GET_OUT_STATUS);
            SpDataProcessor.getInstance().send(pvCommand);
        }
        return mView;
    }

    private void init() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            mOutType = bundle.getInt("outType");
        }
        //升温除湿 显示辅膨胀阀
        if (mOutType != 3) {
            mLiAuxValve.setVisibility(View.INVISIBLE);
        } else {
            mLiAuxValve.setVisibility(View.VISIBLE);
        }
        mRadioGroup.setOnCheckedChangeListener(this);
        disableRadioGroup(mRadioGroup);
    }


    //能力测试选项不可点击
    public void disableRadioGroup(RadioGroup radioGroup) {
        for (int i = 0; i < radioGroup.getChildCount(); i++) {
            radioGroup.getChildAt(i).setClickable(false);
        }
    }

    //能力测试选项可点击
    public void enableRadioGroup(RadioGroup radioGroup) {
        for (int i = 0; i < radioGroup.getChildCount(); i++) {
            radioGroup.getChildAt(i).setClickable(true);
        }
    }

    //设置默认选中
    public void setSelect(String radioName) {
        switch (radioName) {
            case "0000":
                mRbHotE.setChecked(true);
                break;
            case "0001":
                mRbHotA.setChecked(true);
                break;
            case "0002":
                mRbHotB.setChecked(true);
                break;
            case "0003":
                mRbHotC.setChecked(true);
                break;
            case "0004":
                mRbHotD.setChecked(true);
                break;
            case "0005":
                mRbColdA.setChecked(true);
                break;
            case "0006":
                mRbColdB.setChecked(true);
                break;
            case "0007":
                mRbColdC.setChecked(true);
                break;
            case "0008":
                mRbColdD.setChecked(true);
                break;
        }
    }

    @OnClick({R.id.li_back})
    public void onReturnClick(View view) {
        this.dismiss();
    }

    @OnClick({R.id.bt_function_test})
    public void onTestClick(View view) {
        Log.e("TAG", "onTestClick: "+isFunctionTest);
        if (isFunctionTest) {
            sendCloseTestCommand();
        } else {
            mFunctionTest.setBackground(mContext.getDrawable(R.drawable.btn_bg_common));
            isFunctionTest = true;
            enableRadioGroup(mRadioGroup);
        }
    }

    /**
     * 关闭能力测试命令
     */
    private void sendCloseTestCommand() {
        SpCommand spCommand;
        Log.e("TAG", "sendCloseTestCommand: "+mOutType);
        if (mOutType == 1) {
            spCommand = new LowTempCommand(FunctionObject.ABILITY_TEST);
        } else if (mOutType == 2) {
            spCommand = new PVCommand(FunctionObject.ABILITY_TEST);
        } else {
            spCommand = new UpTempCommand(FunctionObject.ABILITY_TEST);
        }
        spCommand.setData(new byte[]{(byte) 0x00, (byte) 0x09});
        SpDataProcessor.getInstance().send(spCommand);
    }

    @OnClick({R.id.bt_force_flu})
    public void onForceFluClick(View view) {
        //强制收氟
        if (isForceDefrost) {
            return;
        }
        SpCommand spCommand;
        if (mOutType == 1) {
            spCommand = new LowTempCommand(FunctionObject.SET_MODE);
        } else if (mOutType == 2) {
            spCommand = new PVCommand(FunctionObject.SET_MODE);
        } else {
            spCommand = new UpTempCommand(FunctionObject.SET_MODE);
        }
        if (isForceFlu) {
            spCommand.setData(new byte[]{(byte) 0x00, (byte) 0x07});
        } else {
            spCommand.setData(new byte[]{(byte) 0x00, (byte) 0x06});
        }
        SpDataProcessor.getInstance().send(spCommand);
    }

    @OnClick({R.id.bt_force_defrost})
    public void onForceDefrostClick(View view) {
        //强制除霜
        if (isForceFlu) {
            return;
        }
        SpCommand spCommand;
        if (mOutType == 1) {
            spCommand = new LowTempCommand(FunctionObject.FORCE_DEFROST);
        } else if (mOutType == 2) {
            spCommand = new PVCommand(FunctionObject.FORCE_DEFROST);
        } else {
            spCommand = new UpTempCommand(FunctionObject.FORCE_DEFROST);
        }
        if (isForceDefrost) {
            spCommand.setData(new byte[]{(byte) 0x00, (byte) 0x00});
        } else {
            spCommand.setData(new byte[]{(byte) 0x00, (byte) 0x01});
        }
        SpDataProcessor.getInstance().send(spCommand);
    }

    @OnClick({R.id.bt_frequency})
    public void onFrequencyClick(View view) {
        if (frequencyOpen) {
            mFrequency.setBackground(mContext.getDrawable(R.drawable.button_style_bg));
            frequencyOpen = false;
            mEtFrequency.setEnabled(true);
        } else {
            mFrequency.setBackground(mContext.getDrawable(R.drawable.btn_bg_common));
            frequencyOpen = true;
            mEtFrequency.setEnabled(false);
        }
    }

    @OnClick({R.id.bt_main_valve})
    public void onMainValveClick(View view) {
        if (mainValveOpen) {
            mMainValve.setBackground(mContext.getDrawable(R.drawable.button_style_bg));
            mainValveOpen = false;
            mEtMainValve.setEnabled(true);
        } else {
            mMainValve.setBackground(mContext.getDrawable(R.drawable.btn_bg_common));
            mainValveOpen = true;
            mEtMainValve.setEnabled(false);
        }
    }

    @OnClick({R.id.bt_aux_valve})
    public void onAuxValveClick(View view) {
        if (auxValveOpen) {
            mAuxValve.setBackground(mContext.getDrawable(R.drawable.button_style_bg));
            auxValveOpen = false;
            mEtAuxValve.setEnabled(true);
        } else {
            mAuxValve.setBackground(mContext.getDrawable(R.drawable.btn_bg_common));
            auxValveOpen = true;
            mEtAuxValve.setEnabled(false);

        }
    }

    @OnClick({R.id.bt_test_commit})
    public void onCommitClick(View view) {
        String frequencyText = mEtFrequency.getText().toString();
        String mainValveText = mEtMainValve.getText().toString();
        String auxValveText = mEtAuxValve.getText().toString();
        //设置压缩机定频
        if (!StringUtils.isNullOrEmpty(frequencyText) && !frequencyOpen) {
            SpCommand spCommand;
            if (mOutType == 1) {
                spCommand = new LowTempCommand(FunctionObject.SET_POWER);
            } else if (mOutType == 2) {
                spCommand = new PVCommand(FunctionObject.SET_POWER);
            } else {
                spCommand = new UpTempCommand(FunctionObject.SET_POWER);
            }
            byte[] bytes = ByteUtils.int16ToByteArray(Integer.parseInt(frequencyText));
            spCommand.setData(bytes);
            SpDataProcessor.getInstance().send(spCommand);
        }
        //设置主膨胀阀开度
        if (!StringUtils.isNullOrEmpty(mainValveText) && !mainValveOpen) {
            if (!InputLimitUtil.mainExpansionLimit(mainValveText)) {
                ToastUtil.showToast(mContext, getString(R.string.set_format_error));
                return;
            }
            SpCommand spCommand;
            if (mOutType == 1) {
                spCommand = new LowTempCommand(FunctionObject.MAIN_EXPANSION);
            } else if (mOutType == 2) {
                spCommand = new PVCommand(FunctionObject.MAIN_EXPANSION);
            } else {
                spCommand = new UpTempCommand(FunctionObject.MAIN_EXPANSION);
            }
            byte[] bytes = ByteUtils.int16ToByteArray(Integer.parseInt(mainValveText));
            spCommand.setData(bytes);
            SpDataProcessor.getInstance().send(spCommand);
        }
        //设置辅膨胀阀开度
        if (!StringUtils.isNullOrEmpty(auxValveText) && !auxValveOpen) {
            if (!InputLimitUtil.auxExpansionLimit(auxValveText)) {
                ToastUtil.showToast(mContext, getString(R.string.set_format_error));
                return;
            }
            if (mOutType == 3) {
                SpCommand spCommand = new UpTempCommand(FunctionObject.AUX_EXPANSION);
                byte[] bytes = ByteUtils.int16ToByteArray(Integer.parseInt(auxValveText));
                spCommand.setData(bytes);
                SpDataProcessor.getInstance().send(spCommand);
            }
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int i) {
        SpCommand spCommand;
        boolean isSend = false;//是否点击单选框
        if (mOutType == 1) {
            spCommand = new LowTempCommand(FunctionObject.ABILITY_TEST);
        } else if (mOutType == 2) {
            spCommand = new PVCommand(FunctionObject.ABILITY_TEST);
        } else {
            spCommand = new UpTempCommand(FunctionObject.ABILITY_TEST);
        }
        switch (i) {
            case R.id.rb_hotE:
                if (mRbHotE.isPressed()) {
                    spCommand.setData(new byte[]{(byte) 0x00, (byte) 0x00});
                    isSend = true;
                }
                break;
            case R.id.rb_hotA:
                if (mRbHotA.isPressed()) {
                    spCommand.setData(new byte[]{(byte) 0x00, (byte) 0x01});
                    isSend = true;
                }
                break;
            case R.id.rb_hotB:
                if (mRbHotB.isPressed()) {
                    spCommand.setData(new byte[]{(byte) 0x00, (byte) 0x02});
                    isSend = true;
                }
                break;
            case R.id.rb_hotC:
                if (mRbHotC.isPressed()) {
                    spCommand.setData(new byte[]{(byte) 0x00, (byte) 0x03});
                    isSend = true;
                }
                break;
            case R.id.rb_hotD:
                if (mRbHotD.isPressed()) {
                    spCommand.setData(new byte[]{(byte) 0x00, (byte) 0x04});
                    isSend = true;
                }
                break;
            case R.id.rb_coldA:
                if (mRbColdA.isPressed()) {
                    spCommand.setData(new byte[]{(byte) 0x00, (byte) 0x05});
                    isSend = true;
                }
                break;
            case R.id.rb_coldB:
                if (mRbColdB.isPressed()) {
                    spCommand.setData(new byte[]{(byte) 0x00, (byte) 0x06});
                    isSend = true;
                }
                break;
            case R.id.rb_coldC:
                if (mRbColdC.isPressed()) {
                    spCommand.setData(new byte[]{(byte) 0x00, (byte) 0x07});
                    isSend = true;
                }
                break;
            case R.id.rb_coldD:
                if (mRbColdD.isPressed()) {
                    spCommand.setData(new byte[]{(byte) 0x00, (byte) 0x08});
                    isSend = true;
                }
                break;
        }
        if (isFunctionTest && isSend) {
            SpDataProcessor.getInstance().send(spCommand);
            disableRadioGroup(mRadioGroup);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(PVStatusInfo info) {
        if (info != null) {
            initView(info.defrostStatus(), info.airModeHex());
            if (isInput) {
                mEtFrequency.setText(info.frequencyTestValue() + "");
                mEtMainValve.setText(info.mainExpansionTest() + "");
                if (!info.functionTestValue().equals("0009")) {
                    mFunctionTest.setBackground(mContext.getDrawable(R.drawable.btn_bg_common));
                    isFunctionTest = true;
                    setSelect(info.functionTestValue());
                } else {
                    mFunctionTest.setBackground(mContext.getDrawable(R.drawable.button_style_bg));
                    isFunctionTest = false;
                }
                isInput = false;
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(UpTempStatusInfo info) {
    }

    private void initView(int defrost, String airMode) {
        if (defrost == 0) {
            //关
            isForceDefrost = false;
            mForceDefrost.setBackground(mContext.getDrawable(R.drawable.button_style_bg));
        } else {
            //1 开
            isForceDefrost = true;
            mForceDefrost.setBackground(mContext.getDrawable(R.drawable.btn_bg_common));
        }
        if (airMode.equals("0006")) {
            isForceFlu = true;
            mForceFlu.setBackground(mContext.getDrawable(R.drawable.btn_bg_common));
        } else {
            isForceFlu = false;
            mForceFlu.setBackground(mContext.getDrawable(R.drawable.button_style_bg));
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(OutDoorStatusInfo info) {
        if (info != null) {
            initView(info.defrostStatus(), info.airModeHex());
            if (isInput) {
                mEtFrequency.setText(info.frequencyTestValue() + "");
                mEtMainValve.setText(info.mainExpansionTest() + "");
                if (!info.functionTestValue().equals("0009")) {
                    mFunctionTest.setBackground(mContext.getDrawable(R.drawable.btn_bg_common));
                    isFunctionTest = true;
                    setSelect(info.functionTestValue());
                } else {
                    mFunctionTest.setBackground(mContext.getDrawable(R.drawable.button_style_bg));
                    isFunctionTest = false;
                }
                isInput = false;
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(FunctionTestEvent event) {
        if (event != null) {
            switch (event.getType()) {
                case 1:
                    //强制收氟
                    if (event.isSuccess()) {
                        HyApplication.setForceFlu(true);
                        ToastUtil.showToast(mContext, "除氟打开!");
                        isForceFlu = true;
                        mForceFlu.setBackground(mContext.getDrawable(R.drawable.btn_bg_common));
                    } else {
                        ToastUtil.showToast(mContext, "除氟关闭!");
                        HyApplication.setForceFlu(false);
                        isForceFlu = false;
                        mForceFlu.setBackground(mContext.getDrawable(R.drawable.button_style_bg));
                    }
                    break;
                case 2:
                    //设定压缩机频率
                    if (event.isSuccess()) {
                        ToastUtil.showToast(mContext, "定频设置成功!");
                    } else {
                        ToastUtil.showToast(mContext, "定频设置失败!");
                    }
                    break;
                case 3:
                    //强制除霜
                    if (isForceDefrost) {
                        ToastUtil.showToast(mContext, "除霜关闭!");
                        isForceDefrost = false;
                        mForceDefrost.setBackground(mContext.getDrawable(R.drawable.button_style_bg));
                    } else {
                        ToastUtil.showToast(mContext, "除霜打开!");
                        isForceDefrost = true;
                        mForceDefrost.setBackground(mContext.getDrawable(R.drawable.btn_bg_common));
                        //除霜打开获取风机档位
                        FanCommand fanCommand = new FanCommand(FunctionObject.GET_FAN_STATUS);
                        SpDataProcessor.getInstance().send(fanCommand);
                    }
                    break;
                case 4:
                    //能力测试
                    if (event.getStatus().equals("00")) {
                        ToastUtil.showToast(mContext, "能力测试成功!");
                    } else if (event.getStatus().equals("01")) {
                        ToastUtil.showToast(mContext, "能力测试失败!");
                    } else if (event.getStatus().equals("02")) {
                        ToastUtil.showToast(mContext, "关闭测试!");
                        mRadioGroup.clearCheck();
                        disableRadioGroup(mRadioGroup);
                        mFunctionTest.setBackground(mContext.getDrawable(R.drawable.button_style_bg));
                        isFunctionTest = false;
                    }
                    break;
                case 5:
                    //设定主膨胀阀
                    if (event.isSuccess()) {
                        ToastUtil.showToast(mContext, "主膨胀阀设置成功!");
                    } else {
                        ToastUtil.showToast(mContext, "主膨胀阀设置失败!");
                    }
                    break;
                case 6:
                    //设定辅膨胀阀
                    if (event.isSuccess()) {
                        ToastUtil.showToast(mContext, "辅膨胀阀设置成功!");
                    } else {
                        ToastUtil.showToast(mContext, "辅膨胀阀设置失败!");
                    }
                    break;
            }
        }
    }

    @Override
    public void onDestroyView() {
        if (unbinder != null) {
            unbinder.unbind();
        }
        EventBus.getDefault().unregister(this);
        super.onDestroyView();
    }
}
