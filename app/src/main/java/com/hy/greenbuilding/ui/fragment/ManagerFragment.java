package com.hy.greenbuilding.ui.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.hwellyi.smarthome.MainGatewayActivity;
import com.hy.greenbuilding.HyApplication;
import com.hy.greenbuilding.R;
import com.hy.greenbuilding.config.SaveControlInfo;
import com.hy.greenbuilding.event.SetStatusEvent;
import com.hy.greenbuilding.protocol.FunctionObject;
import com.hy.greenbuilding.protocol.ResPonseInfo.ElectricityMeterInfo;
import com.hy.greenbuilding.protocol.SpDataProcessor;
import com.hy.greenbuilding.protocol.command.ControlCommand;
import com.hy.greenbuilding.protocol.command.OTARequestCommand;
import com.hy.greenbuilding.ui.activity.AirValveControlActivity;
import com.hy.greenbuilding.ui.activity.BaseActivity;
import com.hy.greenbuilding.ui.activity.FanResetActivity;
import com.hy.greenbuilding.ui.activity.FanTestActivity;
import com.hy.greenbuilding.utils.InputLimitUtil;
import com.hy.greenbuilding.utils.MySpUtil;
import com.hy.greenbuilding.utils.StringUtils;
import com.hy.greenbuilding.utils.ToastUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * 管理员界面 Fragment
 */
public class ManagerFragment extends Fragment implements CompoundButton.OnCheckedChangeListener {

    // 绑定视图
    @BindView(R.id.li_back)
    ImageView mReturnView;
    @BindView(R.id.li_funTest)
    LinearLayout mFunTestView;
    @BindView(R.id.li_funReset)
    LinearLayout mFunResetView;
    @BindView(R.id.li_fangdong)
    LinearLayout mFangDongView;
    @BindView(R.id.li_lowTemp)
    RelativeLayout mLowTempView;
    @BindView(R.id.tv_spinner_title)
    TextView mTitleSpinner;
    @BindView(R.id.li_spinner)
    LinearLayout mLiSpinner;
    @BindView(R.id.li_gateway)
    LinearLayout mGateWay;
    @BindView(R.id.li_valve)
    LinearLayout mValveView;
    @BindView(R.id.electric_tv)
    LinearLayout electricTv;
    @BindView(R.id.li_air_valve)
    LinearLayout mAirValveView;
    @BindView(R.id.ll_location)
    LinearLayout llLocation;
    @BindView(R.id.ll_screenSet)
    LinearLayout llScreenSet;
    @BindView(R.id.ll_codeShow)
    LinearLayout llCodeShow;
    @BindView(R.id.room_bt)
    LinearLayout roomBt;

    @BindView(R.id.rl_title)
    RelativeLayout rlTitle;
    @BindView(R.id.scrollView)
    ScrollView mScrollView;
    @BindView(R.id.fl_password_overlay)
    LinearLayout flPasswordOverlay;

    @BindView(R.id.reset_bt)
    LinearLayout mResetBt;

    @BindView(R.id.bt_pwd_sure)
    Button btPwdSure;

    @BindView(R.id.et_manager_pwd)
    EditText etManagerPwd;
    @BindView(R.id.set_power_switch)
    Switch set_power_switch;

    private Unbinder unbinder;
    private ListView mTypeLv;
    private PopupWindow typeSelectPopup;
    private List<String> testData;
    private ArrayAdapter<String> testDataAdapter;
    private int termType = 2;
    private String mLowTemp = "低温增焓";
    private String mPV = "光伏";
    private String mUpTemp = "升温除湿";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.manager_main, container, false);
        unbinder = ButterKnife.bind(this, view);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        mReturnView.setVisibility(View.GONE);
        init();
        return view;
    }

    private void init() {
        SaveControlInfo controlData = getControlData();
        int type = controlData.getOutTermType();
        if (type == 1) {
            mTitleSpinner.setText(mLowTemp);
        } else if (type == 2) {
            mTitleSpinner.setText(mPV);
        } else if (type == 3) {
            mTitleSpinner.setText(mUpTemp);
        }
        set_power_switch.setOnCheckedChangeListener(this);

    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            rlTitle.setVisibility(View.GONE);
            mScrollView.setVisibility(View.GONE);
            flPasswordOverlay.setVisibility(View.VISIBLE);
            etManagerPwd.setText("");
        }
    }


    @OnClick(R.id.bt_pwd_sure)
    public void onBtPwdSure(View view) {
        String password = etManagerPwd.getText().toString().trim();
        if (isAdded() && getView() != null) {
            if (!StringUtils.isNullOrEmpty(password) && password.equals(StringUtils.INIT_PASSWORD)) {
                rlTitle.setVisibility(View.VISIBLE);
                mScrollView.setVisibility(View.VISIBLE);
                flPasswordOverlay.setVisibility(View.GONE);
            } else {
                ToastUtil.showToast(getActivity(), "请输入正确的管理员密码！");
            }
        } else {
            rlTitle.setVisibility(View.VISIBLE);
            mScrollView.setVisibility(View.VISIBLE);
            flPasswordOverlay.setVisibility(View.GONE);
        }
    }


    @OnClick(R.id.li_back)
    public void onReturnClick(View view) {
        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    @OnClick(R.id.li_spinner)
    public void onSpinnerClick(View view) {
        initSelectPopup();
        if (typeSelectPopup != null && !typeSelectPopup.isShowing()) {
            typeSelectPopup.showAsDropDown(mLiSpinner, 0, 0);
        }
    }

    @OnClick({R.id.reset_bt})
    public void onResetClick(View view) {
        showResetDialog();
    }

    private AlertDialog mResetDialog;

    private void showResetDialog() {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.reset_system_dialog, null, false);
        mResetDialog = new AlertDialog.Builder(getActivity()).setView(view).create();
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

    @OnClick(R.id.li_funTest)
    public void onFunTestClick(View view) {
        if (InputLimitUtil.isFastDoubleClick() || getContext() == null) {
            return;
        }
        Intent intent = new Intent(getContext(), FanTestActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.li_funReset)
    public void onFunResetClick(View view) {
        if (InputLimitUtil.isFastDoubleClick() || getContext() == null) {
            return;
        }
        Intent intent = new Intent(getContext(), FanResetActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.li_valve)
    public void onValveClick(View view) {
        if (InputLimitUtil.isFastDoubleClick()) {
            return;
        }
        ValveSettingFragment fragment = new ValveSettingFragment();
        fragment.show(getParentFragmentManager(), "valve");
    }

    @OnClick(R.id.electric_tv)
    public void onElectricClick(View view) {
        if (InputLimitUtil.isFastDoubleClick()) {
            return;
        }
        SettingElectricShowFragment fragment = new SettingElectricShowFragment();
        fragment.show(getParentFragmentManager(), "electric");
    }

    @OnClick(R.id.ll_screenSet)
    public void onScreenSetClick(View view) {
        if (InputLimitUtil.isFastDoubleClick()) {
            return;
        }
        SettingScreenSetFragment fragment = new SettingScreenSetFragment();
        fragment.show(getParentFragmentManager(), "screenset");
    }

    @OnClick(R.id.ll_codeShow)
    public void onCodeShowClick(View view) {
        if (InputLimitUtil.isFastDoubleClick()) {
            return;
        }
        SettingCodeShowFragment fragment = new SettingCodeShowFragment();
        fragment.show(getParentFragmentManager(), "sodeshow");
    }

    @OnClick(R.id.room_bt)
    public void onRoomClick(View view) {
        if (InputLimitUtil.isFastDoubleClick()) {
            return;
        }
        RoomFragment fragment = new RoomFragment();
        fragment.show(getParentFragmentManager(), "roomshow");
    }

    @OnClick(R.id.ll_location)
    public void onLocationClick(View view) {
        if (InputLimitUtil.isFastDoubleClick()) {
            return;
        }
        SettingLocationFrament fragment = new SettingLocationFrament();
        fragment.show(getParentFragmentManager(), "location");
    }


    @OnClick(R.id.li_air_valve)
    public void onAirValveClick(View view) {
        if (InputLimitUtil.isFastDoubleClick() || getContext() == null) {
            return;
        }
        Intent intent = new Intent(getContext(), AirValveControlActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.li_fangdong)
    public void onFangDongClick(View view) {
        if (InputLimitUtil.isFastDoubleClick()) {
            return;
        }
        AntiFreezingFragment fragment = new AntiFreezingFragment();
        fragment.show(getParentFragmentManager(), "antiFreezing");
    }

    @OnClick(R.id.li_lowTemp)
    public void onLowTempClick(View view) {
        if (InputLimitUtil.isFastDoubleClick()) {
            return;
        }
        if (mTitleSpinner.getText().toString().equals(mLowTemp)) {
            LowTempFragment fragment = new LowTempFragment();
            fragment.show(getParentFragmentManager(), "lowTemp");
        } else if (mTitleSpinner.getText().toString().equals(mPV)) {
            PVFragment fragment = new PVFragment();
            fragment.show(getParentFragmentManager(), "PV");
        } else if (mTitleSpinner.getText().toString().equals(mUpTemp)) {
            UpTempFragment fragment = new UpTempFragment();
            fragment.show(getParentFragmentManager(), "upTemp");
        }
    }

    @OnClick(R.id.li_gateway)
    public void onGatewayClick(View view) {
        if (InputLimitUtil.isFastDoubleClick() || getContext() == null) {
            return;
        }
        Intent intent = new Intent(getContext(), MainGatewayActivity.class);
        intent.putExtra("main", "2");
        startActivity(intent);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTypeEvent(SetStatusEvent event) {
        Log.e("TAG", "onTypeEvent: "+new Gson().toJson(event));
        if (event != null) {
            if (event.getType() == 5) {
                if (event.getStatus() && getContext() != null) {
                    SaveControlInfo controlInfo = getControlData();
                    controlInfo.setOutTermType(termType);
                    String json = new Gson().toJson(controlInfo);
                    MySpUtil.setParam(getContext(), MySpUtil.MAIN_CONTROL_STATUS, json);
                }
            }else if (event.getType() == 1) {
                if (event.getStatus()) {
                    Log.e("TAG", "onTypeEvent: "+HyApplication.isIsReboot());
                    if (HyApplication.isIsReboot()) {
                        ToastUtil.showToast(getActivity(), "低功耗设置成功！");
                        try {
                            Runtime.getRuntime().exec("reboot -p");
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.e("TAG", "onTypeEvent: "+e.toString());
                        }
                    }
                } else {
                    ToastUtil.showToast(getActivity(), "低功耗设置失败！");
                }
            }
        }
    }

    private void initSelectPopup() {
        if (getContext() == null) return;
        mTypeLv = new ListView(getContext());
        TestData();
        testDataAdapter = new ArrayAdapter<String>(getContext(), R.layout.myspinner_dropdown, testData);
        mTypeLv.setAdapter(testDataAdapter);
        mTypeLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String value = testData.get(position);
                if (!value.equals(mTitleSpinner.getText())) {
                    mTitleSpinner.setText(value);
                    ControlCommand controlCommand = new ControlCommand(FunctionObject.SET_OUTDOOR_TYPE);
                    byte[] sendData = {(byte) 0x00, (byte) 0x02}; // 默认值

                    if (mTitleSpinner.getText().equals(mPV)) {
                        sendData = new byte[]{(byte) 0x00, (byte) 0x02};
                        termType = 2;
                    } else if (mTitleSpinner.getText().equals(mLowTemp)) {
                        sendData = new byte[]{(byte) 0x00, (byte) 0x01};
                        termType = 1;
                    } else if (mTitleSpinner.getText().equals(mUpTemp)) {
                        sendData = new byte[]{(byte) 0x00, (byte) 0x03};
                        termType = 3;
                    }

                    controlCommand.setData(sendData);
                    SpDataProcessor.getInstance().send(controlCommand);
                }
                typeSelectPopup.dismiss();
            }
        });
        int popupWidth = dpToPx(getContext(), 200);
        int popupHeight = dpToPx(getContext(), 200);
        typeSelectPopup = new PopupWindow(mTypeLv, popupWidth, popupHeight, true);

        Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.btn_bg_common1);
        typeSelectPopup.setBackgroundDrawable(drawable);
        typeSelectPopup.setFocusable(true);
        typeSelectPopup.setOutsideTouchable(true);
        typeSelectPopup.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
            }
        });
    }

    private void TestData() {
        testData = new ArrayList<>();
        testData.add(mPV);
        testData.add(mLowTemp);
        testData.add(mUpTemp);
    }

    /**
     * 获取保存的主控板数据 (使用 getContext() 替换 Activity)
     */
    private SaveControlInfo getControlData() {
        if (getContext() == null) return new SaveControlInfo();

        SaveControlInfo controlInfo;
        String json = MySpUtil.getParam(getContext(), MySpUtil.MAIN_CONTROL_STATUS, "").toString();
        if (StringUtils.isNullOrEmpty(json)) {
            controlInfo = new SaveControlInfo();
        } else {
            controlInfo = new Gson().fromJson(json, SaveControlInfo.class);
        }
        return controlInfo;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
        if (unbinder != null) {
            unbinder.unbind();
        }
    }

    private int dpToPx(Context context, int dp) {
        return Math.round(dp * context.getResources().getDisplayMetrics().density);
    }

    /**
     * 管理员密码框
     */
    private void showPasswordDialog(Activity activity) {
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.manager_pwd_dialog, null, false);
        AlertDialog mPwdDialog = new AlertDialog.Builder(activity).setView(view).create();

        // 强制用户输入密码
        mPwdDialog.setCancelable(false);
        mPwdDialog.setCanceledOnTouchOutside(false);

        Button sure = view.findViewById(R.id.bt_pwd_sure);
        EditText mEtManagerPwd = view.findViewById(R.id.et_manager_pwd);

        sure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        mPwdDialog.show();
        if (mPwdDialog.getWindow() != null) {
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
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        switch (compoundButton.getId()) {
            case R.id.set_power_switch:
                ControlCommand controlCommand = new ControlCommand(FunctionObject.SET_LOW_POWER);
                if (isChecked) {
                    byte[] sendData = {(byte) 0x01};
                    controlCommand.setData(sendData);
                } else {
                    byte[] sendData = {(byte) 0x00};
                    controlCommand.setData(sendData);
                }
                HyApplication.setIsReboot(true);
                SpDataProcessor.getInstance().send(controlCommand);
                break;
        }
    }
}
