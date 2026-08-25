package com.hy.greenbuilding.ui.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.Nullable;

import com.hy.greenbuilding.R;
import com.hy.greenbuilding.config.ErrorDefine;
import com.hy.greenbuilding.event.ReceiveMcuDataEvent;
import com.hy.greenbuilding.event.TempStatusUpdateEvent;
import com.hy.greenbuilding.event.UptempStatusChangeEvent;
import com.hy.greenbuilding.presenter.BasePresenter;
import com.hy.greenbuilding.protocol.FunctionObject;
import com.hy.greenbuilding.protocol.ResPonseInfo.UpTempStatusInfo;
import com.hy.greenbuilding.protocol.SpDataProcessor;
import com.hy.greenbuilding.protocol.command.ControlCommand;
import com.hy.greenbuilding.protocol.command.UpTempCommand;
import com.hy.greenbuilding.utils.ByteUtils;
import com.hy.greenbuilding.utils.Hex;
import com.hy.greenbuilding.utils.ToastUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class UpTempTestActivity extends BaseActivity {
    @BindView(R.id.li_back)
    ImageView mReturnView;

    @BindView(R.id.radio_group)
    RadioGroup mDefrostRg;
    @BindView(R.id.normal_rb)
    RadioButton mNormalRb;
    @BindView(R.id.other_rb)
    RadioButton mOtherRb;

    @BindView(R.id.et_frequency_model)
    EditText frequencyModel;
    @BindView(R.id.bt_frequency_model)
    Button frequencyModelBt;
    @BindView(R.id.et_drive_type)
    EditText driveType;
    @BindView(R.id.bt_drive_type)
    Button driveTypeBt;

    @BindView(R.id.manual_frequency_et)
    EditText manualFrequency;
    @BindView(R.id.manual_frequency_bt)
    Button manualFrequencyBt;

    @BindView(R.id.main_eev_model_et)
    EditText mainEEVModel;
    @BindView(R.id.main_eev_model_bt)
    Button mainEEVModelBt;

    @BindView(R.id.aux_eev_model_et)
    EditText auxEEVModel;
    @BindView(R.id.aux_eev_model_bt)
    Button auxEEVModelBt;

    @BindView(R.id.main_eev_open1_et)
    EditText mEEVOpen1;
    @BindView(R.id.main_eev_open1_bt)
    Button mEEVOpen1Bt;
    @BindView(R.id.aux_eev_open1_et)
    EditText auxEEVOpen;
    @BindView(R.id.aux_eev_open1_bt)
    Button auxEEVOpenBt;

    @BindView(R.id.fan_num_et)
    EditText fanNum;
    @BindView(R.id.fan_num_bt)
    Button fanNumBt;
    @BindView(R.id.aux_eev_min_et)
    EditText auxEEVMin;
    @BindView(R.id.aux_eev_min_bt)
    Button auxEEVMinBt;
    @BindView(R.id.speed_max_et)
    EditText speedMax;
    @BindView(R.id.speed_max_bt)
    Button speedMaxBt;
    @BindView(R.id.speed_min_et)
    EditText speedMin;
    @BindView(R.id.speed_min_bt)
    Button speedMinBt;
    @BindView(R.id.fan_action_et)
    EditText fanAction;
    @BindView(R.id.fan_action_bt)
    Button fanActionBt;
    @BindView(R.id.fan_speed_et)
    EditText fanSpeed;
    @BindView(R.id.fan_speed_bt)
    Button fanSpeedBt;

    @BindView(R.id.common_data_et)
    EditText commonDataEt;
    @BindView(R.id.common_data_bt)
    Button commonDataBt;

    @BindView(R.id.receive_data_et)
    EditText receiveDataEt;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initWindow();
        setContentView(R.layout.up_temp_test);
        controlBaseLayoutVisibility(false);
        ButterKnife.bind(this);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        mDefrostRg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                UpTempCommand command = new UpTempCommand(FunctionObject.UP_DEFROST_MODE);
                if (checkedId == R.id.normal_rb) {
                    command.setData(new byte[]{(byte) 0x00, (byte) 0x00});
                } else if (checkedId == R.id.other_rb) {
                    command.setData(new byte[]{(byte) 0x00, (byte) 0x01});
                }
                SpDataProcessor.getInstance().send(command);
                mHandler.removeCallbacks(mRunnable);
                mHandler.postDelayed(mRunnable, 10 * 1000);
            }
        });

        sendStatusCommand();

    }
    private void initWindow() {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

    }

    @OnClick({R.id.li_back})
    public void onReturnClick(View view) {
        finish();
    }

    @OnClick({R.id.bt_frequency_model})
    public void onModelClick(View view) {
        String text = frequencyModel.getText().toString();
        if(TextUtils.isEmpty(text)){
            return;
        }
        UpTempCommand command = new UpTempCommand(FunctionObject.UP_PRESS_TYPE);
        command.setData(ByteUtils.int16ToByteArray(Integer.parseInt(text)));
        SpDataProcessor.getInstance().send(command);
    }

    @OnClick({R.id.bt_drive_type})
    public void onTypeClick(View view) {
        String text = driveType.getText().toString();
        if(TextUtils.isEmpty(text)){
            return;
        }
        UpTempCommand command = new UpTempCommand(FunctionObject.UP_SET_TYPE);
        command.setData(ByteUtils.int16ToByteArray(Integer.parseInt(text)));
        SpDataProcessor.getInstance().send(command);
    }

    @OnClick({R.id.manual_frequency_bt})
    public void onManualClick(View view) {
        String text = manualFrequency.getText().toString();
        if(TextUtils.isEmpty(text)){
            return;
        }
        UpTempCommand command = new UpTempCommand(FunctionObject.UP_FREQUNCY);
        command.setData(ByteUtils.int16ToByteArray(Integer.parseInt(text)));
        SpDataProcessor.getInstance().send(command);
    }

    @OnClick({R.id.main_eev_model_bt})
    public void onMainEEVClick(View view) {
        String text = mainEEVModel.getText().toString();
        if(TextUtils.isEmpty(text)){
            return;
        }
        UpTempCommand command = new UpTempCommand(FunctionObject.UP_MAIN_EEV_MODE);
        command.setData(ByteUtils.int16ToByteArray(Integer.parseInt(text)));
        SpDataProcessor.getInstance().send(command);
    }

    @OnClick({R.id.aux_eev_model_bt})
    public void onAuxEEVClick(View view) {
        String text = auxEEVModel.getText().toString();
        if(TextUtils.isEmpty(text)){
            return;
        }
        UpTempCommand command = new UpTempCommand(FunctionObject.UP_AUX_EEV_MODE);
        command.setData(ByteUtils.int16ToByteArray(Integer.parseInt(text)));
        SpDataProcessor.getInstance().send(command);
    }

    @OnClick({R.id.main_eev_open1_bt})
    public void onMainOpenClick(View view) {
        String text = mEEVOpen1.getText().toString();
        if(TextUtils.isEmpty(text)){
            return;
        }
        UpTempCommand command = new UpTempCommand(FunctionObject.UP_MAIN_EEV_OPEN);
        command.setData(ByteUtils.int16ToByteArray(Integer.parseInt(text)));
        SpDataProcessor.getInstance().send(command);
    }

    @OnClick({R.id.aux_eev_open1_bt})
    public void onAuxOpenClick(View view) {
        String text = auxEEVOpen.getText().toString();
        if(TextUtils.isEmpty(text)){
            return;
        }
        UpTempCommand command = new UpTempCommand(FunctionObject.UP_AUX_EEV_OPEN);
        command.setData(ByteUtils.int16ToByteArray(Integer.parseInt(text)));
        SpDataProcessor.getInstance().send(command);
    }

    @OnClick({R.id.fan_num_bt})
    public void onNumClick(View view) {
        String text = fanNum.getText().toString();
        if(TextUtils.isEmpty(text)){
            return;
        }
        UpTempCommand command = new UpTempCommand(FunctionObject.UP_FAN_NUM);
        command.setData(ByteUtils.int16ToByteArray(Integer.parseInt(text)));
        SpDataProcessor.getInstance().send(command);
    }

    @OnClick({R.id.aux_eev_min_bt})
    public void onEEVMinClick(View view) {
        String text = auxEEVMin.getText().toString();
        if(TextUtils.isEmpty(text)){
            return;
        }
        UpTempCommand command = new UpTempCommand(FunctionObject.UP_AUX_EEV_OPEN_MIN);
        command.setData(ByteUtils.int16ToByteArray(Integer.parseInt(text)));
        SpDataProcessor.getInstance().send(command);
    }

    @OnClick({R.id.speed_max_bt})
    public void onMaxClick(View view) {
        String text = speedMax.getText().toString();
        if(TextUtils.isEmpty(text)){
            return;
        }
        UpTempCommand command = new UpTempCommand(FunctionObject.UP_FAN_SPEED_MAX);
        command.setData(ByteUtils.int16ToByteArray(Integer.parseInt(text)));
        SpDataProcessor.getInstance().send(command);
    }

    @OnClick({R.id.speed_min_bt})
    public void onMinClick(View view) {
        String text = speedMin.getText().toString();
        if(TextUtils.isEmpty(text)){
            return;
        }
        UpTempCommand command = new UpTempCommand(FunctionObject.UP_FAN_SPEED_MIN);
        command.setData(ByteUtils.int16ToByteArray(Integer.parseInt(text)));
        SpDataProcessor.getInstance().send(command);
    }

    @OnClick({R.id.fan_action_bt})
    public void onActionClick(View view) {
        String text = fanAction.getText().toString();
        if(TextUtils.isEmpty(text)){
            return;
        }
        UpTempCommand command = new UpTempCommand(FunctionObject.UP_SPEED_STATUS);
        command.setData(ByteUtils.int16ToByteArray(Integer.parseInt(text)));
        SpDataProcessor.getInstance().send(command);
    }

    @OnClick({R.id.fan_speed_bt})
    public void onSpeedClick(View view) {
        String text = fanSpeed.getText().toString();
        if(TextUtils.isEmpty(text)){
            return;
        }
        UpTempCommand command = new UpTempCommand(FunctionObject.UP_SET_SPEED);
        command.setData(ByteUtils.int16ToByteArray(Integer.parseInt(text)));
        SpDataProcessor.getInstance().send(command);
    }

    @OnClick({R.id.common_data_bt})
    public void onCommonDataClick(View view){
        String text = commonDataEt.getText().toString();
        if(TextUtils.isEmpty(text)){
            return;
        }
        UpTempCommand command = new UpTempCommand(FunctionObject.UP_SET_COMMON_DATA);
        try{
            command.setData(Hex.hexStringToBytes(text));
            SpDataProcessor.getInstance().send4(command);
        }catch (NumberFormatException e){
            ToastUtil.showToast(UpTempTestActivity.this,""+e);
        }

    }

    private void sendStatusCommand() {
        UpTempCommand pvCommand = new UpTempCommand(FunctionObject.UP_GET_OUT_STATUS);
        SpDataProcessor.getInstance().send(pvCommand);
        mHandler.removeCallbacks(mRunnable);
        mHandler.postDelayed(mRunnable, 10 * 1000);
    }
    Handler mHandler = new Handler();
    Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            sendStatusCommand();
        }
    };
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(UpTempStatusInfo info) {
        if (info != null) {

            HashMap<String, Object> upTempMap = info.getDataMap();
            if(!frequencyModel.isFocused())frequencyModel.setText(Objects.requireNonNull(upTempMap.get(ErrorDefine.COMP_TYPE)).toString());
            if(!driveType.isFocused())driveType.setText(Objects.requireNonNull(upTempMap.get(ErrorDefine.DRIVE_TYPE)).toString());
            if(!manualFrequency.isFocused())manualFrequency.setText(Objects.requireNonNull(upTempMap.get(ErrorDefine.MANUAL_FREQUENCY)).toString());
            if(!mainEEVModel.isFocused()) mainEEVModel.setText(Objects.requireNonNull(upTempMap.get(ErrorDefine.MAIN_EEV_MODEL)).toString());
            if(!auxEEVModel.isFocused()) auxEEVModel.setText(Objects.requireNonNull(upTempMap.get(ErrorDefine.AUX_EEV_MODEL)).toString());
            if(!mEEVOpen1.isFocused()) mEEVOpen1.setText(Objects.requireNonNull(upTempMap.get(ErrorDefine.MAIN_EEV_OPEN)).toString());
            if(!auxEEVOpen.isFocused()) auxEEVOpen.setText(Objects.requireNonNull(upTempMap.get(ErrorDefine.AUX_EEV_OPEN)).toString());
            if(!fanNum.isFocused()) fanNum.setText(Objects.requireNonNull(upTempMap.get(ErrorDefine.FAN_NUM)).toString());
            if(!auxEEVMin.isFocused())  auxEEVMin.setText(Objects.requireNonNull(upTempMap.get(ErrorDefine.AUX_EEV_OPEN_MIN)).toString());
            if(!speedMax.isFocused()) speedMax.setText(Objects.requireNonNull(upTempMap.get(ErrorDefine.FAN_SPEED_MAX)).toString());
            if(!speedMin.isFocused()) speedMin.setText(Objects.requireNonNull(upTempMap.get(ErrorDefine.FAN_SPEED_MIN)).toString());
            if(!fanAction.isFocused()) fanAction.setText(Objects.requireNonNull(upTempMap.get(ErrorDefine.MANUAL_FAN_ENABLE)).toString());
            if(!fanSpeed.isFocused())fanSpeed.setText(Objects.requireNonNull(upTempMap.get(ErrorDefine.FAN_SPEED)).toString());

            if (Objects.requireNonNull(upTempMap.get(ErrorDefine.DEFROST_MODEL)).toString().equals("1")) {
                mOtherRb.setChecked(true);
            } else {
                mNormalRb.setChecked(true);
            }

        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdateEvent(UptempStatusChangeEvent info) {
        if (info != null) {
           if(info.isSuccess()){
               ToastUtil.showToast(UpTempTestActivity.this,getString(R.string.set_success));
           }else{
               ToastUtil.showToast(UpTempTestActivity.this,getString(R.string.set_fail));
           }
        }
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMcuReceiveEvent(ReceiveMcuDataEvent info) {
        if (info != null) {
            receiveDataEt.setText(info.getHexString());
        }
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }


}
