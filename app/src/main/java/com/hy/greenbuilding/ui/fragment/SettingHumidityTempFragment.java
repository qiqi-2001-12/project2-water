package com.hy.greenbuilding.ui.fragment;

import android.graphics.Rect;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.hy.greenbuilding.HyApplication;
import com.hy.greenbuilding.R;
import com.hy.greenbuilding.config.SaveControlInfo;
import com.hy.greenbuilding.event.ModeSwitchUpdateEvent;
import com.hy.greenbuilding.event.ResetSystemEvent;
import com.hy.greenbuilding.event.RoomChangeEvent;
import com.hy.greenbuilding.event.SetStatusEvent;
import com.hy.greenbuilding.event.SettingUpdateEvent;
import com.hy.greenbuilding.event.TempSwitchEvent;
import com.hy.greenbuilding.event.TempSwitchUpdateEvent;
import com.hy.greenbuilding.model.RoomInfo;
import com.hy.greenbuilding.mqtt.HDTopic;
import com.hy.greenbuilding.mqtt.MqttUploadManager;
import com.hy.greenbuilding.protocol.FunctionObject;
import com.hy.greenbuilding.protocol.ResPonseInfo.CustomDataInfo;
import com.hy.greenbuilding.protocol.ResPonseInfo.EnvironmentDataInfo;
import com.hy.greenbuilding.protocol.SpDataProcessor;
import com.hy.greenbuilding.protocol.command.ControlCommand;
import com.hy.greenbuilding.protocol.command.CustomCommand;
import com.hy.greenbuilding.ui.widget.KeyboardEditText;
import com.hy.greenbuilding.utils.ByteUtils;
import com.hy.greenbuilding.utils.InputLimitUtil;
import com.hy.greenbuilding.utils.MySpUtil;
import com.hy.greenbuilding.utils.StringUtils;
import com.hy.greenbuilding.utils.ToastUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SettingHumidityTempFragment extends Fragment {
    public static final String ARG_FRAGMENT_TYPE = "fragment_type";
    private static final int DEFAULT_TEMP_MIN = 16;
    private static final int DEFAULT_TEMP_MAX = 30;
    @BindView(R.id.et_humidity)
    KeyboardEditText humidityEdit;

    @BindView(R.id.et_tempMax)
    KeyboardEditText etTempMax;
    @BindView(R.id.et_tempMin)
    KeyboardEditText etTempMin;
    @BindView(R.id.bt_setTemp)
    Button mTempSet;
    @BindView(R.id.et_humidityTemp)
    KeyboardEditText etHumidityTemp;

    @BindView(R.id.switch_humidity_status)
    Switch humiditySwitch;


    @BindView(R.id.tv_humidity_setting)
    TextView tvHumiditySetting;
    @BindView(R.id.tv_humidity)
    TextView tvHumidity;

    @BindView(R.id.ll_dehumidify)
    LinearLayout llDehumidify;
    @BindView(R.id.ll_temperature)
    LinearLayout llTemperature;

    @BindView(R.id.tv_min_minus)
    TextView tvMinMinus;
    @BindView(R.id.tv_min_plus)
    TextView tvMinPlus;
    @BindView(R.id.tv_max_minus)
    TextView tvMaxMinus;
    @BindView(R.id.tv_max_plus)
    TextView tvMaxPlus;

    @BindView(R.id.tv_cold_minus)
    TextView tvColdMinus;
    @BindView(R.id.tv_cold_plus)
    TextView tvColdPlus;
    @BindView(R.id.tv_humid_minus)
    TextView tvHumidMinus;
    @BindView(R.id.tv_humid_plus)
    TextView tvHumidPlus;
    @BindView(R.id.tv_temperature)
    TextView tvTemperature;

    @BindView(R.id.textView2)
    TextView textView2;

    private View rootView;

    @BindView(R.id.set_temp_switch)
    Switch mTempSwitch;
    private boolean eventMode;
    private boolean newSwitchState;
    private String fragmentType;
    private String type;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        rootView = inflater.inflate(R.layout.set_humidity_module, container, false);
        ButterKnife.bind(this, rootView);
        SpDataProcessor.getInstance().send(new ControlCommand(FunctionObject.GET_CONTROL_STATUS));
        eventMode = (int) MySpUtil.getParam(getActivity(), MySpUtil.RUN_Mode_STATUS, 0) == 0;
        newSwitchState = (boolean) MySpUtil.getParam(getContext(), MySpUtil.TEMP_SWITCH, false);


        initData();
        setKeyboardVisibilityListener(rootView);
        setupEditTextListeners();

        if (getArguments() != null) {
            String args1Value = getArguments().getString("agrs1");
            if (args1Value != null) {
                fragmentType = args1Value;
                setFragmentType(fragmentType);
            }
        }
        sendCustomCommand();
        return rootView;
    }

    public static SettingHumidityTempFragment newInstance(String param1) {
        SettingHumidityTempFragment fragment = new SettingHumidityTempFragment();
        Bundle args = new Bundle();
        args.putString("agrs1", param1);
        fragment.setArguments(args);
        return fragment;
    }

    private void setupEditTextListeners() {
        humidityEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    onSetDehumidifyClick(humidityEdit);
                }
            }
        });
        //温度下限
        etTempMin.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    onValueControlClick(etTempMin);
                }
            }
        });
        //温度上限

        etTempMax.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    onValueControlClick(etTempMax);
                }
            }
        });
    }

    public void sendCustomCommand() {
        CustomCommand command = new CustomCommand(FunctionObject.GET_CUSTOM_DATA);
        SpDataProcessor.getInstance().send(command);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            SaveControlInfo controlInfo = MySpUtil.getControlData(this.getActivity());
            if (controlInfo != null) {
                updateHumiditySettingText(controlInfo);
                updateTempSectionText(controlInfo);
            }
            newSwitchState = (boolean) MySpUtil.getParam(getContext(), MySpUtil.TEMP_SWITCH, false);
            Log.e("TAG", "onHiddenChanged: "+newSwitchState);
            mTempSwitch.setChecked(newSwitchState);
            sendCustomCommand();
        }
    }

    public void initData() {
        SaveControlInfo controlInfo = MySpUtil.getControlData(this.getActivity());
        if (controlInfo != null) {
            updateHumiditySettingText(controlInfo);
            updateTempSectionText(controlInfo);
        }

        humiditySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (compoundButton.isPressed() && !(boolean) MySpUtil.getParam(getActivity(), MySpUtil.CLOSE_STATUS, true)) {
                    humiditySwitch.setChecked(!b);
                    ToastUtil.showToast(getActivity(), "请先开机");
                    return;
                }
                ControlCommand humiTempCom = new ControlCommand(FunctionObject.SET_HUMI_SWITCH);
                HDTopic hdTopic = MqttUploadManager.getInstance().getmHDTopic();
                if (b) {
                    humiTempCom.setData(new byte[]{(byte) 1});
                    SpDataProcessor.getInstance().send(humiTempCom);
                    hdTopic.setDeHumiditySwitch((byte) 1);
                } else {
                    humiTempCom.setData(new byte[]{(byte) 0});
                    SpDataProcessor.getInstance().send(humiTempCom);
                    hdTopic.setDeHumiditySwitch((byte) 0);
                }
            }
        });
        mTempSwitch.setChecked(newSwitchState);

        mTempSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {

                if (compoundButton.isPressed() && !(boolean) MySpUtil.getParam(getActivity(), MySpUtil.CLOSE_STATUS, true)) {
                    compoundButton.setChecked(!isChecked);
                    ToastUtil.showToast(getActivity(), "请先开机");
                    return;
                }


                if (!compoundButton.isPressed()) {
                    EventBus.getDefault().post(new TempSwitchEvent(isChecked));
                    return;
                }
                if (!eventMode) {
                    EventBus.getDefault().post(new TempSwitchEvent(isChecked));
                } else {
                    compoundButton.setChecked(!isChecked);
                    // 弹出提示 (可选)
                    ToastUtil.showToast(getActivity(), "当前处于自动模式，无法手动操作");
                }
            }
        });


    }

    private void updateHumiditySettingText(SaveControlInfo controlInfo) {
        if (controlInfo == null) {
            return;
        }
        if (humidityEdit != null) {
            humidityEdit.setText(getDehumidifyText(controlInfo));
        }
    }

    private String getDehumidifyText(SaveControlInfo controlInfo) {
        int value = parseInt(controlInfo.getHumidity(), InputLimitUtil.DEHUMIDIFY_MIN);
        value = clamp(value, InputLimitUtil.DEHUMIDIFY_MIN, InputLimitUtil.DEHUMIDIFY_MAX);
        return String.valueOf(value);
    }

    private void updateTempSectionText(SaveControlInfo controlInfo) {
        if (controlInfo == null) {
            return;
        }
        if (etTempMax != null) {
            etTempMax.setText(getTempMaxText(controlInfo));
        }
        if (etTempMin != null) {
            etTempMin.setText(getTempMinText(controlInfo));
        }
    }

    private String getTempMinText(SaveControlInfo controlInfo) {
        int value = parseInt(controlInfo.getTempMin(), DEFAULT_TEMP_MIN);
        return String.valueOf(clamp(value, DEFAULT_TEMP_MIN, DEFAULT_TEMP_MAX));
    }

    private String getTempMaxText(SaveControlInfo controlInfo) {
        int value = parseInt(controlInfo.getTempMax(), DEFAULT_TEMP_MAX);
        return String.valueOf(clamp(value, DEFAULT_TEMP_MIN, DEFAULT_TEMP_MAX));
    }

    private int parseInt(String value, int fallback) {
        if (TextUtils.isEmpty(value)) {
            return fallback;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private int clamp(int value, int min, int max) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTempSwitchStateUpdate(TempSwitchUpdateEvent event) {
        if (mTempSwitch != null && mTempSwitch.isChecked() != event.getNewSwitchState()) {
            newSwitchState = event.getNewSwitchState();
            mTempSwitch.setChecked(newSwitchState);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onModeSwitchUpdateEvent(ModeSwitchUpdateEvent event) {
        Log.e("TAG", "onModeSwitchUpdateEvent: " + event.isMode());
        eventMode = event.isMode();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void settingUpdateEvent(SettingUpdateEvent settingUpdateEvent) {
        if (settingUpdateEvent != null) {
            if (settingUpdateEvent.getType() == 1) {
                updateTemp(settingUpdateEvent);
                sendCustomCommand();
            }
        }
    }

    public void updateTemp(SettingUpdateEvent settingUpdateEvent) {
        Log.e("TAG", "updateTemp: " + new Gson().toJson(settingUpdateEvent));
        if (!TextUtils.isEmpty(settingUpdateEvent.getTempMin()))
            etTempMin.setText(settingUpdateEvent.getTempMin());

        if (!TextUtils.isEmpty(settingUpdateEvent.getTempMax()))
            etTempMax.setText(settingUpdateEvent.getTempMax());

        if (!TextUtils.isEmpty(settingUpdateEvent.getHumidity())) {
            int value = parseInt(settingUpdateEvent.getHumidity(), InputLimitUtil.DEHUMIDIFY_MIN);
            humidityEdit.setText(String.valueOf(clamp(value, InputLimitUtil.DEHUMIDIFY_MIN, InputLimitUtil.DEHUMIDIFY_MAX)));
        }

    }

    //初始化除湿开关
    public void initHumiditySwitch(int status) {
        Log.e("TAG", "initHumiditySwitch: " + status);
        if (status == 1) {
            humiditySwitch.setChecked(true);
        } else {
            humiditySwitch.setChecked(false);
        }
    }


    @OnClick({R.id.bt_setTemp})
    public void onTempClick(View view) {
        String tempMax = etTempMax.getText().toString();
        String tempMin = etTempMin.getText().toString();
        etTempMax.clearFocus();
        etTempMin.clearFocus();
        if (!StringUtils.isNullOrEmpty(tempMax) && !StringUtils.isNullOrEmpty(tempMin)) {
            BigDecimal bigDecimalMax = new BigDecimal(tempMax).setScale(1, BigDecimal.ROUND_DOWN).multiply(new BigDecimal(10));
            BigDecimal bigDecimalMin = new BigDecimal(tempMin).setScale(1, BigDecimal.ROUND_DOWN).multiply(new BigDecimal(10));
            if (InputLimitUtil.tempLimit(bigDecimalMin, bigDecimalMax) && !tempMax.startsWith("0") && !tempMin.startsWith("0")) {
                byte[] temp1 = ByteUtils.int16ToByteArray(bigDecimalMax.intValue());
                byte[] temp2 = ByteUtils.int16ToByteArray(bigDecimalMin.intValue());
                byte[] data3 = ByteUtils.splicingBytes(temp1, temp2);
                ControlCommand controlCommand = new ControlCommand(FunctionObject.SET_TEMP_SECTION);
                controlCommand.setData(data3);
                SpDataProcessor.getInstance().send(controlCommand);
            } else {
                ToastUtil.showToast(getActivity(), getString(R.string.set_format_error));
            }
        }
    }

    public void setFragmentType(String type) {
        this.type = type;
        if (Objects.equals(type, "temperature")) {
            tvHumiditySetting.setText("室内湿度");
            llDehumidify.setVisibility(View.GONE);
            llTemperature.setVisibility(View.VISIBLE);
            textView2.setText(R.string.humidity_control_title);
            humiditySwitch.setVisibility(View.VISIBLE);
            mTempSwitch.setVisibility(View.GONE);
        } else {
            tvHumiditySetting.setText("室内温度");
            llDehumidify.setVisibility(View.VISIBLE);
            llTemperature.setVisibility(View.GONE);
            textView2.setText("温控");
            humiditySwitch.setVisibility(View.GONE);
            mTempSwitch.setVisibility(View.VISIBLE);
        }
    }


    @OnClick({R.id.tv_min_minus, R.id.tv_min_plus, R.id.tv_max_minus, R.id.tv_max_plus})
    public void onValueControlClick(View view) {
        String tempMax = etTempMax.getText().toString();
        String tempMin = etTempMin.getText().toString();
        int maxTemp = parseInt(tempMax, DEFAULT_TEMP_MAX);
        int minTemp = parseInt(tempMin, DEFAULT_TEMP_MIN);
        switch (view.getId()) {
            case R.id.tv_min_minus:
                minTemp--;
                break;
            case R.id.tv_min_plus:
                minTemp++;
                break;
            case R.id.tv_max_minus:
                maxTemp--;
                break;
            case R.id.tv_max_plus:
                maxTemp++;
                break;
        }
        Log.e("TAG", "onValueControlClick: " + tempMax + " == " + tempMin);


        BigDecimal bigDecimalMax = new BigDecimal(maxTemp).setScale(1, BigDecimal.ROUND_DOWN).multiply(new BigDecimal(10));
        BigDecimal bigDecimalMin = new BigDecimal(minTemp).setScale(1, BigDecimal.ROUND_DOWN).multiply(new BigDecimal(10));
        if (InputLimitUtil.tempLimit(bigDecimalMin, bigDecimalMax)) {
            byte[] temp1 = ByteUtils.int16ToByteArray(bigDecimalMax.intValue());
            byte[] temp2 = ByteUtils.int16ToByteArray(bigDecimalMin.intValue());
            byte[] data3 = ByteUtils.splicingBytes(temp1, temp2);
            ControlCommand controlCommand = new ControlCommand(FunctionObject.SET_TEMP_SECTION);
            controlCommand.setData(data3);
            SpDataProcessor.getInstance().send(controlCommand);
            etTempMax.setText(maxTemp + "");
            etTempMin.setText(minTemp + "");
        } else {
            ToastUtil.showToast(getActivity(), "下限与上限差需要等于大于3℃或数据错误");
            onHiddenChanged(false);
        }


//        if (!StringUtils.isNullOrEmpty(tempMax) && !StringUtils.isNullOrEmpty(tempMin)) {
//            BigDecimal bigDecimalMax = new BigDecimal(tempMax).setScale(1, BigDecimal.ROUND_DOWN).multiply(new BigDecimal(10));
//            BigDecimal bigDecimalMin = new BigDecimal(tempMin).setScale(1, BigDecimal.ROUND_DOWN).multiply(new BigDecimal(10));
//            if (InputLimitUtil.tempLimit(bigDecimalMin, bigDecimalMax) && !tempMax.startsWith("0") && !tempMin.startsWith("0")) {
//                byte[] temp1 = ByteUtils.int16ToByteArray(bigDecimalMax.intValue());
//                byte[] temp2 = ByteUtils.int16ToByteArray(bigDecimalMin.intValue());
//                byte[] data3 = ByteUtils.splicingBytes(temp1, temp2);
//                ControlCommand controlCommand = new ControlCommand(FunctionObject.SET_TEMP_SECTION);
//                controlCommand.setData(data3);
//                SpDataProcessor.getInstance().send(controlCommand);
//            } else {
//                ToastUtil.showToast(getActivity(), getString(R.string.set_format_error));
//            }
//        }


        // 6. 更新 EditText
//        targetEditText.setText(String.valueOf(newValue));
    }

    @OnClick({R.id.tv_cold_minus, R.id.tv_cold_plus})
    public void onSetDehumidifyClick(View view) {
        String humidity = humidityEdit.getText().toString();
        int newValue = parseInt(humidity, InputLimitUtil.DEHUMIDIFY_MIN);
        if (view.getId() == R.id.tv_cold_minus) {
            newValue--;
        } else if (view.getId() == R.id.tv_cold_plus) {
            newValue++;
        }
        if (!InputLimitUtil.dehumidifyLimit(new BigDecimal(newValue))) {
            ToastUtil.showToast(getActivity(), getString(R.string.set_format_error));
            onHiddenChanged(false);
            return;
        }
        humidityEdit.clearFocus();
        humidityEdit.setText(String.valueOf(newValue));
        sendHumiditySetting();
    }

    private void sendHumiditySetting() {
        String humidity = humidityEdit.getText().toString();
        if (StringUtils.isNullOrEmpty(humidity)) {
            return;
        }
        BigDecimal dehumidifyValue;
        try {
            dehumidifyValue = new BigDecimal(humidity);
        } catch (NumberFormatException e) {
            ToastUtil.showToast(getActivity(), getString(R.string.set_format_error));
            onHiddenChanged(false);
            return;
        }
        if (!InputLimitUtil.dehumidifyLimit(dehumidifyValue)) {
            ToastUtil.showToast(getActivity(), getString(R.string.set_format_error));
            onHiddenChanged(false);
            return;
        }
        ByteBuffer byteBuffer = ByteBuffer.allocate(3);
        byteBuffer.put(ByteUtils.int16ToByteArray(dehumidifyValue.intValue()));
        SaveControlInfo controlInfo = MySpUtil.getControlData(getActivity());
        byteBuffer.put((byte) (controlInfo == null ? 35 : controlInfo.getHumidity1()));
        ControlCommand controlCommand = new ControlCommand(FunctionObject.SET_HUMIDITY);
        controlCommand.setData(byteBuffer.array());
        SpDataProcessor.getInstance().send(controlCommand);
    }

    @OnClick({R.id.tv_humid_minus, R.id.tv_humid_plus})
    public void onSetHumidityTemp(View view) {
        String temp = etHumidityTemp.getText().toString();
        int newValue = TextUtils.isEmpty(temp) ? 0 : Integer.parseInt(temp);
        if (view.getId() == R.id.tv_humid_minus) {
            newValue--;
        } else {
            newValue++;
        }

        if (newValue == 0 || newValue > 3) {
            ToastUtil.showToast(getActivity(), "不能小于0，大于3");
            onHiddenChanged(false);
            return;
        }
        etHumidityTemp.setText(String.valueOf(newValue));
        ControlCommand controlCommand = new ControlCommand(FunctionObject.SET_HUMI_TEMP);
        controlCommand.setData(new byte[]{(byte) newValue});
        SpDataProcessor.getInstance().send(controlCommand);
    }

    //杂项数据
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCustomEvent(CustomDataInfo info) {
        if (info != null) {
            initHumiditySwitch(info.getHumiditySwitch());
        }
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onResetEvent(ResetSystemEvent resetSystemEvent) {
        if (resetSystemEvent != null) {
            if (resetSystemEvent.isSuccess()) {
                ToastUtil.showToast(getActivity(), "设置成功！");
            } else {
                ToastUtil.showToast(getActivity(), "设置失败！");
            }
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onStatusEvent(SetStatusEvent event) {
        if (event != null) {
            if (event.getType() == 2) {
                if (event.getStatus()) {
                    ToastUtil.showToast(getActivity(), "湿度设置成功！");
                } else {
                    ToastUtil.showToast(getActivity(), "湿度设置失败！");
                }
            } else if (event.getType() == 3) {
                if (event.getStatus()) {
                    ToastUtil.showToast(getActivity(), "温度设置成功！");
                } else {
                    ToastUtil.showToast(getActivity(), "温度设置失败！");
                }
            } else if (event.getType() == 4) {
                if (event.getStatus()) {
                    ToastUtil.showToast(getActivity(), "重置成功!");
                } else {
                    ToastUtil.showToast(getActivity(), "重置失败!");
                }
            }
        }
    }

    private List<RoomInfo> roomList = new ArrayList<>();

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRoomEvent(EnvironmentDataInfo info) {
        if (info != null) {
            byte[] errorBytes = info.getRoomError();
            HyApplication.setRoomError(errorBytes);
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
                    int temp;
                    if (roomList.get(0).getTemp() == 0) {
                        temp = roomList.get(0).getTemp();

                        for (int i = 0; i < roomList.size(); i++) {
                            if (roomList.get(i).getTemp() != 0) {
                                temp = roomList.get(i).getTemp();
                                break;
                            }
                        }
                    } else {
                        temp = roomList.get(0).getTemp();
                    }

                    Collections.sort(roomList, new Comparator<RoomInfo>() {//湿度显示最大值
                        public int compare(RoomInfo arg0, RoomInfo arg1) {
                            return arg1.getHumidity() - arg0.getHumidity();
                        }
                    });

                    if (Objects.equals(type, "temperature")) {
                        tvHumidity.setText("室内温度  " + temp + "℃");
                        tvTemperature.setText(roomList.get(0).getHumidity() + "%");

                    } else {
                        tvTemperature.setText(temp + "℃");
                        tvHumidity.setText("室内湿度  " + roomList.get(0).getHumidity() + "%");
                    }
                }
            }
        }
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
            if (humidityEdit != null && humidityEdit.hasFocus()) {
                humidityEdit.clearFocus();
            }
            if (etTempMin != null && etTempMin.hasFocus()) {
                etTempMin.clearFocus();
            }
            if (etTempMax != null && etTempMax.hasFocus()) {
                etTempMax.clearFocus();
            }
        }
    }
}
