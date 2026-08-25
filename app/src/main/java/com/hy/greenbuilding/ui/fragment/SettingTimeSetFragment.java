package com.hy.greenbuilding.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.hy.greenbuilding.R;
import com.hy.greenbuilding.config.SaveTimingInfo;
import com.hy.greenbuilding.event.TempControlEvent;
import com.hy.greenbuilding.utils.InputLimitUtil;
import com.hy.greenbuilding.utils.MySpUtil;
import com.hy.greenbuilding.utils.StringUtils;
import com.hy.greenbuilding.utils.ToastUtil;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class SettingTimeSetFragment extends Fragment {

    @BindView(R.id.bt_timing_set)
    Button mTimingSet;
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

    private View rootView;
    private Unbinder unbinder;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.set_time_module, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        updateTiming();
        return rootView;
    }

    public void updateTiming() {
        SaveTimingInfo timingInfo = MySpUtil.getTimingData(this.getActivity());
        if (timingInfo != null) {
            if (!etDay.isFocused()) etDay.setText(timingInfo.getOpenDay() + "");
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
            if (dayLength > 30 || dayLength == 0 || !InputLimitUtil.timingLimit(before1, after1)
                    || !InputLimitUtil.timingLimit(before2, after2) || !InputLimitUtil.timingLimit(before3, after3)) {
                ToastUtil.showToast(getActivity(), getString(R.string.set_format_error));
                return;
            }
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

            //发送主被动模式
            TempControlEvent tempControlEvent = new TempControlEvent(1);
            EventBus.getDefault().post(tempControlEvent);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
