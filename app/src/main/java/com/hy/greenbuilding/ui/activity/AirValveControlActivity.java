package com.hy.greenbuilding.ui.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hy.greenbuilding.R;
import com.hy.greenbuilding.adapter.AirValveSettingAdapter1;
import com.hy.greenbuilding.event.FanErrorEvent;
import com.hy.greenbuilding.model.AirValveItemInfo;
import com.hy.greenbuilding.model.FanDataInfo;
import com.hy.greenbuilding.protocol.FunctionObject;
import com.hy.greenbuilding.protocol.ResPonseInfo.AirValveStatusInfo;
import com.hy.greenbuilding.protocol.ResPonseInfo.FanStatusInfo;
import com.hy.greenbuilding.protocol.SpDataProcessor;
import com.hy.greenbuilding.protocol.command.AirValveCommand;
import com.hy.greenbuilding.protocol.command.FanCommand;
import com.hy.greenbuilding.ui.widget.KeyboardEditText;
import com.hy.greenbuilding.utils.AppManagerUtil;
import com.hy.greenbuilding.utils.ByteUtils;
import com.hy.greenbuilding.utils.Hex;
import com.hy.greenbuilding.utils.ToastUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AirValveControlActivity extends BaseActivity {

    @BindView(R.id.li_back)
    ImageView mReturnView;

    @BindView(R.id.new_wind_cb)
    CheckBox newWindCb;
    @BindView(R.id.new_wind_value_tv)
    TextView newWindSetValueTv;
    @BindView(R.id.new_wind_value_et)
    KeyboardEditText newWindSetValueEt;
    @BindView(R.id.new_wind_value_bt)
    Button newWindSetValueBt;

    @BindView(R.id.exhaust_cb)
    CheckBox exhaustCb;
    @BindView(R.id.exhaust_value_tv)
    TextView exhaustSetValueTv;
    @BindView(R.id.exhaust_value_et)
    KeyboardEditText exhaustSetValueEt;
    @BindView(R.id.exhaust_value_bt)
    Button exhaustSetValueBt;

    @BindView(R.id.auto_bt)
    Button autoBt;
    @BindView(R.id.manual_bt)
    Button manualBt;

    @BindView(R.id.circle_cb)
    CheckBox circleCb;
    @BindView(R.id.circle_value_tv)
    TextView circleSetValueTv;
    @BindView(R.id.circle_value_et)
    KeyboardEditText circleSetValueEt;
    @BindView(R.id.circle_value_bt)
    Button circleSetValueBt;


    @BindView(R.id.recycler_view)
    RecyclerView listView;

    private List<AirValveItemInfo> airValveItemInfoList = new ArrayList<>();
    private AirValveSettingAdapter1 adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initWindow();
        setContentView(R.layout.air_valve_setting_activity);
        ButterKnife.bind(this);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        AppManagerUtil.getAppManager().addActivity(this);
        initList();
        controlBaseLayoutVisibility(false);

        adapter = new AirValveSettingAdapter1(this, airValveItemInfoList);
        listView.setLayoutManager(new GridLayoutManager(this, 3));
        listView.setAdapter(adapter);

        adapter.setOnItemMaxClickListener(new AirValveSettingAdapter1.OnItemMaxClickListener() {
            @Override
            public void onItemMaxClick(View view, int position, String maxValue) {
                if (TextUtils.isEmpty(maxValue)) {
                    return;
                }
                AirValveItemInfo airValveItemInfo = airValveItemInfoList.get(position);
                AirValveCommand airValveCommand = new AirValveCommand(FunctionObject.SET_AIR_VALVE_OPEN_MAX);
                ByteBuffer byteBuffer = ByteBuffer.allocate(3);
                byteBuffer.put((byte) airValveItemInfo.getAddress());
                byteBuffer.put(ByteUtils.int16ToByteArray(Integer.parseInt(maxValue)));
                airValveCommand.setData(byteBuffer.array());
                SpDataProcessor.getInstance().send(airValveCommand);
            }
        });

        adapter.setOnItemClickListener(new AirValveSettingAdapter1.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, String openValue, String maxValue) {
                if (TextUtils.isEmpty(openValue)) {
                    return;
                }
                if (TextUtils.isEmpty(maxValue)) {
                    ToastUtil.showToast(AirValveControlActivity.this, "请先设置最大进步数");
                    return;
                }
                if (Integer.parseInt(openValue) > Integer.parseInt(maxValue)) {
                    ToastUtil.showToast(AirValveControlActivity.this, getString(R.string.set_format_error));
                    return;
                }

                AirValveItemInfo airValveItemInfo = airValveItemInfoList.get(position);
                AirValveCommand airValveCommand = new AirValveCommand(FunctionObject.SET_AIR_VALVE_OPEN);
                ByteBuffer byteBuffer = ByteBuffer.allocate(3);
                byteBuffer.put((byte) airValveItemInfo.getAddress());
                byteBuffer.put(ByteUtils.int16ToByteArray(Integer.parseInt(openValue)));
                airValveCommand.setData(byteBuffer.array());
                SpDataProcessor.getInstance().send(airValveCommand);
            }
        });

        getAirValveStatus();

        newWindCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!newWindCb.isPressed()) {
                    return;
                }
                FanCommand fanCommand = new FanCommand(FunctionObject.SET_STATIC_PRESSURE_MODE);
                if (isChecked) {
                    fanCommand.setData(new byte[]{(byte) 0x00, (byte) 0x01});
                } else {
                    fanCommand.setData(new byte[]{(byte) 0x00, (byte) 0x00});
                }
                SpDataProcessor.getInstance().send(fanCommand);
            }
        });
        exhaustCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!exhaustCb.isPressed()) {
                    return;
                }
                FanCommand fanCommand = new FanCommand(FunctionObject.SET_STATIC_PRESSURE_MODE);
                if (isChecked) {
                    fanCommand.setData(new byte[]{(byte) 0x01, (byte) 0x01});
                } else {
                    fanCommand.setData(new byte[]{(byte) 0x01, (byte) 0x00});
                }
                SpDataProcessor.getInstance().send(fanCommand);
            }
        });
        circleCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!circleCb.isPressed()) {
                    return;
                }
                FanCommand fanCommand = new FanCommand(FunctionObject.SET_STATIC_PRESSURE_MODE);
                if (isChecked) {
                    fanCommand.setData(new byte[]{(byte) 0x02, (byte) 0x01});
                } else {
                    fanCommand.setData(new byte[]{(byte) 0x02, (byte) 0x00});
                }
                SpDataProcessor.getInstance().send(fanCommand);
            }
        });
    }

    private void getAirValveStatus() {
        AirValveCommand airValveCommand = new AirValveCommand(FunctionObject.GET_AIR_VALVE_STATUS);
        SpDataProcessor.getInstance().send(airValveCommand);
    }

    private void initWindow() {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
    }

    private void initList() {
        for (int i = 0; i < 9; i++) {
            AirValveItemInfo info = new AirValveItemInfo();
            info.setValveId(i + 1);
            airValveItemInfoList.add(info);
        }

    }

    @OnClick({R.id.li_back})
    public void onReturnClick(View view) {
        finish();
    }

    @OnClick({R.id.new_wind_value_bt})
    public void onNewWindClick(View view) {
        String text = newWindSetValueEt.getText().toString().trim();
        if (TextUtils.isEmpty(text)) {
            return;
        }
        if (Integer.parseInt(text) > 500) {
            ToastUtil.showToast(AirValveControlActivity.this, getString(R.string.set_format_error));
            return;
        }
        FanCommand fanCommand = new FanCommand(FunctionObject.SET_FAN_PRESSURE_VALUE);
        ByteBuffer byteBuffer = ByteBuffer.allocate(3);
        byteBuffer.put((byte) 0x00);
        byteBuffer.put(ByteUtils.int16ToByteArray(Integer.parseInt(text)));
        fanCommand.setData(byteBuffer.array());
        SpDataProcessor.getInstance().send(fanCommand);
    }

    @OnClick({R.id.exhaust_value_bt})
    public void onExhaustClick(View view) {
        String text = exhaustSetValueEt.getText().toString().trim();
        if (TextUtils.isEmpty(text)) {
            return;
        }
        if (Integer.parseInt(text) > 500) {
            ToastUtil.showToast(AirValveControlActivity.this, getString(R.string.set_format_error));
            return;
        }
        FanCommand fanCommand = new FanCommand(FunctionObject.SET_FAN_PRESSURE_VALUE);
        ByteBuffer byteBuffer = ByteBuffer.allocate(3);
        byteBuffer.put((byte) 0x01);
        byteBuffer.put(ByteUtils.int16ToByteArray(Integer.parseInt(text)));
        fanCommand.setData(byteBuffer.array());
        SpDataProcessor.getInstance().send(fanCommand);

    }

    @OnClick({R.id.circle_value_bt})
    public void onCircleClick(View view) {
        String text = circleSetValueEt.getText().toString().trim();
        if (TextUtils.isEmpty(text)) {
            return;
        }
        if (Integer.parseInt(text) > 500) {
            ToastUtil.showToast(AirValveControlActivity.this, getString(R.string.set_format_error));
            return;
        }
        FanCommand fanCommand = new FanCommand(FunctionObject.SET_FAN_PRESSURE_VALUE);
        ByteBuffer byteBuffer = ByteBuffer.allocate(3);
        byteBuffer.put((byte) 0x02);
        byteBuffer.put(ByteUtils.int16ToByteArray(Integer.parseInt(text)));
        fanCommand.setData(byteBuffer.array());
        SpDataProcessor.getInstance().send(fanCommand);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(AirValveStatusInfo info) {
        FanCommand fanCommand = new FanCommand(FunctionObject.GET_FAN_STATUS);
        SpDataProcessor.getInstance().send(fanCommand);
        if (info != null) {
            List<AirValveItemInfo> airValveList = info.getAirValveData();
            airValveItemInfoList.clear();
            airValveItemInfoList.addAll(airValveList);
            adapter.notifyDataSetChanged();
        }
    }

    private List<FanDataInfo> fanList = new ArrayList<>();

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFanEvent(FanStatusInfo info) {
        if (info != null) {
            fanList.clear();
            fanList.addAll(info.getFanData());
            if (fanList != null && fanList.size() == 4) {
                newWindCb.setChecked(fanList.get(0).getStaticPressure() == 1);
                newWindSetValueTv.setText(fanList.get(0).getRealPressure() + "");
                newWindSetValueEt.setText(fanList.get(0).getSetPressure() + "");

                exhaustCb.setChecked(fanList.get(1).getStaticPressure() == 1);
                exhaustSetValueTv.setText(fanList.get(1).getRealPressure() + "");
                exhaustSetValueEt.setText(fanList.get(1).getSetPressure() + "");

                circleCb.setChecked(fanList.get(2).getStaticPressure() == 1);
                circleSetValueTv.setText(fanList.get(2).getRealPressure() + "");
                circleSetValueEt.setText(fanList.get(2).getSetPressure() + "");
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFanErrorEvent(FanErrorEvent info) {
        if (info != null) {
            if (info.getType() == 5) {
                if (info.getStatus()) {
                    ToastUtil.showToast(this, getString(R.string.set_success));
                    getAirValveStatus();
                } else {
                    ToastUtil.showToast(this, getString(R.string.set_fail));
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
