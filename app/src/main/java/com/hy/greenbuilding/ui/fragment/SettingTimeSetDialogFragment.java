package com.hy.greenbuilding.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.gson.Gson;
import com.hy.greenbuilding.R;
import com.hy.greenbuilding.config.SaveTimingInfo;
import com.hy.greenbuilding.event.SettingUpdateEvent;
import com.hy.greenbuilding.event.TempControlEvent;
import com.hy.greenbuilding.ui.widget.KeyboardLayout;
import com.hy.greenbuilding.utils.InputLimitUtil;
import com.hy.greenbuilding.utils.MySpUtil;
import com.hy.greenbuilding.utils.StringUtils;
import com.hy.greenbuilding.utils.TimingUtils;
import com.hy.greenbuilding.utils.ToastUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class SettingTimeSetDialogFragment extends DialogFragment {

    @BindView(R.id.bt_timing_set)
    Button mTimingSet;
    @BindView(R.id.keyboard_layout)
    KeyboardLayout keyboardLayout;
    @BindView(R.id.et_day)
    EditText etDay;
    @BindView(R.id.et_before_time1)
    EditText mBeforeTime1;
    @BindView(R.id.et_after_time1)
    EditText mAfterTime1;
    @BindView(R.id.et_before_time2)
    EditText mBeforeTime2;
    @BindView(R.id.et_after_time2)
    EditText mAfterTime2;
    @BindView(R.id.et_before_time3)
    EditText mBeforeTime3;
    @BindView(R.id.et_after_time3)
    EditText mAfterTime3;
    @BindView(R.id.switch_humidity_status)
    Switch mTimingSwitch;
    private static final String ARG_IS_TIMING = "is_timing_on";
    private Unbinder unbinder;
    private boolean isTiming;


//    @OnFocusChange({R.id.et_day, R.id.et_before_time1, R.id.et_after_time1, R.id.et_before_time2, R.id.et_after_time2, R.id.et_before_time3, R.id.et_after_time3})
//    public void setOnFocusChangeListener(View view, boolean hasFocus) {
////        if (!hasFocus) {
////            EditText editText = (EditText) view;
////            onTimingClick(editText);
////        }
//    }


    // 🚀 静态工厂方法：安全地创建 DialogFragment 实例并传递参数
    public static SettingTimeSetDialogFragment newInstance(boolean isTiming) {
        SettingTimeSetDialogFragment fragment = new SettingTimeSetDialogFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_IS_TIMING, isTiming);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            if (getTargetFragment() instanceof TimingSwitchListener) {
                mListener = (TimingSwitchListener) getTargetFragment();
            }
        } catch (IllegalStateException e) {
            Log.e("DialogFragment", "No target fragment set. Ensure setTargetFragment() is called.");
        }
    }

    // ---------------------- 接口回调定义 --------------------------
    public interface TimingSwitchListener {
        void onTimingStateChanged(boolean isTimingOn);
    }

    private TimingSwitchListener mListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            isTiming = getArguments().getBoolean(ARG_IS_TIMING, false);
        }
        setStyle(DialogFragment.STYLE_NO_TITLE, 0);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 使用您的布局文件
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        View rootView = inflater.inflate(R.layout.set_time_module, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        initTimingSwitch();
        updateTiming();
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            Window window = getDialog().getWindow();
            DisplayMetrics displayMetrics = new DisplayMetrics();
            requireActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int screenWidth = displayMetrics.widthPixels;
            int twoThirdsScreenWidth = (int) (screenWidth * 2.0 / 3.0);

            // 保持软键盘输入模式
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

            window.setLayout(twoThirdsScreenWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setGravity(Gravity.CENTER);
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (unbinder != null) {
            unbinder.unbind();
        }
        EventBus.getDefault().unregister(this);
    }

    // ---------------------- Switch 初始化与回调 --------------------------

    private void initTimingSwitch() {
        boolean isTimingOn = (boolean) MySpUtil.getParam(requireActivity(), MySpUtil.TIMING_STATUS, false);

        mTimingSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.e("TAG", "onCheckedChanged: " + isChecked);
                if (buttonView.isPressed()) {
                    if (mListener != null) {
                        if (TimingUtils.timeValueIsNull(getActivity()) && !isTiming) {
                            mTimingSwitch.setChecked(!isChecked);
                            ToastUtil.showToast(getActivity(), "请先设置时间段！");
                            return;
                        }
                        mListener.onTimingStateChanged(!isChecked);
                    }
                }
            }
        });
        mTimingSwitch.setChecked(isTimingOn);

        keyboardLayout.setOnKeyboardVisibilityListener(new KeyboardLayout.OnKeyboardVisibilityListener() {
            @Override
            public void onKeyboardShow() {
                // 软键盘弹出时的处理逻辑
                if (getDialog() != null) {
                    getDialog().setCanceledOnTouchOutside(false);
                }
            }

            @Override
            public void onKeyboardHide() {
                // 软键盘收起时的处理逻辑
                if (getDialog() != null) {
                    getDialog().setCanceledOnTouchOutside(true);
                }
                onTimingClick(keyboardLayout);
            }
        });
        //        setTimingFieldsEnabled(isTimingOn);
    }

    public void updateTiming() {
        SaveTimingInfo timingInfo = MySpUtil.getTimingData(this.getActivity());
        if (timingInfo != null) {
            int totalDays = timingInfo.getOpenDay();
            // 2. 获取开始时间戳
            long startTimeStamp = timingInfo.getStartTimeStamp();
            long nowTime = System.currentTimeMillis();
            Log.e("TAG", "showDatetime: " + startTimeStamp + "===========" + nowTime);
            // 3. 计算从开始到现在流逝的毫秒数，并转为天数
            long elapsedDays = (nowTime - startTimeStamp) / (24L * 60 * 60 * 1000);
            // 4. 计算当前真实剩余天数
            int remainingDays = totalDays - (int) elapsedDays;
            // 打印日志查看计算是否正确
            Log.d("Timing", "剩余天数: " + remainingDays + " (总:" + totalDays + ", 已过:" + elapsedDays + ", 已过毫秒:" + (nowTime - startTimeStamp) + ")");
            // 显示剩余天数
            if (!etDay.isFocused())
                etDay.setText(mTimingSwitch.isChecked() ? remainingDays + "" : totalDays + "");

//            if (!etDay.isFocused()) etDay.setText(timingInfo.getOpenDay() + "");
            if (!mBeforeTime1.isFocused()) mBeforeTime1.setText(timingInfo.getBeforeTime1());
            if (!mAfterTime1.isFocused()) mAfterTime1.setText(timingInfo.getAfterTime1());
            if (!mBeforeTime2.isFocused()) mBeforeTime2.setText(timingInfo.getBeforeTime2());
            if (!mAfterTime2.isFocused()) mAfterTime2.setText(timingInfo.getAfterTime2());
            if (!mBeforeTime3.isFocused()) mBeforeTime3.setText(timingInfo.getBeforeTime3());
            if (!mAfterTime3.isFocused()) mAfterTime3.setText(timingInfo.getAfterTime3());
        }
    }

    @OnClick({R.id.bt_timing_set})
    public void onTimingClick(View view) {
        try {
            String dayText = etDay.getText().toString();
            if (StringUtils.isNullOrEmpty(dayText)) {
                dayText = "0";
            }
            int dayLength = Integer.parseInt(dayText);
            String before1 = mBeforeTime1.getText().toString();
            String after1 = mAfterTime1.getText().toString();
            String before2 = mBeforeTime2.getText().toString();
            String after2 = mAfterTime2.getText().toString();
            String before3 = mBeforeTime3.getText().toString();
            String after3 = mAfterTime3.getText().toString();

            if (dayLength > 30 || dayLength == 0 || !InputLimitUtil.timingLimit(before1, after1) || !InputLimitUtil.timingLimit(before2, after2) || !InputLimitUtil.timingLimit(before3, after3)) {
                ToastUtil.showToast(getActivity(), getString(R.string.set_format_error));
                return;
            }

            // 保存数据
            SaveTimingInfo timingInfo = MySpUtil.getTimingData(getActivity());
            timingInfo.setOpenDay(dayLength);
            timingInfo.setBeforeTime1(before1);
            timingInfo.setAfterTime1(after1);
            timingInfo.setBeforeTime2(before2);
            timingInfo.setAfterTime2(after2);
            timingInfo.setBeforeTime3(before3);
            timingInfo.setAfterTime3(after3);
            MySpUtil.setParam(getActivity(), MySpUtil.TIMING_SET, new Gson().toJson(timingInfo));

            ToastUtil.showToast(getActivity(), getString(R.string.set_success));

            // 发送 EventBus 消息
            TempControlEvent tempControlEvent = new TempControlEvent(1);
            EventBus.getDefault().post(tempControlEvent);

            // 关键：保存成功后关闭弹窗
//            dismiss();

        } catch (Exception e) {
            e.printStackTrace();
            ToastUtil.showToast(getActivity(), "保存失败：" + e.getMessage());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void settingUpdateEvent(SettingUpdateEvent settingUpdateEvent) {
        if (settingUpdateEvent != null) {
            if (settingUpdateEvent.getType() == 5) {
                if (isAdded() && getDialog() != null && getDialog().isShowing()) {
                    updateTiming();
                }
            } else if (settingUpdateEvent.getType() == 7) {
                if (mTimingSwitch.isChecked() != settingUpdateEvent.isTimingSwitch())
                    mTimingSwitch.setChecked(settingUpdateEvent.isTimingSwitch());
            }
        }
    }
}
