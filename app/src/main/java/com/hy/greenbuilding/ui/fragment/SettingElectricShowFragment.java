package com.hy.greenbuilding.ui.fragment;

import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.hy.greenbuilding.R;
import com.hy.greenbuilding.protocol.ResPonseInfo.ElectricityMeterInfo;
import com.hy.greenbuilding.protocol.SpDataProcessor;
import com.hy.greenbuilding.protocol.command.MeterCommand;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class SettingElectricShowFragment extends BaseDialogFragment {

    @BindView(R.id.total_electric_value)
    TextView mTotalElectric;
    @BindView(R.id.power_waste_value)
    TextView mPower;
    @BindView(R.id.electric_value)
    TextView mElectricView;
    @BindView(R.id.voltage_value)
    TextView mVoltageView;
    @BindView(R.id.bt_reset_electric)
    TextView mResetElectricView;

    @BindView(R.id.li_back)
    ImageView liBack;

    private View rootView;
    private Unbinder unbinder;

    private MaterialDialog materialDialog;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.DialogFullScreen); //dialog全屏
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.set_electric_module, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        MeterCommand meterCommand = new MeterCommand(1);
        SpDataProcessor.getInstance().send(meterCommand);
        return rootView;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onElectricEvent(ElectricityMeterInfo info) {
        updateElectic(info);
    }

    public void updateElectic(ElectricityMeterInfo info){
        if (info != null) {
            mTotalElectric.setText(info.getTotalElectricity().doubleValue() + "");
            mPower.setText(info.getPower().doubleValue() + "");
            mElectricView.setText(info.getElectric().doubleValue() + "");
            mVoltageView.setText(info.getVoltage().doubleValue() + "");
        }
    }

    @OnClick({R.id.li_back})
    public void onReturnClick(View view) {
        this.dismiss();
    }

    @OnClick({R.id.bt_reset_electric})
    public void onResetElectricClick(View view) {
        materialDialog = new MaterialDialog.Builder(getActivity())
                .title("提示")
                .content("是否重置电量")
                .positiveText("确定")
                .negativeText("取消")
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        materialDialog.dismiss();
                    }
                })
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        MeterCommand meterCommand = new MeterCommand(2);
                        SpDataProcessor.getInstance().send(meterCommand);
                    }
                }).build();
        materialDialog.show();
        if (materialDialog.getTitleView() != null) {
            materialDialog.getTitleView().setTextSize(TypedValue.COMPLEX_UNIT_PX, 20);
        }
        if (materialDialog.getContentView() != null) {
            materialDialog.getContentView().setTextSize(TypedValue.COMPLEX_UNIT_PX, 22);
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

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
