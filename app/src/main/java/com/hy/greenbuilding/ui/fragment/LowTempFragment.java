package com.hy.greenbuilding.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hy.greenbuilding.HyApplication;
import com.hy.greenbuilding.R;
import com.hy.greenbuilding.config.ErrorDefine;
import com.hy.greenbuilding.event.DefrostChangeEvent;
import com.hy.greenbuilding.protocol.FunctionObject;
import com.hy.greenbuilding.protocol.ResPonseInfo.OutDoorStatusInfo;
import com.hy.greenbuilding.protocol.SpDataProcessor;
import com.hy.greenbuilding.protocol.command.LowTempCommand;
import com.hy.greenbuilding.utils.ByteUtils;
import com.hy.greenbuilding.utils.Hex;
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
import butterknife.Unbinder;

/**
 * 低温增焓页面
 */
public class LowTempFragment extends BaseDialogFragment {
    @BindView(R.id.li_back)
    ImageView mReturnView;
    @BindView(R.id.bt_lowTemp_test)
    Button mTempButton;
    @BindView(R.id.low_airMode)
    TextView mAirMode;
    @BindView(R.id.low_runSpeed)
    TextView mRunSpeed;
    @BindView(R.id.low_setSpeed)
    TextView mSetSpeed;
    @BindView(R.id.low_outNtc)
    TextView mOutNtc;
    @BindView(R.id.low_inNtc)
    TextView mInNtc;
    @BindView(R.id.low_frequency_Condition)
    TextView mFrequencyCondition;
    @BindView(R.id.low_testCondition)
    TextView mTestCondition;
    @BindView(R.id.low_expansion_valve)
    TextView mExpansionValve;

    @BindView(R.id.low_OpenMode)
    TextView mOpenMode;
    @BindView(R.id.low_frequency)
    TextView mFrequency;
    @BindView(R.id.low_setTemp)
    TextView mSetTemp;
    @BindView(R.id.low_inTemp)
    TextView mInTemp;
    @BindView(R.id.low_inTemp1)
    TextView mInTemp1;
    @BindView(R.id.low_electric)
    TextView mElectric;
    @BindView(R.id.low_moduleTemp)
    TextView moduleTemp;
    @BindView(R.id.low_defrostSignal)
    TextView mDefrostSignal;
    @BindView(R.id.low_outTemp)
    TextView mOutTemp;
    @BindView(R.id.low_outTemp1)
    TextView mOutTemp1;
    @BindView(R.id.low_exhaustTemp)
    TextView mExhaustTemp;
    @BindView(R.id.low_mainExpansion)
    TextView mainExpansion;
    @BindView(R.id.low_voltage)
    TextView mVoltage;
    @BindView(R.id.low_outFan_speed)
    TextView mFanSpeed;

    @BindView(R.id.rl_lowTemp_error1)
    GridView gridView1;
    @BindView(R.id.rl_lowTemp_error0)
    GridView gridView2;
    @BindView(R.id.rl_ntc_error)
    GridView gridView_ntc;

    private View mView;
    private Unbinder unbinder;
    private Context mContext;
    private ArrayAdapter<String> errorAdapter1;
    private ArrayAdapter<String> errorAdapter2;
    private ArrayAdapter<String> ntcErrorAdapter;
    private List<String> error1List = new ArrayList<>();
    private List<String> error0List = new ArrayList<>();
    private List<String> ntcErrorList = new ArrayList<>();
    private boolean isDefrost;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.DialogFullScreen); //dialog全屏
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        this.getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        mView = inflater.inflate(R.layout.low_temp_main, null);
        mContext = this.getActivity();
        unbinder = ButterKnife.bind(this, mView);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        init();
        return mView;
    }

    private void init() {
        sendStatusCommand();
        mHandler.postDelayed(mRunnable, 5000);

    }

    Handler mHandler = new Handler();
    Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            sendStatusCommand();
            mHandler.removeCallbacks(mRunnable);
            mHandler.postDelayed(mRunnable, 5000);
        }
    };

    @OnClick({R.id.bt_lowTemp_test})
    public void onTempClick(View view) {
        if (InputLimitUtil.isFastDoubleClick()) {
            return;
        }
        LowTempTestFragment fragment = new LowTempTestFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("outType", 1);
        fragment.setArguments(bundle);
        fragment.show(getFragmentManager(), "tempTest");
    }

    @OnClick({R.id.li_back})
    public void onReturnClick(View view) {
        this.dismiss();
    }

    private void sendStatusCommand() {
        LowTempCommand lowTempCommand = new LowTempCommand(FunctionObject.GET_OUT_STATUS);
        SpDataProcessor.getInstance().send(lowTempCommand);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDefrostEvent(DefrostChangeEvent event) {
        if (event != null) {
            isDefrost = event.getDefrostStatus();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(OutDoorStatusInfo info) {
        if (info != null) {
            mOpenMode.setText(info.switchMode());
            mAirMode.setText(info.airConditionerMode());
            mFrequency.setText(info.frequency() + "HZ");
            mSetTemp.setText(info.settingTemp().toString() + " \u2103");
            mInTemp.setText(info.inDoorTemp().toString() + " \u2103");
            mInTemp1.setText(info.inDoorTemp1().toString() + " \u2103");
            mElectric.setText(info.outElectric().toString() + "A");
            moduleTemp.setText(info.moduleTemp().toString() + " \u2103");
            mDefrostSignal.setText(info.defrostSignal());
            mOutTemp.setText(info.outDoorTemp().toString() + " \u2103");
            mOutTemp1.setText(info.outDoorTemp1().toString() + " \u2103");
            mExhaustTemp.setText(info.exHaustTemp().toString() + " \u2103");
            mainExpansion.setText(info.expansion());
            mVoltage.setText(info.voltage().toString() + "V");
            mFanSpeed.setText(info.outFunSpeed().toString() + "PRM");
            mRunSpeed.setText(info.getInTermSpeed());

            initError(info.faultMessage1(), info.faultMessage2());
            initNtcError();

            String json = MySpUtil.getParam(mContext, MySpUtil.NTC_DATA, "").toString();
            if (!StringUtils.isNullOrEmpty(json)) {
                List<String> list = new Gson().fromJson(json, new TypeToken<List<String>>() {
                }.getType());
                if (list != null && list.size() >= 6) {
                    mInNtc.setText(list.get(2) + " \u2103");
                    mOutNtc.setText(list.get(5) + " \u2103");
                }
            }
            mFrequencyCondition.setText(info.frequencyTestValue() + "");
            mExpansionValve.setText(info.mainExpansionTest() + "");
            if (!info.functionTestValue().equals("0009")) {
                switch (info.functionTestValue()) {
                    case "0000":
                        mTestCondition.setText("制热E");
                        break;
                    case "0001":
                        mTestCondition.setText("制热A");
                        break;
                    case "0002":
                        mTestCondition.setText("制热B");
                        break;
                    case "0003":
                        mTestCondition.setText("制热C");
                        break;
                    case "0004":
                        mTestCondition.setText("制热D");
                        break;
                    case "0005":
                        mTestCondition.setText("制冷A");
                        break;
                    case "0006":
                        mTestCondition.setText("制冷B");
                        break;
                    case "0007":
                        mTestCondition.setText("制冷C");
                        break;
                    case "0008":
                        mTestCondition.setText("制冷D");
                        break;
                }
            } else {
                mTestCondition.setText("关");
            }
        }
    }

    /**
     * 加载故障代码
     *
     * @param bytes1
     * @param bytes2
     */
    private void initError(byte[] bytes1, byte[] bytes2) {
        error1List.clear();
        Log.e("TAGinitError", "getBytes: "+ Hex.bytesToHexString(bytes1));

        if (bytes1 != null && bytes1.length == 2) {
            byte[] bytes = ByteUtils.getBitArray(bytes1);
            for (int i = 0; i < bytes.length; i++) {
                if (bytes[i] == 1) {
                    error1List.add(ErrorDefine.LowTempError1[i]);
                }
            }
        }
        error0List.clear();
        if (bytes2 != null && bytes2.length == 2) {
            byte[] bytes = ByteUtils.getBitArray(bytes2);
            if (bytes[7] == 1) {
                error0List.add(ErrorDefine.LowTempError0[0]);
            }
            if (bytes[13] == 1) {
                error0List.add(ErrorDefine.LowTempError0[1]);
            }
            if (bytes[14] == 1) {
                error0List.add(ErrorDefine.LowTempError0[2]);
            }
            if (bytes[15] == 1) {
                error0List.add(ErrorDefine.LowTempError0[3]);
            }
        }
        errorAdapter1 = new ArrayAdapter<String>(mContext, R.layout.error_item, error1List);
        gridView1.setAdapter(errorAdapter1);
        errorAdapter2 = new ArrayAdapter<String>(mContext, R.layout.error_item, error0List);
        gridView2.setAdapter(errorAdapter2);
    }

    private void initNtcError() {
        ntcErrorList.clear();
        byte[] bytes = HyApplication.getNtcError();
        if (bytes != null) {
            if (!isDefrost) {
                if (bytes[2] == 0) {
                    ntcErrorList.add(ErrorDefine.NTC_Error[2]);
                }
            }
            if (bytes[5] == 0) {
                ntcErrorList.add(ErrorDefine.NTC_Error[5]);
            }
            if (bytes[7] == 0) {
                ntcErrorList.add(ErrorDefine.NTC_Error[7]);
            }
        }
        ntcErrorAdapter = new ArrayAdapter<String>(mContext, R.layout.error_item, ntcErrorList);
        gridView_ntc.setAdapter(ntcErrorAdapter);
    }

    @Override
    public void onDestroyView() {
        if (unbinder != null) {
            unbinder.unbind();
        }
        EventBus.getDefault().unregister(this);
        if (mHandler != null) {
            mHandler.removeCallbacks(mRunnable);
            mHandler = null;
        }
        super.onDestroyView();
    }
}
