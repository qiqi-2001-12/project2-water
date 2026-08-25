package com.hy.greenbuilding.ui.activity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.gson.Gson;
import com.hwellyi.smarthome.MainGatewayActivity;
import com.hy.greenbuilding.R;
import com.hy.greenbuilding.config.SaveControlInfo;
import com.hy.greenbuilding.event.SetStatusEvent;
import com.hy.greenbuilding.presenter.BasePresenter;
import com.hy.greenbuilding.protocol.FunctionObject;
import com.hy.greenbuilding.protocol.SpDataProcessor;
import com.hy.greenbuilding.protocol.command.ControlCommand;
import com.hy.greenbuilding.ui.fragment.AntiFreezingFragment;
import com.hy.greenbuilding.ui.fragment.LowTempFragment;
import com.hy.greenbuilding.ui.fragment.PVFragment;
import com.hy.greenbuilding.ui.fragment.UpTempFragment;
import com.hy.greenbuilding.ui.fragment.ValveSettingFragment;
import com.hy.greenbuilding.utils.AppManagerUtil;
import com.hy.greenbuilding.utils.InputLimitUtil;
import com.hy.greenbuilding.utils.MySpUtil;
import com.hy.greenbuilding.utils.StringUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 管理员界面
 */
public class ManagerActivity extends BaseActivity {
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
    @BindView(R.id.li_air_valve)
    LinearLayout mAirValveView;

    private ListView mTypeLv;
    private PopupWindow typeSelectPopup;
    private List<String> testData;
    private ArrayAdapter<String> testDataAdapter;
    private int termType = 2;
    private String mLowTemp = "低温增焓";
    private String mPV = "光伏";
    private String mUpTemp = "升温除湿";
    private String mNoPowerControl = "无动力分控系统";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manager_main);
        ButterKnife.bind(this);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        init();
        AppManagerUtil.getAppManager().addActivity(this);
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
    }

    @OnClick({R.id.li_back})
    public void onReturnClick(View view) {
        finish();
    }

    @OnClick({R.id.li_spinner})
    public void onSpinnerClick(View view) {
        initSelectPopup();
        if (typeSelectPopup != null && !typeSelectPopup.isShowing()) {
            typeSelectPopup.showAsDropDown(mLiSpinner, 0, 0);
        }
    }

    @OnClick({R.id.li_funTest})
    public void onFunTestClick(View view) {
        if (InputLimitUtil.isFastDoubleClick()) {
            return;
        }
        Intent intent = new Intent(this, FanTestActivity.class);
        startActivity(intent);
    }

    @OnClick({R.id.li_funReset})
    public void onFunResetClick(View view) {
        if (InputLimitUtil.isFastDoubleClick()) {
            return;
        }
        Intent intent = new Intent(this, FanResetActivity.class);
        startActivity(intent);
    }

    @OnClick({R.id.li_valve})
    public void onValveClick(View view) {
        if (InputLimitUtil.isFastDoubleClick()) {
            return;
        }
        ValveSettingFragment fragment = new ValveSettingFragment();
        fragment.show(getSupportFragmentManager(), "valve");
    }

    @OnClick({R.id.li_air_valve})
    public void onAirValveClick(View view) {
        if (InputLimitUtil.isFastDoubleClick()) {
            return;
        }
        Intent intent = new Intent(this, AirValveControlActivity.class);
        startActivity(intent);
    }

    @OnClick({R.id.li_fangdong})
    public void onFangDongClick(View view) {
        if (InputLimitUtil.isFastDoubleClick()) {
            return;
        }
        AntiFreezingFragment fragment = new AntiFreezingFragment();
        fragment.show(getSupportFragmentManager(), "antiFreezing");
    }

    @OnClick({R.id.li_lowTemp})
    public void onLowTempClick(View view) {
        if (InputLimitUtil.isFastDoubleClick()) {
            return;
        }
        if (mTitleSpinner.getText().toString().equals(mLowTemp)) {
            LowTempFragment fragment = new LowTempFragment();
            fragment.show(getSupportFragmentManager(), "lowTemp");
        } else if (mTitleSpinner.getText().toString().equals(mPV)) {
            PVFragment fragment = new PVFragment();
            fragment.show(getSupportFragmentManager(), "PV");
        } else if (isUpTempMode(mTitleSpinner.getText().toString())) {
            UpTempFragment fragment = new UpTempFragment();
            fragment.show(getSupportFragmentManager(), "upTemp");
        }
    }

    @OnClick({R.id.li_gateway})
    public void onGatewayClick(View view) {
        if (InputLimitUtil.isFastDoubleClick()) {
            return;
        }
        Intent intent = new Intent(ManagerActivity.this, MainGatewayActivity.class);
        intent.putExtra("main", "2");
        startActivity(intent);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTypeEvent(SetStatusEvent event) {
        if (event != null) {
            if (event.getType() == 5) {
                if (event.getStatus()) {
                    SaveControlInfo controlInfo = getControlData();
                    controlInfo.setOutTermType(termType);
                    String json = new Gson().toJson(controlInfo);
                    MySpUtil.setParam(ManagerActivity.this, MySpUtil.MAIN_CONTROL_STATUS, json);
                }
            }
        }
    }

    private void initSelectPopup() {
        mTypeLv = new ListView(this);
        TestData();
        testDataAdapter = new ArrayAdapter<String>(this, R.layout.myspinner_dropdown, testData);
        mTypeLv.setAdapter(testDataAdapter);
        mTypeLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String value = testData.get(position);
                if (!value.equals(mTitleSpinner.getText())) {
                    mTitleSpinner.setText(value);
                    if (mTitleSpinner.getText().equals(mPV)) {
                        ControlCommand controlCommand = new ControlCommand(FunctionObject.SET_OUTDOOR_TYPE);
                        byte[] sendData = {(byte) 0x00, (byte) 0x02};
                        controlCommand.setData(sendData);
                        termType = 2;
                        SpDataProcessor.getInstance().send(controlCommand);
                    } else if (mTitleSpinner.getText().equals(mLowTemp)) {
                        ControlCommand controlCommand = new ControlCommand(FunctionObject.SET_OUTDOOR_TYPE);
                        byte[] sendData = {(byte) 0x00, (byte) 0x01};
                        controlCommand.setData(sendData);
                        termType = 1;
                        SpDataProcessor.getInstance().send(controlCommand);
                    } else if (isUpTempMode(mTitleSpinner.getText().toString())) {
                        ControlCommand controlCommand = new ControlCommand(FunctionObject.SET_OUTDOOR_TYPE);
                        byte[] sendData = {(byte) 0x00, (byte) 0x03};
                        controlCommand.setData(sendData);
                        termType = 3;
                        SpDataProcessor.getInstance().send(controlCommand);
                    }

                }
                typeSelectPopup.dismiss();
            }
        });
        typeSelectPopup = new PopupWindow(mTypeLv, 200, 260, true);
        Drawable drawable = ContextCompat.getDrawable(this, R.drawable.btn_bg_common1);
        typeSelectPopup.setBackgroundDrawable(drawable);
        typeSelectPopup.setFocusable(true);
        typeSelectPopup.setOutsideTouchable(true);
        typeSelectPopup.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                typeSelectPopup.dismiss();
            }
        });
    }

    private void TestData() {
        testData = new ArrayList<>();
        testData.add(mPV);
        testData.add(mLowTemp);
        testData.add(mUpTemp);
        testData.add(mNoPowerControl);
    }

    private boolean isUpTempMode(String value) {
        return mUpTemp.equals(value) || mNoPowerControl.equals(value);
    }

    /**
     * 获取保存的主控板数据
     *
     * @return
     */
    private SaveControlInfo getControlData() {
        SaveControlInfo controlInfo;
        String json = MySpUtil.getParam(ManagerActivity.this, MySpUtil.MAIN_CONTROL_STATUS, "").toString();
        if (StringUtils.isNullOrEmpty(json)) {
            controlInfo = new SaveControlInfo();
        } else {
            controlInfo = new Gson().fromJson(json, SaveControlInfo.class);
        }
        return controlInfo;
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }
}
