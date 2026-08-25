package com.hy.greenbuilding.ui.fragment;


import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.hy.greenbuilding.R;
import com.hy.greenbuilding.config.SaveFilterScreen;
import com.hy.greenbuilding.event.SettingUpdateEvent;
import com.hy.greenbuilding.ui.activity.BaseActivity;
import com.hy.greenbuilding.utils.InputLimitUtil;
import com.hy.greenbuilding.utils.MySpUtil;
import com.hy.greenbuilding.utils.StringUtils;
import com.hy.greenbuilding.utils.ToastUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class SettingScreenSetFragment extends BaseDialogFragment implements CompoundButton.OnCheckedChangeListener, BaseActivity.KeyboardVisibilityListener {

    @BindView(R.id.use_freshAir_hour)
    TextView useFreshAirHour;
    @BindView(R.id.use_circle1_hour)
    TextView useCircle1Hour;
    @BindView(R.id.use_exhaust_hour)
    TextView useExhaustHour;
    @BindView(R.id.use_circle2_hour)
    TextView useCircle2Hour;
    @BindView(R.id.use_freshAir_reset)
    Button useFreshAirReset;
    @BindView(R.id.use_circle1_reset)
    Button useCircle1Reset;
    @BindView(R.id.use_exhaust_reset)
    Button useExhaustReset;
    @BindView(R.id.use_circle2_reset)
    Button useCircle2Reset;

    @BindView(R.id.change_freshAir_hour)
    EditText changeFreshAirHour;
    @BindView(R.id.change_circle1_hour)
    EditText changeCircle1Hour;
    @BindView(R.id.change_exhaust_hour)
    EditText changeExhaustHour;
    @BindView(R.id.change_circle2_hour)
    EditText changeCircle2Hour;
    @BindView(R.id.freshAir_screen_info)
    TextView freshAirInfo;
    @BindView(R.id.exhaust_screen_info)
    TextView exhaustInfo;
    @BindView(R.id.circle1_screen_info)
    TextView circle1Info;
    @BindView(R.id.circle2_screen_info)
    TextView circle2Info;

    @BindView(R.id.pressure_freshAir)
    EditText pressureFreshAir;
    @BindView(R.id.pressure_circle1)
    EditText pressureCircle1;
    @BindView(R.id.pressure_exhaust)
    EditText pressureExhaust;
    @BindView(R.id.pressure_circle2)
    EditText pressureCircle2;

    @BindView(R.id.new_wind_pressure_value)
    TextView newWindPressureValue;
    @BindView(R.id.exhaust_pressure_value)
    TextView exhaustPressureValue;
    @BindView(R.id.circle1_pressure_value)
    TextView circle1PressureValue;
    @BindView(R.id.circle2_pressure_value)
    TextView circle2PressureValue;


    @BindView(R.id.new_wind_time_cb)
    CheckBox newWindTimeCb;
    @BindView(R.id.exhaust_time_cb)
    CheckBox exhaustTimeCb;
    @BindView(R.id.circle1_time_cb)
    CheckBox circle1TimeCb;
    @BindView(R.id.circle2_time_cb)
    CheckBox circle2TimeCb;

    @BindView(R.id.new_wind_pressure_cb)
    CheckBox newWindPressureCb;
    @BindView(R.id.exhaust_pressure_cb)
    CheckBox exhaustPressureCb;
    @BindView(R.id.circle1_pressure_cb)
    CheckBox circle1PressureCb;
    @BindView(R.id.circle2_pressure_cb)
    CheckBox circle2PressureCb;

    @BindView(R.id.tv_li_filterScreen)
    TextView tvLiFilterScreen;

    @BindView(R.id.tv_pressure_setting)
    TextView tvPressureSetting;

    @BindView(R.id.in_filter_screen_module1)
    LinearLayout inFilterScreenModule1;

    @BindView(R.id.in_filter_screen_module3)
    LinearLayout inFilterScreenModule3;

    @BindView(R.id.li_back)
    ImageView mReturnView;

    private View rootView;
    private Unbinder unbinder;

    private AlertDialog mResetDialog;
    private boolean hidden;

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        this.hidden = hidden;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.DialogFullScreen); //dialog全屏
    }

    @OnClick({R.id.li_back})
    public void onReturnClick(View view) {
        this.dismiss();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.set_screen_module, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        initScreen(true);
        newWindTimeCb.setOnCheckedChangeListener(this);
        exhaustTimeCb.setOnCheckedChangeListener(this);
        circle1TimeCb.setOnCheckedChangeListener(this);
        circle2TimeCb.setOnCheckedChangeListener(this);
        newWindPressureCb.setOnCheckedChangeListener(this);
        exhaustPressureCb.setOnCheckedChangeListener(this);
        circle1PressureCb.setOnCheckedChangeListener(this);
        circle2PressureCb.setOnCheckedChangeListener(this);

        tvPressureSetting.setSelected(false);
        tvLiFilterScreen.setSelected(true);
        return rootView;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void settingUpdateEvent(SettingUpdateEvent settingUpdateEvent) {
        if (settingUpdateEvent != null) {
            if (settingUpdateEvent.getType() == 6) {
                initScreen(true);
            }
        }
    }

    public void initScreen(boolean isInit) {
        //滤网数据显示
        SaveFilterScreen saveFilterScreen = MySpUtil.getFilterScreen(this.getActivity());
        if (saveFilterScreen != null) {
            //滤网使用
            String freshAirUse = saveFilterScreen.getFreshAirUse();
            String exhaustUse = saveFilterScreen.getExhaustUse();
            String circle1Use = saveFilterScreen.getCircle1Use();
            String circle2Use = saveFilterScreen.getCircle2Use();
            if (!StringUtils.isNullOrEmpty(freshAirUse))
                useFreshAirHour.setText(Long.valueOf(freshAirUse) / 3600 + "");
            if (!StringUtils.isNullOrEmpty(exhaustUse))
                useExhaustHour.setText(Long.valueOf(exhaustUse) / 3600 + "");
            if (!StringUtils.isNullOrEmpty(circle1Use))
                useCircle1Hour.setText(Long.valueOf(circle1Use) / 3600 + "");
            if (!StringUtils.isNullOrEmpty(circle2Use))
                useCircle2Hour.setText(Long.valueOf(circle2Use) / 3600 + "");
            //滤网更换
            String freshAirChange = saveFilterScreen.getFreshAirChange();
            String exhaustChange = saveFilterScreen.getExhaustChange();
            String circle1Change = saveFilterScreen.getCircle1Change();
            String circle2Change = saveFilterScreen.getCircle2Change();
            if (isInit) {
                if (!changeFreshAirHour.isFocused())
                    changeFreshAirHour.setText(saveFilterScreen.getFreshAirChange());
                if (!changeExhaustHour.isFocused())
                    changeExhaustHour.setText(saveFilterScreen.getExhaustChange());
                if (!changeCircle1Hour.isFocused())
                    changeCircle1Hour.setText(saveFilterScreen.getCircle1Change());
                if (!changeCircle2Hour.isFocused())
                    changeCircle2Hour.setText(saveFilterScreen.getCircle2Change());
            }
            boolean b1 = saveFilterScreen.isFreshAirUseTime();
            newWindTimeCb.setChecked(b1);
            boolean b2 = saveFilterScreen.isExhaustUseTime();
            exhaustTimeCb.setChecked(b2);
            boolean b3 = saveFilterScreen.isCircle1UseTime();
            circle1TimeCb.setChecked(b3);
            boolean b4 = saveFilterScreen.isCircle2UseTime();
            circle2TimeCb.setChecked(b4);

            newWindPressureCb.setChecked(saveFilterScreen.isFreshAirUsePressure());
            exhaustPressureCb.setChecked(saveFilterScreen.isExhaustUsePressure());
            circle1PressureCb.setChecked(saveFilterScreen.isCircle1UsePressure());
            circle2PressureCb.setChecked(saveFilterScreen.isCircle2UsePressure());

            if (!StringUtils.isNullOrEmpty(freshAirUse) && !StringUtils.isNullOrEmpty(freshAirChange)) {
                //更换新风滤网
                if (Long.valueOf(freshAirUse) / 3600 > Long.valueOf(freshAirChange) && b1) {
                    freshAirInfo.setVisibility(View.VISIBLE);
                } else {
                    freshAirInfo.setVisibility(View.INVISIBLE);
                }
            }
            if (!StringUtils.isNullOrEmpty(exhaustUse) && !StringUtils.isNullOrEmpty(exhaustChange)) {
                //更换排风滤网
                if (Long.valueOf(exhaustUse) / 3600 > Long.valueOf(exhaustChange) && b2) {
                    exhaustInfo.setVisibility(View.VISIBLE);
                } else {
                    exhaustInfo.setVisibility(View.INVISIBLE);
                }
            }
            if (!StringUtils.isNullOrEmpty(circle1Use) && !StringUtils.isNullOrEmpty(circle1Change)) {
                //更换内循环1滤网
                if (Long.valueOf(circle1Use) / 3600 > Long.valueOf(circle1Change) && b3) {
                    circle1Info.setVisibility(View.VISIBLE);
                } else {
                    circle1Info.setVisibility(View.INVISIBLE);
                }
            }
            if (!StringUtils.isNullOrEmpty(circle2Use) && !StringUtils.isNullOrEmpty(circle2Change)) {
                //更换内循环2滤网
                if (Long.valueOf(circle2Use) / 3600 > Long.valueOf(circle2Change) && b4) {
                    circle2Info.setVisibility(View.VISIBLE);
                } else {
                    circle2Info.setVisibility(View.INVISIBLE);
                }
            }
            if (isInit) {
                if (!pressureFreshAir.isFocused())
                    pressureFreshAir.setText(saveFilterScreen.getFreshAirPressure());
                if (!pressureExhaust.isFocused())
                    pressureExhaust.setText(saveFilterScreen.getExhaustPressure());
                if (!pressureCircle1.isFocused())
                    pressureCircle1.setText(saveFilterScreen.getCircle1Pressure());
                if (!pressureCircle2.isFocused())
                    pressureCircle2.setText(saveFilterScreen.getCircle2Pressure());
            }
            newWindPressureValue.setText(saveFilterScreen.getFreshAirPressureValue());
            exhaustPressureValue.setText(saveFilterScreen.getExhaustPressureValue());
            circle1PressureValue.setText(saveFilterScreen.getCircle1PressureValue());
            circle2PressureValue.setText(saveFilterScreen.getCircle2PressureValue());
        }
    }

    //滤网重置确认框
    private void showResetDialog(int type) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.reset_dialog, null, false);
        mResetDialog = new AlertDialog.Builder(getActivity()).setView(view).create();
        Button sure = view.findViewById(R.id.bt_reset_sure);
        sure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SaveFilterScreen saveFilterScreen = MySpUtil.getFilterScreen(getActivity());
                if (type == 1) {
                    useFreshAirHour.setText("0");
                    saveFilterScreen.setFreshAirUse("0");
                } else if (type == 2) {
                    useExhaustHour.setText("0");
                    saveFilterScreen.setExhaustUse("0");
                } else if (type == 3) {
                    useCircle1Hour.setText("0");
                    saveFilterScreen.setCircle1Use("0");
                } else if (type == 4) {
                    useCircle2Hour.setText("0");
                    saveFilterScreen.setCircle2Use("0");
                }
                MySpUtil.setParam(getActivity(), MySpUtil.FILTER_SCREEN_DATA, new Gson().toJson(saveFilterScreen));
                mResetDialog.dismiss();
            }
        });
        mResetDialog.show();
        mResetDialog.getWindow().setLayout(550, 460);
    }


    @OnClick({R.id.use_freshAir_reset})
    public void onFreshUseClick(View view) {
        showResetDialog(1);
    }

    @OnClick({R.id.tv_pressure_setting})
    public void onPressureSetting(View view) {
        inFilterScreenModule1.setVisibility(View.GONE);
        inFilterScreenModule3.setVisibility(View.VISIBLE);
        tvPressureSetting.setSelected(true);
        tvLiFilterScreen.setSelected(false);
    }

    @OnClick({R.id.tv_li_filterScreen})
    public void onLiFilterScreen(View view) {
        inFilterScreenModule1.setVisibility(View.VISIBLE);
        inFilterScreenModule3.setVisibility(View.GONE);
        tvPressureSetting.setSelected(false);
        tvLiFilterScreen.setSelected(true);
    }

    @OnClick({R.id.use_exhaust_reset})
    public void onExhaustUseClick(View view) {
        showResetDialog(2);
    }

    @OnClick({R.id.use_circle1_reset})
    public void onCircle1UseClick(View view) {
        showResetDialog(3);
    }

    @OnClick({R.id.use_circle2_reset})
    public void onCircle2UseClick(View view) {
        showResetDialog(4);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        SaveFilterScreen saveFilterScreen = MySpUtil.getFilterScreen(getActivity());
        if (buttonView.getId() == R.id.new_wind_time_cb) {
            if (isChecked) {
                newWindPressureCb.setChecked(false);
                if (saveFilterScreen != null) {
                    saveFilterScreen.setFreshAirUseTime(true);
                    saveFilterScreen.setFreshAirUsePressure(false);
                    MySpUtil.setParam(getActivity(), MySpUtil.FILTER_SCREEN_DATA, new Gson().toJson(saveFilterScreen));
                }
            } else {
                if (saveFilterScreen != null) {
                    saveFilterScreen.setFreshAirUseTime(false);
                    MySpUtil.setParam(getActivity(), MySpUtil.FILTER_SCREEN_DATA, new Gson().toJson(saveFilterScreen));
                }
            }
        } else if (buttonView.getId() == R.id.exhaust_time_cb) {
            if (isChecked) {
                exhaustPressureCb.setChecked(false);
                if (saveFilterScreen != null) {
                    saveFilterScreen.setExhaustUseTime(true);
                    saveFilterScreen.setExhaustUsePressure(false);
                    MySpUtil.setParam(getActivity(), MySpUtil.FILTER_SCREEN_DATA, new Gson().toJson(saveFilterScreen));
                }
            } else {
                if (saveFilterScreen != null) {
                    saveFilterScreen.setExhaustUseTime(false);
                    MySpUtil.setParam(getActivity(), MySpUtil.FILTER_SCREEN_DATA, new Gson().toJson(saveFilterScreen));
                }
            }
        } else if (buttonView.getId() == R.id.circle1_time_cb) {
            if (isChecked) {
                circle1PressureCb.setChecked(false);
                if (saveFilterScreen != null) {
                    saveFilterScreen.setCircle1UseTime(true);
                    saveFilterScreen.setCircle1UsePressure(false);
                    MySpUtil.setParam(getActivity(), MySpUtil.FILTER_SCREEN_DATA, new Gson().toJson(saveFilterScreen));
                }
            } else {
                if (saveFilterScreen != null) {
                    saveFilterScreen.setCircle2UseTime(false);
                    MySpUtil.setParam(getActivity(), MySpUtil.FILTER_SCREEN_DATA, new Gson().toJson(saveFilterScreen));
                }
            }
        } else if (buttonView.getId() == R.id.circle2_time_cb) {
            if (isChecked) {
                circle2PressureCb.setChecked(false);
                if (saveFilterScreen != null) {
                    saveFilterScreen.setCircle2UseTime(true);
                    saveFilterScreen.setCircle2UsePressure(false);
                    MySpUtil.setParam(getActivity(), MySpUtil.FILTER_SCREEN_DATA, new Gson().toJson(saveFilterScreen));
                }
            } else {
                if (saveFilterScreen != null) {
                    saveFilterScreen.setCircle2UseTime(false);
                    MySpUtil.setParam(getActivity(), MySpUtil.FILTER_SCREEN_DATA, new Gson().toJson(saveFilterScreen));
                }
            }
        } else if (buttonView.getId() == R.id.new_wind_pressure_cb) {
            if (isChecked) {
                newWindTimeCb.setChecked(false);
                if (saveFilterScreen != null) {
                    saveFilterScreen.setFreshAirUsePressure(true);
                    saveFilterScreen.setFreshAirUseTime(false);
                    MySpUtil.setParam(getActivity(), MySpUtil.FILTER_SCREEN_DATA, new Gson().toJson(saveFilterScreen));
                }
            } else {
                if (saveFilterScreen != null) {
                    saveFilterScreen.setFreshAirUsePressure(false);
                    MySpUtil.setParam(getActivity(), MySpUtil.FILTER_SCREEN_DATA, new Gson().toJson(saveFilterScreen));
                }
            }
        } else if (buttonView.getId() == R.id.exhaust_pressure_cb) {
            if (isChecked) {
                exhaustTimeCb.setChecked(false);
                if (saveFilterScreen != null) {
                    saveFilterScreen.setExhaustUsePressure(true);
                    saveFilterScreen.setExhaustUseTime(false);
                    MySpUtil.setParam(getActivity(), MySpUtil.FILTER_SCREEN_DATA, new Gson().toJson(saveFilterScreen));
                }
            } else {
                if (saveFilterScreen != null) {
                    saveFilterScreen.setExhaustUsePressure(false);
                    MySpUtil.setParam(getActivity(), MySpUtil.FILTER_SCREEN_DATA, new Gson().toJson(saveFilterScreen));
                }
            }
        } else if (buttonView.getId() == R.id.circle1_pressure_cb) {
            if (isChecked) {
                circle1TimeCb.setChecked(false);
                if (saveFilterScreen != null) {
                    saveFilterScreen.setCircle1UsePressure(true);
                    saveFilterScreen.setCircle1UseTime(false);
                    MySpUtil.setParam(getActivity(), MySpUtil.FILTER_SCREEN_DATA, new Gson().toJson(saveFilterScreen));
                }
            } else {
                if (saveFilterScreen != null) {
                    saveFilterScreen.setCircle2UsePressure(false);
                    MySpUtil.setParam(getActivity(), MySpUtil.FILTER_SCREEN_DATA, new Gson().toJson(saveFilterScreen));
                }
            }
        } else if (buttonView.getId() == R.id.circle2_pressure_cb) {
            if (isChecked) {
                circle2TimeCb.setChecked(false);
                if (saveFilterScreen != null) {
                    saveFilterScreen.setCircle2UsePressure(true);
                    saveFilterScreen.setCircle2UseTime(false);
                    MySpUtil.setParam(getActivity(), MySpUtil.FILTER_SCREEN_DATA, new Gson().toJson(saveFilterScreen));
                }
            } else {
                if (saveFilterScreen != null) {
                    saveFilterScreen.setCircle2UsePressure(false);
                    MySpUtil.setParam(getActivity(), MySpUtil.FILTER_SCREEN_DATA, new Gson().toJson(saveFilterScreen));
                }
            }
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // 注册键盘监听器
        if (context instanceof BaseActivity) {
            ((BaseActivity) context).setKeyboardVisibilityListener(this);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        // 清理监听器
        if (getActivity() instanceof BaseActivity) {
            ((BaseActivity) getActivity()).setKeyboardVisibilityListener(null);
        }
    }

    @Override
    public void onKeyboardVisibilityChanged(boolean isVisible) {
        // 【键盘状态变化回调】
        if (isVisible) {
            Log.d("Keyboard", "软键盘已显示 SettingScreenSetFragment");
        } else {
            Log.d("Keyboard", "软键盘已隐藏 SettingScreenSetFragment");
            if (inFilterScreenModule1.getVisibility() == View.VISIBLE && inFilterScreenModule3.getVisibility() == View.GONE && !hidden) {
                String freshAir = changeFreshAirHour.getText().toString();
                String exhaust = changeExhaustHour.getText().toString();
                String circle1 = changeCircle1Hour.getText().toString();
                String circle2 = changeCircle2Hour.getText().toString();
                SaveFilterScreen saveFilterScreen = MySpUtil.getFilterScreen(getActivity());
                if (InputLimitUtil.startWithZero(freshAir) || InputLimitUtil.startWithZero(freshAir)
                        || InputLimitUtil.startWithZero(freshAir) || InputLimitUtil.startWithZero(freshAir)) {
                    ToastUtil.showToast(getActivity(), getString(R.string.set_format_error));
                    return;
                }

                if (saveFilterScreen != null) {
                    saveFilterScreen.setFreshAirChange(freshAir);
                    saveFilterScreen.setExhaustChange(exhaust);
                    saveFilterScreen.setCircle1Change(circle1);
                    saveFilterScreen.setCircle2Change(circle2);
                    MySpUtil.setParam(getActivity(), MySpUtil.FILTER_SCREEN_DATA, new Gson().toJson(saveFilterScreen));
                    ToastUtil.showToast(getActivity(), getString(R.string.set_success));
                }
            } else if (inFilterScreenModule1.getVisibility() == View.GONE && inFilterScreenModule3.getVisibility() == View.VISIBLE && !hidden) {
                String freshAir = pressureFreshAir.getText().toString();
                String exhaust = pressureExhaust.getText().toString();
                String circle1 = pressureCircle1.getText().toString();
                String circle2 = pressureCircle2.getText().toString();
                if (InputLimitUtil.startWithZero(freshAir) || InputLimitUtil.startWithZero(freshAir) || InputLimitUtil.startWithZero(freshAir) || InputLimitUtil.startWithZero(freshAir)) {
                    ToastUtil.showToast(getActivity(), getString(R.string.set_fail));
                    return;
                }
                SaveFilterScreen saveFilterScreen = MySpUtil.getFilterScreen(getActivity());
                if (saveFilterScreen != null) {
                    saveFilterScreen.setFreshAirPressure(freshAir);
                    saveFilterScreen.setExhaustPressure(exhaust);
                    saveFilterScreen.setCircle1Pressure(circle1);
                    saveFilterScreen.setCircle2Pressure(circle2);
                    MySpUtil.setParam(getActivity(), MySpUtil.FILTER_SCREEN_DATA, new Gson().toJson(saveFilterScreen));
                    ToastUtil.showToast(getActivity(), getString(R.string.set_success));
                }
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
