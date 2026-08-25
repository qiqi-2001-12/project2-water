package com.hy.greenbuilding.ui.activity;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.gson.Gson;
import com.hy.greenbuilding.R;
import com.hy.greenbuilding.event.FanErrorEvent;
import com.hy.greenbuilding.protocol.FunctionObject;
import com.hy.greenbuilding.protocol.ResPonseInfo.CO2StatusInfo;
import com.hy.greenbuilding.protocol.ResPonseInfo.FanStatusInfo;
import com.hy.greenbuilding.protocol.SpDataProcessor;
import com.hy.greenbuilding.protocol.command.EnvironmentCommand;
import com.hy.greenbuilding.protocol.command.FanCommand;
import com.hy.greenbuilding.ui.fragment.FanTestDataFragment;
import com.hy.greenbuilding.utils.AppManagerUtil;
import com.hy.greenbuilding.utils.Hex;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FanTestActivity extends BaseActivity {
    @BindView(R.id.li_back)
    ImageView mReturnView;

    @BindView(R.id.type_rg)
    RadioGroup typeRg;
    @BindView(R.id.type1_rb)
    RadioButton mType1Rb;
    @BindView(R.id.type2_rb)
    RadioButton mType2Rb;

    @BindView(R.id.model_rg)
    RadioGroup modelRg;
    @BindView(R.id.model1_rb)
    RadioButton mModel1Rb;
    @BindView(R.id.model2_rb)
    RadioButton mModel2Rb;
    @BindView(R.id.model3_rb)
    RadioButton mModel3Rb;
    @BindView(R.id.model4_rb)
    RadioButton mModel4Rb;
    @BindView(R.id.model5_rb)
    RadioButton mModel5Rb;


    private FanTestDataFragment dataFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        setContentView(R.layout.fantest_page);
        ButterKnife.bind(this);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        dataFragment = new FanTestDataFragment();
        init();
        controlBaseLayoutVisibility(false);

        AppManagerUtil.getAppManager().addActivity(this);

        //获取风机状态
        sendFanCommand();

    }


    private void init() {
        startFragment();
        typeRg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (!typeRg.findViewById(checkedId).isPressed()){
                    return;
                }
                sendTypeModelCommand();
            }
        });
        modelRg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (!modelRg.findViewById(checkedId).isPressed()){
                    return;
                }
                sendTypeModelCommand();
            }
        });
    }

    private void startFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.fragment_content1, dataFragment);
        fragmentTransaction.commit();
    }


    @OnClick({R.id.li_back})
    public void onReturnClick(View view) {
        finish();
    }

    Handler mHandler = new Handler();
    Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            sendFanCommand();
        }
    };

    private void sendFanCommand() {
        FanCommand fanCommand = new FanCommand(FunctionObject.GET_FAN_STATUS);
        SpDataProcessor.getInstance().send(fanCommand);

//        mHandler.removeCallbacks(mRunnable);
//        mHandler.postDelayed(mRunnable, 10 * 1000);
    }

    private void sendTypeModelCommand() {
        FanCommand fanCommand = new FanCommand(FunctionObject.SEARCH_FAN_TYPE_MODEL);
        int installType = -1;
        int installModel = -1;
        if (mType1Rb.isChecked()) {
            installType = 1;
        } else if (mType2Rb.isChecked()) {
            installType = 2;
        }
        if (mModel1Rb.isChecked()) {
            installModel = 1;
        } else if (mModel2Rb.isChecked()) {
            installModel = 2;
        } else if (mModel3Rb.isChecked()) {
            installModel = 3;
        }else if (mModel4Rb.isChecked()) {
            installModel = 4;
        }else if (mModel5Rb.isChecked()) {
            installModel = 5;
        }
        if (installType == -1 || installModel == -1) {
            return;
        }
        fanCommand.setData(new byte[]{(byte) installType, (byte) installModel});
        SpDataProcessor.getInstance().send(fanCommand);
    }

    @Override
    public void onDestroy() {
        if (mHandler != null) {
            mHandler.removeCallbacks(mRunnable);
            mHandler = null;
        }
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(FanStatusInfo info) {
        EnvironmentCommand command = new EnvironmentCommand(FunctionObject.GET_PM_CO2);
        SpDataProcessor.getInstance().send(command);
        if (info != null) {
            dataFragment.setFanData(info);
            int type = info.getFanInstallType();
            int model = info.getFanInstallModel();
            if (type == 1) {
                mType1Rb.setChecked(true);
            } else if (type == 2) {
                mType2Rb.setChecked(true);
            }
            Log.e("TAG", "sendTypeModelCommand:model =   "+ model);

            if (model == 1) {
                mModel1Rb.setChecked(true);
            } else if (model == 2) {
                mModel2Rb.setChecked(true);
            } else if (model == 3) {
                mModel3Rb.setChecked(true);
            } else if (model == 4) {
                mModel4Rb.setChecked(true);
            } else if (model == 5) {
                mModel5Rb.setChecked(true);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCO2Event(CO2StatusInfo info) {
        if (info != null) {
            dataFragment.setCo2Data(info);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void update(String s) {
        if (s != null && s.equals("fan_update")) {
            dataFragment.initData();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFanErrorEvent(FanErrorEvent info) {
        Log.e("TAG", "sendTypeModelCommand = onFanErrorEvent: "+new Gson().toJson(info));
        if (info != null) {
            dataFragment.setFanErrorData(info);
        }
    }

}
