package com.hy.greenbuilding.ui.activity;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.hy.greenbuilding.R;
import com.hy.greenbuilding.config.SaveFanReset;
import com.hy.greenbuilding.event.FanResetEvent;
import com.hy.greenbuilding.model.FanDataInfo;
import com.hy.greenbuilding.protocol.FunctionObject;
import com.hy.greenbuilding.protocol.ResPonseInfo.DCFanStatusInfo;
import com.hy.greenbuilding.protocol.ResPonseInfo.FanStatusInfo;
import com.hy.greenbuilding.protocol.SpDataProcessor;
import com.hy.greenbuilding.protocol.command.DCFanCommand;
import com.hy.greenbuilding.protocol.command.FanCommand;
import com.hy.greenbuilding.utils.AppManagerUtil;
import com.hy.greenbuilding.utils.ByteUtils;
import com.hy.greenbuilding.utils.Hex;
import com.hy.greenbuilding.utils.MySpUtil;
import com.hy.greenbuilding.utils.StringUtils;
import com.hy.greenbuilding.utils.ToastUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FanResetActivity extends BaseActivity {


    @BindView(R.id.li_back)
    ImageView mReturnView;
    @BindView(R.id.bt_search_address)
    Button searchAddressBt;

    @BindView(R.id.et_value_wind1)
    EditText mValueWind1;
    @BindView(R.id.et_address_wind1)
    EditText mAddressWind1;
    @BindView(R.id.response_value_wind1)
    TextView respValueWind1;
    @BindView(R.id.response_address_wind1)
    TextView respAddressWind1;
    @BindView(R.id.bt_open_wind1)
    Button openWind1;
    @BindView(R.id.bt_close_wind1)
    Button closeWind1;
    @BindView(R.id.bt_set_wind1)
    Button setWind1;

    @BindView(R.id.et_value_wind2)
    EditText mValueWind2;
    @BindView(R.id.et_address_wind2)
    EditText mAddressWind2;
    @BindView(R.id.response_value_wind2)
    TextView respValueWind2;
    @BindView(R.id.response_address_wind2)
    TextView respAddressWind2;
    @BindView(R.id.bt_open_wind2)
    Button openWind2;
    @BindView(R.id.bt_close_wind2)
    Button closeWind2;
    @BindView(R.id.bt_set_wind2)
    Button setWind2;

    @BindView(R.id.et_value_wind3)
    EditText mValueWind3;
    @BindView(R.id.et_address_wind3)
    EditText mAddressWind3;
    @BindView(R.id.response_value_wind3)
    TextView respValueWind3;
    @BindView(R.id.response_address_wind3)
    TextView respAddressWind3;
    @BindView(R.id.bt_open_wind3)
    Button openWind3;
    @BindView(R.id.bt_close_wind3)
    Button closeWind3;
    @BindView(R.id.bt_set_wind3)
    Button setWind3;

    @BindView(R.id.et_value_wind4)
    EditText mValueWind4;
    @BindView(R.id.et_address_wind4)
    EditText mAddressWind4;
    @BindView(R.id.response_value_wind4)
    TextView respValueWind4;
    @BindView(R.id.response_address_wind4)
    TextView respAddressWind4;
    @BindView(R.id.bt_open_wind4)
    Button openWind4;
    @BindView(R.id.bt_close_wind4)
    Button closeWind4;
    @BindView(R.id.bt_set_wind4)
    Button setWind4;

    @BindView(R.id.et_dcFan)
    EditText dcFanEdit;
    @BindView(R.id.dcSpeed_response)
    TextView dcFanResponse;
    @BindView(R.id.bt_setDCFan)
    Button mBtDCFan;

    private String fanAddress;
    private byte fanType;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initWindow();
        setContentView(R.layout.fan_reset_main);
        ButterKnife.bind(this);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        init();

        AppManagerUtil.getAppManager().addActivity(this);
        controlBaseLayoutVisibility(false);

        //DC转速
        String speed = MySpUtil.getParam(this, MySpUtil.DC_FAN_DATA, "").toString();
        dcFanEdit.setText(speed);

        //获取DC风机状态
        DCFanCommand dcFanCommand = new DCFanCommand(FunctionObject.GET_DC_FAN_STATUS);
        SpDataProcessor.getInstance().send(dcFanCommand);
    }

    private void initWindow() {
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
    }


    private void init() {
        FanCommand fanCommand = new FanCommand(FunctionObject.GET_FAN_STATUS);
        SpDataProcessor.getInstance().send(fanCommand);
        mHandler.removeCallbacks(mRunnable);
        mHandler.postDelayed(mRunnable, 5000);
        SaveFanReset saveFanReset = getFanReset();
        if (saveFanReset != null) {
            mValueWind1.setText(saveFanReset.getValueWind1());
            mAddressWind1.setText(saveFanReset.getAddressWind1());
            mValueWind2.setText(saveFanReset.getValueWind2());
            mAddressWind2.setText(saveFanReset.getAddressWind2());
            mValueWind3.setText(saveFanReset.getValueWind3());
            mAddressWind3.setText(saveFanReset.getAddressWind3());
            mValueWind4.setText(saveFanReset.getValueWind4());
            mAddressWind4.setText(saveFanReset.getAddressWind4());
        }
    }

    @OnClick({R.id.li_back})
    public void onReturnClick(View view) {
        finish();
    }


    @OnClick({R.id.bt_search_address})
    public void onSearchClick(View view) {
        FanCommand fanCommand = new FanCommand(FunctionObject.SEARCH_FAN_ADDRESS);
        byte[] bytes = new byte[]{(byte) 0x01};
        fanCommand.setData(bytes);
        SpDataProcessor.getInstance().send(fanCommand);
    }

    @OnClick({R.id.bt_setDCFan})
    public void onDcFanClick(View view) {
        String fanSpeed = dcFanEdit.getText().toString();
        if (!StringUtils.isNullOrEmpty(fanSpeed)) {
            if (Integer.parseInt(fanSpeed) > 100) {
                ToastUtil.showToast(this, getString(R.string.set_format_error));
                return;
            }
            DCFanCommand dcFanCommand = new DCFanCommand(FunctionObject.SET_DC_FAN_SPEED);
            byte[] bytes = ByteUtils.int16ToByteArray(Integer.parseInt(fanSpeed));
            dcFanCommand.setData(bytes);
            SpDataProcessor.getInstance().send(dcFanCommand);
        }
    }

    @OnClick({R.id.bt_open_wind1})
    public void openWind1Click(View view) {
        String input = mValueWind1.getText().toString();
        testFanOpen((byte) 0x00, input);
    }

    @OnClick({R.id.bt_close_wind1})
    public void closeWind1Click(View view) {
        testFanClose((byte) 0x00);
    }

    @OnClick({R.id.bt_set_wind1})
    public void setWind1Click(View view) {
        String value = mValueWind1.getText().toString();
        String address = mAddressWind1.getText().toString();
        setFanValue((byte) 0x00, value, address);
    }

    @OnClick({R.id.bt_open_wind2})
    public void openWind2Click(View view) {
        String input = mValueWind2.getText().toString();
        testFanOpen((byte) 0x01, input);
    }

    @OnClick({R.id.bt_close_wind2})
    public void closeWind2Click(View view) {
        testFanClose((byte) 0x01);
    }

    @OnClick({R.id.bt_set_wind2})
    public void setWind2Click(View view) {
        String value = mValueWind2.getText().toString();
        String address = mAddressWind2.getText().toString();
        setFanValue((byte) 0x01, value, address);

    }

    @OnClick({R.id.bt_open_wind3})
    public void openWind3Click(View view) {
        String input = mValueWind3.getText().toString();
        testFanOpen((byte) 0x02, input);
    }

    @OnClick({R.id.bt_close_wind3})
    public void closeWind3Click(View view) {
        testFanClose((byte) 0x02);
    }

    @OnClick({R.id.bt_set_wind3})
    public void setWind3Click(View view) {
        String value = mValueWind3.getText().toString();
        String address = mAddressWind3.getText().toString();
        setFanValue((byte) 0x02, value, address);
    }

    @OnClick({R.id.bt_open_wind4})
    public void openWind4Click(View view) {
        String input = mValueWind4.getText().toString();
        testFanOpen((byte) 0x03, input);
    }

    @OnClick({R.id.bt_close_wind4})
    public void closeWind4Click(View view) {
        testFanClose((byte) 0x03);
    }

    @OnClick({R.id.bt_set_wind4})
    public void setWind4Click(View view) {
        String value = mValueWind4.getText().toString();
        String address = mAddressWind4.getText().toString();
        setFanValue((byte) 0x03, value, address);
    }


    /**
     * 测试风机单开
     *
     * @param type
     * @param input
     */
    private void testFanOpen(byte type, String input) {
        FanCommand fanCommand = new FanCommand(FunctionObject.TEST_FAN_VALUE);
        if (StringUtils.isNullOrEmpty(input)) {
            ToastUtil.showToast(this, "未设置风量");
        } else {
            byte[] valueByte = ByteUtils.int16ToByteArray(Integer.parseInt(input));
            byte[] bytes = ByteUtils.splicingBytes(new byte[]{type}, valueByte);
            fanCommand.setData(bytes);
            SpDataProcessor.getInstance().send(fanCommand);

            //保存
            SaveFanReset saveFanReset = getFanReset();
            if (type == 0) {
                saveFanReset.setValueWind1(input);
            } else if (type == 1) {
                saveFanReset.setValueWind2(input);
            } else if (type == 2) {
                saveFanReset.setValueWind3(input);
            } else if (type == 3) {
                saveFanReset.setValueWind4(input);
            }
            MySpUtil.setParam(this, MySpUtil.FAN_RESET, new Gson().toJson(saveFanReset));
            ToastUtil.showToast(this, getString(R.string.set_success));
        }
    }

    /**
     * 测试风机单关
     *
     * @param type
     */
    private void testFanClose(byte type) {
        FanCommand fanCommand = new FanCommand(FunctionObject.TEST_FAN_VALUE);
        byte[] bytes = {type, (byte) 0x00, (byte) 0x00};
        fanCommand.setData(bytes);
        SpDataProcessor.getInstance().send(fanCommand);
    }

    /**
     * 设置
     *
     * @param type
     * @param value
     * @param address
     */
    private void setFanValue(byte type, String value, String address) {
        FanCommand fanCommand = new FanCommand(FunctionObject.TEST_FAN_VALUE);
        if (StringUtils.isNullOrEmpty(value)) {
            ToastUtil.showToast(this, "请先设置风量");
            return;
        }
        if (StringUtils.isNullOrEmpty(address)) {
            ToastUtil.showToast(this, "请先设置地址");
            return;
        }
        if (Integer.parseInt(address) > 247) {
            ToastUtil.showToast(this, "地址输入错误！");
            return;
        }
        fanAddress = address;
        fanType = type;
        byte[] valueByte = ByteUtils.int16ToByteArray(Integer.parseInt(value));
        byte[] bytes = ByteUtils.splicingBytes(new byte[]{type}, valueByte);
        fanCommand.setData(bytes);
        SpDataProcessor.getInstance().send(fanCommand);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                FanCommand fanCommand1 = new FanCommand(FunctionObject.SET_FAN_ADDRESS);
                byte[] bytes1 = {fanType, (byte) Integer.parseInt(fanAddress)};
                fanCommand1.setData(bytes1);
                SpDataProcessor.getInstance().send(fanCommand1);

            }
        }, 1000);

        //保存
        SaveFanReset saveFanReset = getFanReset();
        if (type == 0) {
            saveFanReset.setValueWind1(value);
            saveFanReset.setAddressWind1(address);
        } else if (type == 1) {
            saveFanReset.setValueWind2(value);
            saveFanReset.setAddressWind2(address);
        } else if (type == 2) {
            saveFanReset.setValueWind3(value);
            saveFanReset.setAddressWind3(address);
        } else if (type == 3) {
            saveFanReset.setValueWind4(value);
            saveFanReset.setAddressWind4(address);
        }
        MySpUtil.setParam(this, MySpUtil.FAN_RESET, new Gson().toJson(saveFanReset));
    }

    Handler mHandler = new Handler();
    Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            FanCommand fanCommand = new FanCommand(FunctionObject.GET_FAN_STATUS);
            SpDataProcessor.getInstance().send(fanCommand);
            mHandler.removeCallbacks(mRunnable);
            mHandler.postDelayed(mRunnable, 5000);
        }
    };

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onResetEvent(FanResetEvent info) {
        if (info != null && info.getBytes() != null) {
            if (Hex.bytesToHexString(info.getBytes()).contains("00")) {
                ToastUtil.showToast(this, getString(R.string.set_success));
            } else {
                ToastUtil.showToast(this, getString(R.string.set_fail));
            }
        }

    }

    private List<FanDataInfo> fanList = new ArrayList<>();

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(FanStatusInfo info) {
        if (info != null) {
            fanList.clear();
            fanList.addAll(info.getFanData());
            if (fanList != null && fanList.size() == 4) {
                respValueWind1.setText(fanList.get(0).getWindValue());
                respAddressWind1.setText(fanList.get(0).getFanAddress());
                respValueWind2.setText(fanList.get(1).getWindValue());
                respAddressWind2.setText(fanList.get(1).getFanAddress());
                respValueWind3.setText(fanList.get(2).getWindValue());
                respAddressWind3.setText(fanList.get(2).getFanAddress());
                respValueWind4.setText(fanList.get(3).getWindValue());
                respAddressWind4.setText(fanList.get(3).getFanAddress());
            }
        }
    }

    /**
     * 获取保存的配置数据
     *
     * @return
     */
    private SaveFanReset getFanReset() {
        String json = MySpUtil.getParam(this, MySpUtil.FAN_RESET, "").toString();
        SaveFanReset fanReset;
        if (StringUtils.isNullOrEmpty(json)) {
            fanReset = new SaveFanReset();
        } else {
            fanReset = new Gson().fromJson(json, SaveFanReset.class);
        }
        return fanReset;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDCFanEvent(DCFanStatusInfo info) {
        if (info != null) {
            updateDCFanStatus(info);
        }
    }

    public void updateDCFanStatus(DCFanStatusInfo info) {
        switch (info.getType()) {
            case FunctionObject.GET_DC_FAN_STATUS:
                int dcSpeed = info.getDCSpeed();
                dcFanResponse.setText(dcSpeed + "");
                break;
            case FunctionObject.SET_DC_FAN_SPEED:
                if (info.getSuccess()) {
                    MySpUtil.setParam(this, MySpUtil.DC_FAN_DATA, dcFanEdit.getText().toString());
                    ToastUtil.showToast(this, getString(R.string.set_success));
                } else {
                    ToastUtil.showToast(this, getString(R.string.set_fail));
                }
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mHandler != null) {
            mHandler.removeCallbacks(mRunnable);
            mHandler = null;
        }
        EventBus.getDefault().unregister(this);
    }
}
