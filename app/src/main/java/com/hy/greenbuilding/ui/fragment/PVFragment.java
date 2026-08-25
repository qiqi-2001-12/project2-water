package com.hy.greenbuilding.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
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
import com.hy.greenbuilding.protocol.ResPonseInfo.PVStatusInfo;
import com.hy.greenbuilding.protocol.SpDataProcessor;
import com.hy.greenbuilding.protocol.command.PVCommand;
import com.hy.greenbuilding.utils.ByteUtils;
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
 * 光伏页面
 */
public class PVFragment extends BaseDialogFragment {

    @BindView(R.id.li_back)
    ImageView mReturnView;
    @BindView(R.id.bt_pvTemp_test)
    Button mPvTestButton;
    @BindView(R.id.pv_switchStatus)
    TextView switchStatus;
    @BindView(R.id.pv_setMode)
    TextView mSetMode;
    @BindView(R.id.pv_inTempLeft)
    TextView pv_inTempLeft;
    @BindView(R.id.pv_setTempLeft)
    TextView pv_setTempLeft;
    @BindView(R.id.pv_ntc1)
    TextView mNtc1;
    @BindView(R.id.pv_ntc2)
    TextView mNtc2;
    @BindView(R.id.pv_frequency_Condition)
    TextView mFrequencyCondition;
    @BindView(R.id.pv_testCondition)
    TextView mTestCondition;
    @BindView(R.id.pv_expansion_valve)
    TextView mExpansionValve;

    @BindView(R.id.pv_OpenMode)
    TextView mOpenMode;
    @BindView(R.id.pv_defrostStatus)
    TextView mDefrostStatus;
    @BindView(R.id.pv_frequency)
    TextView mFrequency;
    @BindView(R.id.pv_airMode)
    TextView pv_airMode;
    @BindView(R.id.pv_setTemp)
    TextView pv_setTemp;
    @BindView(R.id.pv_inTemp)
    TextView pv_inTemp;
    @BindView(R.id.pv_outTemp)
    TextView pv_outTemp;
    @BindView(R.id.pv_condensation_temp)
    TextView pv_condensation_temp;
    @BindView(R.id.pv_exhaustTemp)
    TextView pv_exhaustTemp;
    @BindView(R.id.low_returnTemp)
    TextView low_returnTemp;
    @BindView(R.id.pv_outFanSpeed)
    TextView pv_outFanSpeed;
    @BindView(R.id.pv_outElectric)
    TextView pv_outElectric;
    @BindView(R.id.pv_voltage)
    TextView pv_voltage;
    @BindView(R.id.pv_moduleTemp)
    TextView pv_moduleTemp;
    @BindView(R.id.pv_power)
    TextView pv_power;
    @BindView(R.id.pv_totalPower)
    TextView pv_totalPower;
    @BindView(R.id.low_mainExpansion)
    TextView pv_mainExpansion;
    @BindView(R.id.low_auxExpansion)
    TextView pv_auxExpansion;
    @BindView(R.id.pv_outNtc)
    TextView pvOutNtc;

    @BindView(R.id.rl_pv_error1)
    GridView gridView1;
    @BindView(R.id.rl_pv_error0)
    GridView gridView2;
    @BindView(R.id.rl_ntc_error)
    GridView gridView_ntc;

    @BindView(R.id.tv_left_title)
    TextView tvLeftTitle;

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
        mView = inflater.inflate(R.layout.pv_main,null);
        mContext = this.getActivity();
        unbinder = ButterKnife.bind(this,mView);
        if(!EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().register(this);
        }
        tvLeftTitle.setText("光伏");
        init();
        return mView;
    }
    private void init(){
        sendStatusCommand();
        mHandler.postDelayed(mRunnable,5000);
    }

    @OnClick({R.id.bt_pvTemp_test})
    public void onPVTestClick(View view) {
        if(InputLimitUtil.isFastDoubleClick()){
            return;
        }
        LowTempTestFragment fragment = new LowTempTestFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("outType",2);
        fragment.setArguments(bundle);
        fragment.show(this.getActivity().getSupportFragmentManager(),"tempTest");
    }

    @OnClick({R.id.li_back})
    public void onReturnClick(View view) {
        this.dismiss();
    }

    private void sendStatusCommand(){
        PVCommand pvCommand = new PVCommand(FunctionObject.GET_OUT_STATUS);
        SpDataProcessor.getInstance().send(pvCommand);
    }

    Handler mHandler = new Handler();
    Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            sendStatusCommand();
            mHandler.removeCallbacks(mRunnable);
            mHandler.postDelayed(mRunnable,5000);
        }
    };

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDefrostEvent(DefrostChangeEvent event){
        if(event != null){
            isDefrost = event.getDefrostStatus();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(PVStatusInfo info){
        if(info != null){
            switchStatus.setText(info.switchMode());
            mSetMode.setText(info.airConditionerMode());
            pv_setTempLeft.setText(info.settingTemp()+" \u2103");
            pv_inTempLeft.setText(info.inDoorTemp()+" \u2103");
            mOpenMode.setText(info.switchMode());
            mDefrostStatus.setText(info.defrostSignal());
            mFrequency.setText(info.frequency()+"HZ");
            pv_airMode.setText(info.airConditionerMode());
            pv_setTemp.setText(info.settingTemp()+" \u2103");
            pv_inTemp.setText(info.inDoorTemp()+" \u2103");
            pv_outTemp.setText(info.outDoorTemp()+" \u2103");
            pv_condensation_temp.setText(info.outDoorTemp1()+" \u2103");
            pv_exhaustTemp.setText(info.exHaustTemp()+" \u2103");
            low_returnTemp.setText(info.exReturnTemp()+" \u2103");
            pv_outFanSpeed.setText(info.outFunSpeed().toString()+"PRM");
            pv_outElectric.setText(info.outElectric().toString()+"A");
            pv_voltage.setText(info.voltage().toString()+"V");
            pv_moduleTemp.setText(info.moduleTemp().toString()+" \u2103");
            pv_power.setText(info.pvPower()+"KW");
            pv_totalPower.setText(info.pvTotalPower()+"KW");
            pv_mainExpansion.setText(info.mainExpansion());

            initError(info.faultMessage1(),info.faultMessage2());
            initNtcError();

            String json = MySpUtil.getParam(mContext, MySpUtil.NTC_DATA, "").toString();
            if (!StringUtils.isNullOrEmpty(json)) {
                List<String> list = new Gson().fromJson(json, new TypeToken<List<String>>() {}.getType());
                if(list != null && list.size() >= 6){
                    pvOutNtc.setText(list.get(5)+" \u2103");
                }
            }
            mFrequencyCondition.setText(info.frequencyTestValue()+"");
            mExpansionValve.setText(info.mainExpansionTest()+"");
            if(!info.functionTestValue().equals("0009")){
                switch (info.functionTestValue()){
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
            }else{
                mTestCondition.setText("关");
            }
        }
    }

    /**
     * 加载故障代码
     * @param bytes1
     * @param bytes2
     */
    private void initError(byte[] bytes1,byte[] bytes2){
        error1List.clear();
        if( bytes1 != null && bytes1.length == 2){
            byte[] bytes = ByteUtils.getBitArray(bytes1);
            for(int i = 0;i<bytes.length; i++){
                if(bytes[i] == 1){
                    error1List.add(ErrorDefine.PVError1[i]);
                }
            }
        }
        error0List.clear();
        if( bytes2 != null && bytes2.length == 2){
            byte[] bytes = ByteUtils.getBitArray(bytes2);
            for(int i = 0;i<bytes.length; i++){
                if(bytes[i] == 1){
                    error0List.add(ErrorDefine.PVError0[i]);
                }
            }
        }
        errorAdapter1 = new ArrayAdapter<String>(mContext,R.layout.error_item, error1List);
        gridView1.setAdapter(errorAdapter1);
        errorAdapter2 = new ArrayAdapter<String>(mContext,R.layout.error_item, error0List);
        gridView2.setAdapter(errorAdapter2);
    }

    private void initNtcError(){
        ntcErrorList.clear();
        byte[] bytes = HyApplication.getNtcError();
        if(bytes != null){
            if(!isDefrost){
                if(bytes[2] == 0){
                    ntcErrorList.add(ErrorDefine.NTC_Error[2]);
                }
            }
            if(bytes[5] == 0){
                ntcErrorList.add(ErrorDefine.NTC_Error[5]);
            }
            if(bytes[7] == 0){
                ntcErrorList.add(ErrorDefine.NTC_Error[7]);
            }
        }
        ntcErrorAdapter = new ArrayAdapter<String>(mContext,R.layout.error_item, ntcErrorList);
        gridView_ntc.setAdapter(ntcErrorAdapter);
    }

    @Override
    public void onDestroyView() {
        if(unbinder != null){
            unbinder.unbind();
        }
        EventBus.getDefault().unregister(this);
        if(mHandler != null){
            mHandler.removeCallbacks(mRunnable);
            mHandler = null;
        }
        super.onDestroyView();
    }
}
